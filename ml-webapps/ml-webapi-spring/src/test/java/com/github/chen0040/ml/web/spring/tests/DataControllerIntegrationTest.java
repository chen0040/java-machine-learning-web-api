package com.github.chen0040.ml.web.spring.tests;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.commands.MLProjectElementCreateCommand;
import com.github.chen0040.ml.web.spring.WebApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by root on 7/8/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WebApplication.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
public class DataControllerIntegrationTest {

    public static final String BATCHES = "batches";
    private String project1_id;
    private String project2_id;
    private String batch1_id;
    private String batch2_id;

    @Value("${local.server.port}")
    private int port;


    private static URL base;
    private static RestTemplate template;

    @Before
    public void setUp() throws Exception {
        initialize(port);

        project1_id = ProjectControllerTest.addProject(port, "title1", "author1", "editor1", new Date(), "description 1").getId();
        project2_id = ProjectControllerTest.addProject(port, "title2", "author2", "editor2", new Date(), "description 2").getId();
        batch1_id = addBatch(port, project1_id);
        batch2_id = addBatch(port, project2_id);
    }

    private static void initialize(int port) throws MalformedURLException {
        if(base != null) return;
        base = new URL("http://localhost:" + port );
        template = new TestRestTemplate();
    }

    @Test
    public void testListBatches() throws Exception {
        ResponseEntity<String> response = template.getForEntity(base.toString().concat("/").concat(BATCHES).concat("?light=true"), String.class);

        String json = response.getBody();

        System.out.println(json);

    }

    protected static Map<String, Object> createBatch(int port, String projectId) throws MalformedURLException, UnsupportedEncodingException {
        initialize(port);
        MLProjectElementCreateCommand cmd = new MLProjectElementCreateCommand();
        cmd.setProjectId(projectId);
        cmd.setPrototype("batch");

        Map<String, Object> params = new HashMap<>();
        params.put("cmd", JSON.toJSONString(cmd));

        HttpEntity<String> request = createRequest(params);

        ResponseEntity<String> response = template.postForEntity(
                base.toString().concat("/").concat(BATCHES).concat("/factory"),
                request,
                String.class);

        String json = response.getBody();

        return JSON.parseObject(json, new TypeReference<Map<String, Object>>(){}.getType());
    }

    public static String addBatch(int port, String project_id) throws MalformedURLException, UnsupportedEncodingException {
        Map<String, Object> batch = createBatch(port, project_id);

        assertNotNull(batch);

        Map<String, Object> params = new HashMap<>();
        params.put("batch", JSON.toJSONString(batch, SerializerFeature.BrowserCompatible));

        HttpEntity<String> request = createRequest(params);

        ResponseEntity<String> response = template.postForEntity(
                base.toString().concat("/").concat(BATCHES),
                request,
                String.class);

        String json = response.getBody();

        IntelliContext savedInstance = JSON.parseObject(json, IntelliContext.class);

        return savedInstance.getId();
    }

    public static HttpEntity<String> createRequest(Map<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for(String pname : params.keySet()){
            if(first){
                first = false;
            } else {
                sb.append("&");
            }

            sb.append(pname).append("=").append(URLEncoder.encode(params.get(pname).toString(), "UTF-8"));
        }

        return createRequest(sb.toString());
    }



    public static HttpEntity<String> createRequest(String content){
        String contentType = "application/x-www-form-urlencoded";

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("Content-Type", contentType);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        HttpEntity<String> request = new HttpEntity<String>(content, headers);
        return request;
    }


}
