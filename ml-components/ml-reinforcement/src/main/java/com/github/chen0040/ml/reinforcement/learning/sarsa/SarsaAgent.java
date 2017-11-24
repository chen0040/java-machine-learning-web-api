package com.github.chen0040.ml.reinforcement.learning.sarsa;

import java.io.Serializable;
import java.util.Random;
import java.util.Set;

/**
 * Created by chen0469 on 9/27/2015 0027.
 * @brief Implement temporal-difference learning Sarsa, which is an on-policy TD control algorithm
 */
public class SarsaAgent implements Cloneable, Serializable{
    private SarsaLearner learner;
    private int currentState;
    private int currentAction;
    private int prevState;
    private int prevAction;

    public int getCurrentState(){
        return currentState;
    }

    public int getCurrentAction(){
        return currentAction;
    }

    public int getPrevState() { return prevState; }

    public int getPrevAction() { return prevAction; }

    public void start(int currentState){
        this.currentState = currentState;
        this.prevState = -1;
        this.prevAction = -1;
    }

    public int selectAction(){
        return selectAction(null);
    }

    public int selectAction(Set<Integer> actionsAtState){
        if(currentAction == -1){
            currentAction = learner.selectAction(currentState, actionsAtState);
        }

        return currentAction;
    }

    public void update(int actionTaken, int newState, double immediateReward){
        update(actionTaken, newState, null, immediateReward);
    }

    public void update(int actionTaken, int newState, Set<Integer> actionsAtNewState, double immediateReward){

        int futureAction = learner.selectAction(currentState, actionsAtNewState);

        learner.update(currentState, actionTaken, newState, futureAction, immediateReward);

        prevState = this.currentState;
        this.prevAction = actionTaken;

        currentAction = futureAction;
        currentState = newState;
    }

    public static void main(String[] args){
        int stateCount = 100;
        int actionCount = 10;
        SarsaAgent agent = new SarsaAgent(stateCount, actionCount);

        double reward = 0; //immediate reward by transiting from prevState to currentState
        Random random = new Random();
        agent.start(random.nextInt(stateCount));
        int actionTaken = agent.selectAction();
        for(int time=0; time < 1000; ++time){

            System.out.println("Agent does action-"+actionTaken);

            int newStateId = random.nextInt(actionCount);
            reward = random.nextDouble();

            System.out.println("Now the new state is "+newStateId);
            System.out.println("Agent receives Reward = "+reward);

            agent.update(actionTaken, newStateId, reward);
        }
    }


    public SarsaLearner getLearner(){
        return learner;
    }

    public void setLearner(SarsaLearner learner){
        this.learner = learner;
    }

    public SarsaAgent(int stateCount, int actionCount, double alpha, double gamma, double initialQ){
        learner = new SarsaLearner(stateCount, actionCount, alpha, gamma, initialQ);
    }

    public SarsaAgent(int stateCount, int actionCount){
        learner = new SarsaLearner(stateCount, actionCount);
    }

    public SarsaAgent(SarsaLearner learner){
        this.learner = learner;
    }

    public SarsaAgent(){

    }

    public void enableEligibilityTrace(double lambda){
        SarsaLambdaLearner acll = new SarsaLambdaLearner(learner);
        acll.setLambda(lambda);
        learner = acll;
    }

    @Override
    public Object clone(){
        SarsaAgent clone = new SarsaAgent();
        clone.copy(this);
        return clone;
    }

    public void copy(SarsaAgent rhs){
        learner = (SarsaLearner)rhs.learner.clone();
        currentAction = rhs.currentAction;
        currentState = rhs.currentState;
        prevAction = rhs.prevAction;
        prevState = rhs.prevState;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof SarsaAgent){
            SarsaAgent rhs = (SarsaAgent)obj;
            return prevAction == rhs.prevAction
                    && prevState == rhs.prevState
                    && currentAction == rhs.currentAction
                    && currentState == rhs.currentState
                    && learner.equals(rhs.learner);
        }
        return false;
    }
}
