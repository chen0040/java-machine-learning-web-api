package com.github.chen0040.ml;

import java.io.Serializable;

public class Clause implements Serializable, Cloneable
{
    private static final long serialVersionUID = -4073774254458691213L;

    private FuzzySet variable;
    private String variableValue;
    private String operator;

    public void setVariable(FuzzySet variable) {
        this.variable = variable;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Clause(FuzzySet variable, String operator, String value)
    {
        this.variable = variable;
        this.variableValue = value;
        this.operator = operator;
    }

    @Override
    public String toString()
    {
        return variable.getName() + " " + operator + " " + variableValue;
    }

    @Override
    public Object clone(){
        return new Clause(variable, operator, variableValue);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = hash * 31 + variable.hashCode();
        hash = hash * 31 + operator.hashCode();
        hash = hash * 31 + variableValue.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Clause){
            Clause rhs = (Clause)obj;

            if(!variable.equals(rhs.variable)) {
                return false;
            }

            if(!operator.equals(rhs.operator)) {
                return false;
            }

            return variableValue.equals(rhs.variableValue);
        }
        return false;
    }

    public FuzzySet getVariable()
    {
        return variable;
    }

    public String getVariableValue()
    {
        return variableValue;
    }
}
