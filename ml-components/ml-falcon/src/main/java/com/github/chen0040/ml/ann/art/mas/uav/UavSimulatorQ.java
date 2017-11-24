package com.github.chen0040.ml.ann.art.mas.uav;

import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.falcon.TDFalcon;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavTdFalcon;
import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavAgent;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class UavSimulatorQ extends UavSimulator {

    public UavSimulatorQ(UavSimulatorConfig config, FalconConfig falconConfig){
        super(config, falconConfig);
    }

    @Override
    protected Falcon createBrain(){
        TDFalcon brain = new TDFalcon(falconConfig);
        return brain;
    }

    @Override
    protected UavAgent createAgent(int agentId, Falcon brain){

        UavTdFalcon newAgent = new UavTdFalcon((TDFalcon)brain, agentId, config.numStates, config.numActions);
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

        UavSimulatorQ simulator = new UavSimulatorQ(config, falconConfig);
        simulator.runSims();
    }
}
