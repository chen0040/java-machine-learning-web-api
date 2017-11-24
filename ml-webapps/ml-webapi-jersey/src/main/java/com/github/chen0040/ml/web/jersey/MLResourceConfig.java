package com.github.chen0040.ml.web.jersey;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.github.chen0040.ml.sdk.modules.SdkServiceMediator;
import com.github.chen0040.ml.web.resources.AlgorithmResource;
import com.github.chen0040.ml.web.resources.DataResource;
import com.github.chen0040.ml.web.resources.ProjectResource;
import com.github.chen0040.ml.web.resources.RandomNameGeneratorResource;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.SpringLifecycleListener;
import org.springframework.web.filter.RequestContextFilter;

import javax.ws.rs.ApplicationPath;

/**
 * Created by memeanalytics on 24/8/15.
 * Provide Dependency Injection
 */
@ApplicationPath("/ml")
public class MLResourceConfig extends ResourceConfig {

    public MLResourceConfig(SdkServiceMediator mediator){

        register(DataResource.class);
        register(AlgorithmResource.class);
        register(ProjectResource.class);
        register(RandomNameGeneratorResource.class);

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(mediator).to(SdkServiceMediator.class);
            }
        });

        register(CORSResponseFilter.class);

        JacksonJsonProvider json = new JacksonJsonProvider().configure(SerializationFeature.INDENT_OUTPUT, true).
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).
                configure(SerializationFeature.WRAP_ROOT_VALUE, false).
                configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).
                configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);



        register(json);
        register(MultiPartFeature.class);

        register(SpringLifecycleListener.class);
        register(RequestContextFilter.class);
    }
}
