package com.github.chen0040.ml.textmining.distance;

/**
 * Created by root on 10/15/15.
 */
public class TFIDF {

    public static int getTermFrequency(String term, String doc){
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = doc.indexOf(term, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += term.length();

            }
        }

        return count;
    }

    public static double getInverseDocumentFrequency(String term, String[] docs){
        int N = docs.length;
        int n_t = 0;
        for(int i=0; i < N; ++i){
            n_t += (docs[i].contains(term) ? 1 : 0);
        }
        return Math.log(N / n_t);
    }

    public static double getTFIDF(String term, String doc, String[] docs){
        int tf = getTermFrequency(term, doc);
        double idf = getInverseDocumentFrequency(term, docs);
        return tf * idf;
    }

    public static double[] getTFIDF(String[] terms, String doc, String[] docs){
        int K = terms.length;
        double[] v = new double[K];
        for(int k=0; k < K; ++k){
            v[k] = getTFIDF(terms[k], doc, docs);
        }
        return v;
    }

    public static double[][] getTFIDF(String[] terms, String[] docs){
        int N = docs.length;
        double[][] V = new double[N][];
        for(int i=0; i < N; ++i){
            V[i] = getTFIDF(terms, docs[i], docs);
        }
        return V;
    }
}
