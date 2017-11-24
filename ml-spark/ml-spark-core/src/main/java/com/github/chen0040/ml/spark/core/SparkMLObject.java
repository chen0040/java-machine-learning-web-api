package com.github.chen0040.ml.spark.core;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by memeanalytics on 13/9/15.
 */
public interface SparkMLObject {
    String getDescription();
    void setDescription(String title);

    String getTitle();
    void setTitle(String title);

    Date getCreated();
    void setCreated(Date created);

    Date getUpdated();
    void setUpdated(Date updated);

    String getCreatedBy();
    void setCreatedBy(String user);

    String getUpdatedBy();
    void setUpdatedBy(String user);

    void setId(String id);
    String getId();

    void setProjectId(String id);
    String getProjectId();

    boolean getIsShell();
    void setIsShell(boolean isShell);

    HashMap<String, Double> getAttributes();
    double getAttribute(String name);
    void setAttribute(String name, double value);
}
