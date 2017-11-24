package com.github.chen0040.sk.dom.csvdom;


import com.github.chen0040.sk.dom.basic.DomElement;
import com.github.chen0040.sk.dom.basic.DomService;

import java.io.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class CSVService extends DomService {
    public static final String quoteSplitPM = "(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    private static CSVService instance;
    public static CSVService getInstance(){
        if(instance == null){
            instance = new CSVService();
        }
        return instance;
    }

    public boolean readDoc(File file, String cvsSplitBy, final boolean hasHeader, Function<DomElement, Boolean> onLineReady, Consumer<Exception> onFailed){
        BufferedReader br = null;
        String line = "";
        if(cvsSplitBy==null) cvsSplitBy = ",";

        boolean success = true;
        try {

            br = new BufferedReader(new FileReader(file));

            boolean firstLine = true;
            int lineIndex = 0;
            while ((line = br.readLine()) != null) {

                line = line.trim();

                if(line.equals("")) continue;
                if(firstLine && hasHeader){
                    firstLine = false;
                    continue;
                }

                boolean containsQuote = false;
                if(line.contains("\"")){
                    containsQuote = true;
                    cvsSplitBy = cvsSplitBy + quoteSplitPM;
                }

                String[] values = line.split(cvsSplitBy);

                if(containsQuote){
                    for(int i=0; i < values.length; ++i){
                        values[i] = stripQuote(values[i]);
                    }
                }

                if(onLineReady != null) {
                    onLineReady.apply(new DomElement(values, lineIndex));
                }
                lineIndex++;
            }

        } catch (FileNotFoundException e) {
            success = false;
            if(onFailed != null) onFailed.accept(e);
            else e.printStackTrace();
        } catch (IOException e) {
            success = false;
            if(onFailed != null) onFailed.accept(e);
            else e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    success = false;
                    if(onFailed != null) onFailed.accept(e);
                    else e.printStackTrace();
                }
            }
        }

        return success;
    }

    public boolean readDoc(String text, String cvsSplitBy, Function<String[], Boolean> onLineReady, Consumer<Exception> onFailed){
        BufferedReader br = null;
        String line = "";
        if(cvsSplitBy==null) cvsSplitBy = ",";

        boolean success = true;
        try {

            br = new BufferedReader(new StringReader(text));
            while ((line = br.readLine()) != null) {

                line = line.trim();

                if(line.equals("")) continue;

                boolean containsQuote = false;
                if(line.contains("\"")){
                    containsQuote = true;
                    cvsSplitBy = cvsSplitBy + quoteSplitPM;
                }

                String[] values = line.split(cvsSplitBy);

                if(containsQuote){
                    for(int i=0; i < values.length; ++i){
                        values[i] = stripQuote(values[i]);
                    }
                }

                if(onLineReady != null){
                    onLineReady.apply(values);
                }
            }

        } catch (FileNotFoundException e) {
            success = false;
            if(onFailed != null) onFailed.accept(e);
            else e.printStackTrace();
        } catch (IOException e) {
            success = false;
            if(onFailed != null) onFailed.accept(e);
            else e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    success = false;
                    if(onFailed != null) onFailed.accept(e);
                    else e.printStackTrace();
                }
            }
        }

        return success;
    }
}
