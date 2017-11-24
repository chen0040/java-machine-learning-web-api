package com.github.chen0040.ml.ann.art.mas.flocking;

import com.github.chen0040.ml.ann.art.mas.flocking.flocks.GameWorld;

/**
 * Created by chen0469 on 10/2/2015 0002.
 */
public class FlockingSimulatorProgress {
    private GameWorld gameWorld;
    private int trial;
    private int run;
    private int step;

    public FlockingSimulatorProgress(int run, int trial, int step, GameWorld gameWorld){
        this.run = run;
        this.trial = trial;
        this.step = step;
        this.gameWorld = gameWorld;
    }

    public GameWorld getGameWorld() {
        return gameWorld;
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
}
