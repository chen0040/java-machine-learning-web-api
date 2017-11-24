package com.github.chen0040.ml.webclient.jersey;

import com.github.chen0040.ml.commons.tables.DataTable;
import com.github.chen0040.ml.commons.commands.MLProjectElementCreateCommand;
import com.github.chen0040.ml.commons.tables.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by xschen on 10/7/2016.
 */
public class IntelliSession extends BasicSession {

    private static Logger logger = LoggerFactory.getLogger(IntelliSession.class);

    public static final String PROJECTS = "projects";
    public static final String BATCHES = "batches";
    public static final String MLMODULES = "mlmodules";

    private static final String namespace = "ml";



    private String host = "localhost:8900";
    private String projectId = "";

    private String owner = "chen0040";
    private String token = "--x--";

    private Map<String, String> modules = new HashMap<>();

    private Set<String> tables = new HashSet<>();

    public IntelliSession(String host, String projectName) {
        this(host, projectName, null);
    }

    public IntelliSession(String host, String projectName, Client client) {
        super(host.concat("/").concat(namespace), client);

        this.host = host;
        this.token = UUID.randomUUID().toString();

        HashMap<String, Object> response = toHashMap(target(PROJECTS).path("search").path(projectName).request().get());

        if(response != null && response.containsKey("id")){
            this.projectId = (String)response.get("id");
        } else {
            String projectId = addProject(projectName, owner, owner, new Date(), token);
            this.projectId = projectId;
        }
    }

    private String addProject(String title, String author, String editor, Date created, String description){
        HashMap<String, Object> project = new HashMap<>();
        project.put("title", title);
        project.put("createdBy", author);
        project.put("updatedBy", editor);
        project.put("created", created);
        project.put("updated", created);
        project.put("description", description);

        Entity<HashMap<String, Object>> projectEntity = Entity.entity(project, MediaType.APPLICATION_JSON_TYPE);

        Response response = target(PROJECTS).request().post(projectEntity);

        return (String)toHashMap(response).get("id");
    }

    private String addModule(String name, String prototype){

        MLProjectElementCreateCommand cmd = new MLProjectElementCreateCommand();
        cmd.setProjectId(projectId);
        cmd.setPrototype(prototype);
        cmd.setTitle(name);
        cmd.setDescription(name + ":" + prototype);
        cmd.setId(name);

        Entity<MLProjectElementCreateCommand> cmdEntity = Entity.entity(cmd, MediaType.APPLICATION_JSON_TYPE);

        Response response = target(MLMODULES).path("factory").request().post(cmdEntity);

        HashMap<String, Object> newInstance = response.readEntity(new GenericType<HashMap<String, Object>>() {
        });

        return (String)newInstance.get("id");
    }

    private IntelliSession buildTable(DataTable table){

        String id = table.getId();
        table.setProjectId(projectId);

        if(!tables.contains(id)) {
            Entity<DataTable> cmdEntity = Entity.entity(table, MediaType.APPLICATION_JSON_TYPE);
            Response response = target(BATCHES).path("table").request().post(cmdEntity);

            tables.add(id);
        }

        return this;
    }

    public IntelliSession trainNBC(String name, DataTable table) {
        return train(name, table, "com.github.chen0040.ml.bayes.nbc.NBC");
    }


    public IntelliSession trainSVR(String name, DataTable table){
        return train(name, table, "com.github.chen0040.ml.svm.regression.SVR");
    }

    public IntelliSession trainGLM(String name, DataTable table) {
        return train(name, table, "com.github.chen0040.ml.glm.solvers.GlmSolver");
    }

    public IntelliSession trainKMeans(String name, DataTable table){
        return train(name, table, "com.github.chen0040.ml.clustering.kmeans.KMeans");
    }

    public IntelliSession trainIsolationForest(String name, DataTable table){
        return train(name, table, "com.github.chen0040.ml.trees.iforest.IsolationForest");
    }

    private IntelliSession train(String name, DataTable table, String prototype) {
        buildTable(table);


        modules.put(name, addModule(name, prototype));

        train(name, table.getId());

        return this;
    }



    private void train(String moduleId, String batchId){
        HashMap<String, Object> response2 = toHashMap(target(MLMODULES).path("batch-update")
                .queryParam("moduleId", moduleId)
                .queryParam("batchId", batchId)
                .request().get());
    }

    public DataRow predict(String name, DataRow row){

        Entity<DataRow> tupleEntity = Entity.entity(row, MediaType.APPLICATION_JSON_TYPE);
        return target(MLMODULES).path("predict-row").queryParam("moduleId", name)
                .request().post(tupleEntity).readEntity(DataRow.class);
    }




}
