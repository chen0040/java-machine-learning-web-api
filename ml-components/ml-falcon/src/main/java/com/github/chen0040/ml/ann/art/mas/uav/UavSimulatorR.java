package com.github.chen0040.ml.ann.art.mas.uav;

import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavAgent;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavRFalcon;
import com.github.chen0040.ml.ann.art.falcon.RFalcon;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class UavSimulatorR extends UavSimulator {

    @Override
    protected Falcon createBrain(){
        RFalcon brain = new RFalcon(falconConfig);
        return brain;
    }

    @Override
    protected UavAgent createAgent(int agentId, Falcon brain){

        return new UavRFalcon((RFalcon)brain, agentId, config.numStates, config.numActions);
    }

    public UavSimulatorR(UavSimulatorConfig config, FalconConfig falconConfig){
        super(config, falconConfig);
    }

    public static void main(String[] args){
        UavSimulatorConfig config = new UavSimulatorConfig();
        config.setInterval(10);
        config.loadUav_a280();

        FalconConfig falconConfig = new FalconConfig();
        falconConfig.numAction = config.numActions;
        falconConfig.numState = config.numStates;
        falconConfig.numReward = 2;

        UavSimulatorR simulator = new UavSimulatorR(config, falconConfig);
        simulator.runSims();
    }
}
