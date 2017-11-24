package com.github.chen0040.ml.web.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Arrays;

/**
 * Created by chen0 on 2/6/2016.
 */
public class Loader implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(Loader.class);



    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        logger.info("Loader triggered at {}", contextRefreshedEvent.getTimestamp());

        ApplicationContext context = contextRefreshedEvent.getApplicationContext();
        String[] beanNames = context.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            logger.info("bean: {}", beanName);
        }
        logger.info("Run loader...");


    }


}
