package com.github.chen0040.sk.utils;

import java.util.List;
import java.util.Map;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class StringHelper {
    public static double parseDouble(String data, double default_value){

        if(data==null) return default_value;

        // The following "twisted" implementation is to get around the (im)precision problem of double parsing
        float value2 = 0;

        boolean parse2_success = true;
        try{
            value2 = Float.parseFloat(data);
        }catch(NumberFormatException ex){
            parse2_success = false;
        }

        if(parse2_success){
            return value2;
        }else{
            double value;
            try{
                value = Double.parseDouble(data);
            }catch(NumberFormatException ex){
                value = default_value;
            }
            return value;
        }


    }


    public static int parseInteger(String data, int default_value){
        return new Double(parseDouble(data, default_value)).intValue();
    }



    public static boolean parseBoolean(String data){
        if(data.equals("1")) return true;
        else if(data.equals("0")) return false;
        else {
            String ldata = data.toLowerCase();
            if(ldata.equals("true")) return true;
            else if(ldata.equals("false")) return false;
        }
        double val = parseDouble(data, 0);
        return val > 0.5;
    }

    public static <K, V> String toString(Map<K, V> map){
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("{");
        for(Map.Entry<K, V> entry : map.entrySet()){
            if(first){
                first = false;
            }else{
                sb.append(", ");
            }

            String keyString = entry.getKey() != null ? entry.getKey().toString() : "(null)";
            String valString = entry.getValue() != null ? entry.getValue().toString() : "(null)";
            sb.append(String.format("%s:%s", keyString, valString));
        }
        sb.append("}");
        return sb.toString();
    }

    public static <T> String toString(List<T> list){
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("[");
        for(T tuple : list){
            if(first){
                first = false;
            }else{
                sb.append(", ");
            }
            String tupleString = tuple != null ? tuple.toString() : "(null)";
            sb.append(tupleString);
        }
        sb.append("]");
        return sb.toString();
    }

    public static String toString(double[] list){
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("[");
        for(double tuple : list){
            if(first){
                first = false;
            }else{
                sb.append(", ");
            }
            String tupleString = String.format("%f", tuple);
            sb.append(tupleString);
        }
        sb.append("]");
        return sb.toString();
    }

    public static boolean isNumeric(String data){
        try
        {
            double d = Double.parseDouble(data);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}
