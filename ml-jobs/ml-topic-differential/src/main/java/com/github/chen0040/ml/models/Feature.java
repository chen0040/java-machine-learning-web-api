package com.github.chen0040.ml.models;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.Serializable;

/**
 * Algorithm feature
 * Created by xiaoyong on 26/10/15.
 */

public class Feature implements Jsonable,Serializable{
    String name;

    public Feature() {
    }

    public Feature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toJson() {
        JSONSerializer serializer = new JSONSerializer();
        serializer.config(SerializerFeature.QuoteFieldNames, false);
        serializer.config(SerializerFeature.UseSingleQuotes, true);
        serializer.write(this);
        return serializer.getWriter().toString();
    }
}
