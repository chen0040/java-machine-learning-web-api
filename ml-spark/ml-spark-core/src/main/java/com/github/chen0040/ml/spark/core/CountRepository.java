package com.github.chen0040.ml.spark.core;

import java.io.Serializable;
import java.util.*;

/**
 * Created by memeanalytics on 18/8/15.
 */
public class CountRepository implements Cloneable,Serializable {
    private Map<String, CountRepository> chain = new TreeMap<String, CountRepository>();
    private String eventName;
    private double supportCount ;

    public void copy(CountRepository rhs){
        supportCount = rhs.supportCount;
        eventName = rhs.eventName;
        chain.clear();

        for(String key : rhs.chain.keySet()){
            chain.put(key, (CountRepository) rhs.chain.get(key).clone());
        }

    }

    @Override
    public Object clone(){
        CountRepository clone = new CountRepository();
        clone.copy(this);

        return clone;
    }

    public CountRepository(){
        supportCount = 0;
    }

    public CountRepository(String evt){
        this.eventName = evt;
    }

    public String getEventName(){
        return eventName;
    }

    public double getSupportCount(){
        return supportCount;
    }

    public Map<String, CountRepository> getChain(){
        return chain;
    }

    public List<String> getSubEventNames(String... eventNames){
        return getSubEventNames(this, eventNames);
    }

    private List<String> getSubEventNames(CountRepository repo, String... eventNames){
        int eventNameCount = eventNames.length;
        if(eventNameCount == 0){
            List<String> events = new ArrayList<>();
            for(String eventName : repo.chain.keySet()){
                events.add(eventName);
            }
            return events;
        }

        if(repo.chain.containsKey(eventNames[0])) {
            repo = repo.chain.get(eventNames[0]);

            if (eventNameCount == 1) {
                return getSubEventNames(repo);
            } else {
                String[] subevents = new String[eventNameCount - 1];
                for (int j = 1; j < eventNameCount; ++j) {
                    subevents[j - 1] = eventNames[j];
                }
                return getSubEventNames(repo, subevents);
            }
        }else{
            return new ArrayList<>();
        }
    }

    public void addSupportCount(String... events){
        addSupportCount(1, events);
    }

    public void addSupportCount(double increment, String... events){
        int eventCount = events.length;
        if(eventCount==0){
            supportCount+=increment;
        }else {
            String evt = events[0];
            CountRepository c;
            if (chain.containsKey(evt)) {
                c = chain.get(evt);
            } else {
                c = new CountRepository(evt);
                chain.put(evt, c);
            }
            if (eventCount == 1) {
                c.addSupportCount(increment);
            } else {
                String[] subevents = new String[eventCount - 1];
                for (int j = 1; j < eventCount; ++j) {
                    subevents[j - 1] = events[j];
                }
                c.addSupportCount(increment, subevents);
            }
        }
    }

    public double getProbability(String eventName){
        if(supportCount==0) return 0;
        double count = getSupportCount(eventName);
        return count / supportCount;
    }

    // return the conditional probability of B given A happened
    public double getConditionalProbability(String eventA, String eventB){
        double givenCount = getSupportCount(eventA);
        if(givenCount == 0) return 0;
        return getSupportCount(eventA, eventB) / givenCount;

    }

    public double getSupportCount(String... events){
        int eventCount = events.length;
        if(eventCount == 0){
            return supportCount;
        }
        else{
            String evt = events[0];
            CountRepository c = null;
            if(chain.containsKey(evt)){
                c = chain.get(evt);
            }

            if(c == null) return 0;

            if(eventCount == 1){
                return c.getSupportCount();
            }else{
                String[] subevents = new String[eventCount-1];
                for(int j=1; j < eventCount; ++j){
                    subevents[j-1] = events[j];
                }
                return c.getSupportCount(subevents);
            }
        }
    }
}
