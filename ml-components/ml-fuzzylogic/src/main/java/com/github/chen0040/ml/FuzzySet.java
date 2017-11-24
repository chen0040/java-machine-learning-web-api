package com.github.chen0040.ml;


import com.github.chen0040.ml.memberships.Membership;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FuzzySet implements Serializable, Cloneable
{
    private static final long serialVersionUID = -4073774254458691213L;

    protected Map<String, Membership> mMemberships = new HashMap<>();
    protected List<String> mValues = new ArrayList<>();

    private double minValue = 0;
    private double maxValue = 1;
    private double mDeltaX = 0.0001;
    private String mName = null;
    private double mValX = 0;

    public FuzzySet(String name, double lower_bound, double upper_bound, double dX)
    {
        this.mName = name;
        this.minValue = lower_bound;
        this.maxValue = upper_bound;
        this.mDeltaX = dX;
    }

    public FuzzySet(String name, double lower_bound, double upper_bound)
    {
        this.mName = name;
        this.minValue = lower_bound;
        this.maxValue = upper_bound;
    }

    public Membership GetMembership(String value)
    {
        return mMemberships.get(value);
    }

    public String GetMembershipName(int index)
    {
        return mValues.get(index);
    }

    public int GetMembershipCount()
    {
        return mValues.size();
    }

    public Membership GetMembership(int index)
    {
        return GetMembership(mValues.get(index));
    }

    public double getX() {
        return mValX;
    }

    public void setX(double value){
        mValX = value;
    }

    public String getName()
    {
        return mName;
    }

    public double GetMinX()
    {
        return minValue;
    }

    public double GetMaxX()
    {
        return maxValue;
    }

    public double GetDeltaX()
    {
        return mDeltaX;
    }

    public void addMembership(String membershipname, Membership membership)
    {
        mValues.add(membershipname);
        mMemberships.put(membershipname, membership);
    }

    public void fire(List<Rule> rules)
    {
        Map<String, List<Double>> degrees = new HashMap<>();
        for (int i = 0; i < mValues.size(); ++i)
        {
            degrees.put(mValues.get(i), new ArrayList<>());
        }

        for (int i = 0; i < rules.size(); ++i)
        {
            Rule rule = rules.get(i);
            Clause consequent = rule.getConsequent();
            if (consequent.getVariable() == this)
            {
                double y = 1;

                for (int j = 0; j < rule.getAntecedentCount(); ++j)
                {
                    Clause antecedent = rule.getAntecedent(j);
                    FuzzySet variable = antecedent.getVariable();
                    String value = antecedent.getVariableValue();
                    Membership ms = variable.GetMembership(value);
                    double degree = ms.degree(variable.getX());
                    if (y > degree)
                    {
                        y = degree;
                    }

                }
                degrees.get(consequent.getVariableValue()).add(y);
            }
        }

        Map<String, Double> consequent_degrees = getRootSumSquare(degrees);

        mValX = getAreaCentroid(consequent_degrees);
    }

    public Map<String, Double> getRootSumSquare(Map<String, List<Double>> degrees)
    {
        Map<String, Double> results = new HashMap<>();

        for(String value : degrees.keySet())
        {
            List<Double> de = degrees.get(value);
            double squareSum = 0;
            for (int i = 0; i < de.size(); ++i)
            {
                double v = de.get(i);
                squareSum += v * v;
            }
            results.put(value, Math.sqrt(squareSum));
        }

        return results;
    }

    /*
    public double getFuzzyCentroid(Map<String, double> degrees)
    {
        double sumxy=0;
        double sumy=0;
        for(int i=0; i<m_values.Count; i++)
        {
            String value=m_values[i);
            double y=degrees[value).doubleValue();
            double x=m_memberships[value)[CentroidX();
            sumxy=
            sumy+=y;
        }
    }
    */

    public double getAreaCentroid(Map<String, Double> degrees)
    {
        double sumxy = 0;
        double sumy = 0;
        for (double x = minValue; x <= maxValue; x += mDeltaX)
        {
            for (int i = 0; i < mValues.size(); ++i)
            {
                Membership ms = mMemberships.get(mValues.get(i));
                double d1 = degrees.get(mValues.get(i));
                double d2 = ms.degree(x);
                double y = d1 > d2 ? d2 : d1;

                sumxy += (x * y);
                sumy += y;
            }
        }

        return sumxy / sumy;
    }

}
