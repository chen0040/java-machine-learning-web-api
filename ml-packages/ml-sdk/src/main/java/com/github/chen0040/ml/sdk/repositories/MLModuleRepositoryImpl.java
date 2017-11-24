package com.github.chen0040.ml.sdk.repositories;


import com.github.chen0040.ml.commons.AbstractMLModule;
import com.github.chen0040.ml.commons.MLModule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by memeanalytics on 24/8/15.
 */
public class MLModuleRepositoryImpl implements MLModuleRepository {
    private Map<String, MLModule> modules;
    private static final Logger logger = Logger.getLogger(String.valueOf(MLModuleRepositoryImpl.class));

    public MLModuleRepositoryImpl(){
        modules = new ConcurrentHashMap<String, MLModule>();
    }

    public MLModule save(MLModule module) {


        modules.put(module.getId(), module);

        return module;
    }



    private String getId(HashMap<String, Object> module){
        if(module.containsKey("id")){
            return (String)module.get("id");
        }
        return null;
    }

    public MLModule deleteById(String id) {
        return modules.remove(id);
    }

    public MLModule findById(String id) {
        return clone(modules.get(id));
    }

    public Collection<MLModule> findAll() {
        return clone(modules.values());
    }

    public Collection<MLModule> findAllByProjectId(String projectId)
    {
        List<MLModule> result = new ArrayList<MLModule>();
        try {

            for (MLModule module : modules.values()) {
                if (module.getProjectId().equals(projectId)) {
                    result.add(clone(module));
                }
            }
        }catch(Exception ex){
            logger.log(Level.SEVERE, "findAllByProjectId failed", ex);
        }

        return result;
    }

    private MLModule clone(MLModule module){
        return (MLModule)((AbstractMLModule)module).clone();
    }

    private Collection<MLModule> clone(Collection<MLModule> modules2){
        List<MLModule> clones = new ArrayList<MLModule>();
        for(MLModule m : modules2){
            clones.add(clone(m));
        }
        return clones;
    }
}
