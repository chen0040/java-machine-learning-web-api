package com.github.chen0040.ml.ann.art.mas.traffic.env;

import com.github.chen0040.ml.ann.art.mas.utils.Vec2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

public class Graph implements Cloneable
{
    private ArrayList<Integer>[] adj;
    private Vec2D[] positions;
    private int numNodes;

    @Override
    public Object clone(){
        Graph g = new Graph(numNodes);
        g.copy(this);
        return g;
    }

    public int getDegree(int v){
        return adj[v].size();
    }

    public void copy(Graph rhs){
        numNodes = rhs.numNodes;
        positions = new Vec2D[numNodes];
        for(int i=0; i < numNodes; ++i){
            positions[i] = (Vec2D)rhs.position(i).clone();
        }
        adj = new ArrayList[numNodes];
        for(int i=0; i < numNodes; ++i){
            adj[i] = (ArrayList<Integer>)rhs.adj[i].clone();
        }
    }

    public Graph(int numNodes){
        this.numNodes = numNodes;
        adj = new ArrayList[numNodes];
        for(int i=0; i < numNodes; ++i){
            adj[i] = new ArrayList<Integer>();
        }
        positions = new Vec2D[numNodes];
        for(int i = 0; i < numNodes; ++i){
            positions[i] = new Vec2D();
        }
    }

    public List<Integer> getCluster(int v, int maxCount){
        HashSet<Integer> points = new HashSet<Integer>();
        getCluster(v, points, maxCount);

        return new ArrayList<Integer>(points);
    }

    private void getCluster(int v, HashSet<Integer> points, int maxCount){
        if(points.size() >= maxCount) return;
        List<Integer> neighbors = findNeighbors(v);
        points.add(v);
        for(int i=0; i < neighbors.size(); ++i){
            if(points.size() >= maxCount) {
                return;
            }
            int v2 = neighbors.get(i);
            if(!points.contains(v2)){
                getCluster(v2, points, maxCount);
            }
        }
    }

    public static void reconstructPath(HashMap<Integer, Integer> came_from, int current_node, List<Integer> p)
    {
        if (came_from.containsKey(current_node))
        {
            reconstructPath(came_from, came_from.get(current_node), p);
            p.add(current_node);
        }
        else
        {
            p.add(current_node);
        }
    }

    public List<Integer> astar(List<Integer> original_path) {
        ArrayList<Integer>[] adj2 = new ArrayList[numNodes];
        for(int i=0; i < numNodes; ++i){
            adj2[i] = new ArrayList<Integer>();
        }
        for(int i=0; i < original_path.size()-1; ++i){
            int j = i+1;
            int state1_id = original_path.get(i);
            int state2_id = original_path.get(j);
            if(!adj2[state1_id].contains(state2_id)){
                adj2[state1_id].add(state2_id);
            }
            if(!adj2[state2_id].contains(state1_id)){
                adj2[state2_id].add(state1_id);
            }
        }
        int start_node = original_path.get(0);
        int end_node = original_path.get(original_path.size()-1);

        HashSet<Integer> blocked_nodes = new HashSet<Integer>();
        List<Integer> p = astar(adj2, start_node, end_node, blocked_nodes, new BiFunction<Integer, Integer, Double>() {
            public Double apply(Integer state1, Integer state2) {
                return Graph.this.getLineDistance(state1, state2);
            }
        });
        return p;
    }

    public static List<Integer> astar(ArrayList<Integer>[] adj, int start_node, int end_node, HashSet<Integer> blocked_nodes, BiFunction<Integer, Integer, Double> getLineDistance)
    {
        int numNodes = adj.length;

        HashSet<Integer> open_set = new HashSet<Integer>();
        HashSet<Integer> closed_set = new HashSet<Integer>();
        HashMap<Integer, Integer> came_from = new HashMap<Integer, Integer>();


        int current_node = start_node;
        double[] g_score = new double[numNodes];
        double[] h_score = new double[numNodes];
        double[] f_score = new double[numNodes];

        g_score[start_node] = 0.0;
        h_score[start_node] = getLineDistance.apply(start_node, end_node);
        f_score[start_node] = g_score[start_node] + h_score[start_node];

        open_set.add(start_node);

        while (open_set.size() > 0)
        {
            double lowest_f_score = Double.MAX_VALUE;
            int node_x = -1;
            for (int node : open_set)
            {
                double f_score_value = f_score[node];
                if (lowest_f_score > f_score_value)
                {
                    lowest_f_score = f_score_value;
                    node_x = node;
                }
            }

            if (node_x == end_node)
            {
                List<Integer> p = new ArrayList<Integer>();
                reconstructPath(came_from, end_node, p);
                return p;
            }

            open_set.remove(node_x);
            closed_set.add(node_x);
            Iterable<Integer> neighbor_nodes_x = adj[node_x];
            for (Integer node_y : neighbor_nodes_x)
            {
                if (blocked_nodes.contains(node_y) && node_y != end_node) continue;
                if (closed_set.contains(node_y)) continue;

                double tentative_g_score = g_score[node_x] + getLineDistance.apply(node_x, node_y);
                boolean tentative_is_better = false;
                if (!open_set.contains(node_y)) {
                    open_set.add(node_y);
                    tentative_is_better = true;
                } else if (tentative_g_score < g_score[node_y]) {
                    tentative_is_better = true;
                }

                if (tentative_is_better) {
                    came_from.put(node_y, node_x);
                    g_score[node_y] = tentative_g_score;
                    h_score[node_y] = getLineDistance.apply(node_y, end_node);
                    f_score[node_y] = g_score[node_y] + h_score[node_y];
                }
            }
        }

        return null;
    }

    public boolean isConnected(int vi, int vj){
        return adj[vi].contains(vj);
    }

    public Vec2D position(int v){
        return positions[v];
    }

    public void position(int v, Vec2D pos){
        positions[v] = pos;
    }

    public int edgeCount(){
        int edgeCount = 0;
        for(int i=0; i < adj.length; ++i){
            edgeCount += adj[i].size();
        }
        return edgeCount / 2;
    }

    public boolean isDenseGraph()
    {
        int edge_count = edgeCount();
        int node_count = positions.length;

        return node_count > 1 && edge_count > 10 && Math.log(node_count) * node_count < edge_count;
    }

    public boolean isSparseGraph()
    {
        return !isDenseGraph();
    }

    public List<Integer> findNeighbors(int nodeId){
        return adj[nodeId];
    }

    public void connect(int from, int to) {
        if(!adj[from].contains(to)) {
            adj[from].add(to);
        }
        if(!adj[to].contains(from)) {
            adj[to].add(from);
        }
    }

    public int getDegree() {
        int node_count = positions.length;
        if (node_count == 0) return 0;
        int edge_count = 0;
        for(int nodeId = 0; nodeId < node_count; ++nodeId)
        {
            edge_count += adj[nodeId].size();
        }
        return edge_count / node_count;
    }

    public Vec2D computeCenter()
    {
        Vec2D pt = null;

        for(int nodeId = 0; nodeId < numNodes; ++nodeId) {
            if (pt == null) {
                pt = (Vec2D) positions[nodeId].clone();
            } else {
                Vec2D pt2 = positions[nodeId];
                pt.setX(pt.getX() + pt2.getX());
                pt.setY(pt.getY() + pt2.getY());
            }
        }

        pt.setX(pt.getX() / numNodes);
        pt.setY(pt.getY() / numNodes);

        return pt;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public double getLineDistance(int v1, int v2){
        return positions[v1].distance(positions[v2]);
    }
}

