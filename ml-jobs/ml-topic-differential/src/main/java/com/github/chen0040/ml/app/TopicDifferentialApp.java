package com.github.chen0040.ml.app;

import com.github.chen0040.ml.services.MLDataService;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

/**
 * Created by root on 11/18/15.
 */
public class TopicDifferentialApp {

    public static Logger logger = LoggerFactory.getLogger(TopicDifferentialApp.class);

    public static void main(String[] args){

        Injector injector = Guice.createInjector(new AppGuiceInjector());

        MLDataService dataService = injector.getInstance(MLDataService.class);

        dataService.startScheduler();


        System.out.println("Topic Differential Visualization Server started!");

        Spark.port(9091);

        Spark.get("/hello", new Route() {
            public Object handle(Request request, Response response) throws Exception {

                logger.info(request.body());
                return "{'test':'test'}";
            }

        });

        Spark.get("/", new Route() {
            public Object handle(Request request, Response response) throws Exception {

                return "Topic Differential Visualization Server is running!";
            }

        });
    }
}
