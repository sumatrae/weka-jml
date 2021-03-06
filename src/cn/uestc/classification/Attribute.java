package cn.sumatrae.classification;

import java.util.*;

public class Attribute {
    //name of attribute
    private String attributeName;
    //value range of attribute
    private ArrayList<String> valueList = new ArrayList<String>();
    private boolean isLabel;

    public Attribute(String name) {
        this.attributeName = name;
        this.isLabel = false;
    }

    public ArrayList<String> getValueList() {
        return this.valueList;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setValueList(ArrayList<String> lis) {
        this.valueList = lis;
    }

    public void setIsLabel() {
        this.isLabel = true;
    }

    public boolean getIsLabel() {
        return this.isLabel;
    }

}
