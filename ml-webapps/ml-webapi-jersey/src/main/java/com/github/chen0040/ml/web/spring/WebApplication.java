package com.github.chen0040.ml.web.spring;

import com.github.chen0040.ml.sdk.modules.SdkServiceMediatorImpl;
import com.github.chen0040.ml.sdk.modules.SdkServiceMediator;
import com.github.chen0040.ml.web.jersey.MLResourceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

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
    public Loader loader(){
        return new Loader();
    }


    @Bean
    public MLResourceConfig mlResourceConfig(){
        SdkServiceMediator mediator = new SdkServiceMediatorImpl();
        return new MLResourceConfig(mediator);
    }

    public static void main(String[] args){
        ApplicationContext context = SpringApplication.run(WebApplication.class, args);
    }
}

