package com.github.chen0040.ml.textretrieval.filters;

import java.util.ArrayList;
import java.util.List;

public class PorterStemmer extends AbstractTextFilter {
    @Override
    public List<String> filter(List<String> words) {
        List<String> result = new ArrayList<>();
        for (String word : words) {
            result.add(Stem(word));
        }
        return result;
    }

    public static String Stem(String word) {
        char[] b = word.toCharArray();

        int[] jk = new int[2];
        jk[0] = 0;
        jk[1] = 0;

        jk[1] = b.length - 1;
        if (jk[1] > 1) {
            Step1(b, jk);
            Step2(b, jk);
            Step3(b, jk);
            Step4(b, jk);
            Step5(b, jk);
            Step6(b, jk);
        }

        return new String(b, 0, jk[1] + 1);
    }

    /// <summary>
    ///  m() measures the number of consonant sequences between 0 and j. if c is
    /// a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
    /// presence,
    /// <c><v>       gives 0
    /// <c>vc<v>     gives 1
    /// <c>vcvc<v>   gives 2
    /// <c>vcvcvc<v> gives 3
    /// ....
    /// </summary>
    /// <param name="j"></param>
    /// <returns></returns>
    private static int GetConsonantSeqCount(char[] b, int j) {
        int n = 0;
        int i = 0;

        // check one vowel sequence
        while (true) {
            if (i > j) return n;
            if (!IsConsonant(b, i)) break;
            i++;
        }

        i++;

        while (true) {
            // check one constant sequence
            while (true) {
                if (i > j) return n;
                if (IsConsonant(b, i)) break;
                i++;
            }
            i++;

            n++; // now one vowel sequence followed by one constant sequence is found, increment by 1

            // check one vowel sequence
            while (true) {
                if (i > j) return n;
                if (!IsConsonant(b, i)) break;
                i++;
            }
            i++;
        }
    }

    private static boolean IsConsonant(char[] b, int i) {
        switch (b[i]) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return false;
            case 'y':
                return (i == 0) ? true : !IsConsonant(b, i - 1);
            default:
                return true;
        }
    }

    /// <summary>
    /// vowelinstem() is true <=> 0,...j contains a vowel
    /// </summary>
    private static boolean ContainsVowel(char[] b, int j) {
        int i;
        for (i = 0; i <= j; i++) if (!IsConsonant(b, i)) return true;
        return false;
    }

    /// <summary>
    /// cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
    /// and also if the second c is not w,x or y. this is used when trying to
    /// restore an e at the end of a short word. e.g.
    ///
    /// cav(e), lov(e), hop(e), crim(e), but
    /// snow, box, tray.
    /// </summary>
    /// <param name="b"></param>
    /// <param name="i"></param>
    /// <returns></returns>
    private static boolean IsConsonantVowelConsonant(char[] b, int i) {
        if (i < 2 || !IsConsonant(b, i) || IsConsonant(b, i - 1) || !IsConsonant(b, i - 2)) return false;
        {
            int ch = b[i];
            if (ch == 'w' || ch == 'x' || ch == 'y') return false;
        }
        return true;
    }

    private static boolean EndsWith(char[] b, int[] jk, String s) {
        int l = s.length();
        int o = jk[1] - l + 1;
        if (o < 0) return false;
        for (int i = 0; i < l; i++) if (b[o + i] != s.charAt(i)) return false;
        jk[0] = jk[1] - l;
        return true;
    }

    private static void Replace(char[] b, int[] jk, String s) {
        int l = s.length();
        int o = jk[0] + 1;
        for (int i = 0; i < l; i++) b[o + i] = s.charAt(i);
        jk[1] = jk[0] + l;
    }

    /// <summary>
    /// doublec(j) is true <=> j,(j-1) contain a double consonant.
    /// </summary>
    /// <param name="b"></param>
    /// <param name="j"></param>
    /// <returns></returns>
    private static boolean IsDoubleConsonant(char[] b, int j) {
        if (j < 1) return false;
        if (b[j] != b[j - 1]) return false;
        return IsConsonant(b, j);
    }


    /// <summary>
    /// Step1() gets rid of plurals and -ed or -ing. e.g.
    ///
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
    /// </summary>
    /// <param name="b"></param>
    /// <param name="k"></param>
    private static void Step1(char[] b, int[] jk) {
        if (b[jk[1]] == 's') {
            if (EndsWith(b, jk, "sses")) {
                jk[1] -= 2;
            } else {
                if (EndsWith(b, jk, "ies")) {
                    Replace(b, jk, "i");
                } else {
                    if (b[jk[1] - 1] != 's') jk[1]--;
                }
            }
        }

        if (EndsWith(b, jk, "eed")) {
            if (GetConsonantSeqCount(b, jk[0]) > 0) jk[1]--;
        } else {
            if ((EndsWith(b, jk, "ed") || EndsWith(b, jk, "ing")) && ContainsVowel(b, jk[0])) {
                jk[1] = jk[0];
                if (EndsWith(b, jk, "at")) Replace(b, jk, "ate");
                else {
                    if (EndsWith(b, jk, "bl")) {
                        Replace(b, jk, "ble");
                    } else {
                        if (EndsWith(b, jk, "iz")) Replace(b, jk, "ize");
                        else {
                            if (IsDoubleConsonant(b, jk[1])) {
                                jk[1]--;
                                {
                                    int ch = b[jk[1]];
                                    if (ch == 'l' || ch == 's' || ch == 'z') jk[1]++;
                                }
                            } else if (GetConsonantSeqCount(b, jk[0]) == 1 && IsConsonantVowelConsonant(b, jk[1])) {
                                Replace(b, jk, "e");
                            }
                        }
                    }
                }
            }
        }
    }

    /// <summary>
    /// Step2() turns terminal y to i when there is another vowel in the stem.
    /// </summary>
    /// <param name="b"></param>
    /// <param name="j"></param>
    /// <param name="k"></param>
    private static void Step2(char[] b, int[] jk) {
        int j = jk[0];
        int k = jk[1];

        if (EndsWith(b, jk, "y") && ContainsVowel(b, j)) {
            b[k] = 'i';
        }
    }

    private static void r(char[] b, int[] jk, String s) {
        int j = jk[0];
        int k = jk[1];

        if (GetConsonantSeqCount(b, j) > 0) {
            Replace(b, jk, s);
        }
    }

    /// <summary>
    /// Step3() maps double suffices to single ones. so -ization ( = -ize plus -ation) maps to -ize etc. note that the String before the suffix must give m() > 0.
    /// </summary>
    /// <param name="b"></param>
    /// <param name="j"></param>
    /// <param name="k"></param>
    private static void Step3(char[] b, int[] jk) {
        if (jk[1] == 0) return;

        switch (b[jk[1] - 1]) {
            case 'a':
                if (EndsWith(b, jk, "ational")) {
                    r(b, jk, "ate");
                    break;
                }
                if (EndsWith(b, jk, "tional")) {
                    r(b, jk, "tion");
                    break;
                }
                break;
            case 'c':
                if (EndsWith(b, jk, "enci")) {
                    r(b, jk, "ence");
                    break;
                }
                if (EndsWith(b, jk, "anci")) {
                    r(b, jk, "ance");
                    break;
                }
                break;
            case 'e':
                if (EndsWith(b, jk, "izer")) {
                    r(b, jk, "ize");
                    break;
                }
                break;
            case 'l':
                if (EndsWith(b, jk, "bli")) {
                    r(b, jk, "ble");
                    break;
                }
                if (EndsWith(b, jk, "alli")) {
                    r(b, jk, "al");
                    break;
                }
                if (EndsWith(b, jk, "entli")) {
                    r(b, jk, "ent");
                    break;
                }
                if (EndsWith(b, jk, "eli")) {
                    r(b, jk, "e");
                    break;
                }
                if (EndsWith(b, jk, "ousli")) {
                    r(b, jk, "ous");
                    break;
                }
                break;
            case 'o':
                if (EndsWith(b, jk, "ization")) {
                    r(b, jk, "ize");
                    break;
                }
                if (EndsWith(b, jk, "ation")) {
                    r(b, jk, "ate");
                    break;
                }
                if (EndsWith(b, jk, "ator")) {
                    r(b, jk, "ate");
                    break;
                }
                break;
            case 's':
                if (EndsWith(b, jk, "alism")) {
                    r(b, jk, "al");
                    break;
                }
                if (EndsWith(b, jk, "iveness")) {
                    r(b, jk, "ive");
                    break;
                }
                if (EndsWith(b, jk, "fulness")) {
                    r(b, jk, "ful");
                    break;
                }
                if (EndsWith(b, jk, "ousness")) {
                    r(b, jk, "ous");
                    break;
                }
                break;
            case 't':
                if (EndsWith(b, jk, "aliti")) {
                    r(b, jk, "al");
                    break;
                }
                if (EndsWith(b, jk, "iviti")) {
                    r(b, jk, "ive");
                    break;
                }
                if (EndsWith(b, jk, "biliti")) {
                    r(b, jk, "ble");
                    break;
                }
                break;
            case 'g':
                if (EndsWith(b, jk, "logi")) {
                    r(b, jk, "log");
                    break;
                }
                break;
        }
    }

    /// <summary>
    /// Step4() deals with -ic-, -full, -ness etc. similar strategy to Step3.
    /// </summary>
    private static void Step4(char[] b, int[] jk) {
        switch (b[jk[1]]) {
            case 'e':
                if (EndsWith(b, jk, "icate")) {
                    r(b, jk, "ic");
                    break;
                }
                if (EndsWith(b, jk, "ative")) {
                    r(b, jk, "");
                    break;
                }
                if (EndsWith(b, jk, "alize")) {
                    r(b, jk, "al");
                    break;
                }
                break;
            case 'i':
                if (EndsWith(b, jk, "iciti")) {
                    r(b, jk, "ic");
                    break;
                }
                break;
            case 'l':
                if (EndsWith(b, jk, "ical")) {
                    r(b, jk, "ic");
                    break;
                }
                if (EndsWith(b, jk, "ful")) {
                    r(b, jk, "");
                    break;
                }
                break;
            case 's':
                if (EndsWith(b, jk, "ness")) {
                    r(b, jk, "");
                    break;
                }
                break;
        }
    }

    /// <summary>
    /// Step5() takes off -ant, -ence etc., in context <c>vcvc<v>.
    /// </summary>
    private static void Step5(char[] b, int[] jk) {
        if (jk[1] == 0) return; /* for Bug 1 */
        switch (b[jk[1] - 1]) {
            case 'a':
                if (EndsWith(b, jk, "al")) break;
                return;
            case 'c':
                if (EndsWith(b, jk, "ance")) break;
                if (EndsWith(b, jk, "ence")) break;
                return;
            case 'e':
                if (EndsWith(b, jk, "er")) break;
                return;
            case 'i':
                if (EndsWith(b, jk, "ic")) break;
                return;
            case 'l':
                if (EndsWith(b, jk, "able")) break;
                if (EndsWith(b, jk, "ible")) break;
                return;
            case 'n':
                if (EndsWith(b, jk, "ant")) break;
                if (EndsWith(b, jk, "ement")) break;
                if (EndsWith(b, jk, "ment")) break;
                /* element etc. not stripped before the m */
                if (EndsWith(b, jk, "ent")) break;
                return;
            case 'o':
                if (EndsWith(b, jk, "ion") && jk[0] >= 0 && (b[jk[0]] == 's' || b[jk[0]] == 't')) break;
                /* j >= 0 fixes Bug 2 */
                if (EndsWith(b, jk, "ou")) break;
                return;
            /* takes care of -ous */
            case 's':
                if (EndsWith(b, jk, "ism")) break;
                return;
            case 't':
                if (EndsWith(b, jk, "ate")) break;
                if (EndsWith(b, jk, "iti")) break;
                return;
            case 'u':
                if (EndsWith(b, jk, "ous")) break;
                return;
            case 'v':
                if (EndsWith(b, jk, "ive")) break;
                return;
            case 'z':
                if (EndsWith(b, jk, "ize")) break;
                return;
            default:
                return;
        }
        if (GetConsonantSeqCount(b, jk[0]) > 1) jk[1] = jk[0];
    }

    /// <summary>
    /// Step6() removes a final -e if m() > 1.
    /// </summary>
    private static void Step6(char[] b, int[] jk) {
        jk[0] = jk[1];
        if (b[jk[1]] == 'e') {
            int a = GetConsonantSeqCount(b, jk[0]);
            if (a > 1 || a == 1 && !IsConsonantVowelConsonant(b, jk[1] - 1)) jk[1]--;
        }
        if (b[jk[1]] == 'l' && IsDoubleConsonant(b, jk[1]) && GetConsonantSeqCount(b, jk[0]) > 1) jk[1]--;
    }

    @Override
    public Object clone(){
        return new PorterStemmer();
    }

}
