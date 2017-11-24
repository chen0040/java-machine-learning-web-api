package com.github.chen0040.ml.web.tests;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.bayes.nbc.NBC;
import com.github.chen0040.ml.commons.MLModule;
import com.github.chen0040.ml.commons.tables.DataColumnCollection;
import com.github.chen0040.ml.sdk.models.MLProject;
import com.github.chen0040.ml.sdk.modules.SdkServiceMediatorImpl;
import com.github.chen0040.ml.sdk.services.MLModuleFactory;
import com.github.chen0040.ml.web.spring.WebApplication;
import com.github.chen0040.ml.web.tests.utils.FileUtils;
import com.github.chen0040.ml.commons.tables.DataRow;
import com.github.chen0040.ml.sdk.models.Instance;
import com.github.chen0040.ml.commons.commands.MLProjectElementCreateCommand;
import com.github.chen0040.ml.web.jersey.MLResourceConfig;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by memeanalytics on 24/8/15.
 */
public class AlgorithmResourceTest extends JerseyTest {

    public static final String MLMODULES = "mlmodules";
    private String project1_id;
    private String project2_id;
    private String nbc1_id;
    private String nbc2_id;

    @Override
    protected Application configure(){
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        ResourceConfig rc = new MLResourceConfig(new SdkServiceMediatorImpl());


        rc.property("contextConfig", new AnnotationConfigApplicationContext(WebApplication.class));
        //rc.register(SpringLifecycleListener.class).register(RequestContextFilter.class);
        return rc;
    }



    @Override
    protected void configureClient(ClientConfig config) {
        config.register(MultiPartFeature.class);
    }

    protected Response createModule(String projectId, String prototype, String title, String description){

        MLProjectElementCreateCommand cmd = new MLProjectElementCreateCommand();
        cmd.setProjectId(projectId);
        cmd.setPrototype(prototype);
        cmd.setTitle(title);
        cmd.setDescription(description);

        Entity<MLProjectElementCreateCommand> cmdEntity = Entity.entity(cmd, MediaType.APPLICATION_JSON_TYPE);

        Response response = target(MLMODULES).path("factory").request().post(cmdEntity);

        return response;
    }

    @Before
    public void setupProjects(){
        project1_id = ProjectResourceTest.addProject(this, "title1", "author1", "editor1", new Date(), "description 1").readEntity(MLProject.class).getId();
        project2_id = ProjectResourceTest.addProject(this, "title2", "author2", "editor2", new Date(), "description 2").readEntity(MLProject.class).getId();
        nbc1_id = addModule(project1_id, NBC.class.getCanonicalName());
        nbc2_id = addModule(project2_id, NBC.class.getCanonicalName());
    }

    @Test
    public void testAttributeValueDiscretizerDeserialization(){
        String prototype = NBC.class.getCanonicalName();
        Response response = createModule(project1_id, prototype, "NBC", "NBC Description");
        HashMap<String, Object> hmap = toHashMap(response);

        String json1 = JSON.toJSONString(hmap, SerializerFeature.BrowserCompatible);

        assertNotNull(json1);

        MLModule module = MLModuleFactory.getInstance().convert(hmap);
        NBC nbc = (NBC)module;
        assertNotNull(nbc);

        String json2 = JSON.toJSONString(hmap, SerializerFeature.BrowserCompatible);

        assertNotNull(json2);

        System.out.println("json1: ");
        System.out.println(json1);
        System.out.println("json2: ");
        System.out.println(json2);
    }

    protected HashMap<String, Object> toHashMap(Response response){
        return response.readEntity(new GenericType<HashMap<String, Object>>(){});
    }

    protected Collection<HashMap<String, Object>> toHashMaps(Response response){
        return response.readEntity(new GenericType<Collection<HashMap<String, Object>>>(){});
    }

    private String addModule(String project_id, String prototype){
        Response response = createModule(project_id, prototype, "NBC", "NBC description");

        System.out.println(prototype);

        assertEquals(200, response.getStatus());

        HashMap<String, Object> newInstance = response.readEntity(new GenericType<HashMap<String, Object>>() {
        });

        assertNotNull(newInstance);




        return (String)newInstance.get("id");
    }

    @Test
    public void testAddModules(){
        Collection<String> prototypes = MLModuleFactory.getInstance().getAllPrototypes();
        for(String prototype : prototypes) {
            addModule(project1_id, prototype);
            addModule(project2_id, prototype);
        }
    }

    @Test
    public void testDeleteModule(){
        HashMap<String, Object> response = toHashMap(target(MLMODULES).path(nbc1_id).request().delete());
        assertNotNull(response);

    }

    @Test
    public void testGetModules(){
        Collection<HashMap<String, Object>> response = toHashMaps(target(MLMODULES).request().get());
        assertEquals(2, response.size());
    }

    private String uploadClassifierTrainingFile(String project_id){
        final FileDataBodyPart filePart = new FileDataBodyPart("input_data", FileUtils.getResourceFile("heart_scale"));

        final MultiPart multipart = new FormDataMultiPart()
                .field("projectId", project_id)
                .field("version", "CSV_HEART_SCALE")
                .field("hasHeader", "0")
                .bodyPart(filePart);

        HashMap<String, Object> response = toHashMap(target(DataResourceTest.BATCHES).path("csv").request()
                .post(Entity.entity(multipart, multipart.getMediaType())));

        assertNotNull(response);

        return (String)response.get("id");
    }

    private static double atof(String s)
    {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return(d);
    }

    private static int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    private List<Instance> loadClassifierTrainingFile() throws IOException {
        List<Instance> instances = new ArrayList<>();

        BufferedReader fp = new BufferedReader(new InputStreamReader(new FileInputStream(FileUtils.getResourceFile("heart_scale"))));

        DataColumnCollection columns = new DataColumnCollection();

        while (true) {
            String line = fp.readLine();
            if (line == null) break;

            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            String label = st.nextToken();

            int m = st.countTokens() / 2;



            Map<String, String> row = new HashMap<>();

            for (int j = 0; j < m; j++) {
                int index = atoi(st.nextToken()) - 1;
                String columnName = "c" + index;
                row.put(columnName, st.nextToken());

                if(columns.indexOf(columnName) == -1) {
                    columns.add(columnName);
                }
            }

            Instance instance = new Instance(columns);
            instance.setLabel(label);

            for(String columnName : row.keySet()){
                instance.set(columnName, row.get(columnName));
            }

            instances.add(instance);
        }

        fp.close();



        return instances;
    }

    @Test
    public void testNBCTrain(){
        String batch_id = uploadClassifierTrainingFile(project1_id);

        String module_id = addModule(project1_id, NBC.class.getCanonicalName());

        HashMap<String, Object> response2 = toHashMap(target(MLMODULES).path("batch-update")
                .queryParam("moduleId", module_id)
                .queryParam("batchId", batch_id)
                .request().get());

        assertNotNull(response2);
    }


    @Test
    public void testNBCPredict() throws IOException {
        String batch_id = uploadClassifierTrainingFile(project1_id);
        String module_id = addModule(project1_id, NBC.class.getCanonicalName());

        HashMap<String, Object> response2 = toHashMap(target(MLMODULES).path("batch-update")
                .queryParam("moduleId", module_id)
                .queryParam("batchId", batch_id)
                .request().get());

        assertNotNull(response2);

        List<Instance> instances = loadClassifierTrainingFile();

        for(Instance instance : instances){
            Entity<Map<Integer, String>> tupleEntity = Entity.entity(instance.getData(), MediaType.APPLICATION_JSON_TYPE);
            target(MLMODULES).path("match").queryParam("moduleId", module_id)
                    .request().post(tupleEntity);
        }
    }

    @Test
    public void testNBCPredictDataRow() throws IOException {
        String batch_id = uploadClassifierTrainingFile(project1_id);
        String module_id = addModule(project1_id, NBC.class.getCanonicalName());

        HashMap<String, Object> response2 = toHashMap(target(MLMODULES).path("batch-update")
                .queryParam("moduleId", module_id)
                .queryParam("batchId", batch_id)
                .request().get());

        assertNotNull(response2);

        List<Instance> instances = loadClassifierTrainingFile();

        for(Instance instance : instances){

            Entity<DataRow> tupleEntity = Entity.entity(instance.toDataRow(), MediaType.APPLICATION_JSON_TYPE);
            target(MLMODULES).path("predict-row").queryParam("moduleId", module_id)
                    .request().post(tupleEntity);
        }
    }


    @Test
    public void testGetModulesByProject(){
        Collection<HashMap<String, Object>> response1 = toHashMaps(target(MLMODULES).path("project").queryParam("projectId", project1_id).request().get());
        assertEquals(1, response1.size());

        Collection<HashMap<String, Object>> response2 = toHashMaps(target(MLMODULES).path("project").queryParam("projectId", project2_id).request().get());
        assertEquals(1, response2.size());
    }


    @Test
    public void testGetModule(){
        HashMap<String, Object> response = toHashMap(target(MLMODULES).path(nbc1_id).request().get());
        assertNotNull(response);
    }

    @Test
    public void testRepository(){
        HashMap<String, Object> response1 = toHashMap(target(MLMODULES).path(nbc1_id).request().get());
        HashMap<String, Object> response2 = toHashMap(target(MLMODULES).path(nbc1_id).request().get());

        assertEquals(response1.get("id"), response2.get("id"));
    }
}
