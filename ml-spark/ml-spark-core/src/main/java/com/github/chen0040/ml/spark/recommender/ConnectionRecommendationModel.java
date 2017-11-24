package com.github.chen0040.ml.spark.recommender;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by memeanalytics on 14/9/15.
 */
public class ConnectionRecommendationModel implements Serializable, Cloneable {
    public String friend1;
    public String friend2;
    public HashSet<String> commonFriends;

    @Override
    public Object clone(){
        ConnectionRecommendationModel clone = new ConnectionRecommendationModel();
        clone.friend1 = friend1;
        clone.friend2 = friend2;
        if(this.commonFriends != null){
            clone.commonFriends = new HashSet<>();
            for(String f : commonFriends){
                clone.commonFriends.add(f);
            }
        }
        return clone;

    }
}
