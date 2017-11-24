package com.github.chen0040.ml.webclient.jersey;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by xschen on 10/7/2016.
 */
public class BasicSession implements Serializable {
    private String baseUrl;
    private Client client;

    public BasicSession(String baseUrl, Client client){
       this.baseUrl = baseUrl;
        this.client = client;
        if(client == null){
            this.client = ClientBuilder.newClient();
        }
    }


    protected WebTarget target(String relativePath) {

        return client.target(baseUrl).path(relativePath);
    }

    protected WebTarget target(){
        return target(baseUrl);
    }

    protected HashMap<String, Object> toHashMap(Response response){
        HashMap<String, Object> map = response.readEntity(new GenericType<HashMap<String, Object>>(){});
        return map;
    }
}
