package com.github.chen0040.ml.web.tests;

import com.github.chen0040.ml.commons.tables.DataTable;
import com.github.chen0040.ml.sdk.models.MLProject;
import com.github.chen0040.ml.sdk.modules.SdkServiceMediatorImpl;
import com.github.chen0040.ml.web.spring.WebApplication;
import com.github.chen0040.ml.web.tests.utils.FileUtils;
import com.github.chen0040.ml.commons.tables.DataRow;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by memeanalytics on 25/8/15.
 */
public class DataResourceTest extends JerseyTest {
    public static final String BATCHES = "batches";
    private String project1_id;
    private String project2_id;
    private String batch1_id;
    private String batch2_id;

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

    protected static Response createBatch(JerseyTest test, String projectId){

        MLProjectElementCreateCommand cmd = new MLProjectElementCreateCommand();
        cmd.setProjectId(projectId);
        cmd.setPrototype("batch");

        Entity<MLProjectElementCreateCommand> cmdEntity = Entity.entity(cmd, MediaType.APPLICATION_JSON_TYPE);

        Response response = test.target(BATCHES).path("factory").request().post(cmdEntity);

        return response;
    }

    @Before
    public void setupProjects(){
        project1_id = ProjectResourceTest.addProject(this, "title1", "author1", "editor1", new Date(), "description 1").readEntity(MLProject.class).getId();
        project2_id = ProjectResourceTest.addProject(this, "title2", "author2", "editor2", new Date(), "description 2").readEntity(MLProject.class).getId();
        batch1_id = addBatch(this, project1_id);
        batch2_id = addBatch(this, project2_id);
    }



    protected HashMap<String, Object> toHashMap(Response response){
        return response.readEntity(new GenericType<HashMap<String, Object>>(){});
    }

    protected Collection<HashMap<String, Object>> toHashMaps(Response response){
        return response.readEntity(new GenericType<Collection<HashMap<String, Object>>>(){});
    }

    public static String addBatch(JerseyTest test, String project_id){
        Response response = createBatch(test, project_id);

        assertEquals(200, response.getStatus());

        HashMap<String, Object> newInstance = response.readEntity(new GenericType<HashMap<String, Object>>() {
        });

        assertNotNull(newInstance);


        Entity<HashMap<String, Object>> moduleEntity = Entity.entity(newInstance, MediaType.APPLICATION_JSON_TYPE);

        Response response2 = test.target(BATCHES).request().post(moduleEntity);


        assertEquals(200, response2.getStatus());

        HashMap<String, Object> persistedInstance = response2.readEntity(new GenericType<HashMap<String, Object>>() {
        });

        assertNotNull(persistedInstance);

        return (String)persistedInstance.get("id");
    }

    @Test
    public void testAddBatch(){
        addBatch(this, project1_id);
        addBatch(this, project2_id);
    }

    @Test
    public void testAddTable(){
        DataTable table = new DataTable();
        table.setName("test");
        table.setProjectId(project1_id);

        table.columns().add("c1");
        table.columns().add("c2");

        DataRow row = table.newRow();
        row.cell("c1", 10);
        row.cell("c2", 20);

        table.rows().add(row);

        Entity<DataTable> cmdEntity = Entity.entity(table, MediaType.APPLICATION_JSON_TYPE);

        Response response = target(BATCHES).path("table").request().post(cmdEntity);

        String content = response.readEntity(String.class);

        System.out.println(content);

    }

    @Test
    public void testGetBatches(){
        Collection<HashMap<String, Object>> response = toHashMaps(target(BATCHES).request().get());
        assertEquals(2, response.size());
    }

    @Test
    public void testGetBatchesByProject(){
        Collection<HashMap<String, Object>> response1 = toHashMaps(target(BATCHES).path("project").queryParam("projectId", project1_id).request().get());
        assertEquals(1, response1.size());

        Collection<HashMap<String, Object>> response2 = toHashMaps(target(BATCHES).path("project").queryParam("projectId", project2_id).request().get());
        assertEquals(1, response2.size());
    }

    @Test
    public void testDeleteBatch(){
        HashMap<String, Object> response = toHashMap(target(BATCHES).path(batch1_id).request().delete());
        assertNotNull(response);

    }

    @Test
    public void testUploadCSV_v1(){
        final FileDataBodyPart filePart = new FileDataBodyPart("input_data", FileUtils.getResourceFile("heart_scale"));

        final MultiPart multipart = new FormDataMultiPart()
                .field("projectId", project1_id)
                .field("version", "CSV_HEART_SCALE")
                .field("hasHeader", "0")
                .field("output_column_label", "0")
                .bodyPart(filePart);

        HashMap<String, Object> response = toHashMap(target(BATCHES).path("csv").request()
                .post(Entity.entity(multipart, multipart.getMediaType())));

        assertNotNull(response);
    }

    @Test
    public void testUploadCSV_v1_WithUpdate(){
        final FileDataBodyPart filePart = new FileDataBodyPart("input_data", FileUtils.getResourceFile("heart_scale"));

        final MultiPart multipart = new FormDataMultiPart()
                .field("projectId", project1_id)
                .field("version", "CSV_HEART_SCALE")
                .field("hasHeader", "0")
                .field("output_column_label", "0")
                .bodyPart(filePart);

        HashMap<String, Object> response = toHashMap(target(BATCHES).path("csv").request()
                .post(Entity.entity(multipart, multipart.getMediaType())));

        assertNotNull(response);

        response.put("title", "Title changed");
        response.put("description", "Description changed");


        HashMap<String, Object> response2 = toHashMap(target(BATCHES).request().post(Entity.entity(response, MediaType.APPLICATION_JSON_TYPE)));

        assertNotNull(response2);

        assertEquals("Title changed", response2.get("title"));
        assertEquals("Description changed", response2.get("description"));


    }

    @Test
    public void testUploadCSV(){
        final FileDataBodyPart filePart = new FileDataBodyPart("input_data", FileUtils.getResourceFile("X1.txt"));

        final MultiPart multipart = new FormDataMultiPart()
                .field("projectId", project1_id)
                .field("version", "CSV_DATA_TABLE")
                .field("splitBy", " ")
                .field("hasHeader", "0")
                .field("output_column_label", "-1")
                .field("output_column_numeric", "-1")
                .bodyPart(filePart);

        HashMap<String, Object> response = toHashMap(target(BATCHES).path("csv").request()
                .post(Entity.entity(multipart, multipart.getMediaType())));

        assertNotNull(response);
    }

    @Test
    public void testGetBatch(){
        HashMap<String, Object> response = toHashMap(target(BATCHES).path(batch1_id).request().get());
        assertNotNull(response);
    }

    @Test
    public void testRepository(){
        HashMap<String, Object> response1 = toHashMap(target(BATCHES).path(batch1_id).request().get());
        HashMap<String, Object> response2 = toHashMap(target(BATCHES).path(batch1_id).request().get());

        assertEquals(response1.get("id"), response2.get("id"));
    }
}
