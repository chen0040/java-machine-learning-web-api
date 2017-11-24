package com.github.chen0040.ml.ann.art.mas.uav;

import com.github.chen0040.ml.ann.art.mas.uav.aco.AntColony;
import com.github.chen0040.ml.ann.art.mas.uav.agents.UavAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen0469 on 10/1/2015 0001.
 */
public class UavSimulatorReport {
    private List<UavSimulatorReportSection> sections = new ArrayList<>();


    private double totalDistance;
    private double totalNodeCovered;
    private int numCode;

    public UavSimulatorConfig getConfig() {
        return config;
    }

    public void setConfig(UavSimulatorConfig config) {
        this.config = config;
    }

    public List<UavSimulatorReportSection> getSections() {
        return sections;
    }

    public void setSections(List<UavSimulatorReportSection> sections) {
        this.sections = sections;
    }

    private UavSimulatorConfig config;


    private int rd = 0;

    public UavSimulatorReport(UavSimulatorConfig config){
        this.config = config;

    }

    public String recordTrial(int trial, int step, UavSimulator sim){

        String message = "";



        int numAgents = config.getNumAgents();
        UavAgent[] agents = sim.agents;
        AntColony maze = sim.colony;
        int nnodes = maze.getNumNodes();


        for (int agt = 0; agt < numAgents; agt++) {
            if(agt == 0) {
                numCode = agents[agt].getNodeCount();
            }
            double distance = maze.getUavDistanceTravelled(agt);

            int nodes = maze.calcNodeCovered(agt);
            totalDistance += distance;
            totalNodeCovered += nodes;
        }
        //minDistance = maze.getBestUavSolution().getDistanceTravelled();

        if (trial % config.getInterval() == 0) {
            int sample = config.getInterval();
            double avgDistance = totalDistance / (sample * numAgents);
            double avgNodeCovered = totalNodeCovered / (sample * numAgents);
            double n_codes = numCode;

            UavSimulatorReportSection section = new UavSimulatorReportSection();
            section.setAvgDistance(avgDistance);
            section.setAvgNodeCovered(avgNodeCovered);
            section.setTrial(trial);
            section.setNumCodes(numCode);
            section.setAvgCost(avgDistance * Math.pow((double)nnodes / (avgNodeCovered+1), 2.0));

            rd++;

            sections.add(section);

            message = section.toString();

            System.out.println(sim.getName() + " >> " + message);

            totalDistance = 0;
            totalNodeCovered = 0;
        }

        return message;
    }
}
