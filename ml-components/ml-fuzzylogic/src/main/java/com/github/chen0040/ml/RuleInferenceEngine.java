package com.github.chen0040.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleInferenceEngine
    {
        List<String> mVariables = new ArrayList<>();
        Map<String, FuzzySet> mWorkingMemory = new HashMap<>();
        List<Rule> mRules = new ArrayList<>();

        public void AddFuzzySet(String variable, FuzzySet set)
        {
            mVariables.add(variable);
            mWorkingMemory.put(variable, set);
        }

        public int getFuzzySetCount()
        {
                return mVariables.size();

        }

        public void AddRule(Rule fr)
        {
            mRules.add(fr);
        }

        public void SetVariable(String variable, double crispValue)
        {
            mWorkingMemory.get(variable).setX(crispValue);
        }

        public double GetVariable(String variable)
        {
            return mWorkingMemory.get(variable).getX();
        }

        public void Infer(FuzzySet output)
        {
            output.fire(mRules);
        }
    }
