package com.github.chen0040.ml.ann.art.mas.flocking.flocks;

import com.github.chen0040.ml.ann.art.mas.utils.Vec2I;

import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by chen0469 on 9/30/2015 0030.
 */
public class GameWorld {
    private  int size=16;
    public final boolean binarySonar = false;

    //public RewardType rewardType = RewardType.Linear;
    private int numBoids;

    public GameTarget target;

    private int[][] boid_signals;
    private int[][] obstacle;

    private Boid[] boids;
    private int[] minSteps;

    public int getSize(){
        return size;
    }

    public GameWorld(int size, int numMines, int numAgents) {
        refreshMaze(size, numMines, numAgents);
    }

    private int[][] createSquare(int size, int value) {
        int[][] tiles = new int[size][];
        for (int i = 0; i < size; ++i) {
            tiles[i] = new int[size];
            for(int j=0; j < size; ++j) {
                tiles[i][j] = value;
            }
        }

        return tiles;
    }

    public boolean hasAgent(int x, int y){
        return boid_signals[x][y] >= 0;
    }

    public boolean hasMine(int x, int y){
        return obstacle[x][y] == 1;
    }

    public void updateVehicleState(Boid boid){
        Vec2I prevPosition = boid.getPrevPosition();

        boid_signals[prevPosition.getX()][prevPosition.getY()] = -1;

        if(isHitObstacle(boid)) {
            boid.notifyHitMine();
        } else if(isHitTarget(boid)) {
            boid.notifyHitTarget();
        } else if(boid_signals[boid.getX()][boid.getY()] != -1) {
            boid.notifyConflicting();
        } else {
            boid_signals[boid.getX()][boid.getY()] = boid.getId();
        }
    }

    public boolean isOutOfField(Vec2I pos){
        return isOutOfField(pos.getX(), pos.getY());
    }

    public boolean isOutOfField(int x, int y){
        return x < 0 || y < 0;
    }

    public double getReward(int boidId, boolean immediate){
        return getReward(boids[boidId].getCurrentPosition(), boids[boidId].getCurrentBearing(), immediate);
    }

    private static Random random = new Random();

    public Vec2I getBoidCentroid(Vec2I pos, int k){
        if(k < 1) k = 1;
        SortedMap<Double, Vec2I> knn = new TreeMap<>();
        for(int boidId = 0; boidId < numBoids; ++boidId){
            Vec2I pos2 = boids[boidId].getCurrentPosition();
            int distance = pos2.getRange(pos) ;
            if(distance == 0) continue;
            knn.put(distance + 0.0000001 * random.nextDouble(), pos2);
            if(knn.size() > k){
                knn.remove(knn.lastKey());
            }
        }

        int x = 0;
        int y = 0;
        for(Vec2I pos2 : knn.values()){
            x += pos2.getX();
            y += pos2.getY();
        }
        x /= k;
        y /= k;

        return new Vec2I(x, y);
    }

    public double getBoidCentroidBearing(Vec2I pos, int k){
        if(k < 1) k = 1;
        SortedMap<Double, Integer> knn = new TreeMap<>();
        for(int boidId = 0; boidId < numBoids; ++boidId){
            Vec2I pos2 = boids[boidId].getCurrentPosition();
            int distance = pos2.getRange(pos);
            if(distance == 0) continue;
            knn.put(distance+ 0.0000001 * random.nextDouble(), boidId);
            if(knn.size() > k){
                knn.remove(knn.lastKey());
            }
        }

        double sum = 0;
        for(Integer boidId : knn.values()){
            int bearing = getCurrentBearing(boidId);
            sum += bearing;
        }

        return sum / k;
    }

    public int getAverageDistance(Map<Integer, Integer> knn){
        int sum = 0;
        int k = knn.size();
        for(Map.Entry<Integer, Integer> entry : knn.entrySet()){
            int distance = entry.getKey();
            int boidId = entry.getValue();
            sum += distance;
        }
        return sum / k;
    }

    public boolean flocking = true;

    public Map<Integer, Integer> getNearestBoids(Vec2I pos, int k){
        if(k < 1) k = 1;


        SortedMap<Double, Integer> knn2 = new TreeMap<>();
        for(int boidId = 0; boidId < numBoids; ++boidId){
            Vec2I pos2 = boids[boidId].getCurrentPosition();
            int distance = pos2.getRange(pos);
            if(distance == 0) continue;
            knn2.put(distance+ 0.0000001 * random.nextDouble(), boidId);
            if(knn2.size() > k){
                knn2.remove(knn2.lastKey());
            }
        }

        SortedMap<Integer, Integer> knn = new TreeMap<>();
        for(Map.Entry<Double, Integer> entry : knn2.entrySet()){
            knn.put(entry.getKey().intValue(), entry.getValue());
        }

        return knn;
    }

    public double getReward(Vec2I pos, int currentBearing, boolean immediate)
    {
        if(target.withinTarget(pos) ) {
            return 1;
        }

        if(isOutOfField(pos)) return -1;
        if(hasMine(pos.getX(), pos.getY()))  return 0;


        if( immediate) {
            if(flocking) {
                int r1 = pos.getRange(target.getCenter()); // distance to target
                int r2 = pos.getRange(getBoidCentroid(pos, 3)); // distance to centroid
                int r3 = getAverageDistance(getNearestBoids(pos, 1)); // distance to nearest neighbor
                double r4 = getFearFactor(pos, currentBearing);

                int centroid_bearing = (int)(Math.floor(getBoidCentroidBearing(pos, 2)));

                double bearing_deviation = (8 + centroid_bearing - currentBearing) % 8;

                //double r = r1 + (double) r2 / (1 + r3);
                double f = 1 / (1 + Math.max(r2 - r3 + r4 + bearing_deviation, 0) + r1);
                return f; //adjust intermediate reward
            } else {
                int r = pos.getRange(target.getCenter());
                return 1.0 / (1 + r); //adjust intermediate reward
            }
            /*
            if (rewardType == RewardType.Linear) {
                if(flocking) {
                    int r1 = pos.getRange(target.getCenter());
                    int r2 = pos.getRange(getBoidCentroid(pos, (int) (Math.log(numBoids) * 2)));
                    int r3 = getAverageDistance(getNearestBoids(pos, (int) (Math.log(numBoids))));
                    double r = r1 + (double) r2 / (1 + r3);
                    if (r > 10) r = 10;
                    return 1.0 - r / 10.0; //adjust intermediate reward
                } else {
                    int r = pos.getRange(target.getCenter());
                    if (r > 20) r = 20;
                    return 1.0 - r / 20.0; //adjust intermediate reward
                }
            } else {
                if(flocking) {
                    int r1 = pos.getRange(target.getCenter());
                    int r2 = pos.getRange(getBoidCentroid(pos, (int) (Math.log(numBoids) * 2)));
                    int r3 = getAverageDistance(getNearestBoids(pos, (int) (Math.log(numBoids))));
                    double r = r1 + (double) r2 / (1 + r3);
                    return 1.0 / (1 + r); //adjust intermediate reward
                } else {
                    int r = pos.getRange(target.getCenter());
                    return 1.0 / (1 + r); //adjust intermediate reward
                }
            }
*/


        }


        return 0.0; //no intermediate reward
    }

    public double getReward(Boid boid, int d, boolean immediate)
    {
        Vec2I next_pos = boid.virtual_move(this, d);
        int bearing = ( boid.getCurrentBearing() + d + 8 ) % 8;
        double r = getReward(next_pos, bearing, immediate);
        return r;
    }

    public double getReward(int boidId, int d, boolean immediate){
        return getReward(boids[boidId], d, immediate);
    }

    public void moveTarget()
    {
        Vec2I new_pos;
        int b;
        do {
            b = ( int )( Math.random() * size );
            new_pos = targetTestMove(b);
        } while( !isTargetPositionValid(new_pos) );
        _moveTarget(b);
        return;
    }

    public boolean isTargetPositionValid(Vec2I pos)
    {
        int x = pos.getX();
        int y = pos.getY();

        if( ( x < 0 ) || ( x >= size ) )
            return( false );
        if( ( y < 0 ) || ( y >= size ) )
            return( false );
        if( hasAgent(x, y) )
            return( false );
        return !hasMine(x, y);
    }

    public Vec2I targetTestMove(int d)
    {
        int[] new_pos = new int[2];

        Vec2I targetCenter = target.getCenter();

        new_pos[0] = targetCenter.getX();
        new_pos[1] = targetCenter.getY();
        switch( d )
        {
            case 0:
                new_pos[1]--;
                break;
            case 1:
                new_pos[0]++;
                new_pos[1]--;
                break;
            case 2:
                new_pos[0]++;
                break;
            case 3:
                new_pos[0]++;
                new_pos[1]++;
                break;
            case 4:
                new_pos[1]++;
                break;
            case 5:
                new_pos[0]--;
                new_pos[1]++;
                break;
            case 6:
                new_pos[0]--;
                break;
            case 7:
                new_pos[0]--;
                new_pos[1]--;
                break;
            default:
                break;
        }
        return new Vec2I(new_pos[0], new_pos[1]);
    }

    private void _moveTarget(int d)
    {
        switch( d )
        {
            case 0:
                target.decY();
                break;
            case 1:
                target.incX();
                target.decY();
                break;
            case 2:
                target.incX();
                break;
            case 3:
                target.incX();
                target.incY();
                break;
            case 4:
                target.incY();
                break;
            case 5:
                target.decX();
                target.incY();
                break;
            case 6:
                target.decX();
                break;
            case 7:
                target.decX();
                target.decY();
                break;
            default:
                break;
        }
    }

    public double getFearFactor(Vec2I pos, int currentBearing){
        double[] sonars = getSonar(pos.getX(), pos.getY(), currentBearing);
        double sum =0;
        for(int i=0; i < sonars.length; ++i) {
            sum += sonars[i];
        }
        return sum;
    }

    public double[] getSonar(int boidId){

        Boid boid = boids[boidId];
        int x = boid.getX();
        int y = boid.getY();

        return getSonar(x, y, boid.getCurrentBearing());
    }

    public double[] getSonar(int x, int y, int currentBearing) {

        double[] new_sonar = new double[5];

        if(isOutOfField(x, y)) {
            for (int k=0; k < 5; k++) new_sonar[k] = 0;
            return new_sonar;
        }

        double[] aSonar = new double[8];

        int r = 0;
        while( y-r >= 0 && !hasMine(x, y-r)) r++;
        if (r == 0) aSonar[0] = 0.0;
        else aSonar[0] = 1.0 / (double)r;

        r=0;
        while(x+r <= getSize()-1 && y-r >= 0 && !hasMine(x+r, y-r)) r++;
        if (r==0) aSonar[1] = 0.0;
        else aSonar[1] = 1.0 / (double)r;

        r=0;
        while (x+r <= getSize()-1 && !hasMine(x+r, y)) r++;
        if (r==0) aSonar[2] = 0.0;
        else aSonar[2] = 1.0 / (double)r;

        r=0;
        while (x+r <= getSize()-1 && y+r <= getSize()-1 && !hasMine(x+r, y+r)) r++;
        if (r==0) aSonar[3] = 0.0;
        else aSonar[3] = 1.0 / (double)r;

        r=0;
        while (y+r <= getSize()-1 && !hasMine(x, y+r)) r++;
        if (r==0) aSonar[4] = 0.0;
        else aSonar[4] = 1.0 / (double)r;

        r=0;
        while (x-r >= 0 && y+r <= getSize()-1 && !hasMine(x-r, y+r)) r++;
        if (r==0) aSonar[5] = 0.0;
        else aSonar[5] = 1.0 / (double)r;

        r=0;
        while (x-r>=0 && !hasMine(x-r, y)) r++;
        if (r==0) aSonar[6] = 0.0;
        else aSonar[6] = 1.0 / (double)r;

        r=0;
        while (x-r >= 0 && y-r >= 0 && !hasMine(x-r, y-r)) r++;
        if (r==0) aSonar[7] = 0.0;
        else aSonar[7] = 1.0 / (double)r;

        for (int k=0; k < 5; k++) {
            new_sonar[k] = aSonar[(currentBearing + 6 + k) % 8];
            if (binarySonar)
                if (new_sonar[k] < 1)
                    new_sonar[k]=0; // binary sonar signal
        }

        return new_sonar;
    }

    public double[] getAVSonar(int boidId){
        return boids[boidId].getAVSonar(this);
    }

    public void refreshMaze(int size, int numMines, int numAgents) {
        this.size = size;
        this.numBoids = numAgents;

        target = new GameTarget();
        boid_signals = createSquare(size, -1);
        obstacle = createSquare(size, 0);

        boids = new Boid[numBoids];
        minSteps = new int[numBoids];
        for(int k = 0; k < numBoids; k++ ) {
            boids[k] = new Boid(k);
        }

        int x;
        int y;
        int rowSize = (int)Math.ceil(Math.sqrt(numBoids));
        for(int row=0; row < rowSize; ++row) {
            for (int col = 0; col < rowSize; col++) {
                int k = row*rowSize + col;
                if(k >= numBoids) break;

                x = col * 2;
                boids[k].setX(x);
                y = row * 2;
                boids[k].setY(y);

                boid_signals[x][y] = boids[k].getId();

                boids[k].activate();
            }
        }

        do {
            x = (int) (Math.random()*size);
            target.setX(x);
            y = (int) (Math.random()*size);
            target.setY(y);
        } while (hasAgent(x, y));

        for( int i = 0; i < numMines; i++ ) {
            do {
                x = ( int )( Math.random() * size );
                y = ( int )( Math.random() * size );
            } while( hasAgent(x, y) || hasMine(x, y) || target.withinTarget(x, y) );
            obstacle[x][y] = 1;
        }

        for( int k = 0; k < numBoids; k++ ) {
            boids[k].initBearing(target.getCenter());
            minSteps[k] = boids[k].getCurrentPosition().getRange(target.getCenter());
        }
    }

    public int getMinStep(int boidId){
        return minSteps[boidId];
    }


    /*
    public boolean checkConflict(int boidId, Vec2I pos){
        if( target.withinTarget(pos) )
            return false;
        if( isOutOfField(pos))
            return false;

        int boid_signal = boid_signals[pos.getX()][pos.getY()];
        return boid_signal != -1 && boid_signal != boidId;
    }

    public boolean checkConflict(int boidId) {
        Boid boid = boids[boidId];
        return checkConflict(boidId, boid.getCurrentPosition());
    }*/

    public boolean isActive(int boidId){
        return boids[boidId].isActive();
    }

    public Boid getBoid(int x, int y){
        int boidId = boid_signals[x][y];
        if(boidId == -1) return null;
        return boids[boidId];
    }

    public int getTargetBearing(int boidId) {
        return boids[boidId].getTargetBearing(target.getCenter());
    }

    public int getCurrentBearing(int boidId) {
        return boids[boidId].getCurrentBearing();
    }

    public double getTargetRange( int i ) {
        return 1.0 / (1 + boids[i].getRange(target.getCenter()));
    }

    // return true if the move still keeps the boid within the field
    public boolean withinField(int boidId, int d) {
        Boid boid = boids[boidId];
        int testBearing;

        testBearing = (boid.getCurrentBearing() + d + 8 ) % 8;
        switch (testBearing) {
            case 0:
                if (boid.getY() > 0)
                    return (true);
                break;
            case 1:
                if (boid.getX() < size-1 && boid.getY() > 0)
                    return( true );
                break;
            case 2:
                if (boid.getX() < size-1) return (true);
                break;
            case 3:
                if (boid.getX() < size-1 && boid.getY() < size-1)
                    return( true );
                break;
            case 4:
                if (boid.getY() < size-1)
                    return( true );
                break;
            case 5:
                if (boid.getX() > 0 && boid.getY() < size-1)
                    return (true);
                break;
            case 6:
                if (boid.getX() > 0)
                    return( true );
                break;
            case 7:
                if (boid.getX() > 0 && boid.getY() > 0)
                    return( true );
                break;
            default: break;
        }
        return (false);
    }

    public void turn(int boidId, int b){
        Boid boid = boids[boidId];
        boid.turn(b);
    }

    public boolean isHitObstacle(int boidId){
        Boid boid = boids[boidId];
        return isHitObstacle(boid);
    }

    public boolean isHitObstacle(Boid boid){
        if(boid.isHitObstacle()){
            return true;
        }
        return hasMine(boid.getX(), boid.getY());
    }

    public boolean isHitTarget(int boidId){
        Boid boid = boids[boidId];
        return isHitTarget(boid);
    }

    public boolean isHitTarget(Boid boid){
        if(boid.isHitTarget()){
            return true;
        }
        return target.withinTarget(boid.getCurrentPosition());
    }

    public int move(int boidId, int d){
        Boid boid = boids[boidId];
        return boid.move(this, d);
    }

    public boolean isConflicting(int boidId) {
        Boid boid = boids[boidId];
        return boid.isConflicting();
    }

    public int[][] getCurrentPositions() {
        int[][] positions = new int[numBoids][];
        for(int i=0; i < numBoids; ++i){
            positions[i] = new int[2];
            positions[i][0] = boids[i].getX();
            positions[i][1] = boids[i].getY();
        }
        return positions;
    }

    public int[] getTargetPosition() {
        int[] position = new int[2];
        position[0] = target.getCenter().getX();
        position[1] = target.getCenter().getY();

        return position;
    }

    public int getMine(int i, int j) {
        return obstacle[i][j];
    }


    public boolean willHitTarget(int boidId, int d) {
        Boid boid = boids[boidId];
        Vec2I pos = boid.virtual_move(this, d);
        return target.withinTarget(pos);
    }

    public boolean willConflict(int boidId, int d) {
        Boid boid = boids[boidId];
        Vec2I pos = boid.virtual_move(this, d);
        int boid_signal = boid_signals[pos.getX()][pos.getY()];
        return boid_signal != -1;
    }

    public boolean willHitMine(int boidId, int d){
        Boid boid = boids[boidId];
        Vec2I pos = boid.virtual_move(this, d);
        return hasMine(pos.getX(), pos.getY());
    }
}
