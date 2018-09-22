package com.example.iju95.ambient_browser;

import java.util.ArrayList;

/**
 * Created by iju95 on 2018-06-14.
 */

public class LatLng_name {
    ArrayList<LWLatLng> a;
    String name;
    public LatLng_name(ArrayList<LWLatLng> a,String name) {
        this.a = a;
        this.name =name;
    }
    public ArrayList<LWLatLng> getA() {
        return a;
    }
    public String getName() {
        return name;
    }
}
