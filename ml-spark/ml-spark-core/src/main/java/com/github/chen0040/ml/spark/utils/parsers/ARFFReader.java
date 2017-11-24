package com.github.chen0040.ml.spark.utils.parsers;

import java.io.*;

/**
 * Created by root on 10/27/15.
 */
public class ARFFReader {
    private static final String TAG_RELATION = "@relation";
    private static final String TAG_ATTRIBUTE = "@attribute";
    private static final String TAG_DATA = "@data";

    public static ARFF parse(File file){
        ARFF arff = new ARFF();
        boolean isInData = false;
        int lineIndex = 0;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while((line=reader.readLine())!=null){
                line = line.trim();
                if(line.startsWith(TAG_RELATION)){
                    String relation = line.substring(TAG_RELATION.length()).trim();
                    arff.setRelation(relation);
                } else if(line.startsWith(TAG_ATTRIBUTE)){
                    String attribute_text = line.substring(TAG_ATTRIBUTE.length()+1).trim();
                    int attribute_split = attribute_text.indexOf(' ');
                    String attribute_name = attribute_text.substring(0, attribute_split).trim();

                    String attribute_values_text = attribute_text.substring(attribute_split).trim();
                    attribute_values_text = attribute_values_text.substring(1, attribute_values_text.length()-2);

                    String[] attribute_values = attribute_values_text.split(",");

                    for(int i=0; i < attribute_values.length; ++i){
                        int attribute_value = Integer.parseInt(attribute_values[i].trim());
                        arff.addValue2Attribute(attribute_value, attribute_name);
                    }
                } else if(line.startsWith(TAG_DATA)){
                    isInData = true;
                } else if(isInData) {
                    String[] line_comps = line.split(",");
                    for(int i=0; i < line_comps.length; ++i){
                        String element = line_comps[i].trim();
                        if(element.equals("")) continue;
                        int value = Integer.parseInt(element);
                        arff.addValue2Line(value, lineIndex);
                    }
                    lineIndex++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException iex) {
            iex.printStackTrace();
        }


        return arff;
    }
}
