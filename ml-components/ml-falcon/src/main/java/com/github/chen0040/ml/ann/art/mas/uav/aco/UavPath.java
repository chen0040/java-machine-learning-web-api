package com.github.chen0040.ml.ann.art.mas.uav.aco;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen0469 on 10/8/2015 0008.
 */
public class UavPath extends ArrayList<Integer> {
    private double distanceTravelled;
    private double cost;

    public UavPath(List<Integer> path, double cost, double distanceTravelled){
        super(path);
        this.cost = cost;
        this.distanceTravelled = distanceTravelled;
    }

    public double getCost(){
        return cost;
    }

    public double getDistanceTravelled(){
        return distanceTravelled;
    }
}
