package com.github.chen0040.ml.tests.ann.tsp.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by chen0469 on 10/2/2015 0002.
 */
public class SonarPanel extends JPanel {
    private double[] sonar;
    private double range;
    private Color fore_color;

    public SonarPanel(Color c) {
        fore_color = c;
    }

    public void readSonar(double[] sonar) {
        this.sonar = sonar;
        repaint();
    }


    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if(sonar == null) return;

        int numSonar = sonar.length;

        g.setColor (Color.black);
        for (int i=0; i < numSonar; i++)
            g.drawRect((getWidth()/numSonar)*i, getHeight()/2-getWidth()/numSonar/2, getWidth()/numSonar, getWidth()/numSonar);

        g.setColor( fore_color );
        int r = Math.min( getHeight() / 2, getWidth() / ( 2 * numSonar ) );
        for( int i=0; i<numSonar; i++ )
        {
            int radius = ( int )( sonar[i] * r );
            g.fillOval((getWidth()/numSonar)*i+getWidth()/(2*numSonar)-radius, getHeight()/2-radius, radius*2, radius*2);
        }
    }
}
