package com.github.chen0040.ml.glm.modelselection;

/**
 * Created by memeanalytics on 16/8/15.
 */
public enum ModelSelectionCriteria
{
    pValue, //p-value for the predictor coefficient in the null hypothesis
    AdjustedRSquare, //Adjusted explained variablity in the fitted regression model
    AIC, //Akaikane Information Criteria
    BIC //Bayesian Information Criteria
}

