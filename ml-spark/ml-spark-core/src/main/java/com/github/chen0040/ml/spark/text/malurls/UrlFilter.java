package com.github.chen0040.ml.spark.text.malurls;

import org.apache.commons.validator.routines.UrlValidator;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Created by chen0469 on 11/7/2015 0007.
 */
public class UrlFilter implements Function<String, Boolean>, Serializable {
    @Override
    public Boolean apply(String s) {
        return UrlValidator.getInstance().isValid(s);
    }

    public static void main(String[] args){
        String[] urls = new String[]{"http://google.com",
                "google.com",
                "google.com/search",
                "www.google.com"};

        for(int i=0; i < urls.length; ++i){
            UrlFilter filter = new UrlFilter();
            System.out.println(filter.apply(urls[i]) ? "+1" : "-1");
        }


    }
}
