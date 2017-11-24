package com.github.chen0040.ml.web.tests;

import com.github.chen0040.ml.sdk.modules.SdkServiceMediatorImpl;
import com.github.chen0040.ml.web.spring.WebApplication;
import com.github.chen0040.ml.web.jersey.MLResourceConfig;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.ws.rs.core.Application;

import static org.junit.Assert.assertEquals;

/**
 * Created by xschen on 9/7/2016.
 */
public class RandomNameGeneratorResourceTest extends JerseyTest {

    public static final String RANDNAME = "randname";

    @Override
    protected Application configure() {
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

    @Test
    public void testGetRandNames(){
        System.out.println("start");
        String response = target(RANDNAME).queryParam("count", 1).request().get().readEntity(String.class);
        System.out.println("Returns: " + response);
    }




}


