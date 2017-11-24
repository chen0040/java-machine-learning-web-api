package com.github.chen0040.ml.commons;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by memeanalytics on 26/8/15.
 */
public interface MLObject extends Cloneable {
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

    Map<String, Double> getAttributes();
    double getAttribute(String name);
    void setAttribute(String name, double value);

    int getStatusCode();
    void setStatusCode(int statusCode);

    String getStatusInfo();
    void setStatusInfo(String statusInfo);
}
