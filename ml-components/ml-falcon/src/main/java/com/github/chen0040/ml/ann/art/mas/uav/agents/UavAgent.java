package com.github.chen0040.ml.ann.art.mas.uav.agents;

import com.github.chen0040.ml.ann.art.mas.uav.aco.AntColony;
import com.github.chen0040.ml.ann.art.falcon.Falcon;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public abstract class UavAgent {
    protected double[] state;
    protected double reward;
    protected double[] actions;
    protected double[] newState;
    protected double prevReward;

    private int id;
    protected int numState;
    protected int numAction;
    private Falcon brain;

    public UavAgent(Falcon brain, int id, int numState, int numAction){
        this.id = id;

        this.numState = numState;
        this.numAction = numAction;

        state = new double[this.numState];
        actions = new double[numAction];
        newState = new double[this.numState];

        this.brain = brain;
    }

    public int getId(){
        return id;
    }

    public abstract int selectValidAction(final AntColony maze);

    public abstract void learn(final AntColony maze);

    public void resetAction(){
        for(int i=0; i < numAction; ++i){
            actions[i] = 1 - actions[i];
        }
    }

    public double getPrevReward(){
        return prevReward;
    }

    public void setPrevReward(double reward) {
        prevReward = reward;
    }

    public void setState(double[] state){
        for(int i=0; i < numState / 2; ++i){
            this.state[i] = state[i];
            this.state[i+numState/2] = 1 - state[i];
        }
    }

    public void setNewState(double[] state){
       for(int i=0; i < numState / 2; ++i){
            newState[i] = state[i];
            newState[i+numState/2] = 1 - state[i];
        }
    }

    public double getReward(){
        return reward;
    }

    public void setReward(double immmediateReward){
        reward = immmediateReward;
    }

    public abstract int getNodeCount();

    public void setAction(int actionId){
        for(int i=0; i < numAction; ++i){
            actions[i] = 0;
        }
        actions[actionId] = 1;
    }

    public Set<Integer> getFeasibleActions(AntColony maze){
        Set<Integer> feasibleActions = new HashSet<Integer>();

        int wayCount = maze.getAntAvailableMoves(getId()).size();

        for (int i = 0; i < numAction; i++) {
            if (i < wayCount) {   // valid action
                feasibleActions.add(i);
            }
        }

        return feasibleActions;
    }

}
