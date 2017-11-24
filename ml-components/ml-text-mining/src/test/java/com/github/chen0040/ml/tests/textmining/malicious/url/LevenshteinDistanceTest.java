package com.github.chen0040.ml.tests.textmining.malicious.url;

import com.github.chen0040.ml.textmining.distance.LevenshteinDistance;
import org.testng.annotations.Test;

/**
 * Created by root on 10/15/15.
 */
public class LevenshteinDistanceTest {
    @Test
    public void testMaliciousURL(){

        /*
        curl -XGET "http://d-tomcat-1:9200/syslog/_search?pretty" -d '
{
  "size" : 10,
  "query" : {
    "bool" : {
      "must" : [ {
        "match" : {
          "UserId" : {
            "query" : "b5eeda2d-e4fe-4fe3-8b4b-00f1a7fe4c4e",
            "type" : "boolean"
          }
        }
      }, {
        "range" : {
          "Date" : {
            "from" : 1446775181289,
            "to" : 1446778781289,
            "include_lower" : true,
            "include_upper" : false
          }
        }
      }, {
        "nested" : {
          "query" : {
            "bool" : {
              "must" : {
                "match" : {
                  "tag" : {
                    "query" : "url",
                    "type" : "boolean"
                  }
                }
              }
            }
          },
          "path" : "Tags"
        }
      } ]
    }
  }
  }
}
'
         */



        String[] phishTankUrls = new String[]{
            "http://www.example.com/",
            "http://other.example.com/",
            "http://192.168.0.1/",
            "http://www.microsoft.evil"
        };

        String[] whiteListUrls = new String[]{
                "http://www.google.com",
                "http://www.facebook.com",
                "http://www.microsoft.com",
                "http://www.ask.com"
        };

        String[] sampleUrls=new String[]{
          "http://any.example.com",
                "http://developer.microsoft.com",
                "http://code.google.com",
                "http://code.example.com"
        };

        for(int i = 0; i < sampleUrls.length; ++i){
            int distance_blacklist = LevenshteinDistance.ComputeShortestDistance(sampleUrls[i], phishTankUrls);
            int distance_whitelist = LevenshteinDistance.ComputeShortestDistance(sampleUrls[i], whiteListUrls);

            if(distance_blacklist > distance_whitelist){
                System.out.println("phishing = -1: "+sampleUrls[i]);
            } else{
                System.out.println("phishing = +1: "+sampleUrls[i]);
            }
        }
    }
}
