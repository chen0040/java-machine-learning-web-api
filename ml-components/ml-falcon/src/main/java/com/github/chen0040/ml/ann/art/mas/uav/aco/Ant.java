package com.github.chen0040.ml.ann.art.mas.uav.aco;

import com.github.chen0040.ml.ann.art.mas.utils.VehicleState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen0469 on 9/30/2015 0030.
 */
public class Ant {
    private int currentPosition = 0;
    private int prevPosition = 0;
    private VehicleState state;
    private List<Integer> path = new ArrayList<Integer>();
    private int id;

    public Ant(int agentId) {
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

    public double[] getSonar(AntColony maze) {
        double[] new_sonar = new double[5];


        return new_sonar;
    }

    public double[] getAVSonar(AntColony maze) {
        double[] new_av_sonar = new double[5];

        return new_av_sonar;
    }

    public int move(AntColony maze, int d) {
        List<Integer> neighbors = maze.getAntAvailableMoves(getId());

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

    public int testMove(AntColony net, int d) {
        List<Integer> neighbors = net.getAntAvailableMoves(getId());
        return neighbors.get(d);
    }

    public boolean hasTraversed(int node1) {
        return path.contains(node1);
    }

    public void localSearch(SparseGraph g){
        double distance = g.getCost(path);
        for(int i=0; i < path.size()-1; ++i){
            for(int k = 0; k < 5; ++k){
                int j = (int)(Math.random() * g.getNumNodes());
                if(i == j) continue;

                int temp = path.get(i);
                path.set(i, path.get(j));
                path.set(j, temp);
                double distance2 = g.getCost(path);
                if(distance2 >= distance){
                    temp = path.get(i);
                    path.set(i, path.get(j));
                    path.set(j, temp);
                }
            }
        }
    }


}
