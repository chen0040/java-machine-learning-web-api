package com.github.chen0040.ml.services;


import com.github.chen0040.ml.models.AlgorithmModule;
import com.github.chen0040.ml.repositories.AlgorithmModuleRepository;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by root on 11/11/15.
 */
public class AlgorithmModuleServiceImpl implements AlgorithmModuleService {

    @Inject
    private AlgorithmModuleRepository repository;

    @Override
    public AlgorithmModule findById(String moduleId) {
        return repository.findById(moduleId);
    }

    @Override
    public List<AlgorithmModule> findAll() {
        return repository.findAll();
    }
}
