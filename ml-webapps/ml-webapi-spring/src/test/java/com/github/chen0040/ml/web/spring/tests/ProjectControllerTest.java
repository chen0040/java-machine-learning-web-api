package com.github.chen0040.ml.web.spring.tests;

import com.alibaba.fastjson.JSON;
import com.github.chen0040.ml.web.spring.WebApplication;
import com.github.chen0040.ml.sdk.models.MLProject;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Created by root on 7/8/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = WebApplication.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
public class ProjectControllerTest {

    private static Logger logger = LoggerFactory.getLogger(ProjectControllerTest.class);

    public static final String PROJECTS = "projects";
    private String project1_id;
    private String project2_id;

    @Value("${local.server.port}")
    private static int port;

    private static URL base;
    private static RestTemplate template;

    @Before
    public void setUp() throws Exception {
        initialize(port);
    }



    private static void initialize(int port) throws MalformedURLException {
        if(base != null)  return;
        base = new URL("http://localhost:" + port);
        template = new TestRestTemplate();
    }

    public static MLProject addProject(int port, String title, String author, String editor, Date created, String description) throws MalformedURLException, UnsupportedEncodingException {

        initialize(port);

        MLProject project = new MLProject();
        project.setTitle(title);
        project.setCreatedBy(author);
        project.setUpdatedBy(editor);
        project.setCreated(created);
        project.setUpdated(created);
        project.setDescription(description);


        Map<String, Object> params = new HashMap<>();
        params.put("project", JSON.toJSONString(project));

        HttpEntity<String> request = createRequest(params);


        String url =  base.toString().concat("/").concat(PROJECTS);
        logger.info("post to {}",url);
        ResponseEntity<String> response = template.postForEntity(
                url,
                request,
                String.class);

        String json = response.getBody();

        return JSON.parseObject(json, MLProject.class);
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
