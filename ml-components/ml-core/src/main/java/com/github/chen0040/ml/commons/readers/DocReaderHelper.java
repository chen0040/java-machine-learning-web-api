package com.github.chen0040.ml.commons.readers;

import java.util.HashMap;

/**
 * Created by root on 9/7/15.
 */
public class DocReaderHelper {

    protected static HashMap<Integer, String> toHashMap(String[] values){
        HashMap<Integer, String> hmap = new HashMap<Integer, String>();
        for(int i=0; i < values.length; ++i){
            hmap.put(i, values[i]);
        }
        return hmap;
    }

    public static double atof(String s)
    {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
            System.err.print("NaN or Infinity in input\n");
            System.exit(1);
        }
        return(d);
    }

    public static int atoi(String s)
    {
        int value = 0;
        try {
            value = Integer.parseInt(s);
        }catch(NumberFormatException ex){
            value = 0;
        }
        return value;
    }
}
