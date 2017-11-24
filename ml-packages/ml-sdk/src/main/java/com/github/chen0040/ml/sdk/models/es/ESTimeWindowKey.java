package com.github.chen0040.ml.sdk.models.es;

/**
 * Created by root on 9/7/15.
 */
public class ESTimeWindowKey implements Comparable {
    private long startTime;
    private long endTime;

    public ESTimeWindowKey(long startTime, long endTime){
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString(){
        return String.format("%d-%d");
    }

    @Override
    public int hashCode(){
        return Long.hashCode(startTime) * 31 + Long.hashCode(endTime);
    }

    @Override
    public boolean equals(Object rhs){
        if(rhs instanceof ESTimeWindowKey){
            ESTimeWindowKey rhs2 = (ESTimeWindowKey)rhs;
            return rhs2.startTime == startTime && rhs2.endTime == endTime;
        }
        return false;
    }

    public long getStartTime(){
        return startTime;
    }

    public long getEndTime(){
        return endTime;
    }

    public int compareTo(Object o) {
        if(o instanceof ESTimeWindowKey){
            ESTimeWindowKey rhs = (ESTimeWindowKey)o;
            return Long.compare(this.startTime, rhs.startTime);
        }
        return -1;
    }
}
