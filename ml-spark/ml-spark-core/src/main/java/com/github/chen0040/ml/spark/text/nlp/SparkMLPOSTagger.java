package com.github.chen0040.ml.spark.text.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

public class SparkMLPOSTagger {

	private POSTaggerME posTagger;

	public SparkMLPOSTagger() {
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			File posModelFile = new File(classLoader.getResource("en-pos-maxent.bin").getFile());

			FileInputStream posModelStream = new FileInputStream(posModelFile);
			POSModel posModel = new POSModel(posModelStream);
			posTagger = new POSTaggerME(posModel);
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}

	public String[] tag(String[] words) {
		String[] result = posTagger.tag(words);
		return result;
	}

}
