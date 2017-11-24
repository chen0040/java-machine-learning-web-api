package com.github.chen0040.ml.commons.tuples;

/**
 * Created by memeanalytics on 18/8/15.
 */
public class TupleAttributeLevel implements Cloneable {
    private int attributeIndex;
    private String attributeName;
    private String levelName;
    private int levelIndex;

    public void copy(TupleAttributeLevel rhs){
        attributeIndex = rhs.attributeIndex;
        attributeName = rhs.attributeName;
        levelName = rhs.levelName;
        levelIndex = rhs.levelIndex;
    }

    @Override
    public Object clone(){
        TupleAttributeLevel clone = new TupleAttributeLevel();
        clone.copy(this);

        return clone;
    }

    public TupleAttributeLevel(String name, String level, int index, int levelIndex){
        this.attributeIndex = index;
        this.attributeName = name;
        this.levelName = level;
        this.levelIndex = levelIndex;
    }

    public TupleAttributeLevel(){

    }

    public String getLevelName(){
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public String getAttributeName(){
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public int getAttributeIndex(){
        return attributeIndex;
    }

    public void setAttributeIndex(int attributeIndex) {
        this.attributeIndex = attributeIndex;
    }

    public int getLevelIndex() { return levelIndex;}

    public void setLevelIndex(int levelIndex) {
        this.levelIndex = levelIndex;
    }

    public boolean isCategorical(){
        return levelIndex != -1;
    }

    @Override
    public String toString(){
        if(isCategorical()){
            return attributeName +"["+ levelName +"]";
        }else{
            return attributeName;
        }
    }
}
