package com.github.chen0040.ml.ann.art.mas.uav;

import com.github.chen0040.ml.ann.art.mas.uav.aco.AntColony;

/**
 * Created by chen0469 on 10/2/2015 0002.
 */
public class UavSimulatorProgress {
    private AntColony mineField;
    private int trial;
    private int run;
    private int step;
    private boolean initializing;

    public UavSimulatorProgress(int run, int trial, int step, AntColony mineField, boolean initializing){
        this.run = run;
        this.trial = trial;
        this.step = step;
        this.mineField = mineField;
        this.initializing = initializing;
    }

    public AntColony getMineField() {
        return mineField;
    }

    public int getTrial() {
        return trial;
    }

    public int getRun() {
        return run;
    }

    public int getStep() {
        return step;
    }

    public boolean isInitializing() {
        return initializing;
    }
}
