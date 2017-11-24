package com.github.chen0040.ml.ann.art.mas.flocking.agents;

import com.github.chen0040.ml.ann.art.falcon.*;
import com.github.chen0040.ml.ann.art.falcon.*;
import com.github.chen0040.ml.ann.art.mas.flocking.flocks.GameWorld;

import java.util.Set;

/**
 * Created by chen0469 on 9/29/2015 0029.
 */
public class TDFalconBoidAgent extends FalconBoidAgent {
    private TDFalcon ai;
    public boolean useImmediateRewardAsQ;

    public TDFalconBoidAgent(TDFalcon ai, FalconConfig config, int id, int numSonarInput, int numAVSonarInput, int numBearingInput, int numRangeInput){
        super(ai, id, numSonarInput, numAVSonarInput, numBearingInput, numRangeInput);
        this.ai = ai;
    }

    public TDFalconBoidAgent(TDFalcon ai, FalconConfig config, int id, TDMethod method, int numSonarInput, int numAVSonarInput, int numBearingInput, int numRangeInput){
        super(ai, id, numSonarInput, numAVSonarInput, numBearingInput, numRangeInput);
        ai.method = method;
        this.ai = ai;
    }

    public void decayQEpsilon(){
        ai.decayQEpsilon();
    }

    @Override
    public void learn(final GameWorld maze){
        Set<Integer> feasibleActionAtNewState = getFeasibleActions(maze);
        ai.learnQ(state, actions, newState, feasibleActionAtNewState, reward, createQInject(maze));
    }

    protected QValueProvider createQInject(final GameWorld maze){
        QValueProvider Qinject = new QValueProvider() {
            public QValue queryQValue(double[] state, int actionTaken, boolean isNextAction) {
                if (useImmediateRewardAsQ) {
                    if (isNextAction) {
                        return new QValue(maze.getReward(getId(), actionTaken - 2, true));
                    } else {
                        return new QValue(reward);
                    }
                } else {
                    if (isNextAction) {
                        if (maze.willHitMine(getId(), actionTaken - 2)) {
                            return new QValue(0.0);
                        } else if (maze.willHitTarget(getId(), actionTaken - 2)) {
                            return new QValue(1.0);
                        } else if(isFlocking() && maze.willConflict(getId(), actionTaken-2)) {
                            return new QValue(0.0);
                        }
                    } else {
                        if (maze.isHitObstacle(getId())) {
                            return new QValue(0.0);
                        } else if (maze.isHitTarget(getId())) {
                            return new QValue(1.0); //case reach target
                        } else if(isFlocking() && maze.isConflicting(getId())) {
                            return new QValue(0.0);
                        }
                    }
                    return QValue.Invalid();
                }
            }
        };

        return Qinject;
    }


    @Override
    public int selectValidAction(final GameWorld maze) {
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
