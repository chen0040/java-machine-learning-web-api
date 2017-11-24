package com.github.chen0040.ml.ann.art.mas.minefield;

import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.falcon.TDMethod;
import com.github.chen0040.ml.ann.art.mas.minefield.agents.FalconNavAgent;
import com.github.chen0040.ml.ann.art.mas.minefield.agents.TDFalconNavAgent;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class MineFieldSimulatorSarsaLambda extends MineFieldSimulator {

    public MineFieldSimulatorSarsaLambda(MineFieldSimulatorConfig config, FalconConfig falconConfig){
        super(config, falconConfig);
    }

    @Override
    protected FalconNavAgent createAgent(int agentId){
        int numSonarInput = config.numSonarInput;
        int numAVSonarInput = config.numAVSonarInput;
        int numBearingInput = config.numBearingInput;
        int numRangeInput = config.numRangeInput;


        TDFalconNavAgent newAgent = new TDFalconNavAgent(falconConfig, agentId,TDMethod.Sarsa, numSonarInput, numAVSonarInput, numBearingInput, numRangeInput);
        newAgent.enableEligibilityTrace();
        newAgent.useImmediateRewardAsQ = false;

        if(config.isImmediateRewardProvided()){
            newAgent.setQGamma(0.5);
        } else {
            newAgent.setQGamma(0.9);
        }

        return newAgent;
    }

    public static void main(String[] args){
        MineFieldSimulatorConfig config = new MineFieldSimulatorConfig();
        config.setImmediateRewardProvided(false);
        config.setNumAgents(1);

        FalconConfig falconConfig = new FalconConfig();
        falconConfig.numAction = FalconNavAgent.numAction;
        falconConfig.numState = config.numState();
        falconConfig.numReward = 2;

        MineFieldSimulatorSarsaLambda simulator = new MineFieldSimulatorSarsaLambda(config, falconConfig);
        simulator.runSims();
    }
}
