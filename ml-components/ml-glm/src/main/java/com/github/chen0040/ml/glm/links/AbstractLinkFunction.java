package com.github.chen0040.ml.glm.links;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by memeanalytics on 14/8/15.
 */
public abstract class AbstractLinkFunction implements LinkFunction {
    public abstract double GetLink(double constraint_interval_value);
    public abstract double GetInvLink(double real_line_value);
    public abstract double GetInvLinkDerivative(double real_line_value);

    @Override
    public Object clone(){
        throw new NotImplementedException();
    }
}


