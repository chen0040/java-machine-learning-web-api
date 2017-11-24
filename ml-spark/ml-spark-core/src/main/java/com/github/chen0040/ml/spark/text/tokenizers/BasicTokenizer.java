package com.github.chen0040.ml.spark.text.tokenizers;

/**
 * Created by root on 9/15/15.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenizes strings described in English.
 *
 * Modified from the sample code provided by Jun Araki (link: http://junaraki.net/blog/en/2010/07/17/353.html)
 */
public class BasicTokenizer {


    /** A regular expression for letters and numbers. */
    private static final String regexLetterNumber = "[a-zA-Z0-9]";

    /** A regular expression for non-letters and non-numbers. */
    private static final String regexNotLetterNumber = "[^a-zA-Z0-9]";

    /** A regular expression for separators. */
    private static final String regexSeparator = "[\\?!()\";/\\|`]";

    /** A regular expression for separators. */
    private static final String regexClitics =
            "'|:|-|'S|'D|'M|'LL|'RE|'VE|N'T|'s|'d|'m|'ll|'re|'ve|n't";

    /** Abbreviations. */
    private static final List<String> abbrList =
            Arrays.asList("Co.", "Corp.", "vs.", "e.g.", "etc.", "ex.", "cf.",
                    "eg.", "Jan.", "Feb.", "Mar.", "Apr.", "Jun.", "Jul.", "Aug.",
                    "Sept.", "Oct.", "Nov.", "Dec.", "jan.", "feb.", "mar.",
                    "apr.", "jun.", "jul.", "aug.", "sept.", "oct.", "nov.",
                    "dec.", "ed.", "eds.", "repr.", "trans.", "vol.", "vols.",
                    "rev.", "est.", "b.", "m.", "bur.", "d.", "r.", "M.", "Dept.",
                    "MM.", "U.", "Mr.", "Jr.", "Ms.", "Mme.", "Mrs.", "Dr.",
                    "Ph.D.");

    public BasicTokenizer() {

    }

    /**
     * Tokenizes a string using the algorithms by Grefenstette (1999) and
     * Palmer (2000).
     */
    public static Iterable<String> tokenize(String str) {

        List<String> tokenList = new ArrayList<>();

        // Changes tabs into spaces.
        str = str.replaceAll("\\t", " ");

        // Puts blanks around unambiguous separators.
        str = str.replaceAll("(" + regexSeparator + ")", " $1 ");

        // Puts blanks around commas
        str = str.replaceAll("([^\\s]),", "$1 ,");
        str = str.replaceAll(",([^\\s])", " , $1");

        // Distinguishes single quotes from apstrophes by segmenting off
        // single quotes not preceded by letters.
        str = str.replaceAll("^(')", "$1 ");
        str = str.replaceAll("(" + regexNotLetterNumber + ")'", "$1 '");

        // Segments off unambiguous word-final clitics and punctuations.
        str = str.replaceAll("(" + regexClitics + ")$", " $1");
        str = str.replaceAll(
                "(" + regexClitics + ")(" + regexNotLetterNumber + ")",
                " $1 $2");

        // Deals with periods.
        String[] words = str.trim().split("\\s+");
        Pattern p1 = Pattern.compile(".*" + regexLetterNumber + "\\.");
        Pattern p2 = Pattern.compile(
                "^([A-Za-z]\\.([A-Za-z]\\.)+|[A-Z][bcdfghj-nptvxz]+\\.)$");
        for (String word : words) {
            Matcher m1 = p1.matcher(word);
            Matcher m2 = p2.matcher(word);
            if (m1.matches() && !abbrList.contains(word) && !m2.matches()) {
                // Segments off the period.
                tokenList.add(word.substring(0, word.length() - 1));
                tokenList.add(word.substring(word.length() - 1));
            } else {
                tokenList.add(word);
            }
        }

        return tokenList;
    }

}
