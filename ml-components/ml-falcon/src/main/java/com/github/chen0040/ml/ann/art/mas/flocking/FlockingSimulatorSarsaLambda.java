package com.github.chen0040.ml.ann.art.mas.flocking;

import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.mas.flocking.agents.FalconBoidAgent;
import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.falcon.TDLambdaFalcon;
import com.github.chen0040.ml.ann.art.falcon.TDMethod;
import com.github.chen0040.ml.ann.art.mas.flocking.agents.TDFalconBoidAgent;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class FlockingSimulatorSarsaLambda extends FlockingSimulator {

    public FlockingSimulatorSarsaLambda(FlockingSimulatorConfig config, FalconConfig falconConfig){
        super(config, falconConfig);
    }

    @Override
    protected Falcon createBrain(){
        return new TDLambdaFalcon(falconConfig, TDMethod.Sarsa);
    }

    @Override
    protected FalconBoidAgent createAgent(int agentId, Falcon brain){
        int numSonarInput = config.numSonarInput;
        int numAVSonarInput = config.numAVSonarInput;
        int numBearingInput = config.numBearingInput;
        int numRangeInput = config.numRangeInput;

        TDFalconBoidAgent newAgent = new TDFalconBoidAgent((TDLambdaFalcon)brain, falconConfig, agentId,TDMethod.Sarsa, numSonarInput, numAVSonarInput, numBearingInput, numRangeInput);
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
        config.setImmediateRewardProvided(false);
        config.setName("flocking-sarsa-lambda");

        FalconConfig falconConfig = new FalconConfig();
        falconConfig.numAction = FalconBoidAgent.numAction;
        falconConfig.numState = config.numState();
        falconConfig.numReward = 2;

        FlockingSimulatorSarsaLambda simulator = new FlockingSimulatorSarsaLambda(config, falconConfig);
        simulator.runSims();
    }
}
