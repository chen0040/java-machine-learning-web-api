package com.github.chen0040.ml.ann.art.mas.traffic.agents;

import com.github.chen0040.ml.ann.art.falcon.QValue;
import com.github.chen0040.ml.ann.art.falcon.QValueProvider;
import com.github.chen0040.ml.ann.art.falcon.TDFalcon;
import com.github.chen0040.ml.ann.art.falcon.TDMethod;
import com.github.chen0040.ml.ann.art.mas.traffic.env.TrafficNetwork;
import com.github.chen0040.ml.ann.art.falcon.*;

import java.util.Set;

/**
 * Created by chen0469 on 9/29/2015 0029.
 */
public class TDFalconTrafficAgent extends TrafficAgent {
    private TDFalcon ai;
    public boolean useImmediateRewardAsQ;

    public TDFalconTrafficAgent(TDFalcon brain, int id, int numSonarInput, int numAVSonarInput, int numRangeInput) {
        super(brain, id, numSonarInput, numAVSonarInput, numRangeInput);
        ai = brain;
    }

    public TDFalconTrafficAgent(TDFalcon brain, int id, TDMethod method, int numSonarInput, int numAVSonarInput, int numRangeInput) {
        super(brain, id, numSonarInput, numAVSonarInput, numRangeInput);
        ai = brain;
    }

    public void decayQEpsilon() {
        ai.decayQEpsilon();
    }

    @Override
    public void learn(final TrafficNetwork maze) {
        Set<Integer> feasibleActionAtNewState = getFeasibleActions(maze);
        ai.learnQ(state, actions, newState, feasibleActionAtNewState, reward, createQInject(maze));
    }

    protected QValueProvider createQInject(final TrafficNetwork maze) {
        QValueProvider Qinject = new QValueProvider() {
            public QValue queryQValue(double[] state, int actionTaken, boolean isNextAction) {
                if (useImmediateRewardAsQ) {
                    return new QValue(reward);
                } else {
                    if (isNextAction) {
                        if (maze.willHitMine(getId(), actionTaken)) {
                            return new QValue(0.0);
                        } else if (maze.willHitTarget(getId(), actionTaken)) {
                            return new QValue(1.0);
                        }
                    } else {
                        if (maze.isHitMine(getId())) {
                            return new QValue(0.0);
                        } else if (maze.isHitTarget(getId())) {
                            return new QValue(1.0); //case reach target
                        }
                    }

                    return QValue.Invalid();
                }
            }
        };
        return Qinject;
    }


    @Override
    public int selectValidAction(final TrafficNetwork maze) {
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
