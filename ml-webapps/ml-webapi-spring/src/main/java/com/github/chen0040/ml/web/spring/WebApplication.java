package com.github.chen0040.ml.web.spring;

import com.github.chen0040.ml.sdk.modules.SdkServiceMediator;
import com.github.chen0040.ml.sdk.modules.SdkServiceMediatorImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.AsyncRestTemplate;

/**
 * Created by root on 7/8/16.
 */
@SpringBootApplication
public class WebApplication extends SpringBootServletInitializer
{

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WebApplication.class);
    }

    @Bean
    AsyncRestTemplate asyncRestTemplate() {
        return new AsyncRestTemplate();
    }


    @Bean
    public Loader loader(){
        return new Loader();
    }

    @Bean
    public SdkServiceMediator serviceMediator(){
        SdkServiceMediator mediator = new SdkServiceMediatorImpl();
        return mediator;
    }

    public static void main(String[] args){
        ApplicationContext context = SpringApplication.run(WebApplication.class, args);
    }
}

