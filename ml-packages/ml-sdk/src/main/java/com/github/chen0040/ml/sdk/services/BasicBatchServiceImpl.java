package com.github.chen0040.ml.sdk.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.commons.readers.CSVReaderHelper;
import com.github.chen0040.ml.commons.tables.DataTable;
import com.github.chen0040.ml.sdk.models.BasicBatchOverview;
import com.github.chen0040.ml.sdk.repositories.BasicBatchRepository;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.github.chen0040.ml.commons.tuples.TupleTransformRules;
import com.github.chen0040.ml.sdk.utils.RandomNameTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Created by memeanalytics on 25/8/15.
 */
public class BasicBatchServiceImpl implements BasicBatchService {
    private static final Logger logger = LoggerFactory.getLogger(String.valueOf(BasicBatchServiceImpl.class));

    public static final String CSV_DATA_TABLE = "CSV_DATA_TABLE";
    public static final String CSV_HEART_SCALE = "CSV_HEART_SCALE";
    public static final String JSON_ELASTIC_SEARCH = "JSON_ELASTIC_SEARCH";
    public static final String INTERNAL = "INTERNAL_FORMAT";

    @Inject
    BasicBatchRepository repository;

    private ListeningExecutorService service;

    public BasicBatchServiceImpl(){
        service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    }

    public IntelliContext findById(String id) {
        return repository.findById(id);
    }

    public IntelliContext deleteById(String id) {
        return repository.deleteById(id);
    }

    public Collection<IntelliContext> findAll(boolean shellOnly) {
        Collection<IntelliContext> batches = repository.findAll();
        if(shellOnly){
            return getShells(batches);
        }else{
            return batches;
        }
    }

    public Collection<IntelliContext> findAllByProjectId(String projectId, boolean shellOnly) {
        Collection<IntelliContext> batches = repository.findAllByProjectId(projectId);

        if(shellOnly){
            return getShells(batches);
        }else{
            return batches;
        }
    }

    private List<IntelliContext> getShells(Collection<IntelliContext> batches){
        List<IntelliContext> result = new ArrayList<>();
        for(IntelliContext batch : batches){
            IntelliContext batch2 = batch.clone();
            batch2.clearTuples();
            batch2.setIsShell(true);
            result.add(batch2);
        }
        return result;
    }


    public Map<String, String> getSupportedFormats(){
        Map<String, String> formats = new HashMap<>();
        formats.put(CSV_DATA_TABLE, "standard csv format with optional field separator");
        formats.put(CSV_HEART_SCALE, "csv format in which first character is the label for the tuple, followed by fieldIndex:fieldValue, separated by single space");
        formats.put(JSON_ELASTIC_SEARCH, "json format for data obtained from elastic search query");

        return formats;
    }

    public ListenableFuture<Map<String, String>> getSupportedFormatsAsync(){

        return service.submit(() -> getSupportedFormats());
    }

    public IntelliContext saveFromMap(Map<String, Object> data) {
        String json = JSON.toJSONString(data, SerializerFeature.BrowserCompatible);
        IntelliContext batch = JSON.parseObject(json, IntelliContext.class);
        return save(batch);
    }

    public IntelliContext saveFromCsv(InputStream isInputData,
                                      String projectId,
                                      String version,
                                      String splitBy,
                                      boolean hasHeader,
                                      int outputColumnLabel,
                                      int outputColumnNumeric,
                                      List<Integer> columns_to_add,
                                      List<Integer> columns_to_ignore,
                                      String title,
                                      String description){


        TupleTransformRules options = new TupleTransformRules();
        if(outputColumnLabel != -1){
            options.setOutputColumnLabel(outputColumnLabel);
        }
        if(outputColumnNumeric != -1){
            options.setOutputColumnNumeric(outputColumnNumeric);
        }
        if(columns_to_add != null && columns_to_add.size() > 0){
            for(Integer columnIndex : columns_to_add){
                options.addColumn(columnIndex);
            }
        }
        if(columns_to_ignore != null && columns_to_ignore.size() > 0){
            for(Integer columnIndex : columns_to_ignore){
                options.ignoreColumn(columnIndex);
            }
        }

        IntelliContext batch = newContext(version, options, isInputData, splitBy, hasHeader);

        batch.setProjectId(projectId);
        batch.setTitle(title);
        batch.setDescription(description);

        batch = save(batch);

        return batch;
    }

    private List<String> tokenize(String splitBy){
        String[] words = splitBy.split(";");
        ArrayList<String> keywords = new ArrayList<>();
        for(String w : words){
            keywords.add(w);
        }
        return keywords;
    }

    private IntelliContext newContext(String version, TupleTransformRules options, InputStream isInputData, String splitBy, boolean hasHeader){
        logger.info("fill() invoked with version: {}", version);

        IntelliContext batch;

        if(version!=null){

            if(version.equals(CSV_HEART_SCALE)){
                batch = CSVReaderHelper.readHeartScaleFormatCsv(options, isInputData);
            } else if(version.equals(JSON_ELASTIC_SEARCH)) {
                int timeWindowSize = options.getOutputColumnLabel();
                List<String> keywords = tokenize(splitBy);
                batch = JSONReaderHelper.readElasticSearchFormatJson(isInputData, timeWindowSize, keywords);
            } else {
                batch = CSVReaderHelper.readDefaultCsv(options, isInputData, splitBy, hasHeader);
            }

            batch.setFormat(version);
        }else{
            batch = CSVReaderHelper.readDefaultCsv(options, isInputData, splitBy, hasHeader);
        }

        return batch;
    }





    public IntelliContext save(IntelliContext batch){

        if(batch.getId()==null){
            batch.setId(UUID.randomUUID().toString());
        }

        if(batch.getCreated() != null){
            batch.setCreated(new Date());
        }else{
            batch.setUpdated(new Date());
        }

        if(batch.getIsShell()){
            IntelliContext existing_batch = findById(batch.getId());
            if(existing_batch != null){

                /*
                existing_batch.setDescription(batch.getDescription());
                existing_batch.setTitle(batch.getTitle());
                existing_batch.setUpdated(batch.getUpdated());
                existing_batch.setCreated(batch.getCreated());
                existing_batch.setCreatedBy(batch.getCreatedBy());
                existing_batch.setUpdatedBy(batch.getUpdatedBy());
                existing_batch.setLabelPredictorId(batch.getLabelPredictorId());
                existing_batch.setOutputPredictorId(batch.getOutputPredictorId());
                existing_batch.setFormat(batch.getFormat());
                return save(existing_batch);*/

                batch.setTuples(existing_batch.getTuples());
            }
        }

        return repository.save(batch);
    }

    public ListenableFuture<IntelliContext> saveAsync(final IntelliContext batch){

        return service.submit(() -> save(batch));
    }

    public ListenableFuture<IntelliContext> findByIdAsync(final String id) {

        return service.submit(() -> findById(id));
    }

    public ListenableFuture<IntelliContext> deleteByIdAsync(final String id) {
        return service.submit(() -> deleteById(id));
    }

    public ListenableFuture<Collection<IntelliContext>> findAllAsync(final boolean shellOnly) {

        return service.submit(() -> findAll(shellOnly));
    }

    public ListenableFuture<Collection<IntelliContext>> findAllByProjectIdAsync(final String projectId, final boolean shellOnly) {

        return service.submit(() -> findAllByProjectId(projectId, shellOnly));
    }

    public ListenableFuture<IntelliContext> saveFromMapAsync(final Map<String, Object> batch) {

        return service.submit(() -> saveFromMap(batch));
    }

    public ListenableFuture<IntelliContext> saveFromCsvAsync(final InputStream is,
                                                             final String projectId,
                                                             final String version,
                                                             final String splitBy,
                                                             final boolean hasHeader,
                                                             final int outputColumnLabel,
                                                             final int outputColumnNumeric,
                                                             final List<Integer> columns_to_add,
                                                             final List<Integer> columns_to_ignore,
                                                             final String title,
                                                             final String description){
        return service.submit(() -> saveFromCsv(is, projectId, version, splitBy, hasHeader, outputColumnLabel, outputColumnNumeric, columns_to_add, columns_to_ignore, title, description));
    }

    public IntelliContext createByProjectId(String projectId) {
        IntelliContext batch = new IntelliContext();
        batch.setProjectId(projectId);
        return batch;
    }

    public ListenableFuture<IntelliContext> createByProjectIdAsync(final String projectId) {
        return service.submit(() -> createByProjectId(projectId));
    }

    public Collection<BasicBatchOverview> findAllOverviewsByProjectId(String projectId){
        Collection<IntelliContext> batches = findAllByProjectId(projectId, true);

        List<BasicBatchOverview> overviews = new ArrayList<>();
        for(IntelliContext batch : batches){
            BasicBatchOverview overview = new BasicBatchOverview();
            overview.description = batch.getDescription();
            overview.name = batch.getTitle();
            overview.id = batch.getId();

            overviews.add(overview);
        }

        return overviews;
    }

    public ListenableFuture<Collection<BasicBatchOverview>> findAllOverviewsByProjectIdAsync(final String projectId){

        return service.submit(() -> findAllOverviewsByProjectId(projectId));
    }

    public IntelliContext createBatchInProject(String projectId, Map<String, Object> parameters){

        try {
            IntelliContext batch = null;
            String url = null;
            if (parameters.containsKey("source") && parameters.get("source") != null) {
                String source = parameters.get("source").toString();
                String[] keywords0 = parameters.get("keywords").toString().split(";");
                List<String> keywords1 = new ArrayList<>();
                for (int i = 0; i < keywords0.length; ++i) {
                    keywords1.add(keywords0[i].trim());
                }
                url = parameters.get("url").toString();
                int timeWindowSize = Integer.parseInt(parameters.get("timeWindowSize").toString());
                if (source.equals("URL_ES_V0")) {

                    String content = httpGet(url);

                    batch = JSONReaderHelper.readElasticSearchFormatJson(content, timeWindowSize, keywords1);
                    batch.setFormat(BasicBatchServiceImpl.JSON_ELASTIC_SEARCH);
                }
            }

            if(batch == null){
                throw new NullPointerException("Failed to load any json file!!!");
            }

            batch.setProjectId(projectId);

            String batchName = RandomNameTool.randomName();

            batch.setDescription(batchName + " is grabbed from elastic search at " + url);
            batch.setTitle(batchName);

            return save(batch);
        }catch(Exception ex){
            logger.error("createBatchInProject failed", ex);
        }
        return null;
    }

    public IntelliContext saveTable(DataTable table){
        IntelliContext batch = new IntelliContext();

        try {
            batch.readTable(table);
            batch = save(batch);
        }catch(Exception ex){
            logger.error("saveTable failed", ex);
        }
        return batch;
    }

    public ListenableFuture<IntelliContext> saveTableAsync(DataTable table){
        return service.submit(() -> saveTable(table));
    }

    public String httpGet(String urlToRead) {
        URL url;
        HttpURLConnection conn;
        BufferedReader rd;
        String line;
        StringBuilder result = new StringBuilder();
        try {
            url = new URL(urlToRead);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }


    public ListenableFuture<IntelliContext> createBatchInProjectAsync(final String projectId, final Map<String, Object> parameters){
        return service.submit(() -> createBatchInProject(projectId, parameters));
    }

    public IntelliTuple updateTuple(IntelliTuple tuple, String batchId){
        IntelliTuple existingTuple = null;
        try {
            IntelliContext batch = findById(batchId);
            existingTuple = batch.tupleAtIndex(tuple.getIndex());
            existingTuple.setLabelOutput(tuple.getLabelOutput());

            existingTuple.setModel(tuple.getModel());
            //logger.info("existing: "+existingTuple.getLabelOutput());

            save(batch);
        }catch(Exception ex){
            logger.error("updateTuple failed", ex);
        }
        return existingTuple;
    }

    public ListenableFuture<IntelliTuple> updateTupleAsync(final IntelliTuple tuple, final String batchId){
        return service.submit(() -> updateTuple(tuple, batchId));
    }


}
