package com.github.chen0040.ml.linearalg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chen0469 on 10/11/2015 0011.
 */
public class Orthogonalization {
    /**
    * @brief Convert a set of vectors to a set of orthogonal vectors
    * @param[Iterable] vlist
    * @return The resultant set of orthogonal vectors (i.e. vectors which are mutually perpendicular to each other)
     */
    public static List<Vector> orthogonalize(Iterable<Vector> vlist)
    {
        List<Vector> vstarlist = new ArrayList<Vector>();
        for (Vector v : vlist)
        {
            Vector vstar = v.projectOrthogonal(vstarlist);
            vstarlist.add(vstar);
        }
        return vstarlist;
    }

    /**
    * @brief V and W are two vector spaces, return the orthogonal complement of V in W
    *
    * @param[Iterable] V The spanning set representing V
    * @param[Iterable] W The spanning set representing W
    * @return The set of vectors { w_i } = Span(W) which are perpendicular to the vectors in V
    */
    public static List<Vector> orthogonalize(Iterable<Vector> V, Iterable<Vector> W)
    {
        List<Vector> vstarlist = VectorUtils.removeZeroVectors(orthogonalize(V));
        for (Vector w : W)
        {
            Vector wstar = w.projectOrthogonal(vstarlist);
            vstarlist.add(wstar);
        }

        return vstarlist;
    }

    public static Tuple2<List<Vector>, List<Vector>> orthogonalize2(List<Vector> vlist)
    {
        List<Vector> vstarlist = new ArrayList<Vector>();
        List<Vector> R = new ArrayList<Vector>();
        int D = vlist.size();
        for (Vector v : vlist)
        {
            HashMap<Integer, Double> alpha = new HashMap<Integer, Double>();
            Vector vstar = v.projectOrthogonal(vstarlist, alpha);
            vstarlist.add(vstar);

            Vector r = new Vector(D, alpha);

            R.add(r);

        }
        return Tuple2.create(vstarlist, R);
    }
}
