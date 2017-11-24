package com.github.chen0040.ml.ann.art.mas.traffic.env;

import com.github.chen0040.ml.ann.art.mas.utils.Vec2D;

import java.util.*;

public class GraphGenerator
{
    private int numNodes;
    private int numEdges;
    public boolean cyclic = false;
    private Random random = new Random();
    private double maxX;
    private double minX;
    private double maxY;
    private double minY;
    private boolean randomized = true;
    private double radiusRatio = 1.5f;
    private double randomFactor = 0.6;

    public double getRandomFactor(){
        return randomFactor;
    }

    public void setRandomFactor(double randomFactor) {
        this.randomFactor = randomFactor;
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        this.minX = minX;
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        this.minY = minY;
    }

    public boolean isRandomized() {
        return randomized;
    }

    public void setRandomized(boolean value) {
        randomized = value;
    }

    public double getRadiusRatio() {
        return radiusRatio;
    }

    public void setRadiusRatio(double value) {
        radiusRatio = value;
    }

    public HashSet<Integer> GetCandidateNodes(Integer node, Graph g) {
        HashSet<Integer> candidate_nodes = new HashSet<Integer>();

        int node_per_side = (int) Math.sqrt(getNumNodes());

        double lat_interval = (maxX - minX) * radiusRatio / node_per_side;
        double lng_interval = (maxY - minY) * radiusRatio / node_per_side;

        Vec2D gp = g.position(node);
        SortedMap<Double, Integer> pq = new TreeMap<Double, Integer>();
        for (int candidate_node = 0; candidate_node < g.getNumNodes(); ++candidate_node) {
            if (candidate_node == node) continue;
            Vec2D candidate_gp = g.position(candidate_node);
            double lat_difference = Math.abs(gp.getX() - candidate_gp.getX());
            double lng_difference = Math.abs(gp.getY() - candidate_gp.getY());
            if (lat_difference < lat_interval && lng_difference < lng_interval) {
                double distanceSq = lat_difference * lat_difference + lng_difference * lng_difference;
                if(g.getDegree(candidate_node) < 4) {
                    pq.put(distanceSq, candidate_node);
                    if (pq.size() > 4) {
                        pq.remove(pq.firstKey());
                    }
                }
            }
        }

        for(Integer v : pq.values()){
            candidate_nodes.add(v);
        }

        return candidate_nodes;
    }


    public Vec2D createPosition(int index, Graph g) {
        int node_per_side = (int) Math.sqrt(getNumNodes());

        int row_index = index / node_per_side;
        int col_index = index % node_per_side;

        double lat_interval = (maxX - minX) / node_per_side;
        double lng_interval = (maxY - minY) / node_per_side;

        double min_lat = lat_interval * row_index + minX;
        double min_lng = lng_interval * col_index + minY;

        if (randomized) {
            double lat = random.nextDouble() * lat_interval * randomFactor + min_lat;
            double lng = random.nextDouble() * lng_interval * randomFactor + min_lng;
            return new Vec2D(lat, lng);
        } else {
            return new Vec2D(min_lat + lat_interval / 2, min_lng + lng_interval / 2);
        }
    }

    public void Rearrange(List<Integer> nodes, Graph g) {

        for (Integer v : nodes){
            double lat = 0;
            double lng = 0;
            Collection<Integer> neighbors = g.findNeighbors(v);
            int out_node_count = neighbors.size();
            if (out_node_count > 0) {
                for (Integer neighbor : neighbors) {
                    Vec2D to_gp = g.position(neighbor);
                    lat += to_gp.getX();
                    lng += to_gp.getY();
                }

                lat /= out_node_count;
                lng /= out_node_count;
            }
            Vec2D gp = g.position(v);
            gp.setX(lat);
            gp.setY(lng);
        }
    }

    public GraphGenerator() {

    }

    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int value) {
        numNodes = value;
    }

    public boolean isCyclic(){
        return cyclic;
    }

    public void setCyclic(boolean cyclic){
        this.cyclic = cyclic;
    }

    public int getNumEdges(){
        return numEdges;
    }

    public void setNumEdges(int value) {
        numEdges = value;
    }

    public Graph generate()
    {
        if (getNumNodes() > 1 && getNumEdges() > 10 && Math.log(getNumNodes()) * getNumNodes() < getNumEdges()) {
            return GenerateDenseGraph();
        } else {
            return GenerateSparseGraph();
        }
    }

    private int  GetMaxEdges(Graph g)
    {
        int count = 0;
        for (int node = 0; node < g.getNumNodes(); ++node) {
            count += GetCandidateNodes(node, g).size();
        }
        return count;
    }

    /// <summary>
    /// Shuffle the array.
    /// </summary>
    /// <typeparam name="T">Array element type.</typeparam>
    /// <param name="array">Array to shuffle.</param>
    private <T> void Shuffle(T[] array)
    {
        Random random = this.random;
        for (int i = array.length; i > 1; i--)
        {
            // Pick random element to swap.
            int j = random.nextInt(i); // 0 <= j <= i-1
            // Swap.
            T tmp = array[j];
            array[j] = array[i - 1];
            array[i - 1] = tmp;
        }
    }

    private Graph GenerateDenseGraph()
    {
        Graph g = new Graph(numNodes);
        for(int i=0; i < numNodes; ++i) {
            g.position(i, createPosition(i, g));
        }

        int max_edge_count = GetMaxEdges(g);
        int m = Math.min(max_edge_count, numEdges);

        Boolean[] edgeWanted = new Boolean[max_edge_count];
        for (int edge_index = 0; edge_index < max_edge_count; ++edge_index)
        {
            edgeWanted[edge_index] = edge_index < m;
        }

        Shuffle(edgeWanted);

        for(int node=0, k=0; node < numNodes; node++)
        {
            HashSet<Integer> candidate_nodes = GetCandidateNodes(node, g);
            for(Integer candidate_node : candidate_nodes)
            {
                k++;
                if (k < edgeWanted.length && edgeWanted[k])
                {
                    g.connect(node, candidate_node);
                }
            }
        }
        return g;
    }


    private Graph GenerateSparseGraph() {
        Graph g = new Graph(numNodes);
        for(int v = 0; v < numNodes; ++v){
            g.position(v, createPosition(v, g));
        }

        int max_edge_count = GetMaxEdges(g);
        int m = Math.min(max_edge_count, getNumEdges());

        int max_stag_loop_counter = m * 2;
        int mloop_counter = m;
        int count = m;
        int stag_loop_counter = 0;
        while (count > 0) {
            int v = random.nextInt(numNodes);

            HashSet<Integer> candidate_nodes = GetCandidateNodes(v, g);
            List<Integer> candidate_nodes2 = new ArrayList<Integer>(candidate_nodes);
            int candidate_node_count = candidate_nodes2.size();
            if (candidate_node_count == 0) continue;

            int w = candidate_nodes2.get(random.nextInt(candidate_node_count));

            if(w == v) continue;
            if (g.isConnected(v, w))
            {
                if (stag_loop_counter > mloop_counter) {
                    w = -1;
                    for (Integer candidate_node : candidate_nodes2) {
                        if (g.isConnected(v, candidate_node)) continue;
                        else {
                            if (w == -1) w = candidate_node;
                            else if (random.nextDouble() < 0.5) w = candidate_node;
                        }
                    }

                    if (w == -1) {
                        stag_loop_counter++;
                        if (stag_loop_counter > max_stag_loop_counter) break;
                        else continue;
                    }
                } else {
                    stag_loop_counter++;
                    if (stag_loop_counter > max_stag_loop_counter) break;
                    else continue;
                }
            }

            g.connect(v, w);
            count--;
        }
        return g;
    }
}
