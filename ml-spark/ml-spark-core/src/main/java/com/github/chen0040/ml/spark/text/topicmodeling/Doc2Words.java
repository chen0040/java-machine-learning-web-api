package com.github.chen0040.ml.spark.text.topicmodeling;

import com.github.chen0040.ml.spark.core.SparkMLTuple;
import com.github.chen0040.ml.spark.text.filters.StopWordRemoval;
import com.github.chen0040.ml.spark.text.tokenizers.BasicTokenizer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import sun.net.util.IPAddressUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 11/2/15.
 */
public class Doc2Words implements Serializable {
    public List<String> wordsFromDoc(SparkMLTuple tuple){
        List<String> result = new ArrayList<>();
        List<String> ss = tuple.toBagOfWords();

        StopWordRemoval removal = new StopWordRemoval();
        for (int i = 0; i < ss.size(); ++i) {
            String s = ss.get(i);
            Iterable<String> words = BasicTokenizer.tokenize(s);
            words = removal.filter(words);
            for (String w : words) {

                if (NumberUtils.isNumber(w)) continue;
                if (IPAddressUtil.isIPv4LiteralAddress(w)) continue;
                if (IPAddressUtil.isIPv6LiteralAddress(w)) continue;
                if (w.length() < 4 && !StringUtils.isAlpha(w)) continue;

                result.add(w);
            }
        }

        return result;
    }
}
