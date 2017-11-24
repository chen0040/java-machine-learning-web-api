package com.github.chen0040.ml.glm.solvers;

import com.github.chen0040.ml.commons.tuples.TupleAttributeLevel;

/**
 * Created by memeanalytics on 18/8/15.
 */
public class GlmCoefficients implements  Cloneable{
    private double[] values;
    private TupleAttributeLevel[] descriptors;

    public void copy(GlmCoefficients rhs){
        values = rhs.values == null ? null : rhs.values.clone();
        descriptors = null;
        if(rhs.descriptors != null){
            descriptors = new TupleAttributeLevel[rhs.descriptors.length];
            for(int i=0; i < rhs.descriptors.length; ++i){
                descriptors[i] = rhs.descriptors[i] == null ? null : (TupleAttributeLevel)rhs.descriptors[i].clone();
            }
        }
    }

    @Override
    public Object clone(){
        GlmCoefficients clone = new GlmCoefficients();
        clone.copy(this);
        return clone;
    }

    public GlmCoefficients() {
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values=values;
    }

    public TupleAttributeLevel[] getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(TupleAttributeLevel[] descriptors){
        this.descriptors = descriptors;
    }

    public int size() {
        return values==null ? 0 : values.length;
    }

    @Override
    public String toString() {
        if(values==null || descriptors==null){
            return "(null)";
        }
        StringBuilder sb = new StringBuilder();


        sb.append("{");
        sb.append(String.format("\"(Intercepter)\":%f, ", values[0]));
        for (int i = 1; i < values.length; ++i){
            sb.append(String.format(", \"%s\":%f", descriptors[i-1].toString(), values[i]));
        }
        sb.append("}");
        return sb.toString();
    }
}
