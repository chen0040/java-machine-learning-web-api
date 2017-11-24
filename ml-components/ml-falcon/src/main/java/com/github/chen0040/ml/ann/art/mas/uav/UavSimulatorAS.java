package com.github.chen0040.ml.ann.art.mas.uav;

import com.github.chen0040.ml.ann.art.falcon.Falcon;
import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavAgent;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavAnt;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class UavSimulatorAS extends UavSimulator {

    public UavSimulatorAS(UavSimulatorConfig config, FalconConfig falconConfig){
        super(config, falconConfig);
        name = "UAV-ACS";
    }

    @Override
    protected Falcon createBrain(){
        return null;
    }

    @Override
    protected UavAgent createAgent(int agentId, Falcon brain){

        UavAnt newAgent = new UavAnt(agentId, config.numStates, config.numActions);

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

        UavSimulatorAS simulator = new UavSimulatorAS(config, falconConfig);
        simulator.runSims();
    }
}
