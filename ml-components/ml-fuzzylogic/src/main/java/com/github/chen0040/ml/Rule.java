package com.github.chen0040.ml;

import java.util.ArrayList;
import java.util.List;

public class Rule
    {
        protected List<Clause> mAntecedents = new ArrayList<>();
        protected Clause mConsequent = null;
        String mName;

        public Rule(String name)
        {
            mName = name;
        }

        public Clause getConsequent() {
            return mConsequent;
        }

        public void setmConsequent(Clause value){
            mConsequent = value;
        }

        public void AddAntecedent(Clause antecedent)
        {
            mAntecedents.add(antecedent);
        }

        public Clause getAntecedent(int index)
        {
            return mAntecedents.get(index);
        }

        public int getAntecedentCount()
        {
            return mAntecedents.size();
        }

        /*
        public void fire()
        {
            double Ad=1;
            for(int i=0; i<m_antecedents.size(); ++i)
            {
                FuzzySet variable=m_antecedents.get(i).getVariable();
                String value=m_antecedents.get(i).getVariableValue();
                Membership ms=variable.getMembership(value);
                if(!ms.hasValidX())
                {
                    Ad=0;
                    break;
                }
                double xVal=ms.getX();
                if(xVal >= variable.getMinX() && xVal <= variable.getMaxX())
                {
                    double degree=ms.degree(xVal);
                    if(degree < Ad)
                    {
                        Ad=degree;
                    }
                }
                else
                {
                    Ad=0;
                    break;
                }
            }
		
            Membership ms2=m_consequent.getVariable().getMembership(m_consequent.getVariableValue());
		
            double Ad0=ms2.getDegree();
            if(Ad0 > Ad)
            {
                ms2.setDegree(Ad);
            }
		
            System.out.println(m_name+": "+ms2.getDegree());
        }
        */
    }
