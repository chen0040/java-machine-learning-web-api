package com.github.chen0040.ml.services;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.chen0040.ml.utils.SyntheticDataSequence;
import com.github.chen0040.ml.enums.AlgorithmType;
import com.github.chen0040.ml.enums.SparkConnectionEnum;
import com.github.chen0040.ml.models.AlgorithmModule;
import com.github.chen0040.ml.models.PredictedValue;
import com.github.chen0040.ml.models.spark.SparkDataSeries;
import com.github.chen0040.ml.viewmodels.AlgorithmDashboardPanelRequest;
import com.github.chen0040.ml.models.spark.SparkServerResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;

public class MLSparkServiceImpl implements MLSparkService {

	private static final Logger logger = LoggerFactory.getLogger(MLSparkServiceImpl.class);
	
	private SparkConnectionEnum connectionMode = SparkConnectionEnum.RemoteMockupServer;
	private String sparkUrl; 
	private String mockupServerUrl = "http://localhost:9090";
	private String remoteMockupServerUrl = "http://172.16.2.136:9090/";
	private boolean logSparkViewError;

	
	private ListeningExecutorService service;
	
	public MLSparkServiceImpl(){
		service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
	}
	
	public boolean isLocallyConnected(){
		return connectionMode == SparkConnectionEnum.Local;
	}
	
	
	public boolean getLogSparkViewError(){
		return logSparkViewError;
	}
	
	public void setLogSparkViewError(boolean logged){
		logSparkViewError = logged;
	}
	
	public String getSparkUrl(){
		return sparkUrl;
	}
	
	public void setSparkUrl(String sparkUrl){
		this.sparkUrl = sparkUrl;
	}
	
	public String getUrl(){
		if(connectionMode == SparkConnectionEnum.MockupServer){
			return mockupServerUrl;
		} else if(connectionMode == SparkConnectionEnum.RemoteMockupServer){
			return remoteMockupServerUrl;
		}
		else {
			return sparkUrl;
		}
	}

	@Override
	public List<PredictedValue> tryGetAlgorithmModuleDataView(
			AlgorithmModule module, long currentTime) {

		if(!isLocallyConnected()){

			String companyId = module.getCompanyId();
			String url = getUrl()+"/modules/"+companyId+"/" + module.getId() + "/view";
			
			return getSparkMLView(module, currentTime, url);
		} else {
			return getFakeView(module, currentTime);
		}
	}

	private CloseableHttpClient buildClient(){
		//HttpClientBuilder builder = HttpClientBuilder.create();
        
      int timeout = 10;
		RequestConfig config = RequestConfig.custom()
			    .setSocketTimeout(timeout * 1000)
			    .setConnectTimeout(timeout * 1000)
			    .build();
		
    	CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build(); //builder.build();
    	return httpClient;
	}
	
	
	
	private List<PredictedValue> getSparkMLView(AlgorithmModule module, long currentTime, String url){

		
		List<PredictedValue> spark_data = null;
		
		String moduleId = module.getId();


		logger.info("Query Spark[getView]: "+url);


		long timeWindowInMinutes = 60;
		long startTime = currentTime - timeWindowInMinutes * 60000;
		long endTime = currentTime;

		AlgorithmDashboardPanelRequest request2 = new AlgorithmDashboardPanelRequest();
		request2.setEndTime(endTime);
		request2.setStartTime(startTime);
		request2.setModuleId(moduleId);
		request2.setTimeWindowInMinutes(timeWindowInMinutes);
		request2.setId(moduleId);
		request2.setAlgorithmId(moduleId);
		request2.setTitle("Spark View Request");

		Gson gson = new Gson();

		String body = gson.toJson(request2);
		String json = null;

		CloseableHttpClient httpClient = buildClient();
		long requestTime = System.currentTimeMillis();
		try{
			HttpPost request = new HttpPost(url);
			StringEntity params = new StringEntity(body);
			request.addHeader("content-type", "application/json");
			request.setEntity(params);
			CloseableHttpResponse result = httpClient.execute(request);
			json = EntityUtils.toString(result.getEntity(), "UTF-8");
			result.close();
			httpClient.close();
		} catch (IOException ex) {
			if(logSparkViewError){
				logger.error("failed to get data from spark ml", ex);
			}
		}


		//logger.info("spark: "+json);

		if(json != null){
			long responseTime = System.currentTimeMillis();
			long durationInSeconds = (responseTime - requestTime) / 1000;
			logger.info(getUrl()+" responded in "+durationInSeconds+" secs");

			SparkServerResponse ssr = JSON.parseObject(json, SparkServerResponse.class);

			if(ssr.success){
				json = ssr.values;
				spark_data = JSON.parseObject(json, new TypeReference<List<PredictedValue>>(){}.getType());
			} else {
				logger.warn("spark: return failure flag: "+ ssr.message);
			}
		}
		return spark_data;
	}
	
	



	@Override
	public ListenableFuture<List<PredictedValue>> tryGetAlgorithmModuleDataViewAsync(
			final AlgorithmModule module,
			final long currentTime) {
		ListenableFuture<List<PredictedValue>> future = service.submit(()->{
			return tryGetAlgorithmModuleDataView(module, currentTime);
		});
		return future;
	}

	private int counter = 0;
	public List<PredictedValue> getFakeView(AlgorithmModule module, long currentTime) {
		long timeWindowInMinutes = 60;
		long timeWindowSizeInMilliseconds = timeWindowInMinutes * 60000;
		long startTime = currentTime - timeWindowSizeInMilliseconds;

		AlgorithmType algorithmType = module.getAlgorithmType();

		counter++;

		SimpleDateFormat f = new SimpleDateFormat("yy/MM/dd HH:mm");

		SyntheticDataSequence[] sequences = new SyntheticDataSequence[6];
		for(int i=0; i < sequences.length; ++i){
			sequences[i] = new SyntheticDataSequence(i, counter, i+1);
		}

		List<PredictedValue> values = new ArrayList<>();
		if(algorithmType == AlgorithmType.MLTopics_LDA){

			PredictedValue predictedValue = new PredictedValue();

			int topicCount = 20;
			int docCount = 60;
			double[][] correlations = new double[20][];
			List<String> topics = new ArrayList<>();

			SparkDataSeries wordRankCount = new SparkDataSeries();
			wordRankCount.setName("word-rank-count");
			wordRankCount.setIsHistogramData(0);

			SparkDataSeries wordRankSpecificity = new SparkDataSeries();
			wordRankSpecificity.setName("word-rank-specificity");
			wordRankSpecificity.setIsHistogramData(0);

			for(int wordIndex = 0; wordIndex < 10; ++wordIndex){
				wordRankCount.add(wordIndex, "Word #"+wordIndex, sequences[0].getValue(wordIndex));
				wordRankSpecificity.add(wordIndex, "Word #"+wordIndex, sequences[1].getValue(wordIndex));
			}

			List<SparkDataSeries> docTimeSeriesList = new ArrayList<>();
			for(int topicIndex = 0; topicIndex < topicCount; ++topicIndex){
				topics.add("Topic #"+topicIndex);

				correlations[topicIndex] = new double[topicCount];
				for(int topicIndex2 = 0; topicIndex2 < topicCount; ++topicIndex2){
					double topicValue = sequences[topicIndex % sequences.length].getValue(topicIndex2);
					correlations[topicIndex][topicIndex2] = topicValue;
				}

				SparkDataSeries ts = new SparkDataSeries();
				String featureName = "timeseries-topic-"+topicIndex;
				ts.setName(featureName);
				ts.setIsHistogramData(1);

				for(int docIndex = 0; docIndex < docCount; ++docIndex){
					ts.add(docIndex, f.format(new Date(startTime + docIndex * timeWindowSizeInMilliseconds)), sequences[topicIndex % sequences.length].getValue(docIndex));
				}
				docTimeSeriesList.add(ts);
			}

			wordRankCount.sortDescendingly();
			wordRankSpecificity.sortDescendingly();


			predictedValue.addFeature("topic-summary", JSON.toJSONString(topics, SerializerFeature.BrowserCompatible));
			predictedValue.addFeature("word-rank", JSON.toJSONString(topics, SerializerFeature.BrowserCompatible));
			predictedValue.addFeature("timeseries", JSON.toJSONString(docTimeSeriesList, SerializerFeature.BrowserCompatible));
			predictedValue.addFeature("correlations", JSON.toJSONString(correlations, SerializerFeature.BrowserCompatible));
			predictedValue.addFeature("word-rank-count", JSON.toJSONString(wordRankCount, SerializerFeature.BrowserCompatible));
			predictedValue.addFeature("word-rank-specificity", JSON.toJSONString(wordRankSpecificity, SerializerFeature.BrowserCompatible));


			//options.add("wordrank-all");
			//options.add("correlations");

			values.add(predictedValue);
		} else if(algorithmType == AlgorithmType.MLMaliciousUrls_kNN_with_LevenshteinDistance
				|| algorithmType == AlgorithmType.MLMaliciousUrls_LevenshteinDistance
				|| algorithmType == AlgorithmType.MLMaliciousUrls_NBC){
			PredictedValue predictedValue = new PredictedValue();

			SparkDataSeries ts = new SparkDataSeries();
			ts.setName("timeseries");
			ts.setIsHistogramData(1);
			for(int k=0; k < 60; ++k){
				Date dk = new Date(startTime + k * timeWindowSizeInMilliseconds);
				String label = f.format(dk);
				ts.add(k, label, sequences[0].getValue(k));
			}

			List<String> malurls = new ArrayList<>();
			SparkDataSeries urlCount = new SparkDataSeries();
			urlCount.setName("malicious-urls-count");
			urlCount.setIsHistogramData(0);

			for(int k=0; k < 10; ++k){
				urlCount.add(k, "Word #"+k, sequences[1].getValue(k));
				malurls.add("Word #"+k);
			}

			predictedValue.addFeature("timeseries", JSON.toJSONString(ts, SerializerFeature.BrowserCompatible));
			predictedValue.addFeature("malicious-urls-count", JSON.toJSONString(urlCount, SerializerFeature.BrowserCompatible));
			predictedValue.addFeature("malicious-urls", JSON.toJSONString(malurls, SerializerFeature.BrowserCompatible));

			values.add(predictedValue);
		} else {
			for(int i=0; i < 60; ++i){
				PredictedValue predictedValue = new PredictedValue();
				predictedValue.setDate(startTime + i * timeWindowSizeInMilliseconds);

				if(algorithmType == AlgorithmType.MLAnomaly_IsolationForest){
					predictedValue.setDoubleValue(sequences[0].getValue(i));
					predictedValue.addFeature("critical", ""+sequences[1].getValue(i));
					predictedValue.addFeature("error", ""+sequences[2].getValue(i));
					predictedValue.addFeature("warning", ""+sequences[3].getValue(i));
					predictedValue.addFeature("emergency", ""+sequences[4].getValue(i));
				}
				values.add(predictedValue);
			}
		}

		return values;
	}
}
