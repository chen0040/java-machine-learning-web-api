package com.github.chen0040.ml.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.chen0040.ml.models.AlgorithmModule;
import com.github.chen0040.ml.models.PredictedValue;
import com.google.common.util.concurrent.*;
import com.github.chen0040.ml.enums.AlgorithmType;
import com.github.chen0040.ml.models.Feature;
import com.github.chen0040.ml.models.TopicModel;
import com.github.chen0040.ml.models.spark.SparkDataSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by root on 11/11/15.
 */
public class MLDataServiceImpl implements MLDataService {
    private static final Logger logger = LoggerFactory.getLogger(MLDataServiceImpl.class);

    private ListeningExecutorService service;

    private int intervalInMinutes = 20;

    @Inject
    private MLSparkService sparkService;

    @Inject
    private AlgorithmModuleService moduleService;

    @Inject
    private TopicModelService topicModelService;

    public MLDataServiceImpl(){
        service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    }

    @Override
    public List<PredictedValue> createData(AlgorithmModule module) {
        Date date = new Date();
        long currentTime = date.getTime();

        return sparkService.tryGetAlgorithmModuleDataView(module, currentTime);
    }

    @Override
    public void startScheduler() {
        stepAsync();
    }

    private void stepAsync(){
        Futures.addCallback(step(), new FutureCallback<List<AlgorithmModule>>() {
            @Override
            public void onSuccess(List<AlgorithmModule> algorithmModules) {

                try {
                    Thread.sleep(intervalInMinutes * 60 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stepAsync();
            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
    }

    private ListenableFuture<List<AlgorithmModule>> step(){
        ListenableFuture<List<AlgorithmModule>> future_me = service.submit(()->{
            List<AlgorithmModule> modules = moduleService.findAll();
            for(int i=0; i < modules.size(); ++i) {
                AlgorithmModule module = modules.get(i);
                step(module);
            }
            return modules;
        });

        return future_me;
    }

    private void step(AlgorithmModule module){

        AlgorithmType algorithmType = module.getAlgorithmType();
        if (algorithmType == AlgorithmType.MLTopics_LDA) {
            List<PredictedValue>  values = createData(module);

            if(values == null || values.isEmpty()) {
                logger.warn("Spark@"+sparkService.getUrl()+" does not return data");
                return;
            }

            TopicModel model = new TopicModel();
            model.getModelKey().setModuleId(module.getId());

            Date date = new Date();

            model.getModelKey().setCaptureTime(date.getTime());

            model.setRateOfChangeInTopic(0);
            model.setRateOfChangeInTopWords(0);
            model.setCompanyId(module.getCompanyId());

            for(int i=0; i < values.size(); ++i){
                PredictedValue pv = values.get(i);


                Map<Feature, String> features = pv.getFeatureValuesMap();

                for(Map.Entry<Feature, String> entry : features.entrySet()){
                    Feature feature = entry.getKey();
                    String name = feature.getName().toLowerCase();

                    if(name.equals("topic-summary")){
                        String content = entry.getValue();

                        List<String> topics = JSON.parseObject(content, new TypeReference<List<String>>() {
                        }.getType());

                        model.getTopics().addAll(topics);
                    } else if(name.equals("word-rank-specificity")){
                        String content = entry.getValue();

                        SparkDataSeries ts = JSON.parseObject(content, SparkDataSeries.class);
                        for(int k=0; k < ts.size(); ++k){
                            model.getWordTopicSpecificities().put(ts.get(k).label, ts.get(k).value);
                        }
                    } else if(name.equals("word-rank-count")){
                        String content = entry.getValue();

                        SparkDataSeries ts = JSON.parseObject(content, SparkDataSeries.class);
                        for(int k=0; k < ts.size(); ++k){
                            model.getWordCounts().put(ts.get(k).label, ts.get(k).value);
                        }

                    }


                }
            }

            logger.info("Topic capture @"+date);

            topicModelService.save(model);


        }
    }

}
