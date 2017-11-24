package com.github.chen0040.ml.reinforcement.actionselection;

import com.github.chen0040.ml.reinforcement.models.QModel;
import com.github.chen0040.ml.linearalg.IndexValue;

import java.util.Set;

/**
 * Created by chen0469 on 9/27/2015 0027.
 */
public class GreedyActionSelectionStrategy extends AbstractActionSelectionStrategy {
    @Override
    public IndexValue selectAction(int stateId, QModel model, Set<Integer> actionsAtState) {
        return model.actionWithMaxQAtState(stateId, actionsAtState);
    }

    @Override
    public Object clone(){
        GreedyActionSelectionStrategy clone = new GreedyActionSelectionStrategy();
        return clone;
    }

    @Override
    public boolean equals(Object obj){
        return obj != null && obj instanceof GreedyActionSelectionStrategy;
    }
}
