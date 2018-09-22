package com.example.iju95.ambient_browser;

/**
 * Created by iju95 on 2018-06-15.
 */

public class CrossVertex {
    Double Distance;
    Integer BuildingIndex;
    LWTM X;
    public CrossVertex(double distance, int buildingIndex,LWTM x) {
        Distance = distance;
        BuildingIndex = buildingIndex;
        X= x;
    }
    public LWTM getX() {
        return X;
    }
    public void setX(LWTM x) {
        X = x;
    }
}
