package com.github.chen0040.ml.web.tests;

import com.github.chen0040.ml.sdk.modules.SdkServiceMediatorImpl;
import com.github.chen0040.ml.web.jersey.MLResourceConfig;
import com.github.chen0040.ml.web.spring.WebApplication;
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
 * Created by memeanalytics on 24/8/15.
 */
public class ProjectResourceTest extends JerseyTest {

    public static final String PROJECTS = "projects";
    private String project1_id;
    private String project2_id;

    @Override
    protected Application configure(){
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        ResourceConfig rc = new MLResourceConfig(new SdkServiceMediatorImpl());


        rc.property("contextConfig", new AnnotationConfigApplicationContext(WebApplication.class));
        //rc.register(SpringLifecycleListener.class).register(RequestContextFilter.class);
        return rc;
    }

    public static Response addProject(JerseyTest test, String title, String author, String editor, Date created, String description){
        HashMap<String, Object> project = new HashMap<String, Object>();
        project.put("title", title);
        project.put("createdBy", author);
        project.put("updatedBy", editor);
        project.put("created", created);
        project.put("updated", created);
        project.put("description", description);

        Entity<HashMap<String, Object>> projectEntity = Entity.entity(project, MediaType.APPLICATION_JSON_TYPE);

        Response response = test.target(PROJECTS).request().post(projectEntity);
        return response;
    }

    @Before
    public void setupProjects(){
        project1_id = (String)toHashMap(addProject(this, "title1", "author1", "editor1", new Date(), "description 1")).get("id");
        project2_id = (String)toHashMap(addProject(this, "title2", "author2", "editor2", new Date(), "description 2")).get("id");
    }

    protected HashMap<String, Object> toHashMap(Response response){
        HashMap<String, Object> map = response.readEntity(new GenericType<HashMap<String, Object>>(){});
        return map;
    }

    @Test
    public void testAddProject(){
        Response response = addProject(this, "Title", "Author", "Editor", null, "Description");

        assertEquals(200, response.getStatus());

        HashMap<String, Object> responseProject = toHashMap(response);
        assertNotNull(responseProject);

    }

    @Test
    public void testDeleteProject(){
        HashMap<String, Object> project = toHashMap(target(PROJECTS).path(project1_id).request().delete());
        assertNotNull(project);

    }

    @Test
    public void testGetProjects(){
        Collection<HashMap<String, Object>> response = target(PROJECTS).request().get(new GenericType<Collection<HashMap<String, Object>>>(){});
        assertEquals(2, response.size());
    }


    @Test
    public void testGetProject(){
        HashMap<String, Object> response = toHashMap(target(PROJECTS).path(project1_id).request().get());
        assertNotNull(response);
    }

    @Test
    public void testRepository(){
        HashMap<String, Object> response1 = toHashMap(target(PROJECTS).path(project1_id).request().get());
        HashMap<String, Object> response2 = toHashMap(target(PROJECTS).path(project1_id).request().get());

        assertEquals(response1.get("created"), response2.get("created"));
    }
}
