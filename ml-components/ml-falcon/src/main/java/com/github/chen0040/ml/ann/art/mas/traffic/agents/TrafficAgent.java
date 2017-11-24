package com.github.chen0040.ml.ann.art.mas.traffic.agents;

import com.github.chen0040.ml.ann.art.mas.traffic.env.TrafficNetwork;
import com.github.chen0040.ml.ann.art.falcon.Falcon;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public abstract class TrafficAgent {

    public static final int numAction = 5;

    protected double[] state;
    protected double reward;
    protected double[] actions;
    protected double[] newState;
    protected double prevReward;

    private int id;
    private int numState;
    private int numSonarInput;
    private int numAVSonarInput;
    private int numRangeInput;
    private Falcon brain;

    public TrafficAgent(Falcon brain, int id, int numSonarInput, int numAVSonarInput, int numRangeInput){
        this.id = id;
        numState = numAVSonarInput + numSonarInput + numRangeInput;
        this.numAVSonarInput = numAVSonarInput;
        this.numSonarInput = numSonarInput;
        this.numRangeInput = numRangeInput;

        state = new double[numState];
        actions = new double[numAction];
        newState = new double[numState];

        this.brain = brain;
    }

    public int getId(){
        return id;
    }

    public abstract int selectValidAction(final TrafficNetwork maze);

    public abstract void learn(final TrafficNetwork maze);

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

    public void setState(double[] sonar, double[] av_sonar, double[] range){
        int index =0;
        for(int i=0; i < numSonarInput / 2; ++i){
            state[index+i] = sonar[i];
            state[index+i+numSonarInput/2] = 1 - sonar[i];
        }
        index += numSonarInput;

        for(int i=0; i < numAVSonarInput / 2; ++i){
            state[index+i] = av_sonar[i];
            state[index+i+numAVSonarInput/2] = 1 - av_sonar[i];
        }
        index += numAVSonarInput;


        for(int i = 0; i < numRangeInput / 2; ++i){
            state[index+i] = range[i];
            state[index+i+numRangeInput/2] = 1 - range[i];
        }
    }

    public void setNewState(double[] sonar, double[] av_sonar, double[] range){
        int index =0;
        for(int i=0; i < numSonarInput / 2; ++i){
            newState[index+i] = sonar[i];
            newState[index+i+numSonarInput/2] = 1 - sonar[i];
        }
        index += numSonarInput;

        for(int i=0; i < numAVSonarInput / 2; ++i){
            newState[index+i] = av_sonar[i];
            newState[index+i+numAVSonarInput/2] = 1 - av_sonar[i];
        }
        index += numAVSonarInput;

        for(int i = 0; i < numRangeInput / 2; ++i){
            newState[index+i] = range[i];
            newState[index+i+numRangeInput/2] = 1 - range[i];
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

    public Set<Integer> getFeasibleActions(TrafficNetwork maze){
        Set<Integer> feasibleActions = new HashSet<Integer>();

        int wayCount = maze.findAvailableMoves4Vehicle(getId()).size();

        for (int i = 0; i < numAction; i++) {
            if (i < wayCount) {   // valid action
                feasibleActions.add(i);
            }
        }

        return feasibleActions;
    }
}
