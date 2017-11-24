package com.github.chen0040.ml.sdk.models.es;

/**
 * Created by root on 9/7/15.
 */
public class ESHit {
    public String _index;
    public String _type;
    public String _id;
    public double _score;
    public ESSource _source = new ESSource();

    public int getCount(String keyword) {
        String str = _source.Message;
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(keyword, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += keyword.length();

            }
        }

        return count;
    }
}
