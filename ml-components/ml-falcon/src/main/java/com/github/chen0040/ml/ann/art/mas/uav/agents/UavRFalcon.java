package com.github.chen0040.ml.ann.art.mas.uav.agents;

import com.github.chen0040.ml.ann.art.falcon.RFalcon;
import com.github.chen0040.ml.ann.art.mas.uav.aco.AntColony;

import java.util.Set;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class UavRFalcon extends UavAgent {
    RFalcon ai;
    public UavRFalcon(RFalcon brain, int id, int numState, int numAction) {
        super(brain, id, numState, numAction);
        ai = brain;
    }

    @Override
    public void learn(final AntColony mazed){
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
    public int selectValidAction(final AntColony maze) {
        Set<Integer> feasibleActions = getFeasibleActions(maze);
        int actionId = ai.selectActionId(state, feasibleActions);
        return actionId;
    }
}
