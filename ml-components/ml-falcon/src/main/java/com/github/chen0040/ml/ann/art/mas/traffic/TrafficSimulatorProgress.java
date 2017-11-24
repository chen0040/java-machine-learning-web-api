package com.github.chen0040.ml.ann.art.mas.traffic;

import com.github.chen0040.ml.ann.art.mas.traffic.env.TrafficNetwork;

/**
 * Created by chen0469 on 10/2/2015 0002.
 */
public class TrafficSimulatorProgress {
    private TrafficNetwork mineField;
    private int trial;
    private int run;
    private int step;
    private boolean initializing;

    public TrafficSimulatorProgress(int run, int trial, int step, TrafficNetwork mineField, boolean initializing){
        this.run = run;
        this.trial = trial;
        this.step = step;
        this.mineField = mineField;
        this.initializing = initializing;
    }

    public TrafficNetwork getMineField() {
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
