package com.github.chen0040.ml.services;

import com.github.chen0040.ml.models.TopicModel;
import com.github.chen0040.ml.models.TopicModelCompoundKey;

/**
 * Created by root on 11/18/15.
 */
public interface TopicModelService {
    boolean save(TopicModel model);
    boolean delete(TopicModel model);
    boolean deleteById(TopicModelCompoundKey modelId);
}
