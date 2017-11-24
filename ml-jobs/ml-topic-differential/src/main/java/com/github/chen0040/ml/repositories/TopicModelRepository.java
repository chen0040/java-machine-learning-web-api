package com.github.chen0040.ml.repositories;

import com.github.chen0040.ml.models.TopicModelCompoundKey;
import com.github.chen0040.ml.models.TopicModel;

/**
 * Created by root on 11/18/15.
 */
public interface TopicModelRepository {
    boolean deleteById(TopicModelCompoundKey id);
    boolean save(TopicModel model);
    TopicModel getPrevModelByModuleId(String moduleId);
}
