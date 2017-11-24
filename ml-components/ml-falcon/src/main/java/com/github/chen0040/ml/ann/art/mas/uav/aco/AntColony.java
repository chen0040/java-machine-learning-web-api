package com.github.chen0040.ml.ann.art.mas.uav.aco;

import com.github.chen0040.ml.ann.art.mas.utils.Vec2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen0469 on 9/30/2015 0030.
 */
public class AntColony {

    private int numAnts = 4;
    private int numStates;
    private int numActions;

    private SparseGraph graph;
    private SparseGraph graph0;
    private UavPath bestUavSolution;

    private Ant[] ants;
    private double[][] pheromones;

    private double m_rho = 0.1;
    private double m_beta = 2;
    private double m_alpha = 1;
    private double m_rho2 = 0.1;
    private double tau0;

    public UavPath getBestUavSolution(){
        return bestUavSolution;
    }

    public double[][] getPheromones(){
        return pheromones;
    }

    public AntColony(SparseGraph graph, int numAgents, int numStates, int numActions) {
        this.graph0 = graph;
        this.graph = (SparseGraph) graph0.clone();
        refreshMaze(numAgents, numStates, numActions);
    }

    public int getNumAnts(){
        return numAnts;
    }

    public void updateVehicleState(Ant ant) {
        int state1_id = ant.getPrevPosition();
        int state2_id = ant.getCurrentPosition();

        double pheromone = pheromones[state1_id][state2_id];
        pheromone = (1 - m_rho) * pheromone + m_rho * tau0;
        if (pheromone < tau0)
        {
            pheromone = tau0;
        }

        pheromones[state1_id][state2_id] = pheromone;
        pheromones[state2_id][state1_id] = pheromone;
    }



    public void evaporatePheromone(){
        double pheromone = 0;
        int stateCount = graph.getNumNodes();
        for (int i = 0; i < stateCount; ++i)
        {
            for (int j = 0; j < stateCount; ++j)
            {
                pheromone = pheromones[i][j];
                pheromone = (1 - m_rho2) * pheromone;
                if (pheromone < tau0)
                {
                    pheromone = tau0;
                }
                pheromones[i][j] = pheromone;
            }
        }
    }

    public void depositPheromone(){
        double minDistance = Double.MAX_VALUE;
        int bestAntId = -1;
        for(int antId = 0; antId < numAnts; ++antId) {
            Ant ant = ants[antId];
            double distance = graph.getCost(ant.getPath());
            if(distance < minDistance){
                minDistance = distance;
                bestAntId = antId;
            }
        }
        depositPheromone(ants[bestAntId]);
    }

    private void depositPheromone(Ant ant){
        List<Integer> path = ant.getPath(); //graph.astar(ant.getPath());
        double distance = graph.getCost(path);
        int segment_count = path.size();

        for (int i = 0; i < segment_count-1; ++i) {
            int j = i+1;
            Integer state1_id = path.get(i);
            Integer state2_id = path.get(j);

            double pheromone = pheromones[state1_id][state2_id];

            double p_delta = 1.0 / distance; //graph.getLineDistance(path.get(i), path.get(path.size()-1));
            pheromone += m_rho2 * p_delta;

            pheromones[state1_id][state2_id] = pheromone;
            pheromones[state2_id][state1_id] = pheromone;
        }
    }

    public void localSearch(){
        for(int antId = 0; antId < numAnts; ++antId){
            ants[antId].localSearch(graph);
        }
    }

    public double getReward(int antId){
        Ant tsp = ants[antId];
        return getReward(tsp);
    }

    private double getReward(Ant ant){
        int state2 = ant.getCurrentPosition();
        int state1 = ant.getPrevPosition();
        return 1.0 / (1.0 + graph.getLineDistance(state1, state2));
    }

    public SparseGraph graph(){
        return graph;
    }

    private boolean hasAgent(int v){
        for(int i=0; i < numAnts; ++i){
            if(ants[i].getCurrentPosition()==v) return true;
        }
        return false;
    }

    public void refreshMaze(int numAnts, int numStates, int numActions) {
        int numNodes = graph.getNumNodes();
        this.numActions = numActions;
        this.numStates = numStates;
        this.numAnts = numAnts;

        tau0 = 1.0 / numNodes;
        pheromones = new double[numNodes][];
        for(int i=0; i < numNodes; ++i) {
            pheromones[i] = new double[numNodes];
            for(int j=0; j < numNodes; ++j){
                pheromones[i][j] = tau0;
            }
        }

        ants = new Ant[this.numAnts];
        for (int k = 0; k < this.numAnts; k++) {
            ants[k] = new Ant(k);
            ants[k].setCurrentPosition(-1);
        }

        for (int k = 0; k < this.numAnts; k++) {
            int v;
            do{
                v = (int)(Math.random() * numNodes);
            } while(hasAgent(v));
            ants[k].setCurrentPosition(v);
            ants[k].activate();
        }
    }

    public boolean isActive(int antId) {
        return ants[antId].isActive();
    }

    public void deactivate(int antId) {
        ants[antId].notifyHitTarget();
    }

    public int move(int antId, int d) {
        Ant ant = ants[antId];
        return ant.move(this, d);
    }

    public void updateBestUavSolution(){

        double minCost = Double.MAX_VALUE;
        List<Integer> bestPath = null;
        for(int antId = 0; antId < numAnts; ++antId){
            Ant ant = ants[antId];
            List<Integer> path = ant.getPath();
            double cost = graph.getCost(path);
            if(minCost > cost){
                minCost = cost;
                bestPath = path;
            }
        }

        if(bestUavSolution == null || bestUavSolution.getCost() > minCost){
            bestUavSolution = new UavPath(bestPath, minCost, graph.getUavDistanceTravelled(bestPath));
        }
    }

    public double getUavDistanceTravelled(int antId){
        Ant ant = ants[antId];
        List<Integer> path = ant.getPath();
        return graph.getUavDistanceTravelled(path);
    }

    public Vec2D[] getAntPositions() {
        Vec2D[] positions = new Vec2D[numAnts];
        for (int i = 0; i < numAnts; ++i) {
            int v = ants[i].getCurrentPosition();
            positions[i] = graph.position(v);
        }
        return positions;
    }

    public int getCurrentPosition4Vehicle(int antId) {
        return ants[antId].getCurrentPosition();
    }



    public List<Integer> getAntAvailableMoves(int antId){
        Ant ant = ants[antId];
        List<Integer> forwardNodes = graph.findNeighbors(ant.getCurrentPosition());

        List<Integer> availableMoves = new ArrayList<Integer>();
        for(int i=0; i < forwardNodes.size(); ++i){
            if(availableMoves.size() > numActions) break;
            int v = forwardNodes.get(i);
            if (!ant.hasTraversed(v)){
                availableMoves.add(v);
            }
        }
        return availableMoves;
    }

    public double[] getState(int antId) {
        int v = ants[antId].getCurrentPosition();
        List<Integer> vList = getAntAvailableMoves(antId); //findForwardNodes(v, ants[antId].getPrevPosition());

        double[] sonar = new double[numStates];
        double sum = 0;

        for(int i = 0; i < vList.size(); ++i){
            if(i >= sonar.length)  break;
            double signal = getEdgePheromone(v, vList.get(i));
            sum += signal;
            sonar[i] = signal;
        }


        for(int i = 0; i < vList.size(); ++i) {
            if (i >= sonar.length) break;
            sonar[i] /= sum;
        }
        return sonar;
    }

    public double getEdgePheromone(int state1_id, int state2_id){
        double heuristic_cost = graph.getLineDistance(state1_id, state2_id);

        double pheromone = pheromones[state1_id][state2_id];
        double product = Math.pow(pheromone, m_alpha) * Math.pow(heuristic_cost, m_beta);

        return product;
    }

    public void startAntTraversal() {
        int numNodes = graph.getNumNodes();

        for(int k=0; k < numAnts; ++k){
            ants[k] = new Ant(k);
            ants[k].setCurrentPosition(-1);
        }


        int v = (int)(Math.random() * numNodes);
        for (int k = 0; k < numAnts; k++) {


            ants[k].setCurrentPosition(v);
            ants[k].activate();
        }
    }

    public int[] getAntBearing() {
        int[] bearing = new int[numAnts];
        for(int antId = 0; antId < numAnts; ++antId){
            Ant ant = ants[antId];
            bearing[antId] = getAntBearing(ant);
        }
        return bearing;
    }

    private int getAntBearing(Ant ant){
        int state1 = ant.getPrevPosition();
        int state2 = ant.getCurrentPosition();

        if(state1==state2) return 0;

        Vec2D state1_pos = graph.position(state1);
        Vec2D state2_pos = graph.position(state2);

        Vec2D velocity = state2_pos.minus(state1_pos);
        double angle = Math.atan2(velocity.getY(), velocity.getX());

        if(angle < 0) {
            angle += 2 * Math.PI;
        }

        angle += (Math.PI / 2);

        angle /= (Math.PI / 4);

        return (int)angle % 8;

     }

    public double getTau0() {
        return tau0;
    }

    public double getCost(int agt) {
        Ant ant = ants[agt];
        return graph.getCost(ant.getPath());
    }



    public int getNumNodes() {
        return graph.getNumNodes();
    }

    public void createSparseGraph(double degrees) {
        graph = (SparseGraph) graph0.clone();
        graph.sparsify(degrees);
    }

    public int calcNodeCovered(int agt) {
        return ants[agt].getPath().size();
    }
}
