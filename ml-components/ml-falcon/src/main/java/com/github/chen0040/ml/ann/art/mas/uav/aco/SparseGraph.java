package com.github.chen0040.ml.ann.art.mas.uav.aco;

import com.github.chen0040.ml.ann.art.mas.utils.Vec2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SparseGraph implements Cloneable
{
    private ArrayList<Integer>[] adj;
    private Vec2D[] positions;
    private int numNodes;
    private double maxX;
    private double minX;
    private double maxY;
    private double minY;
    private double longestLineDistance;

    @Override
    public Object clone(){
        SparseGraph g = new SparseGraph();
        g.copy(this);
        return g;
    }

    public void copy(SparseGraph rhs){
        numNodes = rhs.numNodes;
        maxX = rhs.maxX;
        minX = rhs.minX;
        maxY = rhs.maxY;
        minY = rhs.minY;
        positions = new Vec2D[numNodes];
        longestLineDistance = rhs.longestLineDistance;
        for(int i=0; i < numNodes; ++i){
            positions[i] = (Vec2D)rhs.position(i).clone();
        }
        adj = new ArrayList[numNodes];
        for(int i=0; i < numNodes; ++i){
            adj[i] = (ArrayList<Integer>)rhs.adj[i].clone();
        }
    }

    private SparseGraph(){

    }

    public SparseGraph(List<Vec2D> points){
        numNodes = points.size();
        adj = new ArrayList[numNodes];
        for(int i=0; i < numNodes; ++i){
            adj[i] = new ArrayList<Integer>();
            for(int j=0; j < numNodes; ++j){
                if(i==j) continue;
                adj[i].add(j);
            }
        }

        maxX = Double.NEGATIVE_INFINITY;
        minX = Double.MAX_VALUE;
        maxY = Double.NEGATIVE_INFINITY;
        minY = Double.MAX_VALUE;


        positions = new Vec2D[numNodes];
        for(int i = 0; i < numNodes; ++i){
            position(i, points.get(i));
        }

        longestLineDistance = 0;
        for(int i=0; i < numNodes; ++i) {
            for(int j=i+1; j < numNodes; ++j) {
                double distance = getLineDistance(i, j);
                longestLineDistance = Math.max(longestLineDistance, distance);
            }
        }

        for(int i=0; i < numNodes; ++i){
            final int vi = i;
            Collections.sort(adj[vi], new Comparator<Integer>() {
                public int compare(Integer v1, Integer v2) {
                    double d1 = getLineDistance(vi, v1);
                    double d2 = getLineDistance(vi, v2);
                    return Double.compare(d1, d2);
                }
            });
        }


    }

    public boolean isConnected(int vi, int vj){
        return adj[vi].contains(vj);
    }

    public Vec2D position(int v){
        return positions[v];
    }

    public void position(int v, Vec2D pos){
        positions[v] = pos;
        maxX = Math.max(maxX, pos.getX());
        minX = Math.min(minX, pos.getX());
        maxY = Math.max(maxY, pos.getY());
        minY = Math.min(minY, pos.getY());
    }

    public double getUavDistanceTravelled(List<Integer> path) {
        double distance = 0;
        for (int i = 0; i < path.size(); ++i) {
            int j = (i + 1) % path.size();

            int state1_id = path.get(i);
            int state2_id = path.get(j);
            distance += getLineDistance(state1_id, state2_id);
        }

        return distance;
    }

    public double getCost(List<Integer> path){
        double distance = getUavDistanceTravelled(path);

        return distance * Math.pow((double)getNumNodes() / (path.size() + 1), 2.0); // distance + penalty(getNumNodes() - path.size());
    }

    private double penalty(int unvisitedNodeNum){
        return unvisitedNodeNum * longestLineDistance / 2;
    }

    public List<Integer> findNeighbors(int nodeId){
        return adj[nodeId];
    }

    public int getNumNodes() {
        return numNodes;
    }

    public double getLineDistance(int v1, int v2){
        return positions[v1].distance(positions[v2]);
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinX(){
        return minX;
    }

    public double getMaxY(){
        return maxY;
    }

    public double getMinY(){
        return minY;
    }

    public void sparsify(double degrees) {
        int nnsize = (int)(Math.floor(degrees * numNodes));
        for(int i=0; i < adj.length; ++i) {
            ArrayList<Integer> nnl = adj[i];

            int size = nnl.size();

            for(int j=size - 1; j >= nnsize; --j){
                nnl.remove(j);
            }
        }
    }
}

