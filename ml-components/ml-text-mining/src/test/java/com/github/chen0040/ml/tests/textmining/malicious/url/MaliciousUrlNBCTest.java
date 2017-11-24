package com.github.chen0040.ml.tests.textmining.malicious.url;

import com.github.chen0040.ml.textmining.malurls.MaliciousUrlNBC;
import com.github.chen0040.ml.textmining.commons.UrlData;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Created by chen0469 on 11/7/2015 0007.
 */
public class MaliciousUrlNBCTest {
    @Test
    public void testTrainNBC(){


        List<UrlData> dataSet = ARFFUtils.loadSample();

        MaliciousUrlNBC method = new MaliciousUrlNBC();

        method.trainByLabelledUrlDataSet(dataSet);

        double accuracy = 0;
        for(int i=0; i < dataSet.size(); ++i){
            UrlData url = dataSet.get(i);

            boolean correct = (method.isMalicious(url) && url.getResult() == 1) ||
                    (!method.isMalicious(url) && url.getResult()==-1);
            accuracy += (correct ? 1 : 0);
        }

        accuracy /= dataSet.size();

        System.out.println("Training Accuracy: "+accuracy);
        assertTrue(accuracy > 0.8);
    }


}
