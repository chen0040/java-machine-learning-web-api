package com.github.chen0040.ml.tests.spark.text;

import com.github.chen0040.ml.spark.text.nlp.SparkMLPOSTagger;
import com.github.chen0040.ml.spark.text.tokenizers.BasicTokenizer;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.testng.annotations.Test;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 9/15/15.
 */
public class POSTaggerTest extends MLearnTestCase {
    public POSTaggerTest(){
        super("POSTagging");
    }

    private static Function<String, Iterable<String>> tokenize = new Function<String, Iterable<String>>() {
        public Iterable<String> call(String s) throws Exception {
            BasicTokenizer tokenizer = new BasicTokenizer();
            return BasicTokenizer.tokenize(s);
        }
    };

    private static SparkMLPOSTagger tagger = new SparkMLPOSTagger();

    private static PairFunction<Iterable<String>, String[], String[]> tag = new PairFunction<Iterable<String>, String[], String[]>() {
        public Tuple2<String[], String[]> call(Iterable<String> s) throws Exception {

            List<String> s2 = new ArrayList<>();
            for (String w : s) {
                s2.add(w);
            }
            String[] words = new String[s2.size()];
            for (int i = 0; i < words.length; ++i) {
                words[i] = s2.get(i);
            }
            String[] tags = tagger.tag(words);
            return new Tuple2<String[], String[]>(words, tags);
        }
    };

    @Test
    public void testSimple(){
        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("text.analyzer.txt").getAbsolutePath());
        JavaRDD<Iterable<String>> tokens = lines.map(tokenize);



        JavaPairRDD<String[], String[]> tagged = tokens.mapToPair(tag);

        List<Tuple2<String[], String[]>> output = tagged.collect();

        for(int i=0; i < output.size(); ++i){
            Tuple2<String[], String[]> tuple = output.get(i);
            String[] words = tuple._1;
            String[] tags = tuple._2;

            for(int j=0; j < words.length; ++j)
            {
                System.out.print(words[j] + "/" + tags[j] + " ");
            }
            System.out.println();
        }
    }
}
