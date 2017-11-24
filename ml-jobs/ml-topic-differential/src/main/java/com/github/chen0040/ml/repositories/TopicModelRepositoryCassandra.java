package com.github.chen0040.ml.repositories;

import com.github.chen0040.ml.models.TopicModel;
import com.github.chen0040.ml.models.TopicModelCompoundKey;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by root on 11/18/15.
 */
public class TopicModelRepositoryCassandra implements TopicModelRepository {

    private CassandraFactory cassandraFactory;

    private ConcurrentHashMap<String, TopicModel> prevModels = new ConcurrentHashMap<>();

    public TopicModelRepositoryCassandra(){
        cassandraFactory = new CassandraFactory();
    }

    @Override
    public boolean save(TopicModel model) {
        prevModels.put(model.getModelKey().getModuleId(), model);
        return cassandraFactory.addCSObject(model, -1);
    }

    @Override
    public TopicModel getPrevModelByModuleId(String moduleId) {
        if(prevModels.containsKey(moduleId)){
            return prevModels.get(moduleId);
        }
        return null;
    }


    @Override
    public boolean deleteById(TopicModelCompoundKey id) {
        cassandraFactory.deleteCSObject(TopicModel.class, id);
        return false;
    }
}
