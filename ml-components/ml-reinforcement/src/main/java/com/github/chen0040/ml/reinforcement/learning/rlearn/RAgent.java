package com.github.chen0040.ml.reinforcement.learning.rlearn;

import java.io.Serializable;
import java.util.Random;
import java.util.Set;

/**
 * Created by chen0469 on 9/27/2015 0027.
 */
public class RAgent implements Cloneable, Serializable{
    private RLearner learner;
    private int currentState;
    private int currentAction;

    public int getCurrentState(){
        return currentState;
    }

    public int getCurrentAction(){
        return currentAction;
    }

    public void start(int currentState){
        this.currentState = currentState;
    }

    public Object clone(){
        RAgent clone = new RAgent();
        clone.copy(this);
        return clone;
    }

    public void copy(RAgent rhs){
        currentState = rhs.currentState;
        currentAction = rhs.currentAction;
        learner = (RLearner)rhs.learner.clone();
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof RAgent){
            RAgent rhs = (RAgent)obj;
            if(!learner.equals(rhs.learner)) return false;
            if(currentAction != rhs.currentAction) return false;
            return currentState == rhs.currentState;
        }
        return false;
    }

    public int selectAction(){
        return selectAction(null);
    }

    public int selectAction(Set<Integer> actionsAtState){
        if(currentAction==-1){
            currentAction = learner.selectAction(currentState, actionsAtState);
        }
        return currentAction;
    }

    public void update(int newState, double immediateReward){
        update(newState, null, immediateReward);
    }

    public void update(int newState, Set<Integer> actionsAtState, double immediateReward){
        if(currentAction != -1) {
            learner.update(currentState, currentAction, newState, actionsAtState, immediateReward);
            currentState = newState;
            currentAction = -1;
        }
    }

    public RAgent(){

    }

    public static void main(String[] args){
        int stateCount = 100;
        int actionCount = 10;
        RAgent agent = new RAgent(stateCount, actionCount);

        Random random = new Random();
        agent.start(random.nextInt(stateCount));
        for(int time=0; time < 1000; ++time){

            int actionId = agent.selectAction();
            System.out.println("Agent does action-"+actionId);

            int newStateId = random.nextInt(actionCount);
            double reward = random.nextDouble();

            System.out.println("Now the new state is "+newStateId);
            System.out.println("Agent receives Reward = "+reward);

            agent.update(newStateId, reward);
        }
    }


    public RLearner getLearner(){
        return learner;
    }

    public void setLearner(RLearner learner){
        this.learner = learner;
    }

    public RAgent(int stateCount, int actionCount, double alpha, double beta, double rho, double initialQ){
        learner = new RLearner(stateCount, actionCount, alpha, beta, rho, initialQ);
    }

    public RAgent(int stateCount, int actionCount){
        learner = new RLearner(stateCount, actionCount);
    }
}
