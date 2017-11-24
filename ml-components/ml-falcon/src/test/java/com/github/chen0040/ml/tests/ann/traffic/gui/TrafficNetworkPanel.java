package com.github.chen0040.ml.tests.ann.traffic.gui;

import com.github.chen0040.ml.ann.art.mas.traffic.env.Graph;
import com.github.chen0040.ml.ann.art.mas.traffic.env.TrafficNetwork;
import com.github.chen0040.ml.ann.art.mas.utils.FileUtils;
import com.github.chen0040.ml.ann.art.mas.utils.MazePalette;
import com.github.chen0040.ml.ann.art.mas.utils.Vec2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * Created by chen0469 on 10/2/2015 0002.
 */
public class TrafficNetworkPanel extends JPanel {

    private boolean tracking = false;
    private Graph graph;

    private int numMines;
    private int numVehicles;
    private int numTargets;

    private Vec2D[] mines;
    private Vec2D[] vehicles;
    private Vec2D[] targets;
    private ArrayList<Vec2D>[] paths;
    private int[] vehicleBearing;
    private double[][] pheromones;

    private double minX;
    private double minY;
    private double maxX;
    private double maxY;

    private int selectedVehicleId = -1;

    private double min_pheromone = 1;

    public boolean isTracking(){
        return tracking;
    }

    public void setTracking(boolean tracking){
        this.tracking = tracking;
    }

    /**
     * Bome image.
     *
     * @author J.J.
     */
    private Image bombIcon;
    /**
     * Tank image.
     *
     * @author J.J.
     */
    private Image [] tankIcon;
    /**
     * Target image.
     *
     * @author J.J.
     */
    private Image targetIcon;
    /**
     * popup menu for bgimage seleciton.
     *
     * @author Jin Jun
     */
    private JPopupMenu popup;

    public TrafficNetworkPanel(TrafficNetwork m)
    {
        init_MP(m);
        doRefresh(m, -1);
    }

    public void init_MP( TrafficNetwork m) {
        initComponents();

        minX = m.getMinX();
        maxX = m.getMaxX();
        minY = m.getMinY();
        maxY = m.getMaxY();

        numVehicles = m.getNumVehicles();
        numMines = m.getNumMines();
        numTargets = m.getNumTargets();

        graph = (Graph)m.graph().clone();
        paths = new ArrayList[numVehicles];
        vehicles = new Vec2D[numVehicles];
        mines = new Vec2D[numMines];
        targets = new Vec2D[numTargets];
        vehicleBearing = new int[numVehicles];

        for(int i=0; i < numVehicles; ++i){
            paths[i] = new ArrayList<Vec2D>();
        }

        loadImageIcons();
    }

    private void loadImageIcons() {

        try {
            bombIcon = new ImageIcon(FileUtils.getResourceFile("images/bomb.png").toURL()).getImage();
            tankIcon = new Image[8];
            for( int i = 0; i<8; i++ ) {
                tankIcon[i] = new ImageIcon(FileUtils.getResourceFile("images/tank" + i + ".png").toURL()).getImage();
            }
            targetIcon = new ImageIcon(FileUtils.getResourceFile("images/target.png").toURL()).getImage();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void initComponents(){
        popup = new JPopupMenu();
        JMenuItem bgMenuItem = new JMenuItem("Do something");
        bgMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

            }
        });
        popup.add(bgMenuItem);

        this.addMouseListener(new PopupListener());
    }

    public void doRefresh(TrafficNetwork net, int selectedVehicleId) {
        Vec2D[] vehicles = net.getVehiclePositions();
        Vec2D[] targets = net.getTargetPositions();
        Vec2D[] mines = net.getMinePositions();
        int[] bearing = net.getVehicleBearing();
        graph = (Graph)net.graph().clone();
        pheromones = net.getPheromones();
        min_pheromone = net.getTau0();
        this.selectedVehicleId = selectedVehicleId;
        doRefresh(vehicles, mines, targets, bearing);
    }

    private void doRefresh(Vec2D[] vehicles, Vec2D[] mines, Vec2D[] targets, int[] vehicleBearing) {
        if(mines != null) {
            for (int i = 0; i < numMines; ++i) {
                this.mines[i] = (Vec2D) mines[i].clone();
            }
        }
        if(targets != null) {
            for (int i = 0; i < numTargets; ++i) {
                this.targets[i] = (Vec2D) targets[i].clone();
            }
        }
        if(vehicles != null) {
            for (int i = 0; i < numVehicles; ++i) {
                this.vehicles[i] = (Vec2D) vehicles[i].clone();
            }
        }
        if(vehicleBearing != null) {
            for (int i = 0; i < numVehicles; ++i) {
                this.vehicleBearing[i] = vehicleBearing[i];
            }
        }
        setCurrentPath(vehicles);
        repaint();
    }

    private void setCurrentPath(Vec2D[] pos)
            throws ArrayIndexOutOfBoundsException {
        if(pos == null) return;
        try {
            for(int agt = 0; agt < numVehicles; agt++ ) {
                if(pos != null) {
                    vehicles[agt] = (Vec2D)pos[agt].clone();
                    paths[agt].add(vehicles[agt]);
                }
            }
        } catch ( ArrayIndexOutOfBoundsException e ) {
            System.out.println( "path array index out of bound:" + e.getMessage() );
        }
    }

    protected void paintComponent( Graphics g ) {
        super.paintComponent(g);

        double sizeX = maxX - minX;
        double sizeY = maxY - minY;

        double radiusX = getWidth() / sizeX / 2;
        double radiusY = getHeight() / sizeY / 2;
        int radius = (int) Math.min(radiusX, radiusY) / 2;

        g.setColor (Color.red);
        int numNodes = graph.getNumNodes();
        for(int i = 0; i < numNodes - 1; ++i) {
            Vec2D vi = graph.position(i);
            if(vi != null) {
                int x1 = (int) (getWidth() * (vi.getX() - minX) / sizeX + radiusX);
                int y1 = (int) (getHeight() * (vi.getY() - minY) / sizeY + radiusY);
                g.fillRect(x1 - 1, y1 - 1, 2, 2);

                for(int j=i+1; j < numNodes; ++j) {
                    if(!graph.isConnected(i, j)) continue;
                    Vec2D vj = graph.position(j);
                    if(vj != null) {

                        float pheromone = Math.min(5, (float)(pheromones[i][j] / min_pheromone));

                        int x2 = (int) (getWidth() * (vj.getX() - minX) / sizeX + radiusX);
                        int y2 = (int) (getHeight() * (vj.getY() - minY) / sizeY + radiusY);

                        ((Graphics2D)g).setStroke(new BasicStroke(pheromone));
                        g.drawLine(x1, y1, x2, y2);
                    }
                }
            }
        }

        g.setColor (Color.red);
        for(int i = 0; i < numMines; ++i) {
            Vec2D mine = mines[i];
            if(mine != null) {
                g.drawImage(bombIcon,
                        (int) (getWidth() * (mine.getX() - minX) / sizeX + radiusX - radius),
                        (int) (getHeight() * (mine.getY() - minY) / sizeY + radiusY - radius),
                        radius * 2, radius * 2, this);
            }
        }

        g.setColor(Color.orange);
        for(int i = 0; i < numTargets; ++i) {
            Vec2D target = targets[i];
            if(target != null) {
                g.drawImage(targetIcon,
                        (int) (getWidth() * (target.getX() - minX) / sizeX + radiusX - radius),
                        (int) (getHeight() * (target.getY() - minY) / sizeY + radiusY - radius),
                        radius * 2, radius * 2, this);
            }
        }

        radius *=0.75;
        for(int a = 0; a < numVehicles; a++ )
        {
            Vec2D vehicle = vehicles[a];
            if(vehicle == null) continue;

            Color c = MazePalette.AV_Color[a % MazePalette.AV_Color_Num];
            g.setColor(c);

            if(isTracking() && a == selectedVehicleId) {
                ArrayList<Vec2D> path = paths[a];
                int numStep = path.size() - 1;
                for (int i = 0; i < numStep; i++) {
                    int radius_path = radius * (i + 1) / numStep;

                    int x = (int) (getWidth() * (vehicle.getX() - minX) / sizeX + radiusX);
                    int y = (int) (getHeight() * (vehicle.getY() - minY) / sizeY + radiusY);

                    g.fillRect(x - radius_path / 2, y - radius_path / 2, radius_path, radius_path);
                    g.setColor(c);

                    g.drawLine((int) ((getWidth() / sizeX) * (path.get(i).getX() - minX) + radiusX),
                            (int) ((getHeight() / sizeY) * (path.get(i).getY() - minY) + radiusY),
                            (int) ((getWidth() / sizeX) * (path.get(i + 1).getX() - minX) + radiusX),
                            (int) ((getHeight() / sizeY) * (path.get(i + 1).getY() - minY) + radiusY));
                }
            }

            if( vehicleBearing[a] %2 == 0) {
                g.drawImage(tankIcon[vehicleBearing[a]],
                        (int)(getWidth() * (vehicle.getX() - minX) / sizeX + radiusX - radius),
                        (int)(getHeight() * (vehicle.getY() - minY) / sizeY + radiusY - radius),
                        2*radius, 2*radius, this);
            } else {
                g.drawImage(tankIcon[vehicleBearing[a]],
                        (int)(getWidth() * (vehicle.getX() - minX) / sizeX + radiusX - radius),
                        (int)(getHeight() * (vehicle.getY() - minY) / sizeY + radiusY - radius),
                        (int)(2.828*radius), (int)(2.828*radius), this);
            }


        }
    }

    public void clearPaths() {
        paths=new ArrayList[numVehicles];
        for(int i=0; i < numVehicles; ++i){
            paths[i] = new ArrayList<Vec2D>();
        }
        repaint();
    }

    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                if( popup == null ) return;

                popup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }
}
