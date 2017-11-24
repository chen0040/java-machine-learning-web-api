package com.github.chen0040.ml.tests.ann.tsp.gui;

import com.github.chen0040.ml.ann.art.mas.utils.FileUtils;
import com.github.chen0040.ml.ann.art.mas.utils.MazePalette;
import com.github.chen0040.ml.ann.art.mas.uav.aco.AntColony;
import com.github.chen0040.ml.ann.art.mas.uav.aco.SparseGraph;
import com.github.chen0040.ml.ann.art.mas.uav.aco.UavPath;
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
public class UavGraphPanel extends JPanel {

    private boolean tracking = false;
    private SparseGraph graph;

    private int numVehicles;

    private Vec2D[] vehicles;
    private ArrayList<Vec2D>[] paths;
    private UavPath bestPath;
    private int[] vehicleBearing;
    private double[][] pheromones;

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

    public UavGraphPanel(AntColony m)
    {
        init_MP(m);
        doRefresh(m, -1);
    }

    public void init_MP( AntColony m) {
        initComponents();

        numVehicles = m.getNumAnts();

        graph = (SparseGraph)m.graph().clone();
        paths = new ArrayList[numVehicles];
        vehicles = new Vec2D[numVehicles];
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

    public void doRefresh(AntColony net, int selectedVehicleId) {
        Vec2D[] vehicles = net.getAntPositions();
        int[] bearing = net.getAntBearing();
        graph = (SparseGraph)net.graph().clone();

        pheromones = net.getPheromones();
        min_pheromone = net.getTau0();
        bestPath = net.getBestUavSolution();
        this.selectedVehicleId = selectedVehicleId;
        doRefresh(vehicles, bearing);
    }

    private void doRefresh(Vec2D[] vehicles, int[] vehicleBearing) {
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

        double maxX = graph.getMaxX();
        double minX = graph.getMinX();
        double maxY = graph.getMaxY();
        double minY = graph.getMinY();

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

            }
        }

        if(bestPath != null){
            for(int i=0; i < bestPath.size(); ++i){
                int j = (i+1) % bestPath.size();
                int vi = bestPath.get(i);
                int vj = bestPath.get(j);

                Vec2D pos_vi = graph.position(vi);
                Vec2D pos_vj = graph.position(vj);

                g.drawLine((int) ((getWidth() / sizeX) * (pos_vi.getX() - minX) + radiusX),
                        (int) ((getHeight() / sizeY) * (pos_vi.getY() - minY) + radiusY),
                        (int) ((getWidth() / sizeX) * (pos_vj.getX() - minX) + radiusX),
                        (int) ((getHeight() / sizeY) * (pos_vj.getY() - minY) + radiusY));
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
