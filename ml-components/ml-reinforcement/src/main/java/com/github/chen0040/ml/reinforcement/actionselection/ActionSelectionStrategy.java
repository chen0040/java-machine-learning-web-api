package com.github.chen0040.ml.reinforcement.actionselection;

import com.github.chen0040.ml.reinforcement.models.QModel;
import com.github.chen0040.ml.linearalg.IndexValue;
import com.github.chen0040.ml.reinforcement.models.UtilityModel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by chen0469 on 9/27/2015 0027.
 */
public interface ActionSelectionStrategy extends Serializable, Cloneable {
    IndexValue selectAction(int stateId, QModel model, Set<Integer> actionsAtState);
    IndexValue selectAction(int stateId, UtilityModel model, Set<Integer> actionsAtState);
    String getPrototype();
    HashMap<String, String> getAttributes();
}
