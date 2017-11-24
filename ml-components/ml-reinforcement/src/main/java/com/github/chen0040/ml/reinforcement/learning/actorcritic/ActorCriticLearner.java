package com.github.chen0040.ml.reinforcement.learning.actorcritic;

import com.github.chen0040.ml.linearalg.IndexValue;
import com.github.chen0040.ml.linearalg.Vector;
import com.github.chen0040.ml.reinforcement.actionselection.AbstractActionSelectionStrategy;
import com.github.chen0040.ml.reinforcement.actionselection.ActionSelectionStrategy;
import com.github.chen0040.ml.reinforcement.actionselection.ActionSelectionStrategyFactory;
import com.github.chen0040.ml.reinforcement.models.QModel;
import com.github.chen0040.ml.reinforcement.actionselection.GibbsSoftMaxActionSelectionStrategy;

import java.io.Serializable;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by chen0469 on 9/28/2015 0028.
 */
public class ActorCriticLearner implements Cloneable, Serializable{
    protected QModel P;
    protected ActionSelectionStrategy actionSelectionStrategy;



    @Override
    public Object clone(){
        ActorCriticLearner clone = new ActorCriticLearner();
        clone.copy(this);
        return clone;
    }

    public void copy(ActorCriticLearner rhs){
        P = (QModel)rhs.P.clone();
        actionSelectionStrategy = (ActionSelectionStrategy)((AbstractActionSelectionStrategy)rhs.actionSelectionStrategy).clone();
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof ActorCriticLearner){
            ActorCriticLearner rhs = (ActorCriticLearner)obj;
            return P.equals(rhs.P) && actionSelectionStrategy.equals(rhs.actionSelectionStrategy);
        }
        return false;
    }

    public ActorCriticLearner(){

    }

    public ActorCriticLearner(int stateCount, int actionCount){
        this(stateCount, actionCount, 1, 0.7, 0.01);
    }

    public int selectAction(int stateId, Set<Integer> actionsAtState){
        IndexValue iv = actionSelectionStrategy.selectAction(stateId, P, actionsAtState);
        return iv.getIndex();
    }

    public int selectAction(int stateId){
        return selectAction(stateId, null);
    }

    public ActorCriticLearner(int stateCount, int actionCount, double beta, double gamma, double initialP){
        P = new QModel(stateCount, actionCount, initialP);
        P.setAlpha(beta);
        P.setGamma(gamma);

        actionSelectionStrategy = new GibbsSoftMaxActionSelectionStrategy();
    }

    public void update(int currentStateId, int currentActionId, int newStateId, double immediateReward, Function<Integer, Double> V){
        update(currentStateId, currentActionId, newStateId, null, immediateReward, V);
    }

    public void update(int currentStateId, int currentActionId, int newStateId,Set<Integer> actionsAtNewState, double immediateReward, Function<Integer, Double> V){
        double td_error =  immediateReward + V.apply(newStateId) - V.apply(currentStateId);

        double oldP = P.getQ(currentStateId, currentActionId);
        double beta = P.getAlpha(currentStateId, currentActionId);
        double newP = oldP +  beta * td_error;
        P.setQ(currentStateId, currentActionId, newP);
    }

    public static void main(String[] args){
        int stateCount = 100;
        int actionCount = 10;

        ActorCriticLearner learner = new ActorCriticLearner(stateCount, actionCount);
        final Vector stateValues = new Vector(stateCount);

        Random random = new Random();
        int currentStateId = random.nextInt(stateCount);
        for(int time=0; time < 1000; ++time){

            int actionId = learner.selectAction(currentStateId);
            System.out.println("Agent does action-"+actionId);

            int newStateId = random.nextInt(actionCount);
            double reward = random.nextDouble();

            System.out.println("Now the new state is "+newStateId);
            System.out.println("Agent receives Reward = "+reward);

            System.out.println("World state values changed ...");
            for(int stateId = 0; stateId < stateCount; ++stateId){
                stateValues.set(stateId, random.nextDouble());
            }

            learner.update(currentStateId, actionId, newStateId, reward, new Function<Integer, Double>(){
                public Double apply(Integer stateId){
                    return stateValues.get(stateId);
                }
            });
        }
    }

    public String getActionSelection() {
        return ActionSelectionStrategyFactory.serialize(actionSelectionStrategy);
    }

    public void setActionSelection(String conf) {
        this.actionSelectionStrategy = ActionSelectionStrategyFactory.deserialize(conf);
    }


    public QModel getP() {
        return P;
    }

    public void setP(QModel p) {
        P = p;
    }
}
