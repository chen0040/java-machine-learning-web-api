package com.github.chen0040.ml.tests.ann.clustering;

import com.github.chen0040.ml.ann.art.mas.utils.FileUtils;
import com.github.chen0040.ml.ann.art.clustering.ART1Clustering;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.sk.dom.xmldom.XmlService;
import org.testng.annotations.Test;

/**
 * Created by memeanalytics on 22/8/15.
 */
public class TestART1 {
    @Test
    public void testData(){
        IntelliContext batch = new IntelliContext();
        batch.readDoc(FileUtils.getResourceFile("data.xml"), XmlService.getInstance());

        //System.out.println(batch);

        ART1Clustering algorithm = new ART1Clustering();
        algorithm.batchUpdate(batch);

        for(int i = 0; i < batch.tupleCount(); ++i){
            System.out.println(batch.tupleAtIndex(i));
        }

    }
}
