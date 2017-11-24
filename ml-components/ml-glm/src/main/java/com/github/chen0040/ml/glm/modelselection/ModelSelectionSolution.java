package com.github.chen0040.ml.glm.modelselection;

import com.github.chen0040.ml.commons.tuples.TupleColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 16/8/15.
 */
public class ModelSelectionSolution {
    private Exception error;
    private ModelSelectionProblem problem;

    public List<TupleColumn> getFeatures() {
        return features;
    }

    private List<TupleColumn> features;

    public ModelSelectionSolution(Exception error){
        this.error = error;
    }

    public Exception getError(){
        return error;
    }

    public ModelSelectionProblem getProblem(){
        return problem;
    }

    public ModelSelectionSolution(ModelSelectionProblem problem, List<TupleColumn> features){
        this.problem = problem;
        this.features = new ArrayList<TupleColumn>();
        for(TupleColumn c : features){
            this.features.add(c);
        }
    }


}
