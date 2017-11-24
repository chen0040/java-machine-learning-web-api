package com.github.chen0040.ml.ann.art.mas;

import com.github.chen0040.ml.ann.art.mas.flocking.FlockingSimulator;
import com.github.chen0040.ml.ann.art.mas.minefield.MineFieldSimulator;
import com.github.chen0040.ml.ann.art.mas.traffic.TrafficSimulator;
import com.github.chen0040.ml.ann.art.mas.uav.UavSimulator;
import org.apache.commons.cli.*;

/**
 * Created by chen0469 on 1/2/2016 0002.
 */
public class MainSimulator {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("s", "size", true, "specify the mine field size");
        options.addOption("c", "numAgents", true, "specify the number of agents");
        options.addOption("t", "targetMove", true, "specify whether the target is moving");
        options.addOption("m", "falconMode", true, "specify whether the falcon mode is Q-Learn or SARSA");
        options.addOption("f", "mode", true, "specify what mode to run");
        options.addOption("b", "bounded", true, "specify whether TD-FALCON Q is bounded");
        options.addOption("h", "head", true, "specify the starting index of simulation");
        options.addOption("g", "num", true, "specify the number of simulations to run");
        options.addOption("n", "name", true, "specify the folder name of the simulation results");
        options.addOption("j", "jMainClass", true, "specify the main class to run");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            String j = line.getOptionValue("j");
            if(j != null){
                if(j.equals("ATD")) {
                    TrafficSimulator.main(args);
                } else if (j.equals("TD")) {
                    MineFieldSimulator.main(args);
                } else if(j.equals("URAV")) {
                    UavSimulator.main(args);
                }
                else {
                    FlockingSimulator.main(args);
                }
            } else {
                FlockingSimulator.main(args);
            }
        } catch(ParseException pe) {
            pe.printStackTrace();
        }
    }
}
