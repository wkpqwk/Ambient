package com.example.iju95.ambient_browser;


import com.google.android.gms.maps.model.LatLng;

/**
 * Created by iju95 on 2018-06-14.
 */
public class MBR {
    LatLng LU,LD,RU,RD;
    LatLng Center;
    public MBR(LatLng LU, LatLng LD, LatLng RU, LatLng RD) {
        this.LU = LU;
        this.LD = LD;
        this.RU = RU;
        this.RD = RD;
        Center = new LatLng((LU.latitude+LD.latitude)/2.0,(LU.longitude+RU.longitude)/2.0);
    }

    public void setCenter(LatLng center) {
        Center = center;
    }

    public LatLng getCenter() {
        return Center;
    }

    public void setLU(LatLng LU) {
        this.LU = LU;
    }

    public void setLD(LatLng LD) {
        this.LD = LD;
    }

    public void setRU(LatLng RU) {
        this.RU = RU;
    }

    public void setRD(LatLng RD) {
        this.RD = RD;
    }

    public LatLng getLU() {
        return LU;
    }

    public LatLng getLD() {
        return LD;
    }

    public LatLng getRU() {
        return RU;
    }

    public LatLng getRD() {
        return RD;
    }
}