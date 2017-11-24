package com.github.chen0040.ml.commons;

import com.github.chen0040.ml.commons.discrete.AttributeValueDiscretizer;
import com.github.chen0040.ml.commons.tables.DataRow;
import com.github.chen0040.ml.commons.tuples.TupleTransformRules;

import java.util.Map;

/**
 * Created by memeanalytics on 12/8/15.
 */
public interface MLModule extends MLObject {
    BatchUpdateResult batchUpdate(IntelliContext batch);
    double evaluate(IntelliTuple tuple, IntelliContext context);
    boolean isEvaluationSupported(IntelliContext batch);


    String getPrototype();

    MLModuleOutputType getOutputType();
    void setOutputType(MLModuleOutputType outputType);

    IntelliContext getModelSource();
    void setModelSource(IntelliContext modelSource);

    boolean getIsLabelRequiredInBatchUpdate();
    void setIsLabelRequiredInBatchUpdate(boolean isReq);

    void copy(MLModule rhs);

    void setInputDiscretizer(AttributeValueDiscretizer avd);
    AttributeValueDiscretizer getInputDiscretizer();

    IntelliTuple newTuple(Map<Integer, String> data, TupleTransformRules rules);
    IntelliTuple newTuple(Map<Integer, String> data);

    IntelliTuple newTuple();
    IntelliTuple newTuple(DataRow row);
    IntelliTuple newTuple(DataRow row, TupleTransformRules rules);

    DataRow toDataRow(IntelliTuple tuple);
}
