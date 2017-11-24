package com.github.chen0040.ml.ann.art.mas.flocking.flocks;

import com.github.chen0040.ml.ann.art.mas.utils.Vec2I;

/**
 * Created by chen0469 on 10/4/2015 0004.
 */
public class GameTarget {
    private Vec2I center = new Vec2I();
    private int radius = 1;

    public Vec2I getCenter(){
        return center;
    }

    public boolean withinTarget(Vec2I pos){
        int x = pos.getX();
        int y = pos.getY();

        return withinTarget(x, y);
    }

    public boolean withinTarget(int x, int y){
        int x1 = center.getX() - radius;
        int x2 = center.getX() + radius;
        int y1 = center.getY() - radius;
        int y2 = center.getY() + radius;
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }

    public void decX() {
        center.decX();
    }

    public void decY(){
        center.decY();
    }

    public void incX(){
        center.incX();
    }

    public void incY(){
        center.incY();
    }

    public void setX(int x){
        center.setX(x);
    }

    public void setY(int y){
        center.setY(y);
    }
}
