package com.github.chen0040.ml.services;

import com.github.chen0040.ml.models.TopicModel;
import com.github.chen0040.ml.models.TopicModelCompoundKey;
import com.github.chen0040.ml.repositories.TopicModelRepository;

import javax.inject.Inject;

/**
 * Created by root on 11/18/15.
 */
public class TopicModelServiceImpl implements TopicModelService {

    @Inject
    private TopicModelRepository repository;

    @Override
    public boolean save(TopicModel model) {


        TopicModel prevModel = repository.getPrevModelByModuleId(model.getModelKey().getModuleId());
        if(prevModel != null) {
            model.computeRateOfChangeWithPrevInstance(prevModel);
        }

        return repository.save(model);
    }

    @Override
    public boolean delete(TopicModel model) {
        return repository.deleteById(model.getModelKey());
    }

    @Override
    public boolean deleteById(TopicModelCompoundKey modelId){
        return repository.deleteById(modelId);
    }
}
