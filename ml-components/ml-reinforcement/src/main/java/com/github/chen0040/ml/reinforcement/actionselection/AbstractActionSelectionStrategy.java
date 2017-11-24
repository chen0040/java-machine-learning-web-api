package com.github.chen0040.ml.reinforcement.actionselection;

import com.github.chen0040.ml.reinforcement.models.QModel;
import com.github.chen0040.ml.linearalg.IndexValue;
import com.github.chen0040.ml.reinforcement.models.UtilityModel;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by chen0469 on 9/27/2015 0027.
 */
public abstract class AbstractActionSelectionStrategy implements ActionSelectionStrategy {

    private String prototype;
    protected HashMap<String, String> attributes = new HashMap<String, String>();

    public String getPrototype(){
        return prototype;
    }

    public IndexValue selectAction(int stateId, QModel model, Set<Integer> actionsAtState) {
        return new IndexValue();
    }

    public IndexValue selectAction(int stateId, UtilityModel model, Set<Integer> actionsAtState) {
        return new IndexValue();
    }

    public AbstractActionSelectionStrategy(){
        prototype = this.getClass().getCanonicalName();
    }


    public AbstractActionSelectionStrategy(HashMap<String, String> attributes){
        this.attributes = attributes;
    }

    public HashMap<String, String> getAttributes(){
        return attributes;
    }

    @Override
    public abstract Object clone();
}
