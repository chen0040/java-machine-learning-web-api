package com.github.chen0040.ml.ann.art.mas.uav;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavAgent;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavAnt;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavTdFalcon;
import com.github.chen0040.ml.ann.art.mas.uav.aco.AntColony;

import java.io.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public abstract class UavSimulator {
    private static Logger logger = Logger.getLogger(String.valueOf(UavSimulator.class));
    protected UavAgent[] agents;
    protected AntColony colony;
    protected UavSimulatorConfig config;
    protected FalconConfig falconConfig;
    protected boolean running = false;
    protected String message;
    protected Falcon[] brains;
    protected String name= "TSP-ATD";

    public String getName(){
        return name;
    }

    public static void main(String[] args){
        UavSimulatorConfig config = new UavSimulatorConfig();
        config.loadUav_a280();

        FalconConfig falconConfig = new FalconConfig();
        falconConfig.numAction = config.numActions;
        falconConfig.numState = config.numStates;
        falconConfig.numReward = 2;
        falconConfig.isBounded = false;



        UavSimulator simulator = CommandLineUtils.procCommandLines(args, config, falconConfig);
        simulator.runSims();
    }

    public UavSimulator(UavSimulatorConfig config, FalconConfig falconConfig) {
        this.config = config;
        this.falconConfig = falconConfig;
        colony = new AntColony(config.graph(), config.getNumAgents(), config.numStates, config.numActions);

        int numAgents = config.getNumAgents();
        brains = new Falcon[config.getNumAI()];
        for(int i=0; i < config.getNumAI(); ++i){
            brains[i] = createBrain();
        }

        agents = new UavAgent[numAgents];
        for (int i = 0; i < numAgents; ++i) {
            agents[i] = createAgent(i, brains[i % config.getNumAI()]);
        }
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

    public UavSimulatorConfig getConfig(){
        return config;
    }

    public boolean senseActSense(int agentId) {
        int action;

        double[] state = colony.getState(agentId);

        agents[agentId].setState(state);

        logInfo("Sense and Search for an Action:");

        action = agents[agentId].selectValidAction(colony);

        if(!(agents[agentId] instanceof UavAnt)) {
            if (action != -1) {
                colony.move(agentId, action);

                double r = colony.getReward(agentId);

                state = colony.getState(agentId);

                agents[agentId].setNewState(state);
                agents[agentId].setAction(action);    // set action
                agents[agentId].setReward(r);

                return true;
            }
        } else {
            if(action != -1) {
                colony.move(agentId, action);
                return true;
            }
        }

        colony.deactivate(agentId);


        return false;
    }

    public UavSimulatorReport[] runSims(){
        return runSims(null);
    }

    public UavSimulatorReport[] runSims(Consumer<UavSimulatorProgress> progressChanged) {
        running = true;

        message = "Simulation Started";

        UavSimulatorReport[] reports = new UavSimulatorReport[config.getNumRuns()];
        for (int runIndex = 0; runIndex < config.getNumRuns(); runIndex++) {
            int run = runIndex + config.getStartRun();
            if(!running) break;

            UavSimulatorReport rpt = runSim(run, progressChanged);

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
        for(int agt = 0; agt < config.getNumAgents(); ++agt) {
            agents[agt].setPrevReward(colony.getReward(agt));
        }

        int activeAgentCount = 0;
        for (int agt = 0; agt < config.getNumAgents(); agt++) {
            if(!running) break;
            if (!colony.isActive(agt))
                continue;
            doAgentStep(agt);
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

    private void doAgentStep(int agentId) {
        boolean acted = senseActSense(agentId);
        if(acted) {
            agents[agentId].learn(colony);
        }
    }

    protected abstract Falcon createBrain();
    protected abstract UavAgent createAgent(int agentId, Falcon brain);

    private UavSimulatorReport runSim(int run, Consumer<UavSimulatorProgress> progressChanged){
        UavSimulatorReport report = new UavSimulatorReport(config);

        int numAgents = config.getNumAgents();
        int numNodes = colony.getNumNodes();

        brains = new Falcon[config.getNumAI()];
        for(int i=0; i < config.getNumAI(); ++i){
            brains[i] = createBrain();
        }

        agents = new UavAgent[numAgents];
        for (int i = 0; i < numAgents; ++i) {
            agents[i] = createAgent(i, brains[i % config.getNumAI()]);
        }

        colony.createSparseGraph(config.getDegrees());

        colony.refreshMaze(config.getNumAgents(), config.numStates, config.numActions);

        for (int trial = 1; trial <= config.getMaxTrial(); ++trial) {
            if(!running) break;

            colony.startAntTraversal();

            for (int i = 0; i < config.getNumAgents(); ++i) {
                agents[i].setPrevReward(0);
            }

            if(progressChanged != null) {
                UavSimulatorProgress progress = new UavSimulatorProgress(run, trial, 0, colony, true);
                progressChanged.accept(progress);
                try {
                    Thread.sleep(config.getUiInterval());
                } catch (InterruptedException ie) {

                }
            }

            int step = 0;
            for(; step < numNodes; ++step) {
                if(!running) break;
                if(!doStep(step)) {
                    break;
                }
                if(progressChanged != null){
                    UavSimulatorProgress progress = new UavSimulatorProgress(run, trial, step, colony, false);
                    progressChanged.accept(progress);
                    try{
                        Thread.sleep(config.getUiInterval());
                    }catch(InterruptedException ie){

                    }
                }
            }

            if(config.localSearch) {
                colony.localSearch();
            }
            colony.updateBestUavSolution();

            colony.evaporatePheromone();
            colony.depositPheromone();



            message = report.recordTrial(trial, step, this);

            for (int i=0; i < numAgents; ++i){
                if(agents[i] instanceof UavTdFalcon) {
                    ((UavTdFalcon)agents[i]).decayQEpsilon();
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

    public AntColony getColony(){
        return colony;
    }
}
