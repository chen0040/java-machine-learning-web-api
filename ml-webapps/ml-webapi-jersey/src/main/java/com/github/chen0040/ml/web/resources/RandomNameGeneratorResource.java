package com.github.chen0040.ml.web.resources;

import org.kohsuke.randname.RandomNameGenerator;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by memeanalytics on 29/8/15.
 */
@Component
@Path("randname")
public class RandomNameGeneratorResource {
    private static final Random rand = new Random();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getNames(@QueryParam("count") int count){
        List<String> names = new ArrayList<>();
        RandomNameGenerator rnd = new RandomNameGenerator(rand.nextInt());
        for(int i=0; i < count; ++i){
            names.add(rnd.next());
        }
        return names;
    }

    public static String randomName(){
        return new RandomNameGenerator(rand.nextInt()).next();
    }
}
