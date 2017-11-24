package com.github.chen0040.ml.tests.reinforcement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.chen0040.ml.reinforcement.learning.qlearn.QAgent;
import com.github.chen0040.ml.reinforcement.models.QModel;
import com.github.chen0040.ml.reinforcement.learning.actorcritic.ActorCriticAgent;
import com.github.chen0040.ml.reinforcement.learning.actorcritic.ActorCriticLearner;
import com.github.chen0040.ml.reinforcement.models.UtilityModel;
import com.github.chen0040.ml.reinforcement.learning.qlearn.QLearner;
import com.github.chen0040.ml.reinforcement.learning.rlearn.RAgent;
import com.github.chen0040.ml.reinforcement.learning.rlearn.RLearner;
import org.junit.Before;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by chen0469 on 9/27/2015 0027.
 */
public class JsonTest {
    private ObjectMapper mapper;
    private Random random;

    @Before
    public void setup(){
        mapper = new ObjectMapper();
        random = new Random(10);
    }

    private String serialize(Object obj){
        String json = null;
        try {
            json = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    private static <T extends Object> T deserialize(ObjectMapper mapper, String json, Class<T> clazz){
        T obj = null;
        try {
            obj = mapper.readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }



    @Test
    public void testQModel(){
        QModel v = new QModel(100, 100);

        String json = serialize(v);
        System.out.println(json);

        QModel v2 = deserialize(mapper, json, QModel.class);

        assertEquals(v, v2);
    }

    @Test
    public void testUtilityModel(){
        UtilityModel v = new UtilityModel(100, 100);

        String json = serialize(v);
        System.out.println(json);

        UtilityModel v2 = deserialize(mapper, json, UtilityModel.class);

        assertEquals(v, v2);
    }

    @Test
    public void testQLearner(){
        QLearner v = new QLearner(100, 100);

        String json = serialize(v);
        System.out.println(json);

        QLearner v2 = deserialize(mapper, json, QLearner.class);

        assertEquals(v, v2);
    }


    @Test
    public void testACLearner(){
        ActorCriticLearner v = new ActorCriticLearner(100, 100);

        String json = serialize(v);
        System.out.println(json);

        ActorCriticLearner v2 = deserialize(mapper, json, ActorCriticLearner.class);

        assertEquals(v, v2);
    }

    @Test
    public void testACAgent(){
        ActorCriticAgent v = new ActorCriticAgent(100, 100);

        String json = serialize(v);
        System.out.println(json);

        ActorCriticAgent v2 = deserialize(mapper, json, ActorCriticAgent.class);

        assertEquals(v, v2);
    }

    @Test
    public void testRLearner(){
        RLearner v = new RLearner(100, 100);

        String json = serialize(v);
        System.out.println(json);

        RLearner v2 = deserialize(mapper, json, RLearner.class);

        assertEquals(v, v2);
    }

    @Test
    public void testRAgent(){
        RAgent v = new RAgent(100, 100);

        String json = serialize(v);
        System.out.println(json);

        RAgent v2 = deserialize(mapper, json, RAgent.class);

        assertEquals(v, v2);
    }


    @Test
    public void testQAgent(){
        QAgent v = new QAgent(100, 100);

        String json = serialize(v);
        System.out.println(json);

        QAgent v2 = deserialize(mapper, json, QAgent.class);

        assertEquals(v, v2);
    }
}
