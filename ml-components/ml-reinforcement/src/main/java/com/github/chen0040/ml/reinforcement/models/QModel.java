package com.github.chen0040.ml.reinforcement.models;

import com.github.chen0040.ml.linearalg.Vector;
import com.github.chen0040.ml.linearalg.IndexValue;
import com.github.chen0040.ml.linearalg.Matrix;

import java.util.*;

/**
 * @author chen0469
 * @date 9/27/2015 0027.
 * @brief Q is known as the quality of state-action combination, note that it is different from utility of a state
 */
public class QModel {
    /**
    *  @brief Q value for (state_id, action_id) pair
    *  Q is known as the quality of state-action combination, note that it is different from utility of a state
    */
    private Matrix Q;
    /**
    *  @brief $\alpha[s, a]$ value for learning rate: alpha(state_id, action_id)
    */
    private Matrix alpha;

    /**
     * @brief discount factor
     */
    private double gamma = 0.7;

    private int stateCount;
    private int actionCount;

    public QModel(int stateCount, int actionCount, double initialQ){
        this.stateCount = stateCount;
        this.actionCount = actionCount;
        Q = new Matrix(stateCount,actionCount);
        alpha = new Matrix(stateCount, actionCount);
        Q.setAll(initialQ);
        alpha.setAll(0.1);
    }

    public QModel(int stateCount, int actionCount){
        this(stateCount, actionCount, 0.1);
    }

    public QModel(){

    }

    @Override
    public boolean equals(Object rhs){
        if(rhs != null && rhs instanceof QModel){
            QModel rhs2 = (QModel)rhs;


            if(gamma != rhs2.gamma) return false;


            if(stateCount != rhs2.stateCount || actionCount != rhs2.actionCount) return false;

            if((Q!=null && rhs2.Q==null) || (Q==null && rhs2.Q !=null)) return false;
            if((alpha!=null && rhs2.alpha==null) || (alpha==null && rhs2.alpha!=null)) return false;

            System.out.println("Reach here");

            return !((Q != null && !Q.equals(rhs2.Q)) || (alpha != null && !alpha.equals(rhs2.alpha)));

        }
        return false;
    }

    @Override
    public Object clone(){
        QModel clone = new QModel();
        clone.copy(this);
        return clone;
    }

    public void copy(QModel rhs){
        gamma = rhs.gamma;
        stateCount = rhs.stateCount;
        actionCount = rhs.actionCount;
        Q = rhs.Q==null ? null : (Matrix)rhs.Q.clone();
        alpha = rhs.alpha == null ? null : (Matrix)rhs.alpha.clone();
    }

    public Matrix getQ() {
        return Q;
    }

    public double getQ(int stateId, int actionId){
        return Q.get(stateId, actionId);
    }

    public void setQ(Matrix q) {
        Q = q;
    }

    public void setQ(int stateId, int actionId, double Qij){
        Q.set(stateId, actionId, Qij);
    }

    public Matrix getAlpha() {
        return alpha;
    }

    public double getAlpha(int stateId, int actionId){
        return alpha.get(stateId, actionId);
    }

    public void setAlpha(Matrix alpha) {
        this.alpha = alpha;
    }

    public void setAlpha(double defaultAlpha) {
        this.alpha.setAll(defaultAlpha);
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public int getStateCount(){
        return stateCount;
    }

    public int getActionCount(){
        return actionCount;
    }

    public IndexValue actionWithMaxQAtState(int stateId, Set<Integer> actionsAtState){
        com.github.chen0040.ml.linearalg.Vector rowVector = Q.getRow(stateId);
        return rowVector.indexWithMaxValue(actionsAtState);
    }

    private void reset(double initialQ){
        Q.setAll(initialQ);
    }


    public IndexValue actionWithSoftMaxQAtState(int stateId,Set<Integer> actionsAtState, Random random) {
        Vector rowVector = Q.getRow(stateId);
        double sum = 0;

        if(actionsAtState==null){
            actionsAtState = new HashSet<Integer>();
            for(int i=0; i < actionCount; ++i){
                actionsAtState.add(i);
            }
        }

        List<Integer> actions = new ArrayList<Integer>();
        for(Integer actionId : actionsAtState){
            actions.add(actionId);
        }

        double[] acc = new double[actions.size()];
        for(int i=0; i < actions.size(); ++i){
            sum += rowVector.get(actions.get(i));
            acc[i] = sum;
        }


        double r = random.nextDouble() * sum;

        IndexValue result = new IndexValue();
        for(int i=0; i < actions.size(); ++i){
            if(acc[i] >= r){
                int actionId = actions.get(i);
                result.setIndex(actionId);
                result.setValue(rowVector.get(actionId));
                break;
            }
        }

        return result;
    }
}
