package com.github.chen0040.ml.webclient.tests;

import com.alibaba.fastjson.JSON;
import com.github.chen0040.ml.commons.tables.DataTable;
import com.github.chen0040.ml.webclient.jersey.IntelliSession;
import com.github.chen0040.ml.commons.tables.DataRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Created by xschen on 10/7/2016.
 */
public class WebClientTests {

    public static final String PROJECTS = "projects";

    private static Logger logger = LoggerFactory.getLogger(WebClientTests.class);

    private IntelliSession session;
    private String name = "project1";
    private String dataName = "data1";

    private static final String nbcId = "nbc";
    private static final String kmeansId = "kmeans";
    private static final String svrId = "svr";
    private static final String glmId = "glm";
    private static final String iforestId = "isolation-forest";

    private static final String url = "http://localhost:8900";


    private DataTable createTable(){
        DataTable table = new DataTable();
        table.setName(dataName);
        table.setId(dataName);


        for(int i=0; i < 13; ++i){
            table.columns().add("c"+i);
        }


        table.addRow("+1", 0.708333, 1, 1, -0.320755, -0.105023, -1, 1, -0.419847, -1, -0.225806, 1, -1);
        table.addRow("-1", 0.583333, -1, 0.333333, -0.603774, 1, -1, 1, 0.358779, -1, -0.483871, -1, 1);
        table.addRow("+1", 0.166667, 1, -0.333333, -0.433962, -0.383562, -1, -1, 0.0687023, -1, -0.903226, -1, -1, 1);
        table.addRow("-1", 0.458333, 1, 1, -0.358491, -0.374429, -1, -1, -0.480916, 1, -0.935484, -0.333333, 1);
        table.addRow("-1", 0.875, -1, -0.333333, -0.509434, -0.347032, -1, 1, -0.236641, 1, -0.935484, -1, -0.333333, -1);
        table.addRow("-1", 0.5, 1, 1, -0.509434, -0.767123, -1, -1, 0.0534351, -1, -0.870968, -1, -1, 1);
        table.addRow("+1", 0.125, 1, 0.333333, -0.320755, -0.406393, 1, 1, 0.0839695, 1, -0.806452, -0.333333, 0.5);
        table.addRow("+1", 0.25, 1, 1, -0.698113, -0.484018, -1, 1, 0.0839695, 1, -0.612903, -0.333333, 1);
        table.addRow("+1", 0.291667, 1, 1, -0.132075, -0.237443, -1, 1, 0.51145, -1, -0.612903, 0.333333, 1);
        table.addRow("+1", 0.416667, -1, 1, 0.0566038, 0.283105, -1, 1, 0.267176, -1, 0.290323, 1, 1);
        table.addRow("-1", 0.25, 1, 1, -0.226415, -0.506849, -1, -1, 0.374046, -1, -0.83871, -1, 1);
        table.addRow("-1", 1, 1, -0.0943396, -0.543379, -1, 1, -0.389313, 1, -1, -1, -1, 1);
        table.addRow("-1", -0.375, 1, 0.333333, -0.132075, -0.502283, -1, 1, 0.664122, -1, -1, -1, -1, -1);
        table.addRow("+1", 0.333333, 1, -1, -0.245283, -0.506849, -1, -1, 0.129771, -1, -0.16129, 0.333333, -1);
        table.addRow("-1", 0.166667, -1, 1, -0.358491, -0.191781, -1, 1, 0.343511, -1, -1, -1, -0.333333, -1);
        table.addRow("-1", 0.75, -1, 1, -0.660377, -0.894977, -1, -1, -0.175573, -1, -0.483871, -1, -1);
        table.addRow("+1", -0.291667, 1, 1, -0.132075, -0.155251, -1, -1, -0.251908, 1, -0.419355, 0.333333, 1);
        table.addRow("+1", 1, 1, -0.132075, -0.648402, 1, 1, 0.282443, 1, 1, -1, 1);
        table.addRow("-1", 0.458333, 1, -1, -0.698113, -0.611872, -1, 1, 0.114504, 1, -0.419355, -1, -1);
        table.addRow("-1", -0.541667, 1, -1, -0.132075, -0.666667, -1, -1, 0.633588, 1, -0.548387, -1, -1, 1);
        table.addRow("+1", 0.583333, 1, 1, -0.509434, -0.52968, -1, 1, -0.114504, 1, -0.16129, 0.333333, 1);
        table.addRow("-1", -0.208333, 1, -0.333333, -0.320755, -0.456621, -1, 1, 0.664122, -1, -0.935484, -1, -1);
        table.addRow("-1", -0.416667, 1, 1, -0.603774, -0.191781, -1, -1, 0.679389, -1, -0.612903, -1, -1);
        table.addRow("-1", -0.25, 1, 1, -0.660377, -0.643836, -1, -1, 0.0992366, -1, -0.967742, -1, -1, -1);
        table.addRow("-1", 0.0416667, -1, -0.333333, -0.283019, -0.260274, 1, 1, 0.343511, 1, -1, -1, -0.333333, -1);
        table.addRow("-1", -0.208333, -1, 0.333333, -0.320755, -0.319635, -1, -1, 0.0381679, -1, -0.935484, -1, -1, -1);
        table.addRow("-1", -0.291667, -1, 1, -0.169811, -0.465753, -1, 1, 0.236641, 1, -1, -1, -1);
        table.addRow("-1", -0.0833333, -1, 0.333333, -0.509434, -0.228311, -1, 1, 0.312977, -1, -0.806452, -1, -1, -1);
        table.addRow("+1", 0.208333, 1, 0.333333, -0.660377, -0.525114, -1, 1, 0.435115, -1, -0.193548, -0.333333, 1);
        table.addRow("-1", 0.75, -1, 0.333333, -0.698113, -0.365297, 1, 1, -0.0992366, -1, -1, -1, -0.333333, -1);
        table.addRow("+1", 0.166667, 1, 0.333333, -0.358491, -0.52968, -1, 1, 0.206107, -1, -0.870968, -0.333333, 1);
        table.addRow("-1", 0.541667, 1, 1, 0.245283, -0.534247, -1, 1, 0.0229008, -1, -0.258065, -1, -1, 0.5);
        table.addRow("-1", -0.666667, -1, 0.333333, -0.509434, -0.593607, -1, -1, 0.51145, -1, -1, -1, -1, -1);
        table.addRow("+1", 0.25, 1, 1, 0.433962, -0.086758, -1, 1, 0.0534351, 1, 0.0967742, 1, -1, 1);
        table.addRow("+1", -0.125, 1, 1, -0.0566038, -0.6621, -1, 1, -0.160305, 1, -0.709677, -1, 1);
        table.addRow("+1", -0.208333, 1, 1, -0.320755, -0.406393, 1, 1, 0.206107, 1, -1, -1, 0.333333, 1);
        table.addRow("+1", 0.333333, 1, 1, -0.132075, -0.630137, -1, 1, 0.0229008, 1, -0.387097, -1, -0.333333, 1);
        table.addRow("+1", 0.25, 1, -1, 0.245283, -0.328767, -1, 1, -0.175573, -1, -1, -1, -1, -1);
        table.addRow("-1", -0.458333, 1, 0.333333, -0.320755, -0.753425, -1, -1, 0.206107, -1, -1, -1, -1, -1);
        table.addRow("-1", -0.208333, 1, 1, -0.471698, -0.561644, -1, 1, 0.755725, -1, -1, -1, -1, -1);
        table.addRow("+1", -0.541667, 1, 1, 0.0943396, -0.557078, -1, -1, 0.679389, -1, -1, -1, -1, 1);
        table.addRow("-1", 0.375, -1, 1, -0.433962, -0.621005, -1, -1, 0.40458, -1, -1, -1, -1, -1);
        table.addRow("-1", -0.375, 1, 0.333333, -0.320755, -0.511416, -1, -1, 0.648855, 1, -0.870968, -1, -1, -1);
        table.addRow("-1", -0.291667, 1, -0.333333, -0.867925, -0.675799, 1, -1, 0.29771, -1, -1, -1, -1, 1);
        table.addRow("+1", 0.25, 1, 0.333333, -0.396226, -0.579909, 1, -1, -0.0381679, -1, -0.290323, -0.333333, 0.5);
        table.addRow("-1", 0.208333, 1, 0.333333, -0.132075, -0.611872, 1, 1, 0.435115, -1, -1, -1, -1, -1);
        table.addRow("+1", -0.166667, 1, 0.333333, -0.54717, -0.894977, -1, 1, -0.160305, -1, -0.741935, -1, 1, -1);
        table.addRow("+1", -0.375, 1, 1, -0.698113, -0.675799, -1, 1, 0.618321, -1, -1, -1, -0.333333, -1);
        table.addRow("+1", 0.541667, 1, -0.333333, 0.245283, -0.452055, -1, -1, -0.251908, 1, -1, 1, 0.5);
        table.addRow("+1", 0.5, -1, 1, 0.0566038, -0.547945, -1, 1, -0.343511, -1, -0.677419, 1, 1);


        return table;
    }

    @Test
    public void testNbcClassifier(){

        session = new IntelliSession(url, name);

        DataTable table = createTable();
        session = session.trainNBC(nbcId, table);

        for(DataRow row : table.rows()) {
            DataRow predictedRow = session.predict(nbcId, row);
            logger.info(JSON.toJSONString(predictedRow));
        }

    }

    @Test
    public void testKMeansClustering(){

        session = new IntelliSession(url, name);

        DataTable table = createTable();
        session = session.trainKMeans(kmeansId, table);

        for(DataRow row : table.rows()) {
            DataRow predictedRow = session.predict(kmeansId, row);
            logger.info(JSON.toJSONString(predictedRow));
        }
    }

    @Test
    public void testSvmRegression(){

        session = new IntelliSession(url, name);

        DataTable table = createTable();
        session = session.trainSVR(svrId, table);

        for(DataRow row : table.rows()) {
            DataRow predictedRow = session.predict(svrId, row);
            logger.info(JSON.toJSONString(predictedRow));
        }
    }

    @Test
    public void testGlmRegression(){

        session = new IntelliSession(url, name);

        DataTable table = createTable();
        session = session.trainGLM(glmId, table);

        for(DataRow row : table.rows()) {
            DataRow predictedRow = session.predict(glmId, row);
            logger.info(JSON.toJSONString(predictedRow));
        }
    }

    @Test
    public void testIForestAnomalyDetection(){

        session = new IntelliSession(url, name);

        DataTable table = createTable();
        session = session.trainIsolationForest(iforestId, table);

        for(DataRow row : table.rows()) {
            DataRow predictedRow = session.predict(iforestId, row);
            logger.info(JSON.toJSONString(predictedRow));
        }
    }


}
