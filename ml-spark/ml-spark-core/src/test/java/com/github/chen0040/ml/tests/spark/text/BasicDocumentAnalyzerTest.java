package com.github.chen0040.ml.tests.spark.text;

import com.github.chen0040.ml.spark.text.analyzers.BasicDocumentAnalyzer;
import com.github.chen0040.ml.spark.text.tokenizers.BasicTokenizer;
import com.github.chen0040.ml.spark.utils.ESFactory;
import com.github.chen0040.ml.tests.spark.MLearnTestCase;
import com.github.chen0040.ml.tests.spark.utils.FileUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by root on 9/15/15.
 */
public class BasicDocumentAnalyzerTest extends MLearnTestCase {
    public BasicDocumentAnalyzerTest(){
        super("BasicDocumentAnalyzer");
    }

    private static Function<String, Iterable<String>> tokenize = new Function<String, Iterable<String>>() {
        public Iterable<String> call(String s) throws Exception {
            BasicTokenizer tokenizer = new BasicTokenizer();
            return BasicTokenizer.tokenize(s);
        }
    };

    @Test
    public void testAnalyzer(){
        JavaRDD<String> lines = context.textFile(FileUtils.getResourceFile("text.analyzer.txt").getAbsolutePath());
        JavaRDD<Iterable<String>> tokens = lines.map(tokenize);

        BasicDocumentAnalyzer analyzer = new BasicDocumentAnalyzer();
        JavaRDD<Iterable<String>> filtered = analyzer.analyze(tokens);

        List<String> input = lines.collect();
        List<Iterable<String>> output = filtered.collect();

        for(int i=0; i < output.size(); ++i){
            Iterable<String> line = output.get(i);
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(String word : line){
                if(first){
                    first = false;
                }else{
                    sb.append(" ");
                }
                sb.append(word);
            }
            System.out.println("input: " + input.get(i));
            System.out.println("output: " + sb.toString());
        }

    }

    @Test
    public void testJson(){

        InputStream reader = null;
        try {
            reader = new FileInputStream(FileUtils.getResourceFile("json_v0.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JavaRDD<String> lines = ESFactory.getDefault().readMessages(context, reader);

        JavaRDD<Iterable<String>> tokens = lines.map(tokenize);

        BasicDocumentAnalyzer analyzer = new BasicDocumentAnalyzer();
        JavaRDD<Iterable<String>> filtered = analyzer.analyze(tokens);

        List<String> input = lines.collect();
        List<Iterable<String>> output = filtered.collect();

        for(int i=0; i < output.size(); ++i){
            Iterable<String> line = output.get(i);
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(String word : line){
                if(first){
                    first = false;
                }else{
                    sb.append(" ");
                }
                sb.append(word);
            }
            System.out.println("input: " + input.get(i));
            System.out.println("output: " + sb.toString());
        }
    }
}
