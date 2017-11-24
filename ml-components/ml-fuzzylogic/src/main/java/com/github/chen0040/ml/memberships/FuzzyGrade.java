package com.github.chen0040.ml.memberships;

public class FuzzyGrade implements Membership
    {
        public double lowerValue;
        public double upperValue;

        public FuzzyGrade(double lowerValue, double upperValue)
        {
            this.lowerValue = lowerValue;
            this.upperValue = upperValue;
        }

        @Override
        public double degree(double x)
        {
            if (x <= lowerValue)
            {
                return 0;
            }
            else if (x < upperValue)
            {
                return (x - lowerValue) / (upperValue - lowerValue);
            }
            else
            {
                return 1;
            }
        }
    }
