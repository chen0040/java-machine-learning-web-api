package com.github.chen0040.ml.sdk.models;

import com.github.chen0040.ml.commons.MLModuleOutputType;

/**
 * Created by memeanalytics on 4/9/15.
 */
public class MLModuleOverview {
    public String name;
    public String description;
    public String id;
    public MLModuleOutputType outputType;
    public int statusCode = 200;
    public String statusInfo = "";

    public MLModuleOverview(){
        name = "";
        description = "";
        id = "";
    }
}
