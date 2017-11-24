package com.github.chen0040.op.commons.models.solutions;

/**
 * Created by memeanalytics on 12/8/15.
 */
public class NumericSolution {
    private double[] x;
    private double fx;

    public double cost(){
        return fx;
    }

    public double[] values(){
        return x;
    }

    public void mutate(int index, double sigma)
    {
        //x[index] += RandomEngine.Gauss(0, sigma);
    }

    public int getLength(){
        return x != null ? x.length : 0;
    }

    public double get(int index){
        if(x != null && index < x.length){
            return x[index];
        }
        return 0;
    }

    public static NumericSolution divide(NumericSolution a, double val)
    {
        double[] x = new double[a.getLength()];

        for(int i=0; i < x.length; ++i)
        {
            x[i] = a.get(i) / val;
        }
        return new NumericSolution(x, Double.MAX_VALUE);
    }

    public static NumericSolution plus(NumericSolution a, NumericSolution b)
    {
        int length = a.getLength();

        double[] x = new double[length];

        for (int i = 0; i < length; ++i)
        {
            x[i] = a.get(i) + b.get(i);
        }
        return new NumericSolution(x, Double.MAX_VALUE);
    }

    public static NumericSolution times(NumericSolution a, double val)
    {
        double[] x = new double[a.getLength()];

        for (int i = 0; i < x.length; ++i)
        {
            x[i] = a.get(i) * val;
        }
        return new NumericSolution(x, Double.MAX_VALUE);
    }

    public static NumericSolution minus(NumericSolution a, NumericSolution b)
    {
        int length = a.getLength();

        double[] x = new double[length];
        for(int i=0; i < length; ++i)
        {
            x[i]=a.get(i) - b.get(i);
        }
        NumericSolution result = new NumericSolution(x, Double.MAX_VALUE);

        return result;
    }

    public double GetDistanceSq2(NumericSolution rhs)
    {
        if (this.x == null)
        {
            return Double.MAX_VALUE;
        }
        if (rhs.x == null)
        {
            return Double.MAX_VALUE;
        }

        if (rhs.x.length != this.x.length)
        {
            return Double.MAX_VALUE;
        }

        double distanceSq = 0;
        for (int i = 0; i < this.x.length; ++i)
        {
            distanceSq += Math.pow((x[i] - rhs.x[i]), 2);
        }

        return distanceSq;
    }

    public NumericSolution(int dimension)
    {
        x = new double[dimension];
        fx = Double.MAX_VALUE;
    }

    public NumericSolution()
    {
        x = null;
        fx = Double.MAX_VALUE;
    }

    public NumericSolution(double[] x, double fx)
    {
        if(x != null) {
            this.x = new double[x.length];
            for(int i=0; i < x.length; ++i) {
                this.x[i] = x[i];
            }
        }
        this.fx = fx;
    }

    public NumericSolution clone()
    {
        NumericSolution clone = new NumericSolution(x, fx);
        return clone;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof NumericSolution)
        {
            NumericSolution cast_obj = (NumericSolution)obj;
            int length1 = this.x.length;
            int length2 = cast_obj.x.length;
            if (length1 == length2)
            {
                for (int i = 0; i < length1; ++i)
                {
                    if (this.x[i] != cast_obj.get(i))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public NumericSolutionUpdateResult tryUpdateSolution(double[] new_solution, double new_cost)
    {
        double improvement = 0;

        if (fx > new_cost)
        {
            improvement = fx - new_cost;
            fx = new_cost;
            x = new double[new_solution.length];
            for(int i=0; i < new_solution.length; ++i){
                x[i] = new_solution[i];
            }
            return new NumericSolutionUpdateResult(improvement, true);
        }
        else if (x == null)
        {
            int dimension = new_solution.length;
            x = new double[dimension];
            for (int i = 0; i < dimension; ++i)
            {
                x[i] = new_solution[i];
            }
            fx = new_cost;
            return new NumericSolutionUpdateResult(improvement, true);
        }

        return new NumericSolutionUpdateResult(-1, false);
    }



}
