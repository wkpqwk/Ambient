package com.example.iju95.ambient_browser;

import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by iju95 on 2018-06-14.
 */

public class MBR_Name {
    LatLng LU, LD, RU, RD;
    LWTM TM_LU,TM_LD,TM_RU,TM_RD;
    LatLng Center;
    LWTM TM_Center;
    String name;
    public MBR_Name(LatLng LU, LatLng LD, LatLng RU, LatLng RD, String name) {
        this.LU = LU;
        this.LD = LD;
        this.RU = RU;
        this.RD = RD;
        TM_LU = LWLatLngtoTM(new LatLng(LU.latitude, LU.longitude));
        TM_LD = LWLatLngtoTM(new LatLng(LD.latitude, LD.longitude));
        TM_RU = LWLatLngtoTM(new LatLng(RU.latitude, RU.longitude));
        TM_RD = LWLatLngtoTM(new LatLng(RD.latitude, RD.longitude));
        Center = new LatLng((RU.latitude+RD.latitude)/2.0,(LU.longitude+LD.longitude)/2.0);
        TM_Center = LWLatLngtoTM(new LatLng(Center.latitude, Center.longitude));
        this.name = name;
    }

    public LatLng getRD() {
        return RD;
    }

    public void setCenter(LatLng center) {
        Center = center;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LWTM getTM_LU() {
        return TM_LU;
    }

    public LWTM getTM_LD() {
        return TM_LD;
    }

    public LWTM getTM_RU() {
        return TM_RU;
    }

    public LWTM getTM_RD() {
        return TM_RD;
    }

    public LWTM getTM_Center() {
        return TM_Center;
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

    public String getName() {
        return name;
    }
    public LWTM LWLatLngtoTM(LatLng zx_input) {
        double zx_phi = zx_input.latitude / 180.0 * Math.PI;
        double zx_lambda = zx_input.longitude / 180.0 * Math.PI;
        double zx_T = Math.pow(Math.tan(zx_phi), 2.0);
        double zx_C = 0.006739496775478856 * Math.pow(cos(zx_phi), 2.0);
        double zx_A = (zx_lambda - 2.2165681500327987) * cos(zx_phi);
        double zx_N = 6378137.0 / Math.sqrt(1.0 - 0.006694380022900686 * Math.pow(sin(zx_phi), 2.0));
        double zx_M = 6378137.0 * (0.9983242984445848 * zx_phi - 0.0025146070728447813 * sin(2.0 * zx_phi) + 0.0000026390466202308188 * sin(4.0 * zx_phi) - 3.4180461367750593e-9 * sin(6.0 * zx_phi));
        // Y 좌표 출력
        double zx_Y = 500000.0 + 1.0 * (zx_M - 4207498.019150324 + zx_N * Math.tan(zx_phi) * (Math.pow(zx_A, 2.0) / 2.0 + Math.pow(zx_A, 4.0) / 24.0 * (5.0 - zx_T + 9.0 * zx_C + 4.0 * Math.pow(zx_C, 2.0)) + Math.pow(zx_A, 6.0) / 720.0 * (61.0 - 58.0 * zx_T + Math.pow(zx_T, 2.0) + 600.0 * zx_C - 2.2240339359080226)));
        // X 좌표 출력
        double zx_X = 200000.0 + 1.0 * zx_N * (zx_A + Math.pow(zx_A, 3.0) / 6.0 * (1.0 - zx_T + zx_C) + Math.pow(zx_A, 5.0) / 120.0 * (5.0 - 18.0 * zx_T + Math.pow(zx_T, 2.0) + 72.0 * zx_C - 0.3908908129777736));
        return new LWTM(zx_X, zx_Y);
    }   //위도 경도를 TM 좌표로 변경
}