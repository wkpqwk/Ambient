package com.example.iju95.ambient_browser;

/**
 * Created by iju95 on 2018-06-14.
 */

public class LWTM {
    String id ="lwtm";
    double x;
    double y;

    public LWTM(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public boolean equals(double x,double y){
        if(this.x ==x && this.y ==y)
            return true;
        return false;
    }
    public LWTM addition(double x,double y){
        double sum_x = this.x + x;
        double sum_y =this.y + y;
        return new LWTM(sum_x,sum_y);
    }
    public LWTM subtraction(double x,double y){
        double sub_x = this.x - x;
        double sub_y =this.y - y;
        return new LWTM(sub_x,sub_y);
    }
    public LWTM multiplicationWithZoomFactor(double zoomFactor){
        double tm_x = this.x * zoomFactor;
        double tm_y = this.y * zoomFactor;
        return new LWTM(tm_x,tm_y);
    }
    public LWTM divisionWithZoomFactor(double zoomFactor){
        if(zoomFactor ==0)
            return null;
        double tm_x = this.x / zoomFactor;
        double tm_y = this.y / zoomFactor;
        return new LWTM(tm_x,tm_y);
    }
}