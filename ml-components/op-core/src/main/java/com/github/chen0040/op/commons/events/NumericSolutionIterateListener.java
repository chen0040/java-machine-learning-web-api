package com.github.chen0040.op.commons.events;

import com.github.chen0040.op.commons.models.solutions.NumericSolution;
import com.github.chen0040.op.commons.models.solutions.NumericSolutionUpdateResult;

/**
 * Created by memeanalytics on 12/8/15.
 */
public interface NumericSolutionIterateListener {
    void report(NumericSolution solution, NumericSolutionUpdateResult state, int iteration);

}
