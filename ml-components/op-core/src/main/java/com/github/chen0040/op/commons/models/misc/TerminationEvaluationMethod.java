package com.github.chen0040.op.commons.models.misc;

import com.github.chen0040.op.commons.models.solutions.NumericSolutionUpdateResult;

/**
 * Created by memeanalytics on 12/8/15.
 */
public interface TerminationEvaluationMethod {
    boolean shouldTerminate(NumericSolutionUpdateResult state, int iteration);
}
