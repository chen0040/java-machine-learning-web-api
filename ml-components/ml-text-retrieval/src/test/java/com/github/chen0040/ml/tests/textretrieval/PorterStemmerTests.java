package com.github.chen0040.ml.tests.textretrieval;

import com.github.chen0040.ml.textretrieval.filters.PorterStemmer;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 9/10/15.
 */
public class PorterStemmerTests {
    @Test
    public void testBasic(){
        PorterStemmer stemmer = new PorterStemmer();

        /// Step 1:
        ///     caresses  ->  caress
        ///     ponies    ->  poni
        ///     ties      ->  ti
        ///     caress    ->  caress
        ///     cats      ->  cat
        ///     feed      ->  feed
        ///     agreed    ->  agree
        ///     disabled  ->  disable
        ///     matting   ->  mat
        ///     mating    ->  mate
        ///     meeting   ->  meet
        ///     milling   ->  mill
        ///     messing   ->  mess
        ///     meetings  ->  meet
        List<String> words = new ArrayList<>();
        String[] x = new String[]
        {
            "caresses",
                    "ponies",
                    "ties",
                    "caress",
                    "cats",
                    "feed",
                    "agreed",
                    "disabled",
                    "matting",
                    "mating",
                    "meeting",
                    "milling",
                    "messing",
                    "meetings"
        };

        for(int i=0; i < x.length; ++i){
            words.add(x[i]);
        }

        List<String> result = stemmer.filter(words);
        for (int i = 0; i < words.size(); ++i)
        {
            System.out.println(String.format("%s -> %s", words.get(i), result.get(i)));
        }
    }
}
