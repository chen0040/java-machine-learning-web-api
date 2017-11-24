package com.github.chen0040.ml.ann.art.mas.traffic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.mas.traffic.agents.TrafficAgent;
import com.github.chen0040.ml.ann.art.mas.traffic.env.TrafficNetwork;
import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.mas.traffic.agents.TDFalconTrafficAgent;
import com.github.chen0040.ml.ann.art.mas.utils.SimulatorReport;

import java.io.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public abstract class TrafficSimulator {
    private static Logger logger = Logger.getLogger(String.valueOf(TrafficSimulator.class));
    protected TrafficAgent[] agents;
    protected TrafficNetwork trafficNetwork;
    protected TrafficSimulatorConfig config;
    protected FalconConfig falconConfig;
    protected boolean running = false;
    protected String message;
    protected Falcon[] brains;
    protected String simName;

    public TrafficSimulator(TrafficSimulatorConfig config, FalconConfig falconConfig) {
        this.config = config;
        this.falconConfig = falconConfig;
        trafficNetwork = new TrafficNetwork(config.getNetworkNodeCount(), config.getNumMines(), config.getNumAgents(), config.getNumTargets());

        int numAgents = config.getNumAgents();
        brains = new Falcon[config.getNumAI()];
        for(int i=0; i < config.getNumAI(); ++i){
            brains[i] = createBrain();
        }

        agents = new TrafficAgent[numAgents];
        for (int i = 0; i < numAgents; ++i) {
            agents[i] = createAgent(i, brains[i % config.getNumAI()]);
        }

        simName = "ATD-FALCON";
    }

    public static void main(String[] args){
        TrafficSimulatorConfig config = new TrafficSimulatorConfig();
        config.setImmediateRewardProvided(true);


        FalconConfig falconConfig = new FalconConfig();
        falconConfig.numAction = TrafficAgent.numAction;
        falconConfig.numState = config.numState();
        falconConfig.numReward = 2;
        falconConfig.isBounded = false;

        TrafficSimulator simulator = CommandLineUtils.procCommandLines(args, config, falconConfig);
        simulator.runSims();
    }

    public String getMessage() { return message; }

    protected void logInfo(String message) {
        //logger.info(message);
    }

    protected void logWarning(String message) {
        //logger.warning(message);
    }

    public FalconConfig getFalconConfig(){
        return falconConfig;
    }

    public TrafficSimulatorConfig getConfig(){
        return config;
    }

    public boolean senseActSense(int agentId, boolean last, boolean provideImmediateReward) {
        int action;
        double r;

        double[] this_Sonar = trafficNetwork.getSonar(agentId);
        double[] this_PheromoneSonar = trafficNetwork.getPheromoneSignals(agentId);
        double[] this_targetRange = trafficNetwork.getTargetRanges(agentId);

        agents[agentId].setState(this_Sonar, this_PheromoneSonar, this_targetRange);

        logInfo("Sense and Search for an Action:");

        action = agents[agentId].selectValidAction(trafficNetwork);

        if (action != -1) {
            double v = trafficNetwork.move(agentId, action);

            if (v != -1) {
                if (last == true && provideImmediateReward == false) r = 0.0;
                else r = trafficNetwork.getReward4Vehicle(agentId, provideImmediateReward);
            } else  r = 0.0;

            this_Sonar = trafficNetwork.getSonar(agentId);
            this_PheromoneSonar = trafficNetwork.getPheromoneSignals(agentId);
            this_targetRange = trafficNetwork.getTargetRanges(agentId);

            agents[agentId].setNewState(this_Sonar, this_PheromoneSonar, this_targetRange);
            agents[agentId].setAction(action);    // set action
            agents[agentId].setReward(r);

            return true;
        }

        return false;
    }

    public SimulatorReport[] runSims(){
        return runSims(null);
    }

    public SimulatorReport[] runSims(Consumer<TrafficSimulatorProgress> progressChanged) {
        running = true;

        message = "Simulation Started";

        SimulatorReport[] reports = new SimulatorReport[config.getNumRuns()];
        for (int runIndex = 0; runIndex < config.getNumRuns(); runIndex++) {
            int run = runIndex + config.getStartRun();
            if(!running) break;

            SimulatorReport rpt = runSim(run, progressChanged);

            String dirpath = System.getProperty("user.home")+"/"+config.getName();
            File dir = new File(dirpath);

            if(!dir.exists()){
                dir.mkdirs();
            }

            String filepath = dirpath+"/"+String.format("%02d", run)+".json";

            System.out.println("Persisting: "+filepath);

            String json = JSON.toJSONString(rpt, SerializerFeature.BrowserCompatible);

            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath)));
                writer.write(json);
                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            reports[runIndex] = rpt;
        }
        return reports;
    }

    private boolean doStep(int step){
        //!aco.endState(Target_Moving)

        // the code below is required for the RFALCON to behave correctly
        for(int agt = 0; agt < config.getNumAgents(); ++agt) {
            agents[agt].setPrevReward(trafficNetwork.getReward4Vehicle(agt, config.isImmediateRewardProvided()));
        }

        boolean lastFlag = step == (config.getMaxStep() - 1);

        int activeAgentCount = 0;
        for (int agt = 0; agt < config.getNumAgents(); agt++) {
            if(!running) break;
            if (!trafficNetwork.isActive(agt))
                continue;



            doStep(agt, lastFlag);
            activeAgentCount++;
        }

        if(activeAgentCount == 0) {
            return false;
        }

        afterStep();

        return true;
    }

    protected void afterStep(){

    }

    public void doStep(int agentId, boolean last) {
        boolean acted = senseActSense(agentId, last, config.isImmediateRewardProvided());

        if(acted) {
            agents[agentId].learn(trafficNetwork);
        }
    }

    protected abstract Falcon createBrain();
    protected abstract TrafficAgent createAgent(int agentId, Falcon brain);

    private SimulatorReport runSim(int run, Consumer<TrafficSimulatorProgress> progressChanged){
        SimulatorReport report = new SimulatorReport(config, run, simName);

        int numAgents = config.getNumAgents();

        brains = new Falcon[config.getNumAI()];
        for(int i=0; i < config.getNumAI(); ++i){
            brains[i] = createBrain();
        }

        agents = new TrafficAgent[numAgents];
        for (int i = 0; i < numAgents; ++i) {
            agents[i] = createAgent(i, brains[i % config.getNumAI()]);
        }

        trafficNetwork.refreshMaze(config.getNetworkNodeCount(), config.getNumMines(), config.getNumAgents(), config.getNumTargets());


        for (int trial = 1; trial <= config.getMaxTrial(); ++trial) {
            if(!running) break;

            if(config.targetMoving) trafficNetwork.moveTargets();
            trafficNetwork.startNavigation();

            for (int i = 0; i < config.getNumAgents(); ++i) {
                agents[i].setPrevReward(0);
            }

            if(progressChanged != null) {
                TrafficSimulatorProgress progress = new TrafficSimulatorProgress(run, trial, 0, trafficNetwork, true);
                progressChanged.accept(progress);
                try {
                    Thread.sleep(config.getUiInterval());
                } catch (InterruptedException ie) {

                }
            }

            int step = 0;
            for(; step < config.getMaxStep(); ++step) {
                if(!running) break;
                if(!doStep(step)) break;
                if(progressChanged != null){
                    TrafficSimulatorProgress progress = new TrafficSimulatorProgress(run, trial, step, trafficNetwork, false);
                    progressChanged.accept(progress);
                    try{
                        Thread.sleep(config.getUiInterval());
                    }catch(InterruptedException ie){

                    }
                }
            }
            trafficNetwork.evaporatePheromone();
            trafficNetwork.depositPheromone();

            final int step_final = step;

            message = report.recordTrial(trial, step_final, (rpt)->{
                TrafficAgent[] agents = this.agents;
                TrafficNetwork maze = TrafficSimulator.this.trafficNetwork;

                for (int agt = 0; agt < numAgents; agt++) {
                    rpt.numCode[agt] = agents[agt].getNodeCount();
                    if (maze.isHitTarget(agt)) {
                        rpt.success++;
                        rpt.total_step += step_final;
                        rpt.total_min_step += maze.getMinStep(agt);
                    } else if(maze.isHitMine(agt)) {
                        rpt.failure++;
                    } else if (step_final == config.getMaxStep())
                        rpt.time_out++;
                    else if (maze.isConflicting(agt))
                        rpt.conflict++;

                }
            });

            for (int i=0; i < numAgents; ++i){
                if(agents[i] instanceof TDFalconTrafficAgent) {
                    ((TDFalconTrafficAgent)agents[i]).decayQEpsilon();
                }
            }
        }

        return report;
    }

    public void stop(){
        running = false;
    }

    public int getNumAgents() {
        return config.getNumAgents();
    }

    public TrafficNetwork getTrafficNetwork(){
        return trafficNetwork;
    }
}
