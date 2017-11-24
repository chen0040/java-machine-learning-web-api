package com.github.chen0040.ml.textmining.commons.es;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 9/7/15.
 */
public class ESHit {
    public String _index;
    public String _type;
    public String _id;
    public double _score;
    public ESSource _source = new ESSource();

    public int getCountByRegexMatch(String regex) {
        String str = _source.Message;
        int count = 0;

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(str);

        while(matcher.find()){
            count++;
        }

        return count;
    }

    public int getCountByKeyword(String keyword) {
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

    public int getSeverityCount(String keyword){
        return _source.Severity.equalsIgnoreCase(keyword) ? 1 : 0;
    }
}
