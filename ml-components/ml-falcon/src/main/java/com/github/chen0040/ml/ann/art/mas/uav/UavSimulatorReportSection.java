package com.github.chen0040.ml.ann.art.mas.uav;

/**
 * Created by memeanalytics on 3/1/16.
 */
public class UavSimulatorReportSection {
    private double avgDistance;
    private double avgNodeCovered;
    private double avgCost;

    public double getAvgNodeCovered() {
        return avgNodeCovered;
    }

    public int getTrial() {
        return trial;
    }

    public void setTrial(int trial) {
        this.trial = trial;
    }

    private double numCodes;
    private int trial;

    public double getAvgDistance() {
        return avgDistance;
    }

    public void setAvgDistance(double avgDistance) {
        this.avgDistance = avgDistance;
    }
    

    public double getNumCodes() {
        return numCodes;
    }

    public void setNumCodes(double numCodes) {
        this.numCodes = numCodes;
    }

    @Override
    public String toString(){
        return "Trial " + trial
                + "\tAvgCost: " + avgCost
                + "\tAvgDistance: " + avgDistance
                + "\tAvgNodes: " + avgNodeCovered
                + "\tNCodes: " + numCodes;
    }

    public void setAvgNodeCovered(double avgNodeCovered) {
        this.avgNodeCovered = avgNodeCovered;
    }

    public void setAvgCost(double avgCost) {
        this.avgCost = avgCost;
    }

    public double getAvgCost() {
        return avgCost;
    }
}
