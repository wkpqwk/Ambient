package com.example.iju95.ambient_browser;

/**
 * Created by iju95 on 2018-06-14.
 */

public class LWLatLng {
    String id ="lwlatlng";
    double lat;
    double lng;

    public LWLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
    public double getLat() {return lat;}
    public double getLng() {return lng;}
    public boolean equals(double lat,double lng){
        if(this.lat ==lat && this.lng ==lng)
            return true;
        return false;
    }
}
