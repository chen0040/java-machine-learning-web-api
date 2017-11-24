package com.github.chen0040.ml.tests.spark.text.topicmodeling;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.text.topicmodeling.LdaBatchUpdateResult;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import com.github.chen0040.ml.spark.text.tokenizers.BasicTokenizer;
import com.github.chen0040.ml.spark.text.topicmodeling.Lda;
import org.apache.spark.api.java.JavaRDD;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by memeanalytics on 3/11/15.
 */
public class LdaTest extends MLearnTestCase {

    private static Lda method;
    private List<String> vocabulary;

    public LdaTest(){
        super("Lda");
    }

    @Override
    public void setup(){
        super.setup();
        method = new Lda();

        List<String> stopWords = new ArrayList<>();

        File file = FileUtils.getResourceFile("stoplist.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while((line=reader.readLine())!=null){
                String word = line.trim();
                if(!word.equals("")){
                    stopWords.add(word);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        method.addStopWords(stopWords);
    }



    private JavaRDD<SparkMLTuple> createBatch(){

        File file = FileUtils.getResourceFile("documents.txt");

        JavaRDD<SparkMLTuple> batch=null;

        try {
            JavaRDD<String> lines = context.textFile(file.getCanonicalPath());
            batch = lines.filter(line->!line.trim().equals("")).map(line -> {
                line = line.trim();

                int docID = 0;
                String docDate = "";
                String[] fields = line.split("\t");
                String text = fields[0];  // Assume there's just one field, the text
                if (fields.length == 3) {  // If it's in [ID]\t[TAG]\t[TEXT] format...
                    //docID = Integer.parseInt(fields[0]);
                    //docDate = fields[1]; // do not interpret date as anything but a string
                    text = fields[2];
                }

                SparkMLTuple tuple = new SparkMLTuple();
                BasicTokenizer bt = new BasicTokenizer();
                for(String word : bt.tokenize(text)) {
                    tuple.add(word);
                }

                return tuple;

            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        return batch;
    }

    @Test
    public void testLDA(){

        JavaRDD<SparkMLTuple> batch = createBatch();

        method.setAttribute(Lda.TOPIC_COUNT, 20);
        method.setAttribute(Lda.MAX_VOCABULARY_SIZE, 1000000);
        LdaBatchUpdateResult result = (LdaBatchUpdateResult)method.batchUpdate(batch);

        int topicCount = result.topicCount();

        System.out.println("Topic Count: "+topicCount);

        /*
        File modelFile = new File("myLDAModel");
        if(!modelFile.exists()){
            ldaModel.save(context.sc(), "myLDAModel");
            DistributedLDAModel sameModel = DistributedLDAModel.load(context.sc(), "myLDAModel");
        }
        */

    }



}
