package com.github.chen0040.ml.reinforcement.learning.qlearn;

import com.github.chen0040.ml.reinforcement.models.QModel;
import com.github.chen0040.ml.linearalg.IndexValue;
import com.github.chen0040.ml.reinforcement.actionselection.AbstractActionSelectionStrategy;
import com.github.chen0040.ml.reinforcement.actionselection.ActionSelectionStrategy;
import com.github.chen0040.ml.reinforcement.actionselection.ActionSelectionStrategyFactory;
import com.github.chen0040.ml.reinforcement.actionselection.EpsilonGreedyActionSelectionStrategy;

import java.io.Serializable;
import java.util.Random;
import java.util.Set;

/**
 * Created by chen0469 on 9/27/2015 0027.
 * Implement temporal-difference learning Q-Learning, which is an off-policy TD control algorithm
 * Q is known as the quality of state-action combination, note that it is different from utility of a state
 */
public class QLearner implements Serializable,Cloneable {
    protected QModel model;
    private ActionSelectionStrategy actionSelectionStrategy;

    @Override
    public Object clone(){
        QLearner clone = new QLearner();
        clone.copy(this);
        return clone;
    }

    public void copy(QLearner rhs){
        model = (QModel)rhs.model.clone();
        actionSelectionStrategy = (ActionSelectionStrategy)((AbstractActionSelectionStrategy) rhs.actionSelectionStrategy).clone();
    }

    @Override
    public boolean equals(Object obj){
        if(obj !=null && obj instanceof QLearner){
            QLearner rhs = (QLearner)obj;
            if(!model.equals(rhs.model)) return false;
            return actionSelectionStrategy.equals(rhs.actionSelectionStrategy);
        }
        return false;
    }

    public QModel getModel() {
        return model;
    }

    public void setModel(QModel model) {
        this.model = model;
    }

    public String getActionSelection() {
        return ActionSelectionStrategyFactory.serialize(actionSelectionStrategy);
    }

    public void setActionSelection(String conf) {
        this.actionSelectionStrategy = ActionSelectionStrategyFactory.deserialize(conf);
    }

    public QLearner(){

    }

    public QLearner(int stateCount, int actionCount){
        this(stateCount, actionCount, 0.1, 0.7, 0.1);
    }

    public QLearner(QModel model, ActionSelectionStrategy actionSelectionStrategy){
        this.model = model;
        this.actionSelectionStrategy = actionSelectionStrategy;
    }

    public QLearner(int stateCount, int actionCount, double alpha, double gamma, double initialQ)
    {
        model = new QModel(stateCount, actionCount, initialQ);
        model.setAlpha(alpha);
        model.setGamma(gamma);
        actionSelectionStrategy = new EpsilonGreedyActionSelectionStrategy();
    }

    public static void main(String[] args){
        int stateCount = 100;
        int actionCount = 10;

        QLearner learner = new QLearner(stateCount, actionCount);


        Random random = new Random();
        int currentStateId = random.nextInt(stateCount);
        for(int time=0; time < 1000; ++time){

            int actionId = learner.selectAction(currentStateId);
            System.out.println("Controller does action-"+actionId);

            int newStateId = random.nextInt(actionCount);
            double reward = random.nextDouble();

            System.out.println("Now the new state is "+newStateId);
            System.out.println("Controller receives Reward = "+reward);

            learner.update(currentStateId, actionId, newStateId, reward);
            currentStateId = newStateId;
        }
    }

    protected double maxQAtState(int stateId, Set<Integer> actionsAtState){
        IndexValue iv = model.actionWithMaxQAtState(stateId, actionsAtState);
        double maxQ = iv.getValue();
        return maxQ;
    }

    public int selectAction(int stateId, Set<Integer> actionsAtState){
        IndexValue iv = actionSelectionStrategy.selectAction(stateId, model, actionsAtState);
        return iv.getIndex();
    }

    public int selectAction(int stateId){
        return selectAction(stateId, null);
    }


    public void update(int stateId, int actionId, int nextStateId, double immediateReward){
        update(stateId, actionId, nextStateId, null, immediateReward);
    }

    public void update(int stateId, int actionId, int nextStateId, Set<Integer> actionsAtNextStateId, double immediateReward)
    {
        // old_value is $Q_t(s_t, a_t)$
        double oldQ = model.getQ(stateId, actionId);

        // learning_rate;
        double alpha = model.getAlpha(stateId, actionId);

        // discount_rate;
        double gamma = model.getGamma();

        // estimate_of_optimal_future_value is $max_a Q_t(s_{t+1}, a)$
        double maxQ = maxQAtState(nextStateId, actionsAtNextStateId);

        // learned_value = immediate_reward + gamma * estimate_of_optimal_future_value
        // old_value = oldQ
        // temporal_difference = learned_value - old_value
        // new_value = old_value + learning_rate * temporal_difference
        double newQ = oldQ + alpha * (immediateReward + gamma * maxQ - oldQ);

        // new_value is $Q_{t+1}(s_t, a_t)$
        model.setQ(stateId, actionId, newQ);
    }



}
