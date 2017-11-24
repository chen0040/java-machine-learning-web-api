package com.github.chen0040.ml.tests.linearalg;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.linearalg.Vector;
import com.github.chen0040.ml.linearalg.Matrix;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Random;

import static org.testng.Assert.assertEquals;


/**
 * Created by chen0469 on 9/27/2015 0027.
 */
public class JsonTest {
    private Random random;

    @BeforeClass
    public void setup(){
        random = new Random(10);
    }

    private String serialize(Object obj){
        String json = JSON.toJSONString(obj, SerializerFeature.BrowserCompatible);

        return json;
    }

    private static <T extends Object> T deserialize(String json, Class<T> clazz){
        T obj = JSON.parseObject(json, clazz);
        return obj;
    }



    @Test
    public void testVector(){
        Vector v = new Vector(100);

        v.setAll(0.001);

        for(int i=20; i < 40; ++i){
            v.set(i, random.nextDouble());
        }

        for(int i=90; i < 95; ++i){
            v.set(i, random.nextDouble());
        }

        String json = serialize(v);
        //System.out.println(json);

        Vector v2 = deserialize(json, Vector.class);

        assertEquals(v, v2);
    }



    @Test
    public void testMatrix(){
        Matrix m = new Matrix(100, 100);

        m.setAll(0.001);

        for(int i=20; i < 40; ++i){
            m.set(i, random.nextInt(100), random.nextDouble());
        }

        for(int i=90; i < 95; ++i){
            m.set(i, random.nextInt(100), random.nextDouble());
        }

        String json = serialize(m);
        //System.out.println(json);

        Matrix m2 = deserialize(json, Matrix.class);

        assertEquals(m, m2);
    }
}
