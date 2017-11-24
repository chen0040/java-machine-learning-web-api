package com.github.chen0040.ml.gaussianmixture.tests.anomaly.supervised;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.anomaly.AnomalyClassLabels;
import com.github.chen0040.ml.commons.classifiers.F1Score;
import com.github.chen0040.ml.commons.tune.Tunable;
import com.github.chen0040.ml.gaussianmixture.MultiVariateNormalOutliers;
import com.github.chen0040.ml.gaussianmixture.NormalOutliers;
import com.github.chen0040.ml.tuning.MLModuleModelTuner;
import com.github.chen0040.ml.tuning.MLModuleModelTunerResult;
import com.github.chen0040.sk.dom.basic.DomElement;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import com.github.chen0040.sk.utils.StringHelper;
import com.github.chen0040.ml.gaussianmixture.tests.utils.FileUtils;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.testng.Assert.assertEquals;


/**
 * Created by memeanalytics on 12/8/15.
 */
public class TestMLModuleModelTuner {
    @Test
    public void testTune(){
        String[] filenames_X = {"X1.txt", "X2.txt"};
        String[] filenames_Xval = {"Xval1.txt", "Xval2.txt"};
        String[] filenames_yval = { "yval1.txt", "yval2.txt"};
        double[] expected_f1Scores = {0.875, 0.551};

        for(int k=0; k < filenames_X.length; ++k) {
            String filename_X = filenames_X[k];
            String filename_Xval = filenames_Xval[k];
            String filename_yval = filenames_yval[k];
            final double expected_f1Score = expected_f1Scores[k];

            final IntelliContext trainingBatch = new IntelliContext(FileUtils.getResourceFile(filename_X), " ", false);

            final IntelliContext crossValidationBatch = new IntelliContext(FileUtils.getResourceFile(filename_Xval), " ", false);


            final List<String> crossValidationLabels = new ArrayList<>();
            CSVService.getInstance().readDoc(FileUtils.getResourceFile(filename_yval), null, false, new Function<DomElement, Boolean>() {
                public Boolean apply(DomElement line) {
                    String[] values = line.data;
                    assertEquals(1, values.length);
                    boolean is_anomaly = StringHelper.parseBoolean(values[0]);
                    if (is_anomaly) crossValidationLabels.add(AnomalyClassLabels.IS_ANOMALY);
                    else crossValidationLabels.add(AnomalyClassLabels.IS_NOT_ANOMALY);
                    return true;
                }
            }, null);

            assertEquals(crossValidationLabels.size(), crossValidationBatch.tupleCount());

            for(int i = 0; i < crossValidationBatch.tupleCount(); ++i){
                crossValidationBatch.tupleAtIndex(i).setLabelOutput(crossValidationLabels.get(i));
            }

            MultiVariateNormalOutliers algorithm = new MultiVariateNormalOutliers();
            algorithm.setAttribute(MultiVariateNormalOutliers.AUTO_THRESHOLDING, 0);

            MLModuleModelTuner tuner = new MLModuleModelTuner();

            algorithm.batchUpdate(trainingBatch);

            //Map<String, Double> parameters = algorithm.getAttributes();

            List<Tunable> tunables = new ArrayList<Tunable>();

            tunables.add(new Tunable("threshold", 0, 0, 1, "threshold below which considered anomality"));

            MLModuleModelTunerResult result = tuner.tune(algorithm, tunables, new Function<MLModule, Double>() {
                public Double apply(MLModule module) {
                    NormalOutliers nom = (NormalOutliers)module;

                    double score = F1Score.score(nom, crossValidationBatch).getF1Score();

                    if(Double.isNaN(score)){
                        score = 0;
                    }

                    //System.out.println("threshold: "+nom.threshold()+"\tscore: "+score);


                    double cost = 1 - score;
                    return cost;
                }
            });
            System.out.println("Tuned threshold: " + result.getParamValues()[0] + "\tF1 score: " + (1 - result.getCost()) + "\texpected F1 score:" + expected_f1Score);
        }
    }
}
