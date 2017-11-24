package com.github.chen0040.ml.sdk.repositories;

import com.github.chen0040.ml.sdk.models.MLProject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by memeanalytics on 24/8/15.
 */
public class MLProjectRepositoryImpl implements MLProjectRepository {
    private Map<String, MLProject> projects;

    public MLProjectRepositoryImpl(){

        projects = new ConcurrentHashMap<String, MLProject>();

        /*
        projects = new HashMap<>();

        MLProject project1 = new MLProject();
        project1.setId("1");
        project1.setTitle("Title1");
        project1.setCreatedBy("Author1");
        project1.setUpdatedBy("Editor1");
        project1.setCreated(new Date());
        project1.setUpdated(new Date());
        project1.setDescription("Description 1");

        MLProject project2 = new MLProject();
        project2.setId("2");
        project2.setTitle("Title2");
        project2.setCreatedBy("Author2");
        project2.setUpdatedBy("Editor2");
        project2.setCreated(new Date());
        project2.setUpdated(new Date());
        project2.setDescription("Description 2");

        projects.put(project1.getId(), project1);
        projects.put(project2.getId(), project2);*/
    }

    public Collection<MLProject> findAll(){
        return clone(projects.values());
    }

    public Collection<MLProject> clone(Collection<MLProject> projects){
        List<MLProject> projs = new ArrayList<MLProject>();
        for(MLProject proj : projects){
            projs.add(proj.clone());
        }
        return projs;
    }

    public MLProject findById(String id){
        MLProject project = projects.get(id);
        if(project != null){
            project = project.clone();
        }
        return project;
    }

    public MLProject findByTitle(String title){
        for(MLProject project : projects.values()){
            if(project.getTitle().equals(title)){
                return project;
            }
        }

        return null;
    }

    public MLProject save(MLProject project) {
        if(project.getId()==null) {
            project.setId(UUID.randomUUID().toString());
        }
        if(project.getCreated()==null){
            project.setCreated(new Date());
        }
        else {
            project.setUpdated(new Date());
        }

        projects.put(project.getId(), project);
        return project;
    }

    public MLProject deleteById(String id) {
        return projects.remove(id);
    }
}
