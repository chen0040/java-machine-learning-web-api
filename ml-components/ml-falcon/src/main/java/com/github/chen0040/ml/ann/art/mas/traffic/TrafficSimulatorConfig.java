package com.github.chen0040.ml.ann.art.mas.traffic;

import com.github.chen0040.ml.ann.art.mas.utils.SimulatorConfig;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class TrafficSimulatorConfig extends SimulatorConfig {

    private int mineFieldSize = 16; // size of the mine field
    private int numMines = 10; // the number of mines created in the mine field per trial

    private int maxStep = 60; // the maximum number of steps in a trial before the autonomous vehicle is stopped


    private boolean immediateRewardProvided = true; // whether immediate reward is provided in a trial
    private int uiInterval = 500; // interval for each step in ui thread

    public boolean targetMoving = false;

    public int numSonarInput = 10;
    public int numAVSonarInput = 10;
    public int numRangeInput = 0;
    private int numTargets = 2;

    public boolean singleAI = false;
    public boolean applyTDFALCON = true;

    public TrafficSimulatorConfig(){
        super();
    }

    public int getNumAI(){
        if(singleAI) {
            return 1;
        } else {
            return getNumAgents();
        }
    }

    public int numState(){
        return numSonarInput + numAVSonarInput + numRangeInput;
    }

    public int getUiInterval(){
        return uiInterval;
    }

    public void setUiInterval(int interval){
        uiInterval = interval;
    }

    public int getNetworkNodeCount() {
        return mineFieldSize;
    }

    public void setMineFieldSize(int mineFieldSize) {
        this.mineFieldSize = mineFieldSize;
    }

    public int getNumMines() {
        return numMines;
    }

    public void setNumMines(int numMines) {
        this.numMines = numMines;
    }

    public boolean isImmediateRewardProvided(){
        return immediateRewardProvided;
    }

    public void setImmediateRewardProvided(boolean provided){
        immediateRewardProvided = provided;
    }



    public int getMaxStep() {
        return maxStep;
    }

    public void setMaxStep(int maxStep) {
        this.maxStep = maxStep;
    }



    public int getNumTargets() {
        return numTargets;
    }

    public void setNumTargets(int numTargets) {
        this.numTargets = numTargets;
    }


}
