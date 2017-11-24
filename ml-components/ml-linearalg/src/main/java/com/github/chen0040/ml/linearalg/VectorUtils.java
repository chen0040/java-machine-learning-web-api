package com.github.chen0040.ml.linearalg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen0469 on 10/11/2015 0011.
 */
public class VectorUtils {
    public static List<Vector> removeZeroVectors(Iterable<Vector> vlist)
    {
        List<Vector> vstarlist = new ArrayList<Vector>();
        for (Vector v : vlist)
        {
            if (!v.isZero())
            {
                vstarlist.add(v);
            }
        }

        return vstarlist;
    }

    public static Tuple2<List<Vector>, List<Double>> normalize(Iterable<Vector> vlist)
    {
        List<Double> norms = new ArrayList<Double>();
        List<Vector> vstarlist = new ArrayList<Vector>();
        for (Vector v : vlist)
        {
            norms.add(v.norm(2));
            vstarlist.add(v.normalize());
        }

        return Tuple2.create(vstarlist, norms);
    }


}
