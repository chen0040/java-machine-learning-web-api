package com.github.chen0040.ml.trees.id3;

import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.counts.CountRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by memeanalytics on 17/8/15.
 */
public class ID3TreeNode {
    private int tupleCount;
    private int splitAttributeIndex;
    private String attributeValue;
    private ArrayList<ID3TreeNode> childNodes;
    private String classLabel;

    public void copy(ID3TreeNode rhs){
        tupleCount = rhs.tupleCount;
        splitAttributeIndex = rhs.splitAttributeIndex;
        attributeValue = rhs.attributeValue;
        childNodes.clear();
        for(int i=0; i < rhs.childNodes.size(); ++i){
            childNodes.add((ID3TreeNode)rhs.childNodes.get(i).clone());
        }
        classLabel = rhs.classLabel;
    }

    @Override
    public Object clone(){
        ID3TreeNode clone = new ID3TreeNode();
        clone.copy(this);

        return clone;
    }

    public ID3TreeNode(){
        childNodes = new ArrayList<ID3TreeNode>();
    }

    public ID3TreeNode(IntelliContext batch, Random random, int height, int maxHeight){
        childNodes = new ArrayList<ID3TreeNode>();

        tupleCount = batch.tupleCount();
        splitAttributeIndex = -1;
        attributeValue = "";
        classLabel = "";
        updateClassLabel(batch);

        if(tupleCount <= 1 || height == maxHeight){
            return;
        }

        int n = batch.tupleAtIndex(0).tupleLength();

        CountRepository[] counts = new CountRepository[n];
        CountRepository counts2 = new CountRepository();

        for(int i=0; i < n; ++i){
            String category = String.format("field%d", i);
            counts[i] = new CountRepository(category);
        }


        for(int i=0; i < tupleCount; ++i){
            IntelliTuple tuple = batch.tupleAtIndex(i);
            String label = tuple.getLabelOutput();
            String classEventName = "ClassLabel="+label;

            for(int j=0; j < n; ++j){
                String category_value = batch.getAttributeValueAsString(tuple, j);
                counts[j].addSupportCount(category_value, classEventName);
                counts[j].addSupportCount(category_value);
                counts[j].addSupportCount();
            }
            counts2.addSupportCount(classEventName);
            counts2.addSupportCount();
        }


        double entropy_S = 0;
        for(String classEventName : counts2.getSubEventNames()){
            double p_class = counts2.getProbability(classEventName);
            entropy_S += (-p_class * log2(p_class));
        }


        if(entropy_S == 0){ // perfectly classified
            return;
        }else{
            splitAttributeIndex =  -1;

            HashMap<Integer, Double> candidates = new HashMap<Integer, Double>();
            for(int i = 0; i < n; ++i){
                List<String> T = counts[i].getSubEventNames();
                double entropy_reduced = 0;
                for(int j=0; j < T.size(); ++j) {

                    String t = T.get(j);
                    double p_t = counts[i].getProbability(t);
                    List<String> classNames = counts[i].getSubEventNames(t);
                    double entropy_t = 0;
                    for(int k=0; k < classNames.size(); ++k) {
                        double p_class_in_t = counts[i].getConditionalProbability(T.get(j), classNames.get(k));
                        entropy_t += (-p_class_in_t * log2(p_class_in_t));
                    }
                    entropy_reduced += p_t * entropy_t;
                }
                double information_gain = entropy_S - entropy_reduced;

                if(information_gain > 0){
                    candidates.put(i, information_gain);
                }
            }

            if(candidates.isEmpty()){
                return;
            }

            double max_information_gain = 0;
            for(Integer candidateFeatureIndex : candidates.keySet()){
                double information_gain = candidates.get(candidateFeatureIndex);
                if(information_gain > max_information_gain){
                    max_information_gain = information_gain;
                    splitAttributeIndex = candidateFeatureIndex;
                }
            }

            List<String> T = counts[splitAttributeIndex].getSubEventNames();

            IntelliContext[] batches = new IntelliContext[T.size()];

            for(int i=0; i < batches.length; ++i){
                batches[i] = new IntelliContext();
                batches[i].copyWithoutTuples(batch);
            }

            for(int i=0; i < tupleCount; ++i){
                IntelliTuple tuple = batch.tupleAtIndex(i);
                String attribute_value = batch.getAttributeValueAsString(tuple, splitAttributeIndex);
                batches[T.indexOf(attribute_value)].add(tuple);
            }

            for(int i=0; i < batches.length; ++i){
                childNodes.add(new ID3TreeNode(batches[i], random, height+1, maxHeight));
                childNodes.get(i).attributeValue = T.get(i);
            }

        }
    }

    public static double heuristicCost(double n){
        if(n <= 1.0) return 0;
        return 2 * (Math.log(n - 1) + 0.5772156649) - (2 * (n - 1) / n);
    }

    private double log2(double val){
        return Math.log(val) / Math.log(2);
    }

    private void updateClassLabel(IntelliContext batch){
        HashMap<String, Integer> classLabelCounts = new HashMap<String, Integer>();
        for(int i = 0; i < batch.tupleCount(); ++i){
            String label = batch.tupleAtIndex(i).getLabelOutput();
            classLabelCounts.put(label, classLabelCounts.containsKey(label) ? classLabelCounts.get(label)+1 : 1);
        }
        int maxCount = 0;
        for(String label : classLabelCounts.keySet()){
            if(classLabelCounts.get(label) > maxCount){
                maxCount = classLabelCounts.get(label);
                classLabel = label;
            }
        }
        //System.out.println("label: "+classLabel+"\tcount: "+maxCount);
    }

    public String predict(IntelliContext context, IntelliTuple tuple){
        if(childNodes != null){
            String value = context.getAttributeValueAsString(tuple, splitAttributeIndex);

            for(ID3TreeNode child : childNodes){

                if(child.attributeValue.equals(value)){
                    return child.predict(context, tuple);
                }
            }
        }
        return classLabel;
    }


    protected double pathLength(IntelliContext context, IntelliTuple tuple){
        if(childNodes!=null){
            String value = context.getAttributeValueAsString(tuple, splitAttributeIndex);
            for(ID3TreeNode child : childNodes){
                if(child.attributeValue.equals(value)){
                    return child.pathLength(context, tuple)+1.0;
                }
            }
        }

        return heuristicCost(tupleCount);
    }
}
