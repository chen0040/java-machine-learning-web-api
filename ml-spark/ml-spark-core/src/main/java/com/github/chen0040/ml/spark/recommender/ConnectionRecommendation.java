package com.github.chen0040.ml.spark.recommender;

import com.github.chen0040.ml.spark.core.BatchUpdateResult;
import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.core.AbstractSparkMLModule;
import com.github.chen0040.ml.spark.core.SparkMLModule;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import scala.Tuple2;

import java.util.*;

/**
 * Created by root on 9/14/15.
 * This algorithm identifiy for each user A, who he should connect to, in terms of the number of their commonly shared friends
 */
public class ConnectionRecommendation extends AbstractSparkMLModule {

    private List<ConnectionRecommendationModel> model = new ArrayList<ConnectionRecommendationModel>();

    public List<ConnectionRecommendationModel> getModel() {
        return model;
    }

    public void setModel(List<ConnectionRecommendationModel> model) {
        this.model = model;
    }

    @Override
    public Object clone(){
        ConnectionRecommendation clone = new ConnectionRecommendation();
        clone.copy(this);

        return clone;
    }

    @Override
    public void copy(SparkMLModule module){
        super.copy(module);
        model.clear();

        ConnectionRecommendation rhs = (ConnectionRecommendation)module;
        for(int i=0; i < rhs.model.size(); ++i){
            model.add((ConnectionRecommendationModel)rhs.model.get(i).clone());
        }
    }

    @Override
    public BatchUpdateResult batchUpdate(JavaRDD<SparkMLTuple> batch){
        JavaPairRDD<String, Tuple2<String, String>> rdd = batch.flatMapToPair(new PairFlatMapFunction<SparkMLTuple, String, Tuple2<String, String>>() {
            public Iterable<Tuple2<String, Tuple2<String, String>>> call(SparkMLTuple s) throws Exception {
                List<Tuple2<String, Tuple2<String, String>>> result = new ArrayList<Tuple2<String, Tuple2<String, String>>>();

                List<String> friends = s.toBagOfWords();
                String user = s.getLabelOutput();
                for (int i = 0; i < friends.size(); ++i) {
                    Tuple2<String, String> fpair = new Tuple2<String, String>(friends.get(i), null);
                    result.add(new Tuple2<String, Tuple2<String, String>>(user, fpair));
                }

                for (int i = 0; i < friends.size() - 1; ++i) {
                    String friend_i = friends.get(i);
                    for (int j = i + 1; j < friends.size(); ++j) {
                        String friend_j = friends.get(j);

                        //both i and j are friends to user

                        Tuple2<String, String> potential_fpair1 = new Tuple2<String, String>(friend_j, user);
                        result.add(new Tuple2<String, Tuple2<String, String>>(friend_i, potential_fpair1));

                        Tuple2<String, String> potential_fpair2 = new Tuple2<String, String>(friend_i, user);
                        result.add(new Tuple2<String, Tuple2<String, String>>(friend_j, potential_fpair2));
                    }
                }

                return result;
            }
        });

        JavaPairRDD<String, Iterable<Tuple2<String, String>>> rdd2 = rdd.groupByKey();

        JavaPairRDD<String, HashMap<String, HashSet<String>>> rdd3 = rdd2.mapValues(new Function<Iterable<Tuple2<String, String>>, HashMap<String, HashSet<String>>>() {
            public HashMap<String, HashSet<String>> call(Iterable<Tuple2<String, String>> s) throws Exception {
                HashMap<String, HashSet<String>> potential_connections = new HashMap<String, HashSet<String>>();
                for (Tuple2<String, String> t : s) {
                    String toUser = t._1();
                    String commonFriend = t._2();
                    boolean already_friend = (commonFriend == null);

                    if (potential_connections.containsKey(toUser)) {
                        if (already_friend) {
                            potential_connections.put(toUser, null); //
                        } else if (potential_connections.get(toUser) != null) {
                            potential_connections.get(toUser).add(commonFriend);
                        }
                    } else {
                        if (already_friend) {
                            potential_connections.put(toUser, null); //user and toUser already friend so no connection to build again
                        } else {
                            HashSet<String> commonFriends = new HashSet<>();
                            commonFriends.add(commonFriend);
                            potential_connections.put(toUser, commonFriends);
                        }
                    }
                }
                return potential_connections;
            }
        });

        JavaPairRDD<Tuple2<String, String>, HashSet<String>> rdd4 = rdd3.flatMapToPair(new PairFlatMapFunction<Tuple2<String, HashMap<String, HashSet<String>>>, Tuple2<String, String>, HashSet<String>>() {
            public Iterable<Tuple2<Tuple2<String, String>, HashSet<String>>> call(Tuple2<String, HashMap<String, HashSet<String>>> s) throws Exception {
                String friend1 = s._1();
                List<Tuple2<Tuple2<String, String>, HashSet<String>>> result = new ArrayList<Tuple2<Tuple2<String, String>, HashSet<String>>>();

                for (Map.Entry<String, HashSet<String>> entry : s._2().entrySet()) {
                    String friend2 = entry.getKey();
                    HashSet<String> commonFriends = entry.getValue();
                    if(commonFriends == null) continue;

                    Tuple2<String, String> connection = new Tuple2<String, String>(friend1, friend2);
                    result.add(new Tuple2<Tuple2<String, String>, HashSet<String>>(connection, commonFriends));
                }

                return result;
            }
        });

        JavaPairRDD<Tuple2<String, String>, HashSet<String>> rdd5 = rdd4.filter(new Function<Tuple2<Tuple2<String, String>, HashSet<String>>, Boolean>() {
            public Boolean call(Tuple2<Tuple2<String, String>, HashSet<String>> s) throws Exception {
                Tuple2<String, String> connection = s._1();
                String friend1 = connection._1();
                String friend2 = connection._2();

                return friend1.compareTo(friend2) == -1;
            }
        });

        List<Tuple2<Tuple2<String, String>, HashSet<String>>> output = rdd5.collect();

        model.clear();
        for(int i=0; i < output.size(); ++i){
            Tuple2<Tuple2<String, String>, HashSet<String>> tuple = output.get(i);
            ConnectionRecommendationModel c = new ConnectionRecommendationModel();
            c.friend1 = tuple._1()._1();
            c.friend2 = tuple._1()._2();
            c.commonFriends = tuple._2();
            model.add(c);
        }


        return new BatchUpdateResult();
    }
}
