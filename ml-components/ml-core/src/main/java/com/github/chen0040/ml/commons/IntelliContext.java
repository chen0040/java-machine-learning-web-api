package com.github.chen0040.ml.commons;

import com.github.chen0040.ml.commons.docs.BasicDocument;
import com.github.chen0040.ml.commons.docs.DocumentBag;
import com.github.chen0040.ml.commons.tables.DataColumn;
import com.github.chen0040.ml.commons.tables.DataColumnCollection;
import com.github.chen0040.ml.commons.tables.DataTable;
import com.github.chen0040.ml.commons.tuples.TupleAttributeLevel;
import com.github.chen0040.ml.commons.tuples.TupleAttributeLevelSource;
import com.github.chen0040.ml.commons.tuples.TupleTransformColumn;
import com.github.chen0040.ml.commons.tuples.TupleTransformRules;
import com.github.chen0040.sk.utils.StringHelper;
import com.github.chen0040.ml.commons.tables.DataRow;
import com.github.chen0040.ml.commons.tuples.*;
import com.github.chen0040.sk.dom.basic.DomService;
import com.github.chen0040.sk.dom.csvdom.CSVService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class IntelliContext implements MLObject {

    private static Logger logger = LoggerFactory.getLogger(IntelliContext.class);


    private DocumentBag docBag = new DocumentBag();
    private TupleAttributeLevelSource attributeLevelSource;
    private Map<String, Double> attributes;
    private String id;
    private String projectId;
    private List<IntelliTuple> tuples;
    private String title;
    private Date created;
    private Map<String, String> predictedLabelDescriptions = new HashMap<String, String>();
    private String statusInfo = "";
    private int statusCode = 200;
    private Date updated;
    private String createdBy;
    private String updatedBy;
    private String description;
    private String outputPredictorId;
    private String labelPredictorId;
    private String format;
    private boolean isShell;
    private int tupleDimension = 0;

    public int getTupleDimension() {
        return tupleDimension;
    }

    public void setTupleDimension(int tupleDimension) {
        this.tupleDimension = tupleDimension;
    }

    public DocumentBag getDocBag(){
        return docBag;
    }

    public boolean isCategorical(int index){
        if(attributeLevelSource==null) return false;
        return attributeLevelSource.isAttributeMultiLevel(index);
    }

    public IntelliTuple newTuple(Map<Integer, String> data){
        return newTuple(data, null);
    }

    public IntelliTuple newTuple(DataRow row){
        return newTuple(row, null);
    }

    public IntelliTuple newTuple(DataRow row, TupleTransformRules options) {
        IntelliTuple tuple = new IntelliTuple(tupleDimension);

        tuple.setLabelOutput(row.getLabel());
        tuple.setPredictedLabelOutput(row.getPredictedLabel());
        tuple.setNumericOutput(row.getOutputValue());
        tuple.setPredictedNumericOutput(row.getPredictedOutputValue());

        int index=0;
        for(int i=0; i < row.columnCount(); ++i) {
            String columnName = attributeLevelSource.getAttributeName(i);
            double value = row.cell(columnName);

            if(options == null){
                if (attributeLevelSource == null) {
                    tuple.set(index, value);
                } else {
                    if (!attributeLevelSource.isAttributeMultiLevel(index)) {
                        tuple.set(index, value);
                    } else {
                        tuple.set(index, (double)attributeLevelSource.getLevelIndex(index, "" + value));
                    }
                }
                index++;
            } else if(options.shouldAddColumn(i)) {

                if (i == options.getOutputColumnLabel()) {
                    tuple.setLabelOutput("" + value);
                } else if (i == options.getOutputColumnNumeric()) {
                    tuple.setNumericOutput(value);
                } else {
                    if (attributeLevelSource == null) {
                        tuple.set(index, value);
                    } else {
                        if (!attributeLevelSource.isAttributeMultiLevel(index)) {
                            tuple.set(index, value);
                        } else {
                            tuple.set(index, (double)attributeLevelSource.getLevelIndex(index, "" + value));
                        }
                    }
                    index++;
                }
            }

            if(options != null) {
                List<TupleTransformColumn> transformedColumns = options.transformsAtIndex(i);
                if (transformedColumns != null) {
                    for (int k = 0; k < transformedColumns.size(); ++k) {
                        double transformed_value = transformedColumns.get(k).apply(i, value);
                        tuple.set(index, transformed_value);
                        index++;
                    }
                }
            }
        }

        return tuple;
    }

    public IntelliTuple newTuple(Map<Integer, String> data, TupleTransformRules options) {
        IntelliTuple tuple = new IntelliTuple(tupleDimension);

        int index=0;
        for(Integer i : data.keySet()){
            String text = data.get(i).trim();
            if(options == null){
                if (attributeLevelSource == null) {
                    tuple.set(index, StringHelper.parseDouble(text, 0));
                } else {
                    if (StringHelper.isNumeric(text) && !attributeLevelSource.isAttributeMultiLevel(index)) {
                        tuple.set(index, StringHelper.parseDouble(text, 0));
                    } else {
                        tuple.set(index, (double)attributeLevelSource.getLevelIndex(index, text));
                    }
                }
                index++;
            } else if(options.shouldAddColumn(i)) {

                if (i == options.getOutputColumnLabel()) {
                    tuple.setLabelOutput(text);
                } else if (i == options.getOutputColumnNumeric()) {
                    tuple.setNumericOutput(StringHelper.parseDouble(text, 0));
                } else {
                    if (attributeLevelSource == null) {
                        tuple.set(index, StringHelper.parseDouble(text, 0));
                    } else {
                        if (StringHelper.isNumeric(text) && !attributeLevelSource.isAttributeMultiLevel(index)) {
                            tuple.set(index, StringHelper.parseDouble(text, 0));
                        } else {
                            tuple.set(index, (double)attributeLevelSource.getLevelIndex(index, text));
                        }
                    }
                    index++;
                }
            }

            if(options != null) {
                List<TupleTransformColumn> transformedColumns = options.transformsAtIndex(i);
                if (transformedColumns != null) {
                    for (int k = 0; k < transformedColumns.size(); ++k) {
                        double value = StringHelper.parseDouble(text, 0);

                        double transformed_value = transformedColumns.get(k).apply(i, value);
                        tuple.set(index, transformed_value);
                        index++;
                    }
                }
            }
        }

        return tuple;
    }

    public IntelliTuple newNumericTuple(Map<Integer, Double> data, TupleTransformRules options){
        IntelliTuple tuple = new IntelliTuple(tupleDimension);

        int index=0;
        for(Integer i : data.keySet()){
            double value = data.get(i);
            if(options == null){
                if (attributeLevelSource == null) {
                    tuple.set(index, value);
                } else {
                    if (!attributeLevelSource.isAttributeMultiLevel(index)) {
                        tuple.set(index, value);
                    } else {
                        tuple.set(index, (double)attributeLevelSource.getLevelIndex(index, "" + value));
                    }
                }
                index++;
            } else if(options.shouldAddColumn(i)) {

                if (i == options.getOutputColumnLabel()) {
                    tuple.setLabelOutput("" + value);
                } else if (i == options.getOutputColumnNumeric()) {
                    tuple.setNumericOutput(value);
                } else {
                    if (attributeLevelSource == null) {
                        tuple.set(index, value);
                    } else {
                        if (!attributeLevelSource.isAttributeMultiLevel(index)) {
                            tuple.set(index, value);
                        } else {
                            tuple.set(index, (double)attributeLevelSource.getLevelIndex(index, "" + value));
                        }
                    }
                    index++;
                }
            }

            if(options != null) {
                List<TupleTransformColumn> transformedColumns = options.transformsAtIndex(i);
                if (transformedColumns != null) {
                    for (int k = 0; k < transformedColumns.size(); ++k) {
                        double transformed_value = transformedColumns.get(k).apply(i, value);
                        tuple.set(index, transformed_value);
                        index++;
                    }
                }
            }
        }

        return tuple;
    }

    public double[] toNumericArray(IntelliTuple tuple) throws ArrayIndexOutOfBoundsException {
        int n = tuple.tupleLength();
        int N = 0;



        for(int i=0; i < n; ++i){
            if(isCategorical(i)) {
                int catCount = attributeLevelSource.getLevelCountAtAttribute(i);

                if(catCount == 2)
                {
                    catCount=1; //prevent singular point in regression algorithms
                }
                N += catCount;
            }else{
                N++;
            }
        }

        if(N == 0){
            throw new ArrayIndexOutOfBoundsException("N should not be zero");
        }

        int index=0;
        double[] x = new double[N];



        for(int i=0; i < n; ++i){
            if(isCategorical(i)) {
                int catCount = attributeLevelSource.getLevelCountAtAttribute(i);
                if(catCount==2) {
                    catCount = 1; //prevent singular point in regression algorithms;
                }
                for (int j = 0; j < catCount; ++j) {
                    x[index++] = getAttributeValueAsInteger(tuple, i, -1) == j ? 1 : 0;
                }
            }
            else{
                x[index++] = getAttributeValueAsDouble(tuple, i, 0);
            }
        }


        return x;
    }

    public TupleAttributeLevel getAttributeValueAsLevel(IntelliTuple tuple, int index){
        if(isCategorical(index)){
            int levelIndex = getAttributeValueAsInteger(tuple, index, -1);
            if(levelIndex == -1){
                return null;
            }
            return attributeLevelSource.getLevel(index, levelIndex);
        }
        return new TupleAttributeLevel(getAttributeName(index), "", index, -1);
    }

    public String getAttributeValueAsString(IntelliTuple tuple, int index){
        if(isCategorical(index)){
            int levelIndex = getAttributeValueAsInteger(tuple, index, -1);
            if(levelIndex == -1){
                return null;
            }
            return attributeLevelSource.getLevel(index, levelIndex).getLevelName();
        }
        return tuple.getAttributeValue(index, "").toString();
    }

    public int getAttributeValueAsInteger(IntelliTuple tuple, int index, int default_value){
        Object value = tuple.getAttributeValue(index, null);
        if(value == null) return default_value;

        if(value instanceof Double){
            Double val = (Double)value;
            return val.intValue();
        }else if(value instanceof Integer){
            Integer val = (Integer)value;
            return val;
        }else if(value instanceof Float){
            Float val = (Float)value;
            return val.intValue();
        }else if(value instanceof Boolean){
            Boolean val = (Boolean)value;
            return val ? 1 : 0;
        }else if(value instanceof  String){
            return (int)Math.ceil(StringHelper.parseDouble((String) value, default_value));
        }else{
            return (int)Math.ceil(StringHelper.parseDouble((String) value,  default_value));
        }
    }

    public String flattenedInputToString(IntelliTuple tuple){
        StringBuilder sb = new StringBuilder();

        String nullString = "(null)";

        sb.append("{");
        for(int i = 0; i< tuple.tupleLength(); ++i){
            if(i!=0){
                sb.append(", ");
            }

            String keyString = attributeLevelSource != null ? attributeLevelSource.getAttributeName(i) : String.format("field%d", i);

            String valString;
            if(isCategorical(i)){
                int levelCount = attributeLevelSource.getLevelCountAtAttribute(i);
                List<String> catHeaders = attributeLevelSource.getLevelDescriptorsAtAttribute(i);

                if(levelCount == 2) levelCount = 1;

                for(int j=0; j < levelCount;++j) {
                    if(j!=0){
                        sb.append(", ");
                    }
                    keyString = catHeaders.get(j);
                    valString = getAttributeValueAsInteger(tuple, i, -1) == j ? "1" : "0";
                    sb.append(String.format("\"%s\":%s", keyString, valString));
                }
            }else {
                Double item = tuple.get(i, null);
                valString = item != null ? item.toString() : nullString;
                sb.append(String.format("\"%s\":%s", keyString, valString));
            }

        }
        sb.append("}");
        return sb.toString();
    }

    public TupleAttributeLevel[] levelsInDoubleArray(IntelliTuple tuple){
        List<TupleAttributeLevel> descriptors = new ArrayList<TupleAttributeLevel>();

        String nullString = "(null)";

        for(int i = 0; i< tuple.tupleLength(); ++i){

            String keyString = attributeLevelSource != null ? attributeLevelSource.getAttributeName(i) : String.format("field%d", i);

            if(isCategorical(i)){
                List<TupleAttributeLevel> levelsInCategory = attributeLevelSource.getLevelsAtAttribute(i);
                if(levelsInCategory.size()==2){
                    levelsInCategory.remove(1);
                }
                descriptors.addAll(levelsInCategory);
            }else {
                Double item = tuple.get(i, null);
                String valString = item != null ? item.toString() : nullString;
                descriptors.add(new TupleAttributeLevel(keyString, valString, i, -1));
            }

        }

        TupleAttributeLevel[] result = new TupleAttributeLevel[descriptors.size()];
        for(int i=0; i < descriptors.size();++i){
            result[i] = descriptors.get(i);
        }
        return result;
    }

    public double getAttributeValueAsDouble(IntelliTuple tuple, int index, double default_value){
        Object value = tuple.getAttributeValue(index, null);
        if(value == null) return default_value;

        if(value instanceof Double){
            return (Double)value;
        }else if(value instanceof Integer){
            return (Integer)value;
        }else if(value instanceof Float){
            return (Float)value;
        }else {
            if (value instanceof Boolean) {
                Boolean val = (Boolean) value;
                return val ? 1 : 0;
            } else if (value instanceof String) {
                return StringHelper.parseDouble((String) value, default_value);
            } else {
                return StringHelper.parseDouble(value.toString(), default_value);
            }
        }
    }

    public String dataDescription(IntelliTuple tuple) {
        String formattedInput = flattenedInputToString(tuple);
        return String.format("{\"input\":%s, \"dimen\":%d, \"labelOutput\":\"%s\", \"numericOutput\":%s}", formattedInput,
                tuple.tupleLength(),
                tuple.hasLabelOutput() ? tuple.getLabelOutput() : "(null)",
                tuple.hasNumericOutput() ? String.format("%f", tuple.getNumericOutput()) : "null") ;

    }


    public String getAttributeName(int index){
        if(attributeLevelSource != null){
            return attributeLevelSource.getAttributeName(index);
        }
        return String.format("field%d", index);
    }

    public String inputToString(IntelliTuple tuple){
        StringBuilder sb = new StringBuilder();

        String nullString = "(null)";

        sb.append("{");
        for(int i = 0; i< tuple.tupleLength(); ++i){
            if(i!=0){
                sb.append(", ");
            }

            String keyString = attributeLevelSource != null ? attributeLevelSource.getAttributeName(i) : String.format("field%d", i);

            String valString;
            if(isCategorical(i)){
                valString = getAttributeValueAsLevel(tuple, i).getLevelName(); //attributeLevelSource.getLevelName(i, getAttributeValueAsInteger(i, 0));
                if(valString!=null){
                    valString = String.format("\"%s\"", valString);
                }
            }else {
                Double item = tuple.get(i, null);
                valString = item != null ? item.toString() : nullString;
            }
            sb.append(String.format("\"%s\":%s", keyString, valString));
        }
        sb.append("}");
        return sb.toString();
    }

    public void setDocBag(DocumentBag docBag){
        this.docBag = docBag;
    }

    public IntelliContext(File file, String split, boolean hasHeader){
        attributes = new HashMap<>();
        tuples = new ArrayList<>();
        attributeLevelSource = new TupleAttributeLevelSource();
        DomService domService = CSVService.getInstance();
        readDoc(file, domService, split, new TupleTransformRules(), hasHeader);
    }

    public IntelliContext(File file, String split, TupleAttributeLevelSource levels, boolean hasHeader){
        attributes = new HashMap<>();
        tuples = new ArrayList<>();
        DomService domService = CSVService.getInstance();
        readDoc(file, domService, split, levels, new TupleTransformRules(), hasHeader);
    }
    public IntelliContext(File file, String split, TupleTransformRules options, boolean hasHeader){
        attributes = new HashMap<>();
        tuples = new ArrayList<>();
        attributeLevelSource = new TupleAttributeLevelSource();
        DomService domService = CSVService.getInstance();
        readDoc(file, domService, split, options, hasHeader);
    }
    public IntelliContext(File file, String split, TupleAttributeLevelSource levels, TupleTransformRules options, boolean hasHeader){
        attributes = new HashMap<>();
        tuples = new ArrayList<>();
        DomService domService = CSVService.getInstance();
        readDoc(file, domService, split, levels, options, hasHeader);
    }
    public IntelliContext(){
        attributes = new HashMap<>();
        tuples = new ArrayList<>();
        attributeLevelSource = new TupleAttributeLevelSource();
    }
    public IntelliContext(TupleAttributeLevelSource levelSource){
        attributes = new HashMap<>();
        tuples = new ArrayList<>();
        this.attributeLevelSource = levelSource;
    }
    public IntelliContext(DataTable table){
        attributes = new HashMap<>();
        tuples = new ArrayList<>();
        attributeLevelSource = new TupleAttributeLevelSource();
        readTable(table);
    }

    public String toString(List<IntelliTuple> list){
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("[");
        for(IntelliTuple tuple : list){
            if(first){
                first = false;
            }else{
                sb.append(", ");
            }
            sb.append("\n");
            String tupleString = tuple != null ? toString(tuple) : "(null)";
            sb.append(tupleString);
        }
        sb.append("\n]");
        return sb.toString();
    }

    public String toString(IntelliTuple tuple) {
        String formattedInput = inputToString(tuple);
        return String.format("{\"input\":%s, \"dimen\":%d, \"labelOutput\":\"%s\", \"numericOutput\":%s, \"predicted-labelOutput\":\"%s\", \"predicted-numericOutput\":%s}", formattedInput,
                tuple.tupleLength(),
                tuple.hasLabelOutput() ? tuple.getLabelOutput() : "(null)",
                tuple.hasNumericOutput() ? String.format("%f", tuple.getNumericOutput()) : "null",
                tuple.getPredictedLabelOutput()!=null ? tuple.getPredictedLabelOutput() : "(null)",
                String.format("%f", tuple.getPredictedNumericOutput())) ;

    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    public Map<String, Double> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Double> attributes) {
        this.attributes = attributes;
    }

    public void setAttribute(String attrname, double value){
        attributes.put(attrname, value);
    }

    public double getAttribute(String attrname){
        return attributes.get(attrname);
    }

    public BasicDocument docAtIndex(int index){
        return docBag.get(index);
    }

    public int docCount(){
        return docBag.size();
    }

    public void addDoc(BasicDocument doc){
        docBag.add(doc);
    }

    public IntelliContext filter(Function<IntelliTuple, Boolean> filterFunc){
        IntelliContext batch = new IntelliContext(getAttributeLevelSource());
        batch.copyWithoutTuples(this);
        for(int i = 0; i < this.tupleCount(); ++i){
            IntelliTuple tuple = tupleAtIndex(i);
            if(filterFunc.apply(tuple)){
                tuple = (IntelliTuple)tuple.clone();
                batch.add(tuple);
            }
        }
        return batch;
    }

    public boolean getIsShell(){
        return this.isShell;
    }

    public void setIsShell(boolean isShell){
        this.isShell = isShell;
    }

    public void clearTuples(){
        this.tuples.clear();
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getOutputPredictorId() {
        return outputPredictorId;
    }

    public void setOutputPredictorId(String outputPredictorId) {
        this.outputPredictorId = outputPredictorId;
    }

    public String getLabelPredictorId() {
        return labelPredictorId;
    }

    public void setLabelPredictorId(String labelPredictorId) {
        this.labelPredictorId = labelPredictorId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TupleAttributeLevelSource getAttributeLevelSource(){
        return attributeLevelSource;
    }

    public void setAttributeLevelSource(TupleAttributeLevelSource attributeLevelSource) {
        this.attributeLevelSource = attributeLevelSource;
    }

    public List<IntelliTuple> getTuples(){
        return tuples;
    }

    public void setTuples(List<IntelliTuple> tuples){
        this.tuples = tuples;
    }

    public boolean readDoc(String text, final IntelliTuple entity, String cvsSplitBy, TupleAttributeLevelSource levels, final TupleTransformRules options, Consumer<Exception> onFailed){
        this.attributeLevelSource = levels;
        return CSVService.getInstance().readDoc(text, cvsSplitBy, new Function<String[], Boolean>() {
            public Boolean apply(String[] values) {

                IntelliTuple obj = newTuple(toHashMap(values), options);
                add(obj);
                return true;
            }
        }, onFailed);
    }

    private HashMap<Integer, String> toHashMap(String[] values){
        HashMap<Integer, String> hmap = new HashMap<Integer, String>();
        for(int i=0; i < values.length; ++i){
            hmap.put(i, values[i]);
        }

        return hmap;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getProjectId(){
        return projectId;
    }

    public void setProjectId(String projectId){
        this.projectId = projectId;
    }

    public void shuffle(){
        Collections.shuffle(this.tuples);
    }

    public boolean add(IntelliTuple tuple) {


        int dimension = tuple.tupleLength();

        if(dimension > tupleDimension){
            tupleDimension = dimension;
            for(int i=0; i < tuples.size(); ++i){
                tuples.get(i).resize(tupleDimension);
            }
        } else {
            tuple.resize(dimension);
        }

        tuple.setIndex(tuples.size());


        boolean added = tuples.add(tuple);



        return added;
    }

    public int tupleCount(){
        return tuples.size();
    }

    public IntelliTuple tupleAtIndex(int index) {
        return tuples.get(index);
    }

    private void initializeAttributeNames(String[] headers, TupleTransformRules options) {

        if (this.attributeLevelSource == null) {
            this.attributeLevelSource = new TupleAttributeLevelSource();
        }

        int index = 0;
        for(int i=0; i < headers.length; ++i) {
            if(options.shouldAddColumn(i)) {
                if (i == options.getOutputColumnLabel() || i == options.getOutputColumnNumeric()) continue;
                this.attributeLevelSource.setAttributeName(index++, headers[i]);
            }
            List<TupleTransformColumn> transformedColumns = options.transformsAtIndex(i);
            if(transformedColumns != null){
                for(int k=0; k < transformedColumns.size(); ++k) {
                    String attributeName = transformedColumns.get(k).getHeader();
                    this.attributeLevelSource.setAttributeName(index++, attributeName);
                }
            }
        }
    }

    protected boolean readDoc(File file, DomService domService, final IntelliTuple entity, String cvsSplitBy, TupleAttributeLevelSource levels, final TupleTransformRules options, final boolean hasHeader, Consumer<Exception> onFailed){

        this.attributeLevelSource = levels;
        return domService.readDoc(file, cvsSplitBy, false, line -> {

            if (hasHeader && line.lineIndex == 0) {
                initializeAttributeNames(line.data, options);
            } else {
                String[] values = line.data;
                IntelliTuple obj = newTuple(toHashMap(values), options);
                add(obj);
            }

            return true;
        }, onFailed);
    }

    public boolean readDoc(File file, DomService domService){
        return readDoc(file, domService, ",", null, false);
    }

    public boolean readDoc(File file, DomService domService, String split, final TupleTransformRules options, boolean hasHeader){
        return readDoc(file, domService, split, null, options, hasHeader);
    }

    public boolean readDoc(File file, DomService domService, String split, TupleAttributeLevelSource levels, final TupleTransformRules options, final boolean hasHeader){
        this.attributeLevelSource = levels;
        return domService.readDoc(file, split, false, line -> {
            String[] values = line.data;
            if (hasHeader && line.lineIndex == 0) {
                initializeAttributeNames(values, options);
            } else {
                IntelliTuple obj = newTuple(toHashMap(values), options);
                add(obj);
            }

            return true;
        }, null);
    }

    @Override
    public String toString() {
       return toString(this.tuples);
    }

    public String flattenedDetails(){
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        sb.append("[");
        for(IntelliTuple tuple : this.tuples){
            if(first){
                first = false;
            }else{
                sb.append(", ");
            }
            sb.append("\n");
            String tupleString = tuple != null ? dataDescription(tuple) : "(null)";
            sb.append(tupleString);
        }
        sb.append("\n]");
        return sb.toString();
    }

    private void copy(IntelliContext rhs){
        copyWithoutTuples(rhs);

        for(IntelliTuple tuple : rhs.tuples){
            this.add((IntelliTuple)((IntelliTuple)tuple).clone());
        }

    }

    public Map<String, String> getPredictedLabelDescriptions(){
        return predictedLabelDescriptions;
    }

    public void setPredictedLabelDescriptions(HashMap<String, String> descriptions){
        predictedLabelDescriptions = descriptions;
    }

    public void copyWithoutTuples(IntelliContext rhs){
        attributes = new HashMap<String, Double>();
        for(String attrname : rhs.attributes.keySet()){
            attributes.put(attrname, rhs.attributes.get(attrname));
        }

        this.attributeLevelSource = rhs.attributeLevelSource;
        this.projectId = rhs.projectId;
        this.id = rhs.id;
        this.format = rhs.format;
        this.title = rhs.title;
        this.description = rhs.description;
        this.created = rhs.created;
        this.createdBy = rhs.createdBy;
        this.updated = rhs.updated;
        this.updatedBy = rhs.updatedBy;
        this.labelPredictorId = rhs.labelPredictorId;
        this.outputPredictorId = rhs.outputPredictorId;
        this.docBag = (DocumentBag) rhs.docBag.clone();
        this.predictedLabelDescriptions.clear();
        this.statusCode = rhs.statusCode;
        this.statusInfo = rhs.statusInfo;
        this.tupleDimension = rhs.tupleDimension;

        for(String key : rhs.predictedLabelDescriptions.keySet()){
            predictedLabelDescriptions.put(key, rhs.predictedLabelDescriptions.get(key));
        }

        this.isShell = rhs.isShell;
    }

    public IntelliContext clone(){
        IntelliContext clone = new IntelliContext();

        clone.copy(this);

        return clone;
    }

    public void readTable(DataTable table) {
        setId(table.getId());
        setProjectId(table.getProjectId());
        setTitle(table.getName());

        for(DataColumn c : table.columns()) {
            getAttributeLevelSource().setAttributeName(c.getColumnIndex(), c.getName());
        }

        for(DataRow row : table.rows()){
            IntelliTuple tuple = newTuple(row);
            add(tuple);
        }
    }

    public DataRow toDataRow(IntelliTuple tuple) {
        DataColumnCollection columns = attributeLevelSource.columns();

        DataRow row = new DataRow(columns);

        for(int i=0; i < tuple.tupleLength(); ++i){
            String columnName = attributeLevelSource.getAttributeName(i);
            row.cell(columnName, tuple.get(i, 0.0));
        }

        row.setLabel(tuple.getLabelOutput());
        row.setOutputValue(tuple.getNumericOutput());

        row.setPredictedLabel(tuple.getPredictedLabelOutput());
        row.setPredictedOutputValue(tuple.getPredictedNumericOutput());

        row.setRowIndex(tuple.getIndex());

        return row;

    }

    public IntelliTuple newTuple() {
        return new IntelliTuple(tupleDimension);
    }
}
