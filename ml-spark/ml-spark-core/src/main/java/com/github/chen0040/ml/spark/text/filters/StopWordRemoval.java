package com.github.chen0040.ml.spark.text.filters;

import org.apache.commons.lang.math.NumberUtils;
import sun.net.util.IPAddressUtil;

import java.util.*;

public class StopWordRemoval extends AbstractTextFilter implements Cloneable {
    //Stopwords list from Rainbow
    private HashSet<String> stopWords = new HashSet<>();
    private boolean removeNumbers = true;
    private boolean removeIPAddress = true;

    public void setRemoveNumbers(boolean removeNumbers) {
        this.removeNumbers = removeNumbers;
    }

    public boolean getRemoveNumbers() {

        return removeNumbers;
    }

    public void setRemoveIPAddress(boolean removeIPAddress) {
        this.removeIPAddress = removeIPAddress;
    }

    public boolean getRemoveIPAddress() {
        return removeIPAddress;
    }

    public Object clone() {
        StopWordRemoval clone = new StopWordRemoval();
        clone.removeNumbers = this.removeNumbers;
        clone.removeIPAddress = this.removeIPAddress;
        for (String w : stopWords) {
            clone.stopWords.add(w);
        }
        return clone;
    }

    public boolean filter(String w) {
        return accept(w);
    }


    public StopWordRemoval() {
        String[] defaultStopWords = new String[]
                {
                        "'s",
                        "a",
                        "able",
                        "about",
                        "above",
                        "according",
                        "accordingly",
                        "across",
                        "actually",
                        "after",
                        "afterwards",
                        "again",
                        "against",
                        "all",
                        "allow",
                        "allows",
                        "almost",
                        "alone",
                        "along",
                        "already",
                        "also",
                        "although",
                        "always",
                        "am",
                        "among",
                        "amongst",
                        "an",
                        "and",
                        "another",
                        "any",
                        "anybody",
                        "anyhow",
                        "anyone",
                        "anything",
                        "anyway",
                        "anyways",
                        "anywhere",
                        "apart",
                        "appear",
                        "appreciate",
                        "appropriate",
                        "are",
                        "around",
                        "as",
                        "aside",
                        "ask",
                        "asking",
                        "associated",
                        "at",
                        "available",
                        "away",
                        "awfully",
                        "b",
                        "be",
                        "became",
                        "because",
                        "become",
                        "becomes",
                        "becoming",
                        "been",
                        "before",
                        "beforehand",
                        "behind",
                        "being",
                        "believe",
                        "below",
                        "beside",
                        "besides",
                        "best",
                        "better",
                        "between",
                        "beyond",
                        "both",
                        "brief",
                        "but",
                        "by",
                        "c",
                        "came",
                        "can",
                        "cannot",
                        "cant",
                        "cause",
                        "causes",
                        "certain",
                        "certainly",
                        "changes",
                        "clearly",
                        "co",
                        "com",
                        "come",
                        "comes",
                        "concerning",
                        "consequently",
                        "consider",
                        "considering",
                        "contain",
                        "containing",
                        "contains",
                        "corresponding",
                        "could",
                        "course",
                        "currently",
                        "d",
                        "definitely",
                        "described",
                        "despite",
                        "did",
                        "different",
                        "do",
                        "does",
                        "doing",
                        "done",
                        "down",
                        "downwards",
                        "during",
                        "e",
                        "each",
                        "edu",
                        "eg",
                        "eight",
                        "either",
                        "else",
                        "elsewhere",
                        "enough",
                        "entirely",
                        "especially",
                        "et",
                        "etc",
                        "even",
                        "ever",
                        "every",
                        "everybody",
                        "everyone",
                        "everything",
                        "everywhere",
                        "ex",
                        "exactly",
                        "example",
                        "except",
                        "f",
                        "far",
                        "few",
                        "fifth",
                        "first",
                        "five",
                        "followed",
                        "following",
                        "follows",
                        "for",
                        "former",
                        "formerly",
                        "forth",
                        "four",
                        "from",
                        "further",
                        "furthermore",
                        "g",
                        "get",
                        "gets",
                        "getting",
                        "given",
                        "gives",
                        "go",
                        "goes",
                        "going",
                        "gone",
                        "got",
                        "gotten",
                        "greetings",
                        "h",
                        "had",
                        "happens",
                        "hardly",
                        "has",
                        "have",
                        "having",
                        "he",
                        "hello",
                        "help",
                        "hence",
                        "her",
                        "here",
                        "hereafter",
                        "hereby",
                        "herein",
                        "hereupon",
                        "hers",
                        "herself",
                        "hi",
                        "him",
                        "himself",
                        "his",
                        "hither",
                        "hopefully",
                        "how",
                        "howbeit",
                        "however",
                        "i",
                        "ie",
                        "if",
                        "ignored",
                        "immediate",
                        "in",
                        "inasmuch",
                        "inc",
                        "indeed",
                        "indicate",
                        "indicated",
                        "indicates",
                        "inner",
                        "insofar",
                        "instead",
                        "into",
                        "inward",
                        "is",
                        "it",
                        "its",
                        "itself",
                        "j",
                        "just",
                        "k",
                        "keep",
                        "keeps",
                        "kept",
                        "know",
                        "knows",
                        "known",
                        "l",
                        "last",
                        "lately",
                        "later",
                        "latter",
                        "latterly",
                        "least",
                        "less",
                        "lest",
                        "let",
                        "like",
                        "liked",
                        "likely",
                        "little",
                        "ll", //added to avoid words like you'll,I'll etc.
                        "look",
                        "looking",
                        "looks",
                        "ltd",
                        "m",
                        "mainly",
                        "many",
                        "may",
                        "maybe",
                        "me",
                        "mean",
                        "meanwhile",
                        "merely",
                        "might",
                        "more",
                        "moreover",
                        "most",
                        "mostly",
                        "much",
                        "must",
                        "my",
                        "myself",
                        "n",
                        "name",
                        "namely",
                        "nd",
                        "near",
                        "nearly",
                        "necessary",
                        "need",
                        "needs",
                        "neither",
                        "never",
                        "nevertheless",
                        "new",
                        "next",
                        "nine",
                        "no",
                        "nobody",
                        "non",
                        "none",
                        "noone",
                        "nor",
                        "normally",
                        "not",
                        "nothing",
                        "novel",
                        "now",
                        "nowhere",
                        "o",
                        "obviously",
                        "of",
                        "off",
                        "often",
                        "oh",
                        "ok",
                        "okay",
                        "old",
                        "on",
                        "once",
                        "one",
                        "ones",
                        "only",
                        "onto",
                        "or",
                        "other",
                        "others",
                        "otherwise",
                        "ought",
                        "our",
                        "ours",
                        "ourselves",
                        "out",
                        "outside",
                        "over",
                        "overall",
                        "own",
                        "p",
                        "particular",
                        "particularly",
                        "per",
                        "perhaps",
                        "placed",
                        "please",
                        "plus",
                        "possible",
                        "presumably",
                        "probably",
                        "provides",
                        "q",
                        "que",
                        "quite",
                        "qv",
                        "r",
                        "rather",
                        "rd",
                        "re",
                        "really",
                        "reasonably",
                        "regarding",
                        "regardless",
                        "regards",
                        "relatively",
                        "respectively",
                        "right",
                        "s",
                        "said",
                        "same",
                        "saw",
                        "say",
                        "saying",
                        "says",
                        "second",
                        "secondly",
                        "see",
                        "seeing",
                        "seem",
                        "seemed",
                        "seeming",
                        "seems",
                        "seen",
                        "self",
                        "selves",
                        "sensible",
                        "sent",
                        "serious",
                        "seriously",
                        "seven",
                        "several",
                        "shall",
                        "she",
                        "should",
                        "since",
                        "six",
                        "so",
                        "some",
                        "somebody",
                        "somehow",
                        "someone",
                        "something",
                        "sometime",
                        "sometimes",
                        "somewhat",
                        "somewhere",
                        "soon",
                        "sorry",
                        "specified",
                        "specify",
                        "specifying",
                        "still",
                        "sub",
                        "such",
                        "sup",
                        "sure",
                        "t",
                        "take",
                        "taken",
                        "tell",
                        "tends",
                        "th",
                        "than",
                        "thank",
                        "thanks",
                        "thanx",
                        "that",
                        "thats",
                        "the",
                        "their",
                        "theirs",
                        "them",
                        "themselves",
                        "then",
                        "thence",
                        "there",
                        "thereafter",
                        "thereby",
                        "therefore",
                        "therein",
                        "theres",
                        "thereupon",
                        "these",
                        "they",
                        "think",
                        "third",
                        "this",
                        "thorough",
                        "thoroughly",
                        "those",
                        "though",
                        "three",
                        "through",
                        "throughout",
                        "thru",
                        "thus",
                        "to",
                        "together",
                        "too",
                        "took",
                        "toward",
                        "towards",
                        "tried",
                        "tries",
                        "truly",
                        "try",
                        "trying",
                        "twice",
                        "two",
                        "u",
                        "un",
                        "under",
                        "unfortunately",
                        "unless",
                        "unlikely",
                        "until",
                        "unto",
                        "up",
                        "upon",
                        "us",
                        "use",
                        "used",
                        "useful",
                        "uses",
                        "using",
                        "usually",
                        "uucp",
                        "v",
                        "value",
                        "various",
                        "ve", //added to avoid words like I've,you've etc.
                        "very",
                        "via",
                        "viz",
                        "vs",
                        "w",
                        "want",
                        "wants",
                        "was",
                        "way",
                        "we",
                        "welcome",
                        "well",
                        "went",
                        "were",
                        "what",
                        "whatever",
                        "when",
                        "whence",
                        "whenever",
                        "where",
                        "whereafter",
                        "whereas",
                        "whereby",
                        "wherein",
                        "whereupon",
                        "wherever",
                        "whether",
                        "which",
                        "while",
                        "whither",
                        "who",
                        "whoever",
                        "whole",
                        "whom",
                        "whose",
                        "why",
                        "will",
                        "willing",
                        "wish",
                        "with",
                        "within",
                        "without",
                        "wonder",
                        "would",
                        "would",
                        "x",
                        "y",
                        "yes",
                        "yet",
                        "you",
                        "your",
                        "yours",
                        "yourself",
                        "yourselves",
                        "z",
                        "zero"
                };

        Collections.addAll(stopWords, defaultStopWords);
    }

    public void ResetStopWords() {
        stopWords.clear();
    }

    public void addStopWord(String w) {
        stopWords.add(w);
    }

    public void AddStopWords(String[] words) {
        Collections.addAll(stopWords, words);
    }

    public void AddStopWords(Iterator<String> words) {
        while (words.hasNext()) {
            stopWords.add(words.next());
        }
    }

    public boolean accept(String word) {
        if (word.length() == 1) return false;
        if (removeNumbers && isNumeric(word)) {
            return false;
        }
        if (removeIPAddress && isIPAddress(word)) {
            return false;
        }
        return !stopWords.contains(word);
    }

    private boolean isNumeric(String word) {
        return NumberUtils.isNumber(word);
    }

    private boolean isIPAddress(String word) {
        if (IPAddressUtil.isIPv4LiteralAddress(word)) {
            return true;
        } else if (IPAddressUtil.isIPv6LiteralAddress(word)) {
            return true;
        }
        return false;
    }

    @Override
    public Iterable<String> filter(Iterable<String> words) {
        List<String> result = new ArrayList<>();
        for (String w : words) {
            if (accept(w)) {
                result.add(w);
            }
        }

        return result;
    }

    public List<String> filter(List<String> words) {
        List<String> result = new ArrayList<>();
        for (String w : words) {
            if (accept(w)) {
                result.add(w);
            }
        }

        return result;
    }

    public void load(List<String> words) {
        stopWords.clear();
        join(words);
    }

    public void join(List<String> words) {
        for (String w : words) {
            stopWords.add(w);
        }
    }
}
