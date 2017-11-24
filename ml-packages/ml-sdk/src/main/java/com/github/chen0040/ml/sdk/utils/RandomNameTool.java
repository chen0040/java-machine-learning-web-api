package com.github.chen0040.ml.sdk.utils;

import org.kohsuke.randname.RandomNameGenerator;
import java.util.Random;

/**
 * Created by memeanalytics on 29/8/15.
 */
public class RandomNameTool {
    private static final Random rand = new Random();

    public static String randomName(){
        return new RandomNameGenerator(rand.nextInt()).next();
    }
}
