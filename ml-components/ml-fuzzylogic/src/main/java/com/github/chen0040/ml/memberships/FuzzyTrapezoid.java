package com.github.chen0040.ml.memberships;

public class FuzzyTrapezoid implements Membership
{
    public double x0;
    public double x1;
    public double x2;
    public double x3;

    public double getX0() {
        return x0;
    }

    public void setX0(double x0) {
        this.x0 = x0;
    }

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) {
        this.x1 = x1;
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) {
        this.x2 = x2;
    }

    public double getX3() {
        return x3;
    }

    public void setX3(double x3) {
        this.x3 = x3;
    }

    public FuzzyTrapezoid(double x0, double x1, double x2, double x3)
    {
        this.x0 = x0;
        this.x1 = x1;
        this.x2 = x2;
        this.x3 = x3;
    }

    @Override
    public double degree(double x)
    {
        if (x <= x0 || x >= x3)
        {
            return 0;
        }
        else if (x >= x1 && x <= x2)
        {
            return 1;
        }
        else if ((x > x0) && (x < x1))
        {
            return (x - x0) / (x1 - x0);
        }
        else
        {
            return (x3 - x) / (x3 - x2);
        }
    }
}
