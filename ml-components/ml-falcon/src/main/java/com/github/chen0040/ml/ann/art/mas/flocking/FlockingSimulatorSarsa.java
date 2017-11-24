package com.github.chen0040.ml.ann.art.mas.flocking;

import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.falcon.TDFalcon;
import com.github.chen0040.ml.ann.art.falcon.TDMethod;
import com.github.chen0040.ml.ann.art.mas.flocking.agents.FalconBoidAgent;
import com.github.chen0040.ml.ann.art.mas.flocking.agents.TDFalconBoidAgent;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class FlockingSimulatorSarsa extends FlockingSimulator {

    public FlockingSimulatorSarsa(FlockingSimulatorConfig config, FalconConfig falconConfig){
        super(config, falconConfig);
    }

    @Override
    protected Falcon createBrain(){
        return new TDFalcon(falconConfig, TDMethod.Sarsa);
    }

    @Override
    protected FalconBoidAgent createAgent(int agentId, Falcon brain){
        int numSonarInput = config.numSonarInput;
        int numAVSonarInput = config.numAVSonarInput;
        int numBearingInput = config.numBearingInput;
        int numRangeInput = config.numRangeInput;

        TDFalconBoidAgent newAgent = new TDFalconBoidAgent((TDFalcon)brain, falconConfig, agentId,TDMethod.Sarsa, numSonarInput, numAVSonarInput, numBearingInput, numRangeInput);
        newAgent.useImmediateRewardAsQ = false;

        if(config.isImmediateRewardProvided()){
            newAgent.setQGamma(0.5);
        } else {
            newAgent.setQGamma(0.9);
        }

        return newAgent;
    }

    public static void main(String[] args){
        FlockingSimulatorConfig config = new FlockingSimulatorConfig();
        config.setImmediateRewardProvided(true);
        config.setName("flocking-sarsa");

        FalconConfig falconConfig = new FalconConfig();
        falconConfig.numAction = FalconBoidAgent.numAction;
        falconConfig.numState = config.numState();
        falconConfig.numReward = 2;
        falconConfig.isBounded = false;

        FlockingSimulatorSarsa simulator = new FlockingSimulatorSarsa(config, falconConfig);
        simulator.runSims();
    }
}
