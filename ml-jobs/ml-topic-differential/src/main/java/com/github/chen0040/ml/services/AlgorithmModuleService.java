package com.github.chen0040.ml.services;


import com.github.chen0040.ml.models.AlgorithmModule;

import java.util.List;

/**
 * Created by root on 11/11/15.
 */
public interface AlgorithmModuleService {
    AlgorithmModule findById(String moduleId);
    List<AlgorithmModule> findAll();
}
