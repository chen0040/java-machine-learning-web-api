package com.github.chen0040.ml.ann.art.mas.uav;

import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.falcon.TDLambdaFalcon;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavTdFalcon;
import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavAgent;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class UavSimulatorQLambda extends UavSimulator {

    public UavSimulatorQLambda(UavSimulatorConfig config, FalconConfig falconConfig){
        super(config, falconConfig);
    }

    @Override
    protected Falcon createBrain(){
        TDLambdaFalcon brain = new TDLambdaFalcon(falconConfig);
        return brain;
    }

    @Override
    protected UavAgent createAgent(int agentId, Falcon brain){

        UavTdFalcon newAgent = new UavTdFalcon((TDLambdaFalcon)brain, agentId, config.numStates, config.numActions);
        newAgent.useImmediateRewardAsQ = false;
        newAgent.setQGamma(0.5);

        return newAgent;
    }

    public static void main(String[] args){
        UavSimulatorConfig config = new UavSimulatorConfig();
        config.setInterval(10);
        config.loadUav_a280();

        FalconConfig falconConfig = new FalconConfig();
        falconConfig.numAction = config.numActions;
        falconConfig.numState = config.numStates;
        falconConfig.numReward = 2;
        falconConfig.isBounded = false;

        UavSimulatorQLambda simulator = new UavSimulatorQLambda(config, falconConfig);
        simulator.runSims();
    }
}
