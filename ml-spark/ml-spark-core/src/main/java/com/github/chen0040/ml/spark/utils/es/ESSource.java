package com.github.chen0040.ml.spark.utils.es;

import java.io.Serializable;

/**
 * Created by root on 9/7/15.
 */
public class ESSource implements Serializable {
    public String Message;
    public int Size;
    public String UserId;
    public String IP;
    public String Severity;
    public String Host;
    public int PID;
    public String Process;
    public String Facility;
    public long Date;
    //public ESKeyValue KeyValue = new ESKeyValue();
}
