package com.github.chen0040.ml.commons.readers;

import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.tuples.TupleTransformRules;
import com.github.chen0040.sk.dom.basic.DomService;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by root on 9/7/15.
 */
public class CSVReaderHelper extends DocReaderHelper {

    private static final Logger logger = LoggerFactory.getLogger(CSVReaderHelper.class);


    public static IntelliContext readHeartScaleFormatCsv(TupleTransformRules options, InputStream isInputData) {
        IntelliContext batch = new IntelliContext();

        logger.info("Parse heart scale file style");
        try {
            BufferedReader fp = new BufferedReader(new InputStreamReader(isInputData));

            while (true) {
                String line = fp.readLine();
                if (line == null) break;

                //logger.info("Read Line: {}", line);

                StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

                String label = st.nextToken();
                HashMap<Integer, Double> row = new HashMap<>();

                double outputValue = 0;
                try{
                    outputValue = Double.parseDouble(label);
                }catch(NumberFormatException ex){
                    outputValue = 0;
                }

                int m = st.countTokens() / 2;
                for (int j = 0; j < m; j++) {
                    int index = atoi(st.nextToken()) - 1;
                    String columnName = "c" + index;
                    double value = atof(st.nextToken());

                    batch.getAttributeLevelSource().setAttributeName(index, columnName);
                    row.put(index, value);
                }

                IntelliTuple tuple = batch.newNumericTuple(row, options);
                tuple.setLabelOutput(label);
                tuple.setNumericOutput(outputValue);

                batch.add(tuple);
            }

            fp.close();

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return batch;
    }

    public static IntelliContext readHeartScaleFormatCsv(InputStream isInputData){
        return readHeartScaleFormatCsv(null, isInputData);
    }

    public static IntelliContext readDefaultCsv(TupleTransformRules options, InputStream isInputData, String splitBy, boolean hasHeader){
        IntelliContext batch = new IntelliContext();

        String line;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(isInputData));

            boolean firstLine = true;

            while((line = reader.readLine()) != null){


                line = line.trim();

                if(line.equals("")) continue;
                if(firstLine && hasHeader){
                    firstLine = false;
                    continue;
                }

                boolean containsQuote = false;
                if(line.contains("\"")){
                    containsQuote = true;
                    splitBy = splitBy + CSVService.quoteSplitPM;
                }

                String[] values = line.split(splitBy);

                if(containsQuote){
                    for(int i=0; i < values.length; ++i){
                        values[i] = DomService.stripQuote(values[i]);
                    }
                }

                for(int i=0; i < values.length; ++i){
                    batch.getAttributeLevelSource().setAttributeName(i, "c" + i);
                }

                IntelliTuple tuple = batch.newTuple(toHashMap(values), options);

                batch.add(tuple);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return batch;
    }

}
