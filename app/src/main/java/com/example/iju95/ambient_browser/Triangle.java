package com.example.iju95.ambient_browser;

import java.util.ArrayList;

/**
 * Created by iju95 on 2018-06-14.
 */

public class Triangle {
    private ArrayList<LWPoint> points = new ArrayList<LWPoint>();

    public Triangle(LWPoint a, LWPoint b, LWPoint c) {
        super();
        points.add(a);
        points.add(b);
        points.add(c);
    }

    public ArrayList<LWPoint> getPoints() {
        return points;
    }

    public boolean isInside(LWPoint p) {
        int pnpoly = cn_PnPoly(p, this.points);
        boolean ret = pnpoly==0 ? false : true;
        return ret;
    }
    public boolean in(LWPoint p) {
        ArrayList<LWPoint> v = this.points;
        double x,y,z;
        LWPoint a,b,c;
        a = v.get(0);
        b = v.get(1);
        c = v.get(2);

        x= (a.x-p.x)*(c.y-p.y)-(c.x-p.x)*(a.y-p.y);
        y= (c.x-p.x)*(b.y-p.y)-(b.x-p.x)*(c.y-p.y);
        z= (b.x-p.x)*(a.y-p.y)-(a.x-p.x)*(b.y-p.y);
        if(x < 0.00001 && x >-0.000001)
            x=0;
        if( y< 0.00001&& y >-0.000001)
            y=0;
        if(z <0.00001&& z >-0.000001)
            z=0;
        if(( x >0 && y>0 && z>0) ||( x <0 && y<0 && z<0)  )	//이 경우는  내부의 점인 경우{
        {
            if(x!=0 && y!=0 && z!=0)
                return true;
            else
                return false;
        }else {
            return false;
        }
    }
    private int cn_PnPoly(LWPoint P, ArrayList<LWPoint> v) {
        int cn = 0; // the crossing number counter

        // loop through all edges of the polygon
        for (int i = 0; i < v.size(); i++) { // edge from V[i] to V[i+1]
            int nextIndex = (i == (v.size() - 1)) ? 0 : i + 1;
            if (((v.get(i).y <= P.y) && (v.get(nextIndex).y > P.y)) // an upward crossing
                    || ((v.get(i).y > P.y) && (v.get(nextIndex).y <= P.y))) { // a downward crossing
                // compute the actual edge-ray intersect x-coordinate
                double vt = (double) (P.y - v.get(i).y) / (v.get(nextIndex).y - v.get(i).y);
                if (P.x < v.get(i).x + vt * (v.get(nextIndex).x - v.get(i).x)) // P.x < intersect
                    ++cn; // a valid crossing of y=P.y right of P.x
            }
        }
        return (cn & 1); // 0 if even (out), and 1 if odd (in)
    }

    public double getArea() {
        LWPoint p1 = points.get(0);
        LWPoint p2 = points.get(1);
        LWPoint p3 = points.get(2);
        return Math.abs(((p2.x-p1.x)*(p3.y-p1.y))-((p3.x-p1.x)*(p2.y-p1.y))) / 2;
    }
}