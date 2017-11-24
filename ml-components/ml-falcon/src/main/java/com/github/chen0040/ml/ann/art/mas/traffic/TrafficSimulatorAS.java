package com.github.chen0040.ml.ann.art.mas.traffic;

import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.mas.traffic.agents.AntSystemTrafficAgent;
import com.github.chen0040.ml.ann.art.mas.traffic.agents.TrafficAgent;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class TrafficSimulatorAS extends TrafficSimulator {

    public TrafficSimulatorAS(TrafficSimulatorConfig config, FalconConfig falconConfig){
        super(config, falconConfig);
        simName = "TACS";
    }

    @Override
    protected Falcon createBrain(){
        return null;
    }

    @Override
    protected TrafficAgent createAgent(int agentId, Falcon brain){
        int numSonarInput = config.numSonarInput;
        int numAVSonarInput = config.numAVSonarInput;
        int numRangeInput = config.numRangeInput;

        AntSystemTrafficAgent newAgent = new AntSystemTrafficAgent(agentId, numSonarInput, numAVSonarInput, numRangeInput);

        return newAgent;
    }

    public static void main(String[] args){
        TrafficSimulatorConfig config = new TrafficSimulatorConfig();
        config.setImmediateRewardProvided(true);
        config.setNumAgents(30);


        FalconConfig falconConfig = new FalconConfig();
        falconConfig.numAction = TrafficAgent.numAction;
        falconConfig.numState = config.numState();
        falconConfig.numReward = 2;
        falconConfig.isBounded = false;

        TrafficSimulatorAS simulator = new TrafficSimulatorAS(config, falconConfig);
        simulator.runSims();
    }
}
