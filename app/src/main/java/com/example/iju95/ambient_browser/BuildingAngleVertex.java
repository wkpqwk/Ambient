package com.example.iju95.ambient_browser;

/**
 * Created by iju95 on 2018-06-14.
 */

public class BuildingAngleVertex extends BuildingCenterIndex {
    LWTM Angle_a, Angle_b;

    public BuildingAngleVertex(double center, int index, LWTM a, LWTM b) {
        super(center, index);
        Angle_a = a;
        Angle_b = b;
    }
}