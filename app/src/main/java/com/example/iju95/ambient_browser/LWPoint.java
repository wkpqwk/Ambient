package com.example.iju95.ambient_browser;

/**
 * Created by iju95 on 2018-06-14.
 */

public class LWPoint {
    String id="point";
    double x;
    double y;

    public LWPoint(double x, double y) {
        this.x = x;
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
    public LWPoint addition(double x,double y){
        double sum_x = this.x + x;
        double sum_y =this.y + y;
        return new LWPoint(sum_x,sum_y);
    }
    public LWPoint subtraction(double x,double y){
        double sub_x = this.x - x;
        double sub_y =this.y - y;
        return new LWPoint(sub_x,sub_y);
    }
    public LWPoint multiplicationWithZoomFactor(double zoomFactor){
        double point_x = this.x * zoomFactor;
        double point_y = this.y * zoomFactor;
        return new LWPoint(point_x,point_y);
    }
    public LWPoint divisionWithZoomFactor(double zoomFactor){
        if(zoomFactor ==0)
            return null;
        double point_x = this.x / zoomFactor;
        double point_y = this.y / zoomFactor;
        return new LWPoint(point_x,point_y);
    }
}