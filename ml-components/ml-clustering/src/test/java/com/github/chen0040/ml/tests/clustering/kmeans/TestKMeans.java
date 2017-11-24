package com.github.chen0040.ml.tests.clustering.kmeans;

import com.github.chen0040.ml.clustering.kmeans.KMeans;
import com.github.chen0040.ml.commons.IntelliContext;
import com.github.chen0040.ml.commons.IntelliTuple;
import com.github.chen0040.ml.images.ImageIntelliContextFactory;
import com.github.chen0040.ml.tests.clustering.utils.FileUtils;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by memeanalytics on 20/8/15.
 */
public class TestKMeans {

    private static Random rand = new Random();

    @Test
    public void TestImageSegmentation(){
        BufferedImage img=null;
        try{
            img= ImageIO.read(FileUtils.getResourceFile("1.jpg"));
        }catch(IOException ie)
        {
            ie.printStackTrace();
        }

        IntelliContext batch = ImageIntelliContextFactory.newContext(img);

        KMeans cluster = new KMeans();
        cluster.setAttribute(KMeans.MAX_ITERS, 200);

        cluster.setAttribute(KMeans.K, 25);

        cluster.batchUpdate(batch);

        List<Integer> classColors = new ArrayList<Integer>();
        for(int i=0; i < 5; ++i){
            for(int j=0; j < 5; ++j){
                classColors.add(ImageIntelliContextFactory.get_rgb(255, rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
            }
        }

        BufferedImage class_img = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        for(int x=0; x < img.getWidth(); x++)
        {
            for(int y=0; y < img.getHeight(); y++)
            {
                int rgb = img.getRGB(x, y);

                IntelliTuple tuple = ImageIntelliContextFactory.getPixelTuple(batch, rgb);

                int clusterIndex = cluster.getCluster(tuple);

                rgb = classColors.get(clusterIndex);

                class_img.setRGB(x, y, rgb);
            }
        }
    }
}
