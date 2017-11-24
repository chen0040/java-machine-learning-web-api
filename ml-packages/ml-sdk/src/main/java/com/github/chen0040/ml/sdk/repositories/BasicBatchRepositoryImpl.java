package com.github.chen0040.ml.sdk.repositories;

import com.github.chen0040.ml.commons.IntelliContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by memeanalytics on 25/8/15.
 */
public class BasicBatchRepositoryImpl implements BasicBatchRepository {
    private Map<String, IntelliContext> batches = null;

    public BasicBatchRepositoryImpl(){
        batches = new ConcurrentHashMap<String, IntelliContext>();
    }

    public IntelliContext findById(String id) {
        if(batches.containsKey(id)) {
            return clone(batches.get(id));
        } else {
            return null;
        }
    }

    public IntelliContext deleteById(String id) {
        return batches.remove(id);
    }

    public Collection<IntelliContext> findAll() {
        return clone(batches.values());
    }

    public Collection<IntelliContext> findAllByProjectId(String projectId)
    {
        List<IntelliContext> result = new ArrayList<IntelliContext>();
        for(IntelliContext batch : batches.values()){
            if(batch.getProjectId().equals(projectId)) {
                result.add(clone(batch));
            }
        }
        return result;
    }

    private IntelliContext clone(IntelliContext batch){
        return (IntelliContext)batch.clone();
    }

    public Collection<IntelliContext> clone(Collection<IntelliContext> batches2){
        List<IntelliContext> batches3 = new ArrayList<IntelliContext>();
        for(IntelliContext batch : batches2){
            batches3.add(clone(batch));
        }
        return batches3;
    }

    public IntelliContext save(IntelliContext batch) {

        batches.put(batch.getId(), batch);

        return batch;
    }
}
