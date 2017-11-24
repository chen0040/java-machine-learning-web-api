package com.github.chen0040.ml.glm.modelselection;

import com.github.chen0040.statistics.distributions.univariate.StudentT;
import com.github.chen0040.statistics.metrics.StdDev;
import com.github.chen0040.statistics.metrics.Mean;

/**
 * Created by memeanalytics on 16/8/15.
 */
public class ModelSelectionEvaluation {

    /// <summary>
    /// Calculate the Akaike information actionselection for the fitted regression model
    /// The lower the AIC, the better the fitted regression model.
    /// The k term in the AIC penalize the number of parameters added to the regression model
    /// </summary>
    /// <param name="L">The likelihood value of the fitted regression model</param>
    /// <param name="k">The number of fitted parameters (i.e. predictor coefficients in the regression model)</param>
    /// <param name="n">The number of sample shrinkedData points (i.e. the number of records in the training shrinkedData)</param>
    /// <returns>The Akaike information actionselection</returns>
    public static double CalcAIC(double L, int k, int n)
    {
        double AIC = 2 * k - 2 * Math.log(L);
        double AICc = AIC - 2 * k * (k + 1) / (n - k - 1); //AICc is AIC with a correction for finite sample sizes.
        return AICc;
    }

    /// <summary>
    /// Calculate the Bayesian information actionselection for the fitted regression model
    /// The lower the BIC, the better the fitted regression model
    ///
    /// When fitting models, it is possible to increase the likelihood by adding parameters,
    /// but doing so may result in overfitting. Both BIC and AIC resolve this problem by introducing
    /// a penalty term for the number of parameters in the model; the penalty term is larger in BIC than in AIC.
    /// </summary>
    /// <param name="L_hat">The maximized value of the likelihood function of the model given by the fitted predictor coefficients which minimize the unexplained variability in the regression model)</param>
    /// <param name="k">The number of fitted parameters (i.e. predictor coefficients in the regression model)</param>
    /// <param name="n">The number of sample shrinkedData points (i.e. the number of records in the training shrinkedData)</param>
    /// <returns>The Bayesian information actionselection</returns>
    public static double CalcBIC(double L_hat, int k, int n)
    {
        double BIC = -2 * Math.log(L_hat) + k * Math.log(n);
        return BIC;
    }

    /// <summary>
    /// Calculate the adjusted R^2 (the explained variablity by the regression model)
    /// The higher the adjusted R^2, the better the fitted regression model
    /// </summary>
    /// <param name="residuals">the residuals {y_i - X_i * beta_hat }_i</param>
    /// <param name="y">The outcomes (i.e. sample values of the response variable)</param>
    /// <param name="k">The number of parameters (i.e. the predictor coefficients in the regression model)</param>
    /// <param name="n">The number of sample shrinkedData points (i.e., the number records in the training shrinkedData)</param>
    /// <returns></returns>
    public static double CalcAdjustedRSquare(double[] residuals, double[] y, int k, int n)
    {
        double Var_e = 0;
        for (int i = 0; i < residuals.length; ++i)
        {
            Var_e = residuals[i] * residuals[i];
        }
        double Var_y = Math.pow(StdDev.apply(y, Mean.apply(y)), 2);
        return 1 - (Var_e / (n - k - 1)) / (Var_y / (n - 1));
    }



    /// <summary>
    /// The p-values are P(observed or more extreme coefficients != 0 | true coefficient mean is 0)
    /// </summary>
    /// <param name="CoeffPointEstimates">point estimates of the predictor coefficients</param>
    /// <param name="CoeffSEs">standard errors of the predicator coefficients</param>
    /// <param name="n">number of training records</param>
    /// <param name="one_sided">whether the t distribution is one-sided</param>
    /// <returns>p-values</returns>
    public static double[] CalcPValues(double[] CoeffPointEstimates, double[] CoeffSEs, int n, boolean one_sided)
    {
        double null_value = 0;
        int k = CoeffPointEstimates.length;

        double[] pValues = new double[k];
        int df = n - 1;
        for (int i = 0; i < k; ++i)
        {
            double t = (CoeffPointEstimates[i]-null_value) / CoeffSEs[i];
            double pValue = (1- StudentT.GetPercentile(Math.abs(t), df)) * (one_sided ? 1 : 2);

            pValues[i] = pValue;
        }

        return pValues;
    }

    public static SelectionByPvalue SelectFeatureIndexWithMaxPValue(double[] pValues)
    {
        double maxPValue = 0;
        int selectedFeatureIndex = -1;
        for (int i = 1; i < pValues.length; ++i)
        {
            if (maxPValue < pValues[i])
            {
                maxPValue = pValues[i];
                selectedFeatureIndex = i;
            }
        }

        return new SelectionByPvalue(selectedFeatureIndex, maxPValue);
    }
}
