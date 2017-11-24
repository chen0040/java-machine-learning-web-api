package com.github.chen0040.ml.ann.art.mas.traffic.env;

import com.github.chen0040.ml.ann.art.mas.utils.RewardType;
import com.github.chen0040.ml.ann.art.mas.utils.Vec2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by chen0469 on 9/30/2015 0030.
 */
public class TrafficNetwork {
    private RewardType rewardType = RewardType.Exp;
    private int[] targets;

    private int numMines = 10;
    private int numVehicles = 4;
    private int numTargets = 4;

    private GraphGenerator generator;
    private Graph graph;

    private int[] mines2;
    private AutonomousVehicle[] vehicles;
    private double[][] pheromones;

    private double m_rho_local = 0.1;
    private double m_rho_global = 0.5;
    private double m_beta = 2;
    private double m_alpha = 0.8;
    private double mTau0;

    private double[] minSteps;
    private boolean symmetric = false;
    private Random random = new Random();

    public double[][] getPheromones(){
        return pheromones;
    }

    public TrafficNetwork(int size, int numMines, int numAgents, int numTargets) {
        generator = new GraphGenerator();
        generator.setNumNodes(size);
        refreshMaze(size, numMines, numAgents, numTargets);
    }

    public int getNumMines(){
        return numMines;
    }

    public int getNumVehicles(){
        return numVehicles;
    }

    public int getNumTargets(){
        return numTargets;
    }

    public double getMinX(){
        return generator.getMinX();
    }

    public double getMaxX(){
        return generator.getMaxX();
    }

    public double getMinY(){
        return generator.getMinY();
    }

    public double getMaxY(){
        return generator.getMaxY();
    }

    public boolean hasMine(int v) {
        for(int i = 0; i < numMines; ++i) {
            if(mines2[i] == v){
                return true;
            }
        }
        return false;
    }

    public void updateVehicleState(AutonomousVehicle vehicle) {
        int state1_id = vehicle.getPrevPosition();
        int state2_id = vehicle.getCurrentPosition();

        if (isHitMine(vehicle)) {
            vehicle.notifyHitMine();
        } else if (isHitTarget(vehicle)) {
            vehicle.notifyHitTarget();
        }


        double pheromone = pheromones[state1_id][state2_id];
        pheromone = (1 - m_rho_local) * pheromone + m_rho_local * mTau0;
        if (pheromone < mTau0)
        {
            pheromone = mTau0;
        }

        pheromones[state1_id][state2_id] = pheromone;
        if (symmetric)
        {
            pheromones[state2_id][state1_id] = pheromone;
        }
    }

    public double getReward4Vehicle(int vehicleId, boolean immediate) {
        return getRewardAtNode(vehicles[vehicleId].getCurrentPosition(), immediate);
    }

    private boolean isTarget(int nodeId) {
        for (int targetId : targets) {
            if (targetId == nodeId) return true;
        }
        return false;
    }

    public double getRewardAtNode(int nodeId, boolean immediate) {
        if (isTarget(nodeId)) {
            return 1; // reach target
        }

        if (hasMine(nodeId)) return 0;

        if (immediate) {
            if (rewardType == RewardType.Linear) {
                double r = getMinLineDistance(nodeId, targets);
                if (r > 10) r = 10;
                return 1.0 - r / 10.0; //adjust intermediate reward
            } else
                return 1.0 / (1 + getMinLineDistance(nodeId, targets)); //adjust intermediate reward
        }
        return 0.0; //no intermediate reward
    }

    private double getDistanceTravelled(List<Integer> path){
        double distance = 0;
        for(int i=0; i < path.size()-1; ++i){
            int j = i+1;
            int state1_id = path.get(i);
            int state2_id = path.get(j);
            distance += graph.getLineDistance(state1_id, state2_id);
        }
        return distance;
    }

    public void evaporatePheromone(){
        double pheromone = 0;
        int stateCount = graph.getNumNodes();
        for (int i = 0; i < stateCount; ++i)
        {
            for (int j = 0; j < stateCount; ++j)
            {
                pheromone = pheromones[i][j];
                pheromone = (1 - m_rho_global) * pheromone;
                if (pheromone < mTau0)
                {
                    pheromone = mTau0;
                }
                pheromones[i][j] = pheromone;
            }
        }
    }

    public void depositPheromone(){
        boolean success = false;
        double minDistance = Double.MAX_VALUE;
        int bestAntId = -1;
        for(int vehicleId = 0; vehicleId < numVehicles; ++vehicleId) {
            AutonomousVehicle vehicle = vehicles[vehicleId];
            if(vehicle.isHitTarget()) {
                success = true;
                depositPheromone(vehicle);
            }
            double lineDistance = getMinLineDistance(vehicle.getCurrentPosition(), targets);
            if(lineDistance < minDistance){
                minDistance = lineDistance;
                bestAntId = vehicleId;
            }
        }
        if(!success){
            depositPheromone(vehicles[bestAntId]);
        }
    }

    private void depositPheromone(AutonomousVehicle vehicle){
        List<Integer> path = vehicle.getPath(); //graph.astar(vehicle.getPath());
        double distance = getDistanceTravelled(path);
        int segment_count = path.size();

        for (int i = 0; i < segment_count-1; ++i) {
            int j = i+1;
            Integer state1_id = path.get(i);
            Integer state2_id = path.get(j);

            double pheromone = pheromones[state1_id][state2_id];

            double p_delta = getMinLineDistance(path.get(0), targets) / distance; //graph.getLineDistance(path.get(i), path.get(path.size()-1));
            pheromone += m_rho_global * p_delta;

            pheromones[state1_id][state2_id] = pheromone;
            if(symmetric) {
                pheromones[state2_id][state1_id] = pheromone;
            }
        }
    }

    private double getMinLineDistance(int v, int[] V2) {
        double minDistance = Double.MAX_VALUE;
        for (int v2 : V2) {
            if (v2 == v) {
                return 0;
            }
            double lineDistance = graph.getLineDistance(v, v2);
            minDistance = Math.min(lineDistance, minDistance);
        }
        return minDistance;
    }

    public double getReward(AutonomousVehicle vehicle, int d, boolean immediate) {
        int v = vehicle.testMove(this, d);
        double r = getRewardAtNode(v, immediate);
        return r;
    }

    public void moveTargets() {
        for(int i=0; i < targets.length; ++i) {
            int v = targets[i];
            targets[i] = moveTarget(v);
        }
    }

    private int moveTarget(int v) {
        List<Integer> neighbors = new ArrayList<Integer>(graph.findNeighbors(v));
        return neighbors.get(random.nextInt(neighbors.size()));
    }

    private int[] createMines(int numNodes){
        int[] mines = new int[numMines];
        for(int i=0; i < numMines; ++i){
            mines[i] = 0;
        }
        return mines;
    }

    public Graph graph(){
        return graph;
    }

    private boolean hasAgent(int v){
        for(int i=0; i < numVehicles; ++i){
            if(vehicles[i].getCurrentPosition()==v) return true;
        }
        return false;
    }

    public void refreshMaze(int size, int numMines, int numAgents, int numTargets) {
        int numNodes = size * size;
        generator.setNumNodes(numNodes);
        generator.setNumEdges(numNodes * 4);
        generator.setMinX(0);
        generator.setMaxX(size);
        generator.setMinY(0);
        generator.setMaxY(size);

        mTau0 = 1.0 / numNodes;
        pheromones = new double[numNodes][];
        for(int i=0; i < numNodes; ++i) {
            pheromones[i] = new double[numNodes];
            for(int j=0; j < numNodes; ++j){
                pheromones[i][j] = mTau0;
            }
        }

        graph = generator.generate();

        this.numMines = numMines;
        this.numVehicles = numAgents;
        this.numTargets = numTargets;

        targets = new int[this.numTargets];
        mines2 = createMines(numNodes);

        vehicles = new AutonomousVehicle[numVehicles];
        minSteps = new double[numVehicles];
        for (int k = 0; k < numVehicles; k++) {
            vehicles[k] = new AutonomousVehicle(k);
        }

        int seed = (int) (Math.random() * numNodes);
        List<Integer> cluster = graph.getCluster(seed, numVehicles);
        for (int k = 0; k < numVehicles; k++) {
            int v = cluster.get(k);
            vehicles[k].setCurrentPosition(v);
            vehicles[k].activate();
        }

        int v;
        for(int k = 0; k < numTargets; ++k) {
            do {
                v = (int) (Math.random() * numNodes);
                targets[k] = v;
            } while (hasAgent(v));
        }

        for (int i = 0; i < numMines; i++) {
            do {
                v = (int) (Math.random() * numNodes);
            } while (hasAgent(v) || hasMine(v) || isTarget(v));
            mines2[i] = v;
        }


    }

    public double getMinStep(int vehicleId) {
        return minSteps[vehicleId];
    }

    public boolean isActive(int vehicleId) {
        return vehicles[vehicleId].isActive();
    }

    public double[] getTargetRanges(int vehicleId) {
        AutonomousVehicle vehicle = vehicles[vehicleId];
        int v = vehicle.getCurrentPosition();
        List<Integer> vList = findAvailableMoves4Vehicle(vehicleId); //temp

        double[] ranges = new double[5];
        for(int i=0; i < vList.size(); ++i) {
            if(i >= 5) break;
            int vi = vList.get(i);
            ranges[i] = getTargetRangeAtNode(vi);
        }
        return ranges;
    }

    private double getTargetRangeAtNode(int v) {
        Vec2D v_pos = graph.position(v);

        double distance_sum = 0;
        for(int i=0; i < numTargets; ++i){
            int target = targets[i];
            Vec2D target_pos = graph.position(target);
            double distance = v_pos.distance(target_pos);
            if(distance > 15) distance = 15;
            distance_sum += distance;
        }
        return 1.0 - (distance_sum / numTargets) / 15;
    }

    public boolean isHitMine(int vehicleId) {
        AutonomousVehicle vehicle = vehicles[vehicleId];
        return isHitMine(vehicle);
    }

    public boolean isHitMine(AutonomousVehicle vehicle) {
        if (vehicle.isHitMine()) {
            return true;
        }
        return hasMine(vehicle.getCurrentPosition());
    }

    public boolean willHitMine(int vehicleId, int d) {
        int v2 = vehicles[vehicleId].testMove(this, d);
        return hasMine(v2);
    }

    public boolean willHitTarget(int vehicleId, int d) {
        int v2 = vehicles[vehicleId].testMove(this, d);
        return isTarget(v2);
    }

    public boolean isHitTarget(int vehicleId) {
        AutonomousVehicle vehicle = vehicles[vehicleId];
        if (vehicle.isHitTarget()) {
            return true;
        }
        return isHitTarget(vehicle);
    }

    public boolean isHitTarget(AutonomousVehicle vehicle) {
        return isTarget(vehicle.getCurrentPosition());
    }

    public int move(int vehicleId, int d) {
        AutonomousVehicle vehicle = vehicles[vehicleId];
        return vehicle.move(this, d);
    }

    public boolean isConflicting(int vehicleId) {
        AutonomousVehicle vehicle = vehicles[vehicleId];
        return vehicle.isConflicting();
    }

    public Vec2D[] getVehiclePositions() {
        Vec2D[] positions = new Vec2D[numVehicles];
        for (int i = 0; i < numVehicles; ++i) {
            int v = vehicles[i].getCurrentPosition();
            positions[i] = graph.position(v);
        }
        return positions;
    }

    public Vec2D[] getTargetPositions() {
        Vec2D[] positions = new Vec2D[numTargets];

        for(int i=0; i < numTargets; ++i) {
            int target = targets[i];
            positions[i] = graph.position(target);
        }

        return positions;
    }

    public Vec2D[] getMinePositions() {
        Vec2D[] positions = new Vec2D[numMines];
        for(int i = 0; i < numMines; ++i){
            Vec2D position = graph.position(mines2[i]);
            positions[i] = position;
        }
        return positions;
    }

    public List<Integer> findAvailableMoves4Vehicle(int vehicleId){
        AutonomousVehicle vehicle = vehicles[vehicleId];
        List<Integer> forwardNodes = graph.findNeighbors(vehicle.getCurrentPosition());

        List<Integer> availableMoves = new ArrayList<Integer>();
        for(int i=0; i < forwardNodes.size(); ++i){
            int v = forwardNodes.get(i);
            if (!vehicle.hasTraversed(vehicle.getCurrentPosition(), v)){
                availableMoves.add(v);
            }
        }
        return availableMoves;
    }



    public double[] getSonar(int vehicleId) {
        int v = vehicles[vehicleId].getCurrentPosition();
        List<Integer> vList = findAvailableMoves4Vehicle(vehicleId); //temp

        double[] sonar = new double[5];
        for(int i = 0; i < vList.size(); ++i){
            if(i >= sonar.length)  break;
            sonar[i] = getMineSignal(vList.get(i));
        }
        return sonar;
    }

    private double getMineSignal(int v) {
        Vec2D v_pos = graph.position(v);

        double distance_sum = 0;
        for(int i=0; i < numMines; ++i){
            int mine = mines2[i];
            Vec2D mine_pos = graph.position(mine);
            double distance = v_pos.distance(mine_pos);
            if(distance > 15) distance = 15;
            distance_sum += distance;
        }
        return 1.0 - (distance_sum / numMines) / 15;
    }

    public double[] getPheromoneSignals(int vehicleId) {
        int v = vehicles[vehicleId].getCurrentPosition();
        List<Integer> vList = findAvailableMoves4Vehicle(vehicleId); //findForwardNodes(v, vehicles[vehicleId].getPrevPosition());

        double[] sonar = new double[5];
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

    public void startNavigation() {
        int numNodes = graph.getNumNodes();
        for (int k = 0; k < numVehicles; k++) {
            int v;
            do {
                v = (int)(Math.random() * numNodes);
            }while(hasMine(v) || isTarget(v));
            vehicles[k] = new AutonomousVehicle(k);
            vehicles[k].setCurrentPosition(v);
            vehicles[k].activate();
        }

        for (int k = 0; k < numVehicles; k++) {
            minSteps[k] = getMinLineDistance(vehicles[k].getCurrentPosition(), targets);
        }
    }

    public int[] getVehicleBearing() {
        int[] bearing = new int[numVehicles];
        for(int vehicleId = 0; vehicleId < numVehicles; ++vehicleId){
            AutonomousVehicle vehicle = vehicles[vehicleId];
            bearing[vehicleId] = getVehicleBearing(vehicle);
        }
        return bearing;
    }

    private int getVehicleBearing(AutonomousVehicle vehicle){
        int state1 = vehicle.getPrevPosition();
        int state2 = vehicle.getCurrentPosition();

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
        return mTau0;
    }

    public int getCurrentPosition4Vehicle(int id) {
        return vehicles[id].getCurrentPosition();
    }
}
