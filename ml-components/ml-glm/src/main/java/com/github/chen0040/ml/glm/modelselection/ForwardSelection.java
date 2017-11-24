package com.github.chen0040.ml.glm.modelselection;

import com.github.chen0040.ml.glm.metrics.GlmStatistics;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.tuples.TupleColumn;
import com.github.chen0040.ml.glm.metrics.GlmLikelihoodFunction;
import com.github.chen0040.ml.glm.solvers.GlmSolver;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 16/8/15.
 */
/// <summary>
/// The best model is not always the most complicated. Sometimes including variables that are not
/// evidently important can actually reduce the accuracy of the predictions. The model selection strategies
/// in this case help us to elimite from the model variables that are less important.
///
/// The model that includes all available explanatory variables is often referred to as full model. The goals of
/// model selection is to assess whether the full model is the best model. If it isn't, we want to identify a
/// smaller model that is preferable.
///
/// The forward-selection strategy starts with the model that includes only the constant.
/// Variables are added one-at-a-time from the model until no more variables with statistically significant p-values remains.
///
/// </summary>
public class ForwardSelection
{
    public static ModelSelectionSolution forwardSelect(ModelSelectionProblem problem){
        return forwardSelect(problem, ModelSelectionCriteria.AdjustedRSquare);
    }

    public static ModelSelectionSolution forwardSelect(ModelSelectionProblem problem, ModelSelectionCriteria criteria)
    {
        List<TupleColumn> remaining_features = new ArrayList<TupleColumn>();

        for(TupleColumn c : problem.getCandidateFeatures()) {
            remaining_features.add(c);
        }

        double[] outcomes = problem.getExpectedOutputs();
        if(outcomes == null)
        {
            return new ModelSelectionSolution(new Exception("Some tuples in the shrinkedData source do not have output"));
        }
        ModelSelectionProblem.SolverPackage solver_package = problem.buildSolver(remaining_features);
        GlmSolver solver = solver_package.solver;
        IntelliContext solver_batch = solver_package.batch;
        GlmStatistics statistics = solver.showStatistics();


        double fitness_score = -1;
        if (criteria == ModelSelectionCriteria.AdjustedRSquare)
        {
            fitness_score = ModelSelectionEvaluation.CalcAdjustedRSquare(statistics.getResiduals(),
                    outcomes, remaining_features.size(), solver_batch.tupleCount());
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

        List<TupleColumn> candidate_features = new ArrayList<TupleColumn>();
        boolean improved = true;
        while (improved)
        {
            TupleColumn selectedFeature = null;
            for (int i = 0; i < remaining_features.size(); ++i)
            {
                List<TupleColumn> candidate_features_temp = new ArrayList<TupleColumn>();
                candidate_features_temp.addAll(candidate_features);
                candidate_features_temp.add(remaining_features.get(i));

                solver_package = problem.buildSolver(candidate_features_temp);
                solver = solver_package.solver;
                solver_batch = solver_package.batch;
                statistics = solver.showStatistics();

                double new_fitness_score = -1;
                if (criteria == ModelSelectionCriteria.AdjustedRSquare)
                {
                    new_fitness_score = ModelSelectionEvaluation.CalcAdjustedRSquare(statistics.getResiduals(),
                            outcomes, candidate_features_temp.size(), solver_batch.tupleCount());
                }
                else if (criteria == ModelSelectionCriteria.AIC)
                {
                    double L = GlmLikelihoodFunction.getLikelihood(solver.getDistributionFamily(),
                            solver_batch, solver.getCoefficients().getValues());
                    int k = solver.getCoefficients().size();
                    new_fitness_score = -ModelSelectionEvaluation.CalcAIC(L, k,
                            solver_batch.tupleCount()); //negative sign as the lower the AIC, the better the fitted regression model
                }
                else if (criteria == ModelSelectionCriteria.BIC)
                {
                    double L_hat = GlmLikelihoodFunction.getLikelihood(solver.getDistributionFamily(),
                            solver_batch, solver.getCoefficients().getValues());
                    int k = solver.getCoefficients().size();
                    new_fitness_score = -ModelSelectionEvaluation.CalcBIC(L_hat, k,
                            solver_batch.tupleCount()); //negative sign as the lower the BIC, the better the fitted regression model
                }

                if (fitness_score < new_fitness_score)
                {
                    selectedFeature = remaining_features.get(i);
                    fitness_score = new_fitness_score;
                }
            }

            if (selectedFeature == null)
            {
                improved = false;
            }
            else
            {
                candidate_features.add(selectedFeature);
                remaining_features.remove(selectedFeature);
            }
        }

        return new ModelSelectionSolution(problem, candidate_features);
    }

}



