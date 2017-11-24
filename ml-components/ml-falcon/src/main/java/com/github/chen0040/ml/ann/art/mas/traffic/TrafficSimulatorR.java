package com.github.chen0040.ml.ann.art.mas.traffic;

import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.mas.traffic.agents.RFalconTrafficAgent;
import com.github.chen0040.ml.ann.art.mas.traffic.agents.TrafficAgent;
import com.github.chen0040.ml.ann.art.falcon.RFalcon;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class TrafficSimulatorR extends TrafficSimulator {

    @Override
    protected Falcon createBrain(){
        RFalcon brain = new RFalcon(falconConfig);
        return brain;
    }

    @Override
    protected TrafficAgent createAgent(int agentId, Falcon brain){
        int numSonarInput = config.numSonarInput;
        int numAVSonarInput = config.numAVSonarInput;
        int numRangeInput = config.numRangeInput;

        return new RFalconTrafficAgent((RFalcon)brain, agentId, numSonarInput, numAVSonarInput, numRangeInput);
    }

    public TrafficSimulatorR(TrafficSimulatorConfig config, FalconConfig falconConfig){
        super(config, falconConfig);
    }

    public static void main(String[] args){
        TrafficSimulatorConfig config = new TrafficSimulatorConfig();
        config.setImmediateRewardProvided(true);
        config.setNumAgents(30);

        FalconConfig falconConfig = new FalconConfig();
        falconConfig.numAction = TrafficAgent.numAction;
        falconConfig.numState = config.numState();
        falconConfig.numReward = 2;

        TrafficSimulatorR simulator = new TrafficSimulatorR(config, falconConfig);
        simulator.runSims();
    }
}
