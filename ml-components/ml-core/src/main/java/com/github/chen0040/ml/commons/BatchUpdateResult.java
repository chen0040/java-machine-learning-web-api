package com.github.chen0040.ml.commons;

/**
 * Created by memeanalytics on 18/8/15.
 */
public class BatchUpdateResult {
    private boolean success;
    private Exception error;
    public BatchUpdateResult(){
        success = true;
    }

    public BatchUpdateResult(Exception ex){
        success = ex == null;
        error = ex;
    }

    public boolean success(){
        return success;
    }

    public Exception getError(){
        return error;
    }
}
