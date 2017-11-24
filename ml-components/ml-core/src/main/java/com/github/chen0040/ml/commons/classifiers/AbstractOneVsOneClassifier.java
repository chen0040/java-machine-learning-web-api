package com.github.chen0040.ml.commons.classifiers;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Created by chen0469 on 8/20/2015 0020.
 */
public abstract class AbstractOneVsOneClassifier extends AbstractClassifier {
    protected List<BinaryClassifier[]> classifiers;
    private double alpha = 0.1;
    private boolean shuffleBatch = false;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        AbstractOneVsOneClassifier rhs2 = (AbstractOneVsOneClassifier)rhs;
        alpha = rhs2.alpha;
        shuffleBatch = rhs2.shuffleBatch;
        classifiers.clear();
        for(int i=0; i < rhs2.classifiers.size(); ++i){
            BinaryClassifier[] bclassifiers_rhs = rhs2.classifiers.get(i);
            BinaryClassifier[] bclassifiers = new BinaryClassifier[bclassifiers_rhs.length];
            for(int j=0; j < bclassifiers_rhs.length; ++j){
                AbstractBinaryClassifier abc = (AbstractBinaryClassifier)bclassifiers_rhs[j];
                bclassifiers[j] = abc==null ? null : (BinaryClassifier)abc.clone();
            }
            classifiers.add(bclassifiers);
        }

    }

    public AbstractOneVsOneClassifier(List<String> classLabels){
        super(classLabels);
        classifiers = new ArrayList<BinaryClassifier[]>();
    }

    public AbstractOneVsOneClassifier(){
        super();
        classifiers = new ArrayList<BinaryClassifier[]>();
    }

    public boolean isShuffleBatch() {
        return shuffleBatch;
    }

    public void setShuffleBatch(boolean shuffleBatch) {
        this.shuffleBatch = shuffleBatch;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    protected void createClassifiers(IntelliContext batch){
        classifiers = new ArrayList<BinaryClassifier[]>();
        List<String> labels = getClassLabels();
        if(labels.size()==0){
            scan4ClassLabel(batch);
            labels = getClassLabels();
        }
        for(int i=0; i < labels.size(); ++i){
            for(int j=i+1; j < labels.size(); ++j) {
                BinaryClassifier[] pair = new BinaryClassifier[2];
                pair[0] = createClassifier(labels.get(i));
                pair[1] = createClassifier(labels.get(j));
                classifiers.add(pair);
            }
        }
    }

    protected abstract BinaryClassifier createClassifier(String classLabel);
    protected abstract double getClassifierScore(IntelliTuple tuple, BinaryClassifier classifier);

    protected List<IntelliContext> split(IntelliContext batch, int n){
        List<IntelliContext> batches = new ArrayList<IntelliContext>();
        int batchSize = batch.tupleCount() / n;
        for(int i=0; i < n; ++i){
            IntelliContext subbatch = new IntelliContext(batch.getAttributeLevelSource());

            for(int j=0; j < batchSize; ++j){
                int k = i * batchSize + j;
                if(k < batch.tupleCount()) {
                    subbatch.add(batch.tupleAtIndex(k));
                }
            }

            batches.add(subbatch);
        }

        return batches;
    }

    protected List<IntelliContext> remerge(List<IntelliContext> batches, int k){
        List<IntelliContext> newBatches = new ArrayList<IntelliContext>();


        for(int i=0; i < batches.size(); ++i){

            IntelliContext newBatch = new IntelliContext();

            for(int j=0; j < k; ++j){
                int d = (i + j) % batches.size();
                IntelliContext oldBatch = batches.get(d);
                if(j==0) {
                    newBatch.copyWithoutTuples(oldBatch);
                }
                for(int l = 0; l < oldBatch.tupleCount(); ++l){
                    newBatch.add(oldBatch.tupleAtIndex(l));
                }
            }

            newBatches.add(newBatch);
        }
        return newBatches;
    }

    protected boolean isValidTrainingSample(IntelliTuple tuple){
        return tuple.hasLabelOutput();
    }

    @Override
    public BatchUpdateResult batchUpdate(IntelliContext batch) {
        this.setModelSource(batch);
        batch = batch.filter(new Function<IntelliTuple, Boolean>() {
            public Boolean apply(IntelliTuple tuple) {
                return isValidTrainingSample(tuple);
            }
        });

        createClassifiers(batch);

        if(shuffleBatch) batch.shuffle();

        List<IntelliContext> batches = split(batch, classifiers.size());

        int k= Math.max(1, (int)alpha * batches.size());
        batches = remerge(batches, k);

        Exception error = null;
        for(int i=0; i < classifiers.size(); ++i){
            BinaryClassifier[] pair = classifiers.get(i);
            BinaryClassifier classifier1 = pair[0];
            BinaryClassifier classifier2 = pair[1];

            BatchUpdateResult classifierResult1 = classifier1.batchUpdate(batches.get(i));
            BatchUpdateResult classifierResult2 = classifier2.batchUpdate(batches.get(i));

            if(!classifierResult1.success() && error == null){
                error = classifierResult1.getError();
            }

            if(!classifierResult2.success() && error == null){
                error = classifierResult2.getError();
            }
        }

        return new BatchUpdateResult(error);
    }

    @Override
    public String predict(IntelliTuple tuple) {

        String predicatedClassLabel = null;
        HashMap<String, Integer> scores = new HashMap<String, Integer>();

        for(int i=0; i < classifiers.size(); ++i){
            BinaryClassifier[] pair = classifiers.get(i);
            BinaryClassifier classifier1 = pair[0];
            BinaryClassifier classifier2 = pair[1];

            double score1 = getClassifierScore(tuple, classifier1);
            double score2 = getClassifierScore(tuple, classifier2);

            String winningLabel;
            if(score1 > score2) {
                winningLabel = classifier1.getPositiveClassLabel();
            }
            else {
                winningLabel = classifier2.getPositiveClassLabel();
            }
            if(scores.containsKey(winningLabel)){
                scores.put(winningLabel, scores.get(winningLabel) + 1);
            }else {
                scores.put(winningLabel, 1);
            }
        }

        int maxScore = 0;
        for(String label : scores.keySet()){
            int score = scores.get(label);
            if(score > maxScore){
                maxScore= score;
                predicatedClassLabel = label;
            }
        }

        return predicatedClassLabel;
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        throw new NotImplementedException();
    }
}
