package com.github.chen0040.ml.ann.art.mas.traffic.agents;

import com.github.chen0040.ml.ann.art.mas.traffic.env.TrafficNetwork;
import com.github.chen0040.ml.ann.art.falcon.RFalcon;

import java.util.Set;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class RFalconTrafficAgent extends TrafficAgent {
    RFalcon ai;
    public RFalconTrafficAgent(RFalcon brain, int id, int numSonarInput, int numAVSonarInput, int numRangeInput) {
        super(brain, id, numSonarInput, numAVSonarInput, numRangeInput);
        ai = brain;
    }

    @Override
    public void learn(final TrafficNetwork mazed){
        double r = getReward();

        if (r > getPrevReward()) {

            double[] rewards2 = new double[ai.numReward()];
            rewards2[0] = 1;
            rewards2[1] = 0;
            ai.learn(state, actions, rewards2);
            ai.reinforce();
        }
        else if (r == 0 || r <= getPrevReward()) {
            double[] rewards2 = new double[ai.numReward()];
            rewards2[0] = r; // (or 1-r) marks as good action/
            rewards2[1] = 1-r;
            resetAction();                         // seek alternative actions
            ai.learn(state, actions, rewards2);    // learn alternative actions
            ai.penalize();
        }

        ai.decay();
    }

    @Override
    public int getNodeCount(){
        return ai.nodes.size();
    }

    @Override
    public int selectValidAction(final TrafficNetwork maze) {
        Set<Integer> feasibleActions = getFeasibleActions(maze);
        int actionId = ai.selectActionId(state, feasibleActions);
        return actionId;
    }
}
