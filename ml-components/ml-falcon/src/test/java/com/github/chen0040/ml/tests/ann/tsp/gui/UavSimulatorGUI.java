package com.github.chen0040.ml.tests.ann.tsp.gui;

import com.github.chen0040.ml.ann.art.falcon.FalconConfig;
import com.github.chen0040.ml.ann.art.mas.uav.*;
import com.github.chen0040.ml.ann.art.mas.uav.aco.AntColony;
import com.github.chen0040.ml.ann.art.mas.utils.FileUtils;
import com.github.chen0040.ml.ann.art.mas.uav.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Created by chen0469 on 10/2/2015 0002.
 */
public class UavSimulatorGUI extends JFrame {
    private UavSimulator simulator;

    private SonarPanel p_avsonar;
    private TitledBorder av_sonar_title;

    private UavGraphPanel p_field;

    private JLabel label1;
    private JLabel label2;

    private JComboBox ddAgentSelection;

    private int selectedAgentId = 0;

    public UavSimulatorGUI(UavSimulatorConfig config, FalconConfig falconConfig){
        simulator = new UavSimulatorQ(config, falconConfig);
        initialize(simulator.getColony());
    }

    public void initialize (AntColony maze)
    {
        Container container = getContentPane();
        container.removeAll();

        container.setLayout( new GridLayout ( 1, 2, 0, 0 ) );

        p_avsonar = new SonarPanel(Color.yellow);
        JPanel p_avsonarPane = new JPanel();
        p_avsonarPane.setLayout(new BorderLayout());
        av_sonar_title = new TitledBorder("Pheromone Sonar Signal");
        p_avsonarPane.setBorder(av_sonar_title);
        p_avsonarPane.add(p_avsonar, BorderLayout.CENTER);

        JPanel p_sense = new JPanel();
        p_sense.setLayout(new GridLayout(3 * 1, 1, 0, 0));

        p_sense.add(p_avsonarPane);

        p_field = new UavGraphPanel(maze);

        JPanel p_fieldmsg = new JPanel();
        p_fieldmsg.setLayout(new BorderLayout());
        p_fieldmsg.setBorder(new TitledBorder("Minefield (View from the Top)"));
        p_fieldmsg.add(p_field, BorderLayout.CENTER);

        label1 = new JLabel("");
        label2 = new JLabel("");

        label1.setIcon(getIcon("images/right.gif"));
        label2.setIcon(getIcon("images/right.gif"));

        JPanel p_left = new JPanel();
        p_left.setLayout(new BorderLayout());
        p_left.add(p_fieldmsg, BorderLayout.CENTER);
        p_left.add(label1, BorderLayout.SOUTH);

        JPanel p_control = new JPanel();
        p_control.setLayout(new BorderLayout());
        p_control.add(createDropdown_AgentSelection(), BorderLayout.NORTH);
        p_control.add(p_sense, BorderLayout.CENTER);
        p_control.add(label2,BorderLayout.SOUTH);


        container.add(p_left);
        container.add(p_control);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        createMenuBar();

        setSize(900, 450);
        setTitle("Minefield Navigation Simulator");
        setVisible(true);
    }

    private JPanel createDropdown_AgentSelection(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JLabel label = new JLabel("Selected Agent:");
        label.setIcon(getIcon("images/right.gif"));
        panel.add(label);

        ddAgentSelection = new JComboBox();
        for(int agentId = 0; agentId < simulator.getNumAgents(); ++agentId) {
            ddAgentSelection.addItem(agentId);
        }
        ddAgentSelection.setSelectedItem(selectedAgentId);
        panel.add(ddAgentSelection);
        ddAgentSelection.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                UavSimulatorGUI.this.selectedAgentId = (Integer)e.getItem();
                notifyAgentSelectionChanged();
            }
        });

        return panel;
    }

    private void createMenuBar(){
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");
        menuBar.add(menuFile);

        JMenuItem miStartSimulation = new JMenuItem("Start Simulation");
        menuFile.add(miStartSimulation);
        miStartSimulation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runSims();
            }
        });

        JMenuItem miStartSimulationSilent = new JMenuItem("Start Simulation (No GUI)");
        menuFile.add(miStartSimulationSilent);
        miStartSimulationSilent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runSilentSims();
            }
        });

        JMenuItem miStopSimulation = new JMenuItem("Stop Simulation");
        menuFile.add(miStopSimulation);
        miStopSimulation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                simulator.stop();
            }
        });

        JMenu menuView = new JMenu("View");
        menuBar.add(menuView);

        JCheckBoxMenuItem miShowTrack = new JCheckBoxMenuItem("Show Track");
        menuView.add(miShowTrack);
        miShowTrack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem checkButton = (JCheckBoxMenuItem) e.getSource();
                p_field.setTracking(checkButton.isSelected());
            }
        });

        JMenu menuEdit = new JMenu("Edit");
        menuBar.add(menuEdit);

        JMenu menuAgent = new JMenu("Agent");
        menuBar.add(menuAgent);

        JMenu menuAI = createMenu_FALCON();
        menuAgent.add(menuAI);

        JMenu menuFalconConfig = createMenu_FalconConfig();
        menuAgent.add(menuFalconConfig);

        JMenu menuSim = new JMenu("Simulation");
        menuBar.add(menuSim);

        JMenu menuSimInterval = createMenu_SimulationInterval();
        menuSim.add(menuSimInterval);

        JMenu menuSimAgentNum = createMenu_SimulationAgentNum();
        menuEdit.add(menuSimAgentNum);

        JMenu menuMaxTrial = createMenu_MaxTrials();
        menuSim.add(menuMaxTrial);

        JMenu menuRuns = createMenu_Runs();
        menuSim.add(menuRuns);

        this.setJMenuBar(menuBar);

    }


    private JMenu createMenu_Runs(){
        JMenu menu = new JMenu("Runs");
        ButtonGroup buttonGroup = new ButtonGroup();

        int[] options = new int[] { 1, 5, 10, 30 };
        for(Integer runs : options){
            JMenuItem mi1 = createMenuItem_Runs(runs);
            buttonGroup.add(mi1);
            menu.add(mi1);
        }

        return menu;
    }

    private JMenuItem createMenuItem_Runs(final int runs){
        JRadioButtonMenuItem mi1 = new JRadioButtonMenuItem(""+runs);
        mi1.setSelected(simulator.getConfig().getNumRuns() == runs);

        mi1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) e.getSource();
                if (radioButton.isSelected()) {
                    simulator.getConfig().setNumRuns(runs);
                }
            }
        });
        return mi1;
    }

    private JMenu createMenu_FalconConfig(){
        JMenu menu = new JMenu("Learn");

        JCheckBoxMenuItem mi2 = new JCheckBoxMenuItem("Bounded Q");
        mi2.setSelected(simulator.getFalconConfig().isBounded);
        mi2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JCheckBoxMenuItem checkButton = (JCheckBoxMenuItem) e.getSource();
                simulator.getFalconConfig().isBounded = (checkButton.isSelected());
            }
        });
        menu.add(mi2);

        return menu;
    }

    private JMenu createMenu_FALCON(){
        JMenu menu = new JMenu("FALCON");
        ButtonGroup buttonGroup = new ButtonGroup();

        JRadioButtonMenuItem mi1 = new JRadioButtonMenuItem("R-FALCON");
        mi1.setSelected(simulator instanceof UavSimulatorR);
        buttonGroup.add(mi1);
        menu.add(mi1);
        mi1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) e.getSource();
                if (radioButton.isSelected()) {
                    simulator = new UavSimulatorR(simulator.getConfig(), simulator.getFalconConfig());
                }
            }
        });

        JRadioButtonMenuItem mi2 = new JRadioButtonMenuItem("Q-FALCON");
        mi2.setSelected(simulator instanceof UavSimulatorQ);
        buttonGroup.add(mi2);
        menu.add(mi2);
        mi2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) e.getSource();
                if (radioButton.isSelected()) {
                    simulator = new UavSimulatorQ(simulator.getConfig(), simulator.getFalconConfig());
                }
            }
        });

        JRadioButtonMenuItem mi3 = new JRadioButtonMenuItem("Q-FALCON(lambda)");
        mi3.setSelected(simulator instanceof UavSimulatorQLambda);
        menu.add(mi3);
        buttonGroup.add(mi3);
        mi3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) e.getSource();
                if (radioButton.isSelected()) {
                    simulator = new UavSimulatorQLambda(simulator.getConfig(), simulator.getFalconConfig());
                }
            }
        });

        JRadioButtonMenuItem mi4 = new JRadioButtonMenuItem("SARSA-FALCON");
        mi4.setSelected(simulator instanceof UavSimulatorSarsa);
        menu.add(mi4);
        buttonGroup.add(mi4);
        mi4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) e.getSource();
                if (radioButton.isSelected()) {
                    simulator = new UavSimulatorSarsa(simulator.getConfig(), simulator.getFalconConfig());
                }
            }
        });

        JRadioButtonMenuItem mi5 = new JRadioButtonMenuItem("SARSA-FALCON(lambda)");
        mi5.setSelected(simulator instanceof UavSimulatorSarsaLambda);
        menu.add(mi5);
        buttonGroup.add(mi5);
        mi5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) e.getSource();
                if (radioButton.isSelected()) {
                    simulator = new UavSimulatorSarsaLambda(simulator.getConfig(), simulator.getFalconConfig());
                }
            }
        });

        return menu;
    }


    private JMenu createMenu_MaxTrials(){
        JMenu menu = new JMenu("Max Trials");
        ButtonGroup buttonGroup = new ButtonGroup();

        int[] options = new int[] { 1000, 2000, 3000, 4000 };
        for(Integer maxTrials : options){
            JMenuItem mi1 = createMenuItem_MaxTrials(maxTrials);
            buttonGroup.add(mi1);
            menu.add(mi1);
        }

        return menu;
    }

    private JMenuItem createMenuItem_MaxTrials(final int maxTrials){
        JRadioButtonMenuItem mi1 = new JRadioButtonMenuItem(""+maxTrials);
        mi1.setSelected(simulator.getConfig().getMaxTrial() == maxTrials);

        mi1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) e.getSource();
                if (radioButton.isSelected()) {
                    simulator.getConfig().setMaxTrial(maxTrials);
                }
            }
        });

        return mi1;
    }

    SwingWorker worker = null;
    private void runSilentSims(){
        if(worker != null && !worker.isDone()){
            JOptionPane dlg = new JOptionPane(JOptionPane.WARNING_MESSAGE);
            dlg.setMessage("Worker is running, please wait or cancel");
            dlg.setVisible(true);
            return;
        }

         worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                simulator.runSims();
                return null;
            }

             @Override
             protected void done() {
                 super.done();
                 JOptionPane dlg =new JOptionPane(JOptionPane.INFORMATION_MESSAGE);
                 dlg.setMessage("Done!");
                 dlg.setVisible(true);
             }
         };
        worker.execute();
    }

    private HashMap<String, ImageIcon> icons = new HashMap<String, ImageIcon>();

    private ImageIcon getIcon(String filename){
        if(icons.containsKey(filename)){
            return icons.get(filename);
        } else {
            URL url = null;
            try {
                url = FileUtils.getResourceFile(filename).toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            ImageIcon icon = new ImageIcon(url);
            icons.put(filename, icon);
            return icon;
        }
    }

    private JMenu createMenu_SimulationAgentNum(){
        JMenu menu = new JMenu("Vehicles");
        ButtonGroup buttonGroup = new ButtonGroup();

        int[] options = new int[] { 1, 2, 5, 10, 20, 30, 40};

        for(Integer numAgents : options){
            JMenuItem mi1 = createMenuItem_SimulationAgentNum(numAgents);
            buttonGroup.add(mi1);
            menu.add(mi1);
        }

        return menu;
    }

    private JMenuItem createMenuItem_SimulationAgentNum(final int agentNum){
        JRadioButtonMenuItem mi1 = new JRadioButtonMenuItem(""+agentNum);
        mi1.setSelected(simulator.getConfig().getNumAgents()==agentNum);

        mi1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) e.getSource();
                if (radioButton.isSelected()) {
                    simulator.getConfig().setNumAgents(agentNum);
                    notifyNumAgentChanged();
                }
            }
        });

        return mi1;
    }

    private JMenu createMenu_SimulationInterval(){
        JMenu menuInterval = new JMenu("Interval");
        ButtonGroup buttonGroup = new ButtonGroup();

        int[] options = new int[] { 10, 20, 50, 100, 200, 300, 400, 500, 600, 700 };

        for(Integer interval : options){
            JMenuItem mi1 = createMenuItem_SimulationInterval(interval);
            buttonGroup.add(mi1);
            menuInterval.add(mi1);
        }

        return menuInterval;
    }

    private JMenuItem createMenuItem_SimulationInterval(final int interval){
        JRadioButtonMenuItem mi1 = new JRadioButtonMenuItem(""+interval);
        mi1.setSelected(simulator.getConfig().getUiInterval() == interval);

        mi1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButtonMenuItem radioButton = (JRadioButtonMenuItem) e.getSource();
                if (radioButton.isSelected()) {
                    simulator.getConfig().setUiInterval(interval);
                }
            }
        });

        return mi1;
    }

    private void notifyMineFieldChanged(){
        UavSimulatorConfig config = simulator.getConfig();
        AntColony mineField = simulator.getColony();
        mineField.refreshMaze(config.getNumAgents(), config.numStates, config.numActions);
        p_field.init_MP(mineField);
        p_field.repaint();
    }

    private void notifyNumAgentChanged(){
        UavSimulatorConfig config = simulator.getConfig();
        AntColony mineField = simulator.getColony();

        ddAgentSelection.removeAllItems();

        for(int agentId = 0; agentId < config.getNumAgents(); ++agentId){
            ddAgentSelection.addItem(agentId);
        }

        mineField.refreshMaze(config.getNumAgents(), config.numStates, config.numActions);
        p_field.init_MP(mineField);
        p_field.repaint();
    }

    private void notifyAgentSelectionChanged(){
        AntColony mineField = simulator.getColony();
        p_avsonar.readSonar(mineField.getState(selectedAgentId));
    }

    public void runSims(){
        Thread thread = new Thread(new Runnable() {
            public void run() {
                simulator.runSims(new Consumer<UavSimulatorProgress>(){
                    public void accept(UavSimulatorProgress progress) {

                        if(progress.isInitializing()){
                            p_field.clearPaths();
                        }
                        else {
                            final AntColony mineField = progress.getMineField();
                            int step = progress.getStep();
                            p_field.doRefresh(mineField, selectedAgentId);


                            String message = simulator.getMessage();
                            if (message != null && !message.equals("")) {
                                label2.setText(message);
                            }

                            p_avsonar.readSonar(mineField.getState(selectedAgentId));

                            label1.setText("Run: " + progress.getRun() + " Trial: " + progress.getTrial() + " Step: " + step);
                        }
                    }
                });
            }
        });

        thread.start();
    }

    public static void main(String[] args){
        // below is settings from RFalcon
        UavSimulatorConfig config = new UavSimulatorConfig();
        config.loadUav_a280();

        FalconConfig falconConfig = new FalconConfig();
        falconConfig.numAction = config.numActions;
        falconConfig.numState = config.numStates;
        falconConfig.numReward = 2;
        falconConfig.isBounded = false;

        UavSimulatorGUI gui = new UavSimulatorGUI(config, falconConfig);
    }
}
