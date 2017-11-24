package com.github.chen0040.ml.reinforcement.learning.actorcritic;

import com.github.chen0040.ml.linearalg.Vector;

import java.io.Serializable;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by chen0469 on 9/28/2015 0028.
 */
public class ActorCriticAgent implements Serializable,Cloneable {
    private ActorCriticLearner learner;
    private int currentState;
    private int prevState;
    private int prevAction;

    public void enableEligibilityTrace(double lambda){
        ActorCriticLambdaLearner acll = new ActorCriticLambdaLearner(learner);
        acll.setLambda(lambda);
        learner = acll;
    }

    public void start(int stateId){
        currentState = stateId;
        prevAction = -1;
        prevState = -1;
    }

    public ActorCriticLearner getLearner(){
        return learner;
    }

    public void setLearner(ActorCriticLearner learner){
        this.learner = learner;
    }

    public ActorCriticAgent(int stateCount, int actionCount){
        learner = new ActorCriticLearner(stateCount, actionCount);
    }

    public ActorCriticAgent(){

    }

    public ActorCriticAgent(ActorCriticLearner learner){
        this.learner = learner;
    }

    @Override
    public Object clone(){
        ActorCriticAgent clone = new ActorCriticAgent();
        clone.copy(this);
        return clone;
    }

    public void copy(ActorCriticAgent rhs){
        learner = (ActorCriticLearner)rhs.learner.clone();
        prevAction = rhs.prevAction;
        prevState = rhs.prevState;
        currentState = rhs.currentState;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof ActorCriticAgent){
            ActorCriticAgent rhs = (ActorCriticAgent)obj;
            return learner.equals(rhs.learner) && prevAction == rhs.prevAction && prevState == rhs.prevState && currentState == rhs.currentState;

        }
        return false;
    }

    public int selectAction(Set<Integer> actionsAtState){
        return learner.selectAction(currentState, actionsAtState);
    }

    public int selectAction(){
        return learner.selectAction(currentState);
    }

    public void update(int actionTaken, int newState, double immediateReward, final Vector V){
        update(actionTaken, newState, null, immediateReward, V);
    }

    public void update(int actionTaken, int newState, Set<Integer> actionsAtNewState, double immediateReward, final Vector V){

        learner.update(currentState, actionTaken, newState, actionsAtNewState, immediateReward, new Function<Integer, Double>() {
            public Double apply(Integer stateId) {
                return V.get(stateId);
            }
        });

        prevAction = actionTaken;
        prevState = currentState;

        currentState = newState;
    }

    public static void main(String[] args){
        int stateCount = 100;
        int actionCount = 10;

        ActorCriticAgent agent = new ActorCriticAgent(stateCount, actionCount);
        Vector stateValues = new Vector(stateCount);

        Random random = new Random();
        agent.start(random.nextInt(stateCount));
        for(int time=0; time < 1000; ++time){

            int actionId = agent.selectAction();
            System.out.println("Agent does action-"+actionId);

            int newStateId = random.nextInt(actionCount);
            double reward = random.nextDouble();

            System.out.println("Now the new state is "+newStateId);
            System.out.println("Agent receives Reward = "+reward);

            System.out.println("World state values changed ...");
            for(int stateId = 0; stateId < stateCount; ++stateId){
                stateValues.set(stateId, random.nextDouble());
            }

            agent.update(actionId, newStateId, reward, stateValues);
        }
    }
}
