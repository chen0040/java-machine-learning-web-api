package com.github.chen0040.ml.ann.art.mas.uav.agents;

import com.github.chen0040.ml.ann.art.falcon.QValue;
import com.github.chen0040.ml.ann.art.falcon.QValueProvider;
import com.github.chen0040.ml.ann.art.falcon.TDFalcon;
import com.github.chen0040.ml.ann.art.mas.uav.aco.AntColony;

import java.util.Set;

/**
 * Created by chen0469 on 9/29/2015 0029.
 */
public class UavTdFalcon extends UavAgent {
    private TDFalcon ai;
    public boolean useImmediateRewardAsQ;

    public UavTdFalcon(TDFalcon brain, int id, int numState, int numAction) {
        super(brain, id, numState, numAction);
        ai = brain;
    }

    public void decayQEpsilon() {
        ai.decayQEpsilon();
    }

    @Override
    public void learn(final AntColony maze) {
        Set<Integer> feasibleActionAtNewState = getFeasibleActions(maze);
        ai.learnQ(state, actions, newState, feasibleActionAtNewState, reward, createQInject(maze));
    }

    protected QValueProvider createQInject(final AntColony maze) {
        QValueProvider Qinject = new QValueProvider() {
            public QValue queryQValue(double[] state, int actionTaken, boolean isNextAction) {
                if (useImmediateRewardAsQ) {
                    return new QValue(reward);
                } else {
                    return QValue.Invalid();
                }
            }
        };
        return Qinject;
    }


    @Override
    public int selectValidAction(final AntColony maze) {
        Set<Integer> feasibleActions = getFeasibleActions(maze);
        int selectedAction = ai.selectActionId(state, feasibleActions, createQInject(maze));
        return selectedAction;
    }

    @Override
    public int getNodeCount(){
        return ai.nodes.size();
    }

    public void setQGamma(double QGamma) {
        this.ai.QGamma = QGamma;
    }
}
