package com.github.chen0040.ml.repositories;


import com.github.chen0040.ml.models.AlgorithmModule;

import java.util.List;

/**
 * Created by root on 11/11/15.
 */
public interface AlgorithmModuleRepository {
    AlgorithmModule findById(String moduleId);
    List<AlgorithmModule> findAllByCompanyId(String companyId);
    List<AlgorithmModule> findAll();
}
