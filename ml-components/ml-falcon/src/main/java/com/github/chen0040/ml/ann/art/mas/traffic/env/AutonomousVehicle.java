package com.github.chen0040.ml.ann.art.mas.traffic.env;

import com.github.chen0040.ml.ann.art.mas.utils.VehicleState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen0469 on 9/30/2015 0030.
 */
public class AutonomousVehicle {
    private int currentPosition = 0;
    private int prevPosition = 0;
    private VehicleState state;
    private List<Integer> path = new ArrayList<Integer>();
    private int id;

    public AutonomousVehicle(int agentId) {
        this.state = VehicleState.Active;
        this.id = agentId;
    }

    public void setCurrentPosition(int v){
        currentPosition = v;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getPrevPosition() {
        return prevPosition;
    }

    public int getId() {
        return id;
    }

    public void activate() {
        state = VehicleState.Active;
        prevPosition = currentPosition;
    }

    public boolean isActive() {
        return state == VehicleState.Active;
    }

    public void notifyConflicting() {
        state = VehicleState.Conflicting;
    }

    public boolean isConflicting() {
        return state == VehicleState.Conflicting;
    }

    public double[] getSonar(TrafficNetwork maze) {
        double[] new_sonar = new double[5];


        return new_sonar;
    }

    public double[] getAVSonar(TrafficNetwork maze) {
        double[] new_av_sonar = new double[5];

        return new_av_sonar;
    }

    public int move(TrafficNetwork maze, int d) {
        List<Integer> neighbors = maze.findAvailableMoves4Vehicle(getId());

        prevPosition = currentPosition;
        currentPosition = neighbors.get(d);


        maze.updateVehicleState(this);

        path.add(currentPosition);

        return (1);
    }

    public void notifyHitMine() {
        state = VehicleState.HitMine;
    }

    public void notifyHitTarget() {
        state = VehicleState.HitTarget;
    }

    public List<Integer> getPath() {
        return path;
    }

    public boolean isHitTarget() {
        return state == VehicleState.HitTarget;
    }

    public boolean isHitMine() {
        return state == VehicleState.HitMine;
    }

    public int testMove(TrafficNetwork net, int d) {
        List<Integer> neighbors = net.findAvailableMoves4Vehicle(getId());
        return neighbors.get(d);
    }

    public boolean hasTraversed(int node1, int node2) {
        //int recency = Math.max(0, path.size()-30);
        for(int i = 0; i < path.size()-1; ++i){
            if(path.get(i) == node1 && path.get(i+1) == node2){
                return true;
            }
        }
        return false;
    }
}
