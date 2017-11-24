package com.github.chen0040.ml.ann.art.mas.uav;

import com.github.chen0040.ml.ann.art.mas.uav.aco.SparseGraph;
import com.github.chen0040.ml.ann.art.mas.uav.aco.UavPath;
import com.github.chen0040.ml.ann.art.mas.utils.FileUtils;
import com.github.chen0040.ml.ann.art.mas.utils.SimulatorConfig;
import com.github.chen0040.ml.ann.art.mas.utils.Vec2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class UavSimulatorConfig extends SimulatorConfig {
    private int maxTrial = 2000; // the maximum number of trials (a trial is an attempt to navigate to the target from a random location) in a simulation run

    private int uiInterval = 500; // interval for each step in ui thread

    public int numStates = 10;
    public int numActions = 10;

    private SparseGraph graph;
    private UavPath knownPath;
    public boolean singleAI = true;

    public boolean localSearch = false;
    public boolean applyTDFALCON = true;
    private double degrees = 0.5;

    public int getNumAI(){
        if(singleAI) {
            return 1;
        } else {
            return getNumAgents();
        }
    }

    public SparseGraph graph(){
        return graph;
    }

    public int getUiInterval(){
        return uiInterval;
    }

    public void setUiInterval(int interval){
        uiInterval = interval;
    }



    public int getMaxTrial() {
        return maxTrial;
    }

    public void setMaxTrial(int maxTrial) {
        this.maxTrial = maxTrial;
    }


    public void loadUav(List<String> lines){
        List<Vec2D> positions = new ArrayList<Vec2D>();
        for(String line : lines) {
            String[] items = line.split(",");
            Double x = Double.parseDouble(items[1]);
            Double y = Double.parseDouble(items[2]);
            positions.add(new Vec2D(x, y));
        }
        graph = new SparseGraph(positions);
    }

    public void loadUavPath(List<String> lines, SparseGraph g){
        List<Integer> positions = new ArrayList<Integer>();
        for(String line : lines) {
            Integer v = Integer.parseInt(line.trim())-1;
            positions.add(v);
        }
        knownPath = new UavPath(positions, g.getCost(positions), g.getUavDistanceTravelled(positions));

    }


    public void loadUav_a280() {
        List<String> lines = FileUtils.getLines("a280.tsp.3c.csv");
        loadUav(lines);
        lines = FileUtils.getLines("a280.opt.tour.csv");
        loadUavPath(lines, graph);
    }

    public double getKnownDistanceTravelled() {
        return knownPath != null ?knownPath.getDistanceTravelled() : Double.MAX_VALUE;
    }

    public void setDegrees(double degrees) {
        this.degrees = degrees;
    }

    public double getDegrees() {
        return degrees;
    }
}
