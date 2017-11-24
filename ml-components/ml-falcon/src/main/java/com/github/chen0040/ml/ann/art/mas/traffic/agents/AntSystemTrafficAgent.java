package com.github.chen0040.ml.ann.art.mas.traffic.agents;

import com.github.chen0040.ml.ann.art.mas.traffic.env.TrafficNetwork;

import java.util.*;

/**
 * Created by root on 12/30/15.
 */
public class AntSystemTrafficAgent extends TrafficAgent {

    private double q0 = 0.7;

    private static Random random = new Random();

    public AntSystemTrafficAgent(int id, int numSonarInput, int numAVSonarInput, int numRangeInput) {
        super(null, id, numSonarInput, numAVSonarInput, numRangeInput);
    }

    @Override
    public int selectValidAction(TrafficNetwork maze) {


        List<Integer> moves=maze.findAvailableMoves4Vehicle(getId());
        int wayCount = moves.size();

        int feasibleActionMax = Math.min(numAction, wayCount);

        Map<Integer, Double> pheromones = new HashMap<>();

        int current_state = maze.getCurrentPosition4Vehicle(getId());

        double pheromone_sum = 0;
        double max_pheromone = Double.NEGATIVE_INFINITY;
        int action_max = -1;
        double[] acc = new double[feasibleActionMax];
        for(int action = 0; action < feasibleActionMax; ++action) {
            int next_state = moves.get(action);
            double p = maze.getEdgePheromone(current_state, next_state);
            pheromone_sum += p;
            pheromones.put(action, p);
            if(max_pheromone < p){
                max_pheromone = p;
                action_max = action;
            }
            acc[action] = pheromone_sum;
        }


        double q = random.nextDouble();

        int selectedAction = -1;
        if(q <= q0 || pheromone_sum == 0){
            selectedAction = action_max;
        } else {

            for(int action = 0; action < feasibleActionMax; ++action) {
                acc[action] /= pheromone_sum;
            }

            double r = random.nextDouble();
            for(int action = 0; action < feasibleActionMax; ++action) {
                if(r <= acc[action]) {
                    selectedAction = action;
                    break;
                }
            }
        }

        return selectedAction;
    }

    @Override
    public void learn(TrafficNetwork maze) {

    }

    @Override
    public int getNodeCount() {
        return 0;
    }
}
