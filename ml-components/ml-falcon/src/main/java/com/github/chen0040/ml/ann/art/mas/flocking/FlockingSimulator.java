package com.github.chen0040.ml.ann.art.mas.flocking;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.mas.flocking.agents.FalconBoidAgent;
import com.github.chen0040.ml.ann.art.mas.flocking.agents.TDFalconBoidAgent;
import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.mas.flocking.flocks.GameWorld;
import com.github.chen0040.ml.ann.art.mas.utils.SimulatorReport;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public abstract class FlockingSimulator {
    protected FalconBoidAgent[] agents;
    protected Falcon[] brains;
    protected GameWorld gameWorld;
    protected FlockingSimulatorConfig config;
    protected FalconConfig falconConfig;
    protected boolean running = false;
    protected String message;


    public String getMessage() { return message; }

    private static Logger logger = Logger.getLogger(String.valueOf(FlockingSimulator.class));

    public FlockingSimulator(FlockingSimulatorConfig config, FalconConfig falconConfig) {
        this.config = config;
        this.falconConfig = falconConfig;
        gameWorld = new GameWorld(config.getMineFieldSize(), config.getNumMines(), config.getNumAgents());
        gameWorld.flocking = config.flocking;

        brains = new Falcon[config.getNumAI()];
        for(int i=0; i < config.getNumAI(); ++i) {
            brains[i] = createBrain();
        }

        int numAgents = config.getNumAgents();
        agents = new FalconBoidAgent[numAgents];
        for (int i = 0; i < numAgents; ++i) {
            agents[i] = createAgent(i, brains[i % brains.length]);
        }
    }

    protected void logInfo(String message) {
        //logger.info(message);
    }

    protected void logWarning(String message) {
        //logger.warning(message);
    }



    public static void main(String[] args){
        FlockingSimulatorConfig config = new FlockingSimulatorConfig();
        FalconConfig falconConfig = new FalconConfig();

        config.setImmediateRewardProvided(true);

        falconConfig.numAction = FalconBoidAgent.numAction;
        falconConfig.numState = config.numState();
        falconConfig.numReward = 2;

        FlockingSimulator simulator = CommandLineUtils.procCommandLines(args, config, falconConfig);
        simulator.runSims();
    }

    public FalconConfig getFalconConfig(){
        return falconConfig;
    }

    public FlockingSimulatorConfig getConfig(){
        return config;
    }

    public boolean senseActSense(int agentId, boolean last, boolean provideImmediateReward) {
        int action = -1;
        double r;
        int maxTries = 20;
        FalconBoidAgent agent = agents[agentId];
        for(int trie = 0; trie < maxTries; ++trie) {
            double[] this_Sonar = gameWorld.getSonar(agentId);
            double[] this_AVSonar = gameWorld.getAVSonar(agentId);

            int this_bearing = (8 + gameWorld.getTargetBearing(agentId) - gameWorld.getCurrentBearing(agentId)) % 8;
            double this_targetRange = gameWorld.getTargetRange(agentId);

            agent.setState(this_Sonar, this_AVSonar, this_bearing, this_targetRange);

            action = agent.selectValidAction(gameWorld);

            if (action == -1) {   // No valid action; deadend, backtrack
                gameWorld.turn(agentId, 4);
            } else {
                break;
            }
        }

        if(action !=-1){

            double v = gameWorld.move(agentId, action - 2);          // actual movement, aco direction is from -2 to 2

            if (v != -1) {  // if valid move
                if (last == true && provideImmediateReward == false)  //run out of time (without immediate reward)
                    r = 0.0;
                else {
                    r = gameWorld.getReward(agentId, provideImmediateReward);
                }
            } else {   // invalid move
                r = 0.0;
                System.out.println("*** Invalid action " + action + " taken *** ");
            }

            double[] this_Sonar = gameWorld.getSonar(agentId);
            double[] this_AVSonar = gameWorld.getAVSonar(agentId);

            int this_bearing = (8 + gameWorld.getTargetBearing(agentId) - gameWorld.getCurrentBearing(agentId)) % 8;
            double this_targetRange = gameWorld.getTargetRange(agentId);

            agent.setNewState(this_Sonar, this_AVSonar, this_bearing, this_targetRange);
            agent.setAction(action);    // set action
            agent.setReward(r);

            return true;
        }

        return false;
    }

    public SimulatorReport[] runSims(){
        return runSims(null);
    }

    public SimulatorReport[] runSims(Consumer<FlockingSimulatorProgress> progressChanged) {
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

    private List<Integer> createExecutionOrders(){
        int numAgents = config.getNumAgents();
        List<Integer> orders = new ArrayList<Integer>();
        for(int i=0; i < numAgents; ++i){
            orders.add(i);
        }
        Collections.shuffle(orders);
        return orders;
    }

    private boolean doStep(int step){
        //!aco.endState(Target_Moving)

        // the code below is required for the RFALCON to behave correctly
        for(int agt = 0; agt < config.getNumAgents(); ++agt) {
            agents[agt].setPrevReward(gameWorld.getReward(agt, config.isImmediateRewardProvided()));
        }

        boolean lastFlag = step == (config.getMaxStep() - 1);

        if (config.targetMoving) gameWorld.moveTarget();

        int activeAgentCount = 0;

        List<Integer> orders = createExecutionOrders();

        for (Integer agt : orders) {
            if(!running) break;
            if(!gameWorld.isActive(agt))
                continue;

            if(config.targetMoving && agt % 4 == 0) {
                gameWorld.moveTarget();
            }

            doStep(agt, lastFlag);
            activeAgentCount++;
        }

        if(activeAgentCount == 0) return false;

        afterStep();

        return true;
    }

    protected void afterStep(){

    }

    public void doStep(int agentId, boolean last) {
        boolean acted = senseActSense(agentId, last, config.isImmediateRewardProvided());

        if(acted) {
            agents[agentId].learn(gameWorld);
        }
    }

    protected abstract Falcon createBrain();
    protected abstract FalconBoidAgent createAgent(int agentId, Falcon brain);

    private SimulatorReport runSim(int run, Consumer<FlockingSimulatorProgress> progressChanged){
        SimulatorReport report = new SimulatorReport(config, run, "FTD-FALCON");

        final int numAgents = config.getNumAgents();

        brains = new Falcon[config.getNumAI()];
        for(int i=0; i < config.getNumAI(); ++i){
            brains[i] = createBrain();
        }

        agents = new FalconBoidAgent[numAgents];
        for (int i = 0; i < numAgents; ++i) {
            agents[i] = createAgent(i, brains[i % brains.length]);
            agents[i].setFlocking(config.flocking);
        }

        for (int trial = 1; trial <= config.getMaxTrial(); ++trial) {
            if(!running) break;
            gameWorld.flocking = config.flocking;
            gameWorld.refreshMaze(config.getMineFieldSize(), config.getNumMines(), config.getNumAgents());

            for (int i = 0; i < config.getNumAgents(); ++i) {
                agents[i].setPrevReward(0);
            }

            int step = 0;
            for(; step < config.getMaxStep(); ++step) {
                if(!running) break;
                if(!doStep(step)) break;
                if(progressChanged != null){
                    FlockingSimulatorProgress progress = new FlockingSimulatorProgress(run, trial, step, gameWorld);
                    progressChanged.accept(progress);
                    try{
                        Thread.sleep(config.getUiInterval());
                    }catch(InterruptedException ie){

                    }
                }
            }

            final int step_final = step;

            message = report.recordTrial(trial, step_final, (rpt)->{
                FalconBoidAgent[] agents = this.agents;
                GameWorld maze = this.gameWorld;

                for (int agt = 0; agt < numAgents; agt++) {
                    rpt.numCode[agt] = agents[agt].getNodeCount();
                    if (maze.isHitTarget(agt)) {
                        rpt.success++;
                        rpt.total_step += step_final;
                        rpt.total_min_step += maze.getMinStep(agt);
                    } else if(maze.isHitObstacle(agt)) {
                        rpt.failure++;
                    } else if (step_final == config.getMaxStep())
                        rpt.time_out++;
                    else if (maze.isConflicting(agt))
                        rpt.conflict++;

                }
            });

            for (int i=0; i < numAgents; ++i){
                if(agents[i] instanceof TDFalconBoidAgent) {
                    ((TDFalconBoidAgent)agents[i]).decayQEpsilon();
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

    public GameWorld getGameWorld(){
        return gameWorld;
    }
}
