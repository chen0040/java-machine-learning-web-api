package com.github.chen0040.ml.models;

/**
 * Created by root on 11/11/15.
 */

import com.impetus.kundera.index.Index;
import com.impetus.kundera.index.IndexCollection;
import com.github.chen0040.ml.enums.AlgorithmStatus;
import com.github.chen0040.ml.enums.AlgorithmType;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@IndexCollection(columns = { @Index (name = "title"), @Index(name = "companyId")})
public class AlgorithmModule implements AlgorithmUnit, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Column
    private String trainingStartTime = ""; //"0 8 * * 6 ?"; //schedule at 8:00 every Saturday

    @Column
    private long trainingSessionCountPerSchedule = 1;


    @Column
    private String title;

    @Id
    private String id;

    @Column
    private String parentId;

    @Column
    private String companyId;

    @Column
    private long timeWindowInMinutes;

    @Column
    private long timeWindowCount4Training;

    @Column
    private boolean scheduleImmediate = true;

    @Column
    private String scheduleTime =  "0 0 8 ? * 6"; // "0 8 * * 6 ?"; //schedule at 8:00 every Saturday

    @ElementCollection
    @Column
    private Map<String, String> attributes =null;

    @ElementCollection
    @Column
    private List<String> devices ;

    @Column
    private String dashboard = "";

    @Column
    private AlgorithmStatus status = AlgorithmStatus.NotStarted;

    @Column
    private AlgorithmType algorithmType = AlgorithmType.MLAnomaly_IsolationForest;

    public String attribute(String attributeName) { return attributes.get(attributeName); }

    public AlgorithmType getAlgorithmType() { return algorithmType; }

    public Map<String, String> getAttributes() { return attributes; }

    public List<String> getDevices() { return devices; }

    public void setDevices(ArrayList<String> devices){
        this.devices = devices;
    }

    public String getCompanyId() { return companyId; }

    public String getId() { return id; }

    public AlgorithmModule(){
        devices = new ArrayList<>();
        attributes = new HashMap<String, String>();

    }

    @JsonIgnore
    /** PRIMARY KEY(id) **/
    public String getPartitionKey() {
        return this.id;
    }

    public String getParentId(){
        return parentId;
    }

    public void setParentId(String parentId){
        this.parentId = parentId;
    }

    public boolean getScheduleImmediate() {
        return scheduleImmediate;
    }
    public String getScheduleTime(){
        return this.scheduleTime;
    }

    public AlgorithmStatus getStatus() { return status; }
    public long getTimeWindowCount4Training() { return timeWindowCount4Training; }

    public long getTimeWindowInMinutes() { return timeWindowInMinutes; }
    public String getTitle() { return title; }

    public void setAlgorithmType(AlgorithmType algorithmType) { this.algorithmType = algorithmType; }
    public void setAttribute(String attributeName, String attributeValue) { attributes.put(attributeName, attributeValue); }

    public void setAttributes(HashMap<String, String> attributes) { this.attributes = attributes; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    public void setId(String id) { this.id = id; }
    public void setScheduleImmediate(boolean immediate){
        this.scheduleImmediate = immediate;
    }

    public void setScheduleTime(String chronTime){
        this.scheduleTime = chronTime;
    }
    public void setStatus(AlgorithmStatus status) { this.status = status; }

    public void setTimeWindowCount4Training(long timeWindowCount4Training) { this.timeWindowCount4Training = timeWindowCount4Training; }
    public void setTimeWindowInMinutes(long timeWindowInMinutes) { this.timeWindowInMinutes = timeWindowInMinutes; }

    public void setTitle(String title) { this.title = title; }

    public String toString(){
        return id+"@"+title;
    }

    @Override
    public String getTrainingStartTime() {
        return trainingStartTime;
    }

    @Override
    public void setTrainingStartTime(String date) {
        trainingStartTime = date;

    }

    public String getDashboard(){
        return dashboard;
    }

    public void setDashboard(String structure){
        this.dashboard = structure;
    }

    public long getTrainingSessionCountPerSchedule() {
        return trainingSessionCountPerSchedule;
    }

    public void setTrainingSessionCountPerSchedule(long trainingSessionCountPerSchedule) {
        this.trainingSessionCountPerSchedule = trainingSessionCountPerSchedule;
    }

}