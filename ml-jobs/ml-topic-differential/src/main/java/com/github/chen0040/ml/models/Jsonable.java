package com.github.chen0040.ml.models;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.Serializable;

/**
 * Created by xiaoyong on 24/10/15.
 */
public interface Jsonable extends Serializable{
    default String toJson(){
        return JSON.toJSONString(this, SerializerFeature.BrowserCompatible);
    }
    default String toPrettyJson(){
        return  JSON.toJSONString(this, SerializerFeature.PrettyFormat);
    }
    static <T> T fromJson(String jsonString, Class<T> clazz){
        return JSON.parseObject(jsonString, clazz);
    }
}
