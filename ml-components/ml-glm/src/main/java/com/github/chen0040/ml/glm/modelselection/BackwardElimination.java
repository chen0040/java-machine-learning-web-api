package com.github.chen0040.ml.glm.modelselection;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.glm.metrics.GlmStatistics;
import com.github.chen0040.ml.glm.solvers.GlmSolver;
import com.github.chen0040.ml.commons.tuples.TupleColumn;
import com.github.chen0040.ml.glm.metrics.GlmLikelihoodFunction;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 16/8/15.
 */
/// <summary>
/// The best model is not always the most complicated. Sometimes including variables that are not
/// evidently important can actually reduce the accuracy of the predictions. The model selection strategies
/// in this case help us to eliminate from the model variables that are less important.
///
/// The model that includes all available explanatory variables is often referred to as full model. The goals of
/// model selection is to assess whether the full model is the best model. If it isn't, we want to identify a
/// smaller model that is preferable.
///
/// The backward-elimination strategy starts with the model that includes all potentiall predictor variables.
/// Variables are eliminated one-at-a-time from the model until only variables with statistically significant p-values remains.
///
/// </summary>
public class BackwardElimination
{
    public static ModelSelectionSolution eliminateByPValue(ModelSelectionProblem problem) {
        return eliminateByPValue(problem, 0.05, false);
    }

    /// <summary>
    /// The strategy within each elimination step is to drop the variable with the largest p-value, refit the model, and reasses
    /// the inclusion of all variables.
    /// </summary>
    /// <param name="solver"></param>
    /// <param name="records"></param>
    /// <param name="significance_level"></param>
    /// <param name="one_sided"></param>
    /// <returns>The index list of the predictor variables to be included in the regression model</returns>
    public static ModelSelectionSolution eliminateByPValue(ModelSelectionProblem problem, double significance_level, boolean one_sided)
    {
        List<TupleColumn> candidateFeatures = new ArrayList<TupleColumn>();

        for(TupleColumn c : problem.getCandidateFeatures()) {
            candidateFeatures.add(c);
        }

        ModelSelectionProblem.SolverPackage solver_package = problem.buildSolver(candidateFeatures);
        GlmSolver solver = solver_package.solver;
        IntelliContext solver_batch = solver_package.batch;
        double[] pValues = ModelSelectionEvaluation.CalcPValues(solver.getCoefficients().getValues(),
                solver.showStatistics().getStandardErrors(),
                solver_batch.tupleCount(),
                one_sided);

        SelectionByPvalue result = ModelSelectionEvaluation.SelectFeatureIndexWithMaxPValue(pValues);
        int featureIndexWithMaxPValue = result.getFeatureIndex();
        double maxPValue = result.pValue();

        while (maxPValue > significance_level)
        {
            candidateFeatures.remove(featureIndexWithMaxPValue);

            solver_package = problem.buildSolver(candidateFeatures);
            solver = solver_package.solver;
            solver_batch = solver_package.batch;
            pValues = ModelSelectionEvaluation.CalcPValues(solver.getCoefficients().getValues(),
                    solver.showStatistics().getStandardErrors(),
                    solver_batch.tupleCount(),
                    one_sided);

            result = ModelSelectionEvaluation.SelectFeatureIndexWithMaxPValue(pValues);
            featureIndexWithMaxPValue = result.getFeatureIndex();
            maxPValue = result.pValue();
        }

        return new ModelSelectionSolution(problem, candidateFeatures);
    }


    public static ModelSelectionSolution backwardEliminate(ModelSelectionProblem problem) {
        return backwardEliminate(problem, ModelSelectionCriteria.AdjustedRSquare);
    }

    /// <summary>
    /// This is an alternative to using p-values in the backward elimination by using adjusted R^2.
    /// At each elimination step, we refit the model without each of the variable up for potential eliminiation.
    /// If one of these smaller models has a higher adjusted R^2 than our current model, we pick the smaller
    /// model with the largest adjusted R^2. We continue in this way until removing variables does not increase adjusted R^2.
    /// </summary>
    /// <param name="solver"></param>
    /// <param name="records"></param>
    /// <returns>The index list of the predictor variables to be included in the regression model</returns>
    public static ModelSelectionSolution backwardEliminate(ModelSelectionProblem problem, ModelSelectionCriteria criteria)
    {
        List<TupleColumn> candidateFeatures = new ArrayList<TupleColumn>();
        for(TupleColumn c : problem.getCandidateFeatures()) {
            candidateFeatures.add(c);
        }

        double[] outcomes = problem.getExpectedOutputs();
        if(outcomes == null)
        {
            return new ModelSelectionSolution(new Exception("Some tuples in the shrinkedData source do not have output"));
        }

        ModelSelectionProblem.SolverPackage solver_package = problem.buildSolver(candidateFeatures);
        GlmSolver solver = solver_package.solver;
        IntelliContext solver_batch = solver_package.batch;
        GlmStatistics statistics = solver.showStatistics();

        double fitness_score = -1;
        if (criteria == ModelSelectionCriteria.AdjustedRSquare)
        {
            fitness_score = ModelSelectionEvaluation.CalcAdjustedRSquare(statistics.getResiduals(), outcomes,
                    candidateFeatures.size(), solver_batch.tupleCount());
        }
        else if (criteria == ModelSelectionCriteria.AIC)
        {
            double L = GlmLikelihoodFunction.getLikelihood(solver.getDistributionFamily(),
                    solver_batch, solver.getCoefficients().getValues());
            int k = solver.getCoefficients().size();
            fitness_score = -ModelSelectionEvaluation.CalcAIC(L, k,
                    solver_batch.tupleCount()); //negative sign as the lower the AIC, the better the fitted regression model
        }
        else if (criteria == ModelSelectionCriteria.BIC)
        {
            double L_hat = GlmLikelihoodFunction.getLikelihood(solver.getDistributionFamily(),
                    solver_batch, solver.getCoefficients().getValues());
            int k = solver.getCoefficients().size();
            fitness_score = -ModelSelectionEvaluation.CalcBIC(L_hat, k,
                    solver_batch.tupleCount()); //negative sign as the lower the BIC, the better the fitted regression model
        }
        else
        {
            return new ModelSelectionSolution(new NotImplementedException());
        }


        boolean improved = true;
        while (improved)
        {
            int eliminatedFeatureId = -1;
            for (int i = 0; i < candidateFeatures.size(); ++i)
            {
                List<TupleColumn> candidate_features_temp = new ArrayList<TupleColumn>();
                for (int j = 0; j < candidateFeatures.size(); ++j)
                {
                    if (i == j) continue;
                    candidate_features_temp.add(candidateFeatures.get(j));
                }

                solver_package = problem.buildSolver(candidate_features_temp);
                solver = solver_package.solver;
                solver_batch = solver_package.batch;
                statistics = solver.showStatistics();

                double new_fitness_score = -1;
                if (criteria == ModelSelectionCriteria.AdjustedRSquare)
                {
                    new_fitness_score = ModelSelectionEvaluation.CalcAdjustedRSquare(statistics.getResiduals(), outcomes,
                            candidate_features_temp.size(), solver_batch.tupleCount());
                }
                else if (criteria == ModelSelectionCriteria.AIC)
                {
                    double L = GlmLikelihoodFunction.getLikelihood(solver.getDistributionFamily(),
                            solver_batch, solver.getCoefficients().getValues());
                    int k = solver.getCoefficients().size();
                    new_fitness_score = -ModelSelectionEvaluation.CalcAIC(L, k, solver_batch.tupleCount()); //negative sign as the lower the AIC, the better the fitted regression model
                }
                else if (criteria == ModelSelectionCriteria.BIC)
                {
                    double L_hat = GlmLikelihoodFunction.getLikelihood(
                            solver.getDistributionFamily(),
                            solver_batch, solver.getCoefficients().getValues());
                    int k = solver.getCoefficients().size();
                    new_fitness_score = -ModelSelectionEvaluation.CalcBIC(L_hat, k, solver_batch.tupleCount()); //negative sign as the lower the BIC, the better the fitted regression model
                }

                if (fitness_score < new_fitness_score)
                {
                    eliminatedFeatureId = i;
                    fitness_score = new_fitness_score;
                }
            }

            if (eliminatedFeatureId == -1)
            {
                improved = false;
            }
            else
            {
                candidateFeatures.remove(eliminatedFeatureId);
            }
        }

        return new ModelSelectionSolution(problem, candidateFeatures);
    }


}



