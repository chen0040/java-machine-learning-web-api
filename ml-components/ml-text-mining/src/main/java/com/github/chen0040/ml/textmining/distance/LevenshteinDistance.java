package com.github.chen0040.ml.textmining.distance;

/**
 * Created by root on 10/15/15.
 */
public class LevenshteinDistance {

    public static int ComputeShortestDistance(String s, String[] db){
        int shortest_distance = Integer.MAX_VALUE;
        for(String t : db){
            int distance = ComputeDistance(s, t);
            shortest_distance = Math.min(distance, shortest_distance);
        }
        return shortest_distance;
    }



    public static int ComputeDistance(String s, String t)
    {
        int n = s.length();
        int m = t.length();
        int[][] d = new int[n + 1][];

        for(int i=0; i < n+1; ++i)
        {
            d[i]=new int[m + 1];
            for(int j=0; j < m+1; ++j)
            {
                d[i][j]=0;
            }
        }

        // Step 1
        if (n == 0)
        {
            return m;
        }

        if (m == 0)
        {
            return n;
        }

        // Step 2
        for (int i = 0; i <= n; d[i][0] = i++)
        {
        }

        for (int j = 0; j <= m; d[0][j] = j++)
        {
        }

        // Step 3
        for (int i = 1; i <= n; i++)
        {
            //Step 4
            for (int j = 1; j <= m; j++)
            {
                // Step 5
                int cost = (t.charAt(j - 1) == s.charAt(i - 1)) ? 0 : 1;

                // Step 6
                d[i][j] = Math.min(
                        Math.min(d[i - 1][j] + 1, d[i][j - 1] + 1),
                        d[i - 1][j - 1] + cost);
            }
        }
        // Step 7
        return d[n][m];
    }
    public static void main(String[] args)
    {
        System.out.println("Levenshtein Distance");

        String[] s=new String[5];
        s[0]="Hello World";
        s[1]="Hell Wall";
        s[2]="Hello Cold";
        s[3]="jfodsifjelw";
        s[4]="ijkd World";
        for(int i=1; i < 5; ++i)
        {
            System.out.println("Distance between s[0] and s["+i+"] is "+ComputeDistance(s[0], s[i]));
        }
    }
}