package com.github.chen0040.ml.trees.id3;

import com.github.chen0040.ml.commons.BatchUpdateResult;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.classifiers.AbstractClassifier;
import com.github.chen0040.ml.commons.discrete.AbstractAttributeValueDiscretizer;
import com.github.chen0040.ml.commons.discrete.AttributeValueDiscretizer;
import com.github.chen0040.ml.dataprepare.discretize.kmeans.KMeansDiscretizer;

import java.util.Random;
import java.util.function.Function;

/**
 * Created by memeanalytics on 23/8/15.
 */
public class ID3 extends AbstractClassifier {
    private static Random rand = new Random();
    private AttributeValueDiscretizer discretizer;
    private ID3TreeNode tree;
    private int maxHeight;

    @Override
    public void copy(MLModule rhs){
        super.copy(rhs);

        ID3 rhs2 = (ID3)rhs;
        tree = rhs2.tree==null ? null : (ID3TreeNode)rhs2.tree.clone();
        maxHeight = rhs2.maxHeight;
        discretizer = rhs2.discretizer == null ? null : (AttributeValueDiscretizer)((AbstractAttributeValueDiscretizer)rhs2.discretizer).clone();
    }

    @Override
    public Object clone(){
        ID3 clone = new ID3();
        clone.copy(this);

        return clone;
    }

    public ID3(){
        maxHeight = 1000;
        discretizer=new KMeansDiscretizer();
    }

    public AttributeValueDiscretizer getDiscretizer() {
        return discretizer;
    }

    public void setDiscretizer(AttributeValueDiscretizer discretizer) {
        this.discretizer = discretizer;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    public String predict(IntelliTuple tuple) {
        tuple = discretizer.discretize(tuple);
        return tree.predict(getModelSource(), tuple);
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

        discretizer.batchUpdate(batch);

        batch = discretizer.discretize(batch);

        //System.out.println(batch);

        tree = new ID3TreeNode(batch, rand, 0, maxHeight);

        return new BatchUpdateResult();
    }

    @Override
    public double evaluate(IntelliTuple tuple, IntelliContext context) {
        tuple = discretizer.discretize(tuple);
        return tree.pathLength(context, tuple);
    }
}
