package com.example.iju95.ambient_browser;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SizeF;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, SurfaceHolder.Callback,GoogleMap.OnMapLongClickListener,GoogleMap.OnMyLocationButtonClickListener,GoogleMap.OnMapClickListener,SensorEventListener {
    Context mContext;
    /*********시야각 관련 변수 ***********/
    private SensorManager sensorManager;
    private Sensor gsensor;
    private Sensor msensor;
    private SensorManager manager = null;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    float[] mGravity = null;            // 센서 기능
    float[] mGeomagnetic = null;        //센서 기능
    private float[] mmGravity = new float[3];
    private float[] mmGeomagnetic = new float[3];
    private double azimuth = 0f;
    float horizonalAngle;               //카메라 수평 시야각
    float verticalAngle;                //카메라 수직 시야각
    double azimut, Ax, Ay, Bx, By;     //단말기 회전 각도
    ArrayList<LatLng> Viewrange;   //시야의 좌표 3개를 저장하는 배열

    /*********카메라 관련 변수**********/
    private SurfaceView mCameraView;
    private SurfaceHolder mCameraHolder;
    private Camera mCamera;

    /*********구글맵 관련 변수**********/
    private GoogleMap mGoogleMap = null;                //구글맵
    private LocationManager locationManager;    //디바이스 위치를 가져오는데 사용하는 매니저
    private LocationListener locationListener;  // 위치가 변할때 마다 위치를 가져오는 리스너
    RelativeLayout relativeLayout;                // 구글 지도 Layout
    double cnt_latitude = 0.0;                        //현재 위치의 위도
    double cnt_longitude = 0.0;                       //현재 위치의 경도
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);   //위치정보 안받았을 때 default 값으로 서울의 위도와 경도 저장
    private Marker currentMarker = null;                //현재 위치 표시해주는 마커

    /**********기능 관련 변수***********/
    long pressedTime = 0;
    TextView textview;
    private GraphicButton mapButton;
    private GraphicButton searchButton;

    /*********서버 통신 관련 변수 *******/
    ArrayList<LatLng_name> recieve_data;
    ArrayList<BuildingData> TMmap;    // 서버로부터 받은 자신의 주변 건물 정보들
    ArrayList<String> Building_name; // 서버로부터 받은 건물 이름 정보
    boolean Once = false;

    /********건물 Data Control 변수 ******/
    ArrayList<MBR> RMBR;       // 건물 MBR
    ArrayList<MBR_Name> RRMBR;       // Rotate한 건물
    Boolean[] Triangle_in_building; // Rotate한 건물의 총 개수

    /******** 구글맵 그리기 변수 ********/
    Polygon draw_view = null;                           //시야를 그리는 폴리곤
    Polygon[] draw_sight_building = null;         //시야에 보이는 건물을 그리는 폴리곤
    Polygon draw_result = null;
    Polyline[] draw_building2 = null;
    Polyline draw_view2 = null;
    SeekBar seekSize;
    /****위도,경도 --> 주소 변환 변수 ****/
    private AddressResultReceiver mResultReceiver;  // service에서 받은 결과를 처리하는 변수
    private String mAddressOutput;      // 좌표를 주소로 변환시킨 결과

    /******** 건물 탐색 관련 변수 ********/
    TimerTask timer;            // 건물 탐색 시작 Timer
    LWTM v1,v2,v3;              //시야에서 삼각형의 각 Vertex
    int Triangle_in_building_number;              // 시야안에 들어와 있는 건물의 개수
    ArrayList<BuildingCenterIndex> Building_Min_dist;    //사용자 위치와 건물의 거리를 오름차순으로 저장한 배열
    ArrayList<BuildingAngleVertex> Building_Min_angle;     //사용자 위치와 건물이 이루는 가장 큰 각도를 저장함(각 건물에 대해서)
    ArrayList<BuildingAngleVertex> Sight_Search_Building;   //시야에 보이는 건물들 저장하는 변수
    ArrayList<BuildingAngleVertex> Angle_for_search_building;       //
    ArrayList<ArrayList<CrossVertex>> crossvertex;    // 시야각을 100으로 나눈 선분들이 들어있는데, 해당 선분들은 만나는 건물의 모서리에 대한 정보를 갖고 있음
    int Finding;            //가운데 기준으로 탐색했을 경우 발견되는 건물
    String Search_Building_name_view = null;
    Point building_line_a, building_line_b;
    int view_build = -1;
    int view_timer = 0;     //건물 탐색시 시간 관련 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);    //가로로 Fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // 화면회전 X
        mContext = getApplicationContext();
        setContentView(R.layout.activity_main);
        textview = (TextView)findViewById(R.id.test);
        /********** 카메라 및 위치 권한 설정*******/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            CheckPermission();
        }else {
            /************위도,경도 --> 주소 변환 설정 *******************/
            mResultReceiver = new AddressResultReceiver(new Handler());
            mAddressOutput = "";
            /************센서 설정*********/
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mAccelerometer = manager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mMagnetometer = manager
                    .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            /********** 구글 지도 설정*********/
            relativeLayout = (RelativeLayout) findViewById(R.id.overlay);    // 구글 지도 Layout
            FragmentManager fragmentManager = getFragmentManager();
            MapFragment mapFragment = (MapFragment) fragmentManager
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync( this);

            /*********카메라 설정*************/
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            calculateFOV(manager);  //카메라 시야각 설정
            mCameraView = (SurfaceView) findViewById(R.id.cameraView);
            settingGPS();   //gps 설정
            getMyLocation();//내위치찾기 설정
            Camera_init();//카메라설정
        }
    }
    /********* 건물 탐색 관련 함수 *************/
    public TimerTask timerTask() {   //건물 탐색 함수
        TimerTask temptask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {         //메인 스레드에서는 UI 변경이 불가능 하기 때문에 UIThread에서 변경을 처리함
                        if (User_in_building()){  //사용자가 건물 안에 있는 경우 - 이경우는 타이머로 여러번 할 필요가 없을 거 같은데 ?
                            searchButton.setPress(0);
                            timer.cancel();
                            return;
                        }
                        TriangleInCheck();  //시야 안에 건물이 들어가 있는지 확인하는 작업

                        if (Triangle_in_building_number == 0) {     //시야에 건물이 하나도 없는 경우
                            searchButton.setPress(0);
                            timer.cancel();
                            android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(MainActivity.this,R.style.My_dialog);    //위치 설정한 후 다시 시도하라고 dialog 알림창 표시
                            dialog.setTitle("알림")
                                    .setMessage("아무 건물도 없습니다!!")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    }).create().show();
                            return;
                        }

                        Sight_Building();   //시야에 보이는 건물 판단 및 결정

                        if (view_build != Finding) {    // 찾고 있던 건물이 바뀔 경우에는 timer를 다시 초기화시켜 다시 찾는다
                            view_build = Finding;
                            view_timer = 0;
                        } else {                        //계속해서 동일한 건물을 찾고 있는 경우에는 timer를 증가시킨다
                            view_timer++;
                        }
                        if (view_timer == 4) {         //timer가 가득차게 되면 탐색을 멈추고 해당 건물을 출력함
                            searchButton.setPress(2);
                            timer.cancel();
                            //      Server_send.run();
                            startIntentService();       //찾은 건물의 중심 위도 경도를 건물 주소로 바꿈
                            DrawOnTop mDraw = new DrawOnTop(mContext);    //그림을 다시 그림
                            addContentView(mDraw, new android.support.v7.app.ActionBar.LayoutParams(android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT, android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT));
                        }
                    }
                });
            }
        };
        return temptask;
    }
    Boolean User_in_building() {    //사용자가 건물 안에 있는지 확인하는 함수
        for (int i = 0; i < RRMBR.size(); i++) {    //건물의 총 개수만큼 반복함
            //사각형 내부의 점을 구하는 함수를 사용하려고 했으나, RRMBR으로써 4꼭지점 좌표를 모두 이용해야 해서 사각형 내부의 점 함수를 사용할 수 없음
            // 그래서 삼각형으로 2구역을 나눈후 2개의 삼각형안에 자신의 위치가 포함되어있는지를 판단함
            Triangle triangle1 = new Triangle(new LWPoint(RRMBR.get(i).getLU().latitude, RRMBR.get(i).getLU().longitude)
                    , new LWPoint(RRMBR.get(i).getLD().latitude, RRMBR.get(i).getLD().longitude)
                    , new LWPoint(RRMBR.get(i).getRU().latitude, RRMBR.get(i).getRU().longitude));
            Triangle triangle2 = new Triangle(new LWPoint(RRMBR.get(i).getLU().latitude, RRMBR.get(i).getLU().longitude)
                    , new LWPoint(RRMBR.get(i).getLD().latitude, RRMBR.get(i).getLD().longitude)
                    , new LWPoint(RRMBR.get(i).getRD().latitude, RRMBR.get(i).getRD().longitude));
            //건물안에 자신의 좌표가 포함된다면
            if (triangle1.isInside(new LWPoint(cnt_latitude, cnt_longitude)) || triangle2.isInside(new LWPoint(cnt_latitude, cnt_longitude))) {
                android.support.v7.app.AlertDialog.Builder dialog3 = new android.support.v7.app.AlertDialog.Builder(MainActivity.this,R.style.My_dialog);    //위치 설정한 후 다시 시도하라고 dialog 알림창 표시
                dialog3.setTitle("탐색된 건물")
                        .setMessage("현재 " + RRMBR.get(i).getName() + " 건물 안에 있습니다.\n!주의!\n 다른 건물을 탐색하고 싶을 경우에는 밖으로 나가주세요!!")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                Dialog dialog0 = dialog3.create();
                dialog0.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog0.show();
                return true;
            }
        }
        return false;
    }
    void TriangleInCheck() {    //자신의 시야에 들어오는 건물을 확인하는 함수
        Triangle_in_building_number = 0;
        LWTM temp_a, temp_b, temp_c, temp_d,temp_center;
        for (int i = 0; i < RRMBR.size(); i++)  //시야안에 들어오는 건물을 확인하기 위해 전부 false로 체크해둠
            Triangle_in_building[i] = false;

        v1 = LWLatLngtoTM(new LWLatLng(Viewrange.get(0).latitude, Viewrange.get(0).longitude)); //각 변수에 삼각형 x,y TM좌표를 저장함
        v2 = LWLatLngtoTM(new LWLatLng(Viewrange.get(1).latitude, Viewrange.get(1).longitude));
        v3 = LWLatLngtoTM(new LWLatLng(Viewrange.get(2).latitude, Viewrange.get(2).longitude));


        LWPoint point_a, point_b, point_c, point_d;
        point_a = new LWPoint(v1.getX(), v1.getY());
        point_b = new LWPoint(v2.getX(), v2.getY());
        point_c = new LWPoint(v3.getX(), v3.getY());
        Triangle tri = new Triangle(point_a, point_b, point_c);

        for (int i = 0; i < RRMBR.size(); i++) {    //모든 건물에 대해서 시야에 들어와있는지 탐색 과정을 진행함
            temp_a = RRMBR.get(i).getTM_LU();
            temp_b = RRMBR.get(i).getTM_RD();
            temp_c = RRMBR.get(i).getTM_LD();
            temp_d = RRMBR.get(i).getTM_RU();
            temp_center = RRMBR.get(i).getTM_Center();
            point_d = new LWPoint(temp_center.getX(),temp_center.getY());

            if(LineCross(v1,v2,temp_a,temp_b) || LineCross(v1,v2,temp_b,temp_c) || LineCross(v1,v2,temp_c,temp_d) || LineCross(v1,v2,temp_d,temp_a) ||
                    LineCross(v2,v3,temp_a,temp_b) || LineCross(v2,v3,temp_b,temp_c) || LineCross(v2,v3,temp_c,temp_d) || LineCross(v2,v3,temp_d,temp_a) ||
                    LineCross(v3,v1,temp_a,temp_b) || LineCross(v3,v1,temp_b,temp_c) || LineCross(v3,v1,temp_c,temp_d) || LineCross(v3,v1,temp_d,temp_a) ||
                    tri.isInside(point_d)){
                //건물중 한 변이라도 시야와 겹친다면 ?
                Triangle_in_building[i] = true;
                Triangle_in_building_number++;
            }
        }
    }
    public void Sight_Building() {               //가장 가까운 건물을 찾는 과정 + 각 건물과 사용자 위치에서 가장 각도가 벌어진 vertex 찾는 과정 ( 나중에 추후 R-tree로 변경 예정)

        v1 = LWLatLngtoTM(new LWLatLng(Viewrange.get(0).latitude, Viewrange.get(0).longitude)); //각 변수에 삼각형 x,y TM좌표를 저장함
        v2 = LWLatLngtoTM(new LWLatLng(Viewrange.get(2).latitude, Viewrange.get(2).longitude));
        v3 = LWLatLngtoTM(new LWLatLng(Viewrange.get(1).latitude, Viewrange.get(1).longitude));

        Building_Min_dist = new ArrayList<BuildingCenterIndex>();
        Building_Min_angle = new ArrayList<BuildingAngleVertex>();
        Sight_Search_Building = new ArrayList<BuildingAngleVertex>();
        for (int i = 0; i < RRMBR.size(); i++) {        // 각 건물과 현재위치의 거리를 구하는 과정
            LWTM lu = RRMBR.get(i).getTM_LU();
            LWTM ld = RRMBR.get(i).getTM_LD();
            LWTM ru = RRMBR.get(i).getTM_RU();
            LWTM rd = RRMBR.get(i).getTM_RD();
            LWTM cen =RRMBR.get(i).getTM_Center();
            LWTM my = v1;
            ArrayList<BuildingAngleVertex> Min_angle = new ArrayList<BuildingAngleVertex>();
            BuildingAngleVertex anglea = new BuildingAngleVertex((cos_law(lu, ld, my) * 180 / Math.PI), i, lu, ld);
            Min_angle.add(anglea);
            anglea = new BuildingAngleVertex((cos_law(lu, rd, my) * 180 / Math.PI), i, lu, rd);
            Min_angle.add(anglea);
            anglea = new BuildingAngleVertex((cos_law(lu, ru, my) * 180 / Math.PI), i, lu, ru);
            Min_angle.add(anglea);
            anglea = new BuildingAngleVertex((cos_law(ld, rd, my) * 180 / Math.PI), i, ld, rd);
            Min_angle.add(anglea);
            anglea = new BuildingAngleVertex((cos_law(ld, ru, my) * 180 / Math.PI), i, ld, ru);
            Min_angle.add(anglea);
            anglea = new BuildingAngleVertex((cos_law(ru, rd, my) * 180 / Math.PI), i, ru, rd);
            Min_angle.add(anglea);

            descending descendin = new descending();
            Collections.sort(Min_angle, descendin);
            Building_Min_angle.add(Min_angle.get(0));   //가장 큰 각도와 인덱스를 저장함

            ArrayList<BuildingCenterIndex> Min_array = new ArrayList<BuildingCenterIndex>();        //실제 건물의 중심 좌표를 저장하는 용도로 사용하지는 않았지만
            //단순히 가장 짧은 거리와 그 거리의 주인인 빌딩을 저장할 수 있는 클래스의 형태자체는 동일하기 때문에 사용했습니다.
            BuildingCenterIndex dista = new BuildingCenterIndex(Math.sqrt(Math.pow(my.getX() - lu.getX(), 2) + Math.pow(my.getY() - lu.getY(), 2)), i);
            Min_array.add(dista);
            dista = new BuildingCenterIndex(Math.sqrt(Math.pow(my.getX() - ld.getX(), 2) + Math.pow(my.getY() - ld.getY(), 2)), i);
            Min_array.add(dista);
            dista = new BuildingCenterIndex(Math.sqrt(Math.pow(my.getX() - ru.getX(), 2) + Math.pow(my.getY() - ru.getY(), 2)), i);
            Min_array.add(dista);
            dista = new BuildingCenterIndex(Math.sqrt(Math.pow(my.getX() - rd.getX(), 2) + Math.pow(my.getY() - rd.getY(), 2)), i);
            Min_array.add(dista);
            dista = new BuildingCenterIndex(Math.sqrt(Math.pow(my.getX() - cen.getX(), 2) + Math.pow(my.getY() - cen.getY(), 2)), i);
            Min_array.add(dista);

            //현재위치에서 각 꼭지점까지의 거리를 구함
            Ascending ascending = new Ascending();
            Collections.sort(Min_array, ascending);
            //올림차순으로 정렬함
            Building_Min_dist.add(Min_array.get(0));   //가장 짧은 거리와 건물의 인덱스를 저장함
        }
        Distance_Ascending distance_ascending = new Distance_Ascending();
        Collections.sort(Building_Min_dist, distance_ascending);     //가장 가까운 건물 순으로 정렬함
        Angle_for_search_building = new ArrayList<BuildingAngleVertex>();

        Recursion_search(v2, v3, v1);   //시야각 내에 존재하는 건물들을 재귀함수 형태로 반복해서 탐색해 나감

        if (draw_sight_building != null) {               //탐색이 끝나면 기존에 탐색을 지우고 재 할당함
            for (int i = 0; i < draw_sight_building.length; i++)
                draw_sight_building[i].remove();
        }
        if (draw_result != null)
            draw_result.remove();
        draw_sight_building = new Polygon[Sight_Search_Building.size()];
        for (int i = 0; i < Sight_Search_Building.size(); i++) {   //Recursion_search를 통해 탐색된 건물들을 탐색하는 시야를 그림
            LatLng temp_latlng1, temp_latlng2, temp_latlng3;
            temp_latlng1 = new LatLng(LWTMtoLatLng(Sight_Search_Building.get(i).Angle_a).getLat(), LWTMtoLatLng(Sight_Search_Building.get(i).Angle_a).getLng());
            temp_latlng2 = new LatLng(LWTMtoLatLng(Sight_Search_Building.get(i).Angle_b).getLat(), LWTMtoLatLng(Sight_Search_Building.get(i).Angle_b).getLng());
            temp_latlng3 = new LatLng(LWTMtoLatLng(v1).getLat(), LWTMtoLatLng(v1).getLng());
            draw_sight_building[i] = mGoogleMap.addPolygon(new PolygonOptions().add(temp_latlng1).add(temp_latlng2).add(temp_latlng3).strokeColor(Color.BLUE).strokeWidth(3).fillColor(Color.argb(200, 255, 0, 0)));
        }
        Search();   //해당 건물들 중에 탐색하고 싶은 건물을 찾음
        for (int i = 0; i < Angle_for_search_building.size(); i++) {    //찾고 싶은 건물을 파란색으로 칠해줌
            if (Angle_for_search_building.get(i).index == Finding) {
                Search_Building_name_view = Building_name.get(Finding);
                Draw_Building(Angle_for_search_building.get(i));
                draw_result = mGoogleMap.addPolygon(new PolygonOptions().add(RRMBR.get(Finding).getLU()).add(RRMBR.get(Finding).getRD()).add(RRMBR.get(Finding).getLD()).add(RRMBR.get(Finding).getRU()).strokeColor(Color.RED).strokeWidth(5).fillColor(Color.argb(255, 0, 0, 255)));
                break;
            }
        }
    }
    void Recursion_search(LWTM l, LWTM r, LWTM myposition) {
        LWPoint point_aa, point_bb, point_cc, point_dd, point_ff;
        point_aa = new LWPoint(myposition.getX(), myposition.getY());
        point_bb = new LWPoint(l.getX(), l.getY());
        point_cc = new LWPoint(r.getX(), r.getY());

        Triangle triangle = new Triangle(point_aa, point_bb, point_cc);
        for (int i = 0; i < Building_Min_dist.size(); i++) {        //가장 가까운 건물 순서대로 영역에 속해있는지 검사한다.
            LWTM building_a = Building_Min_angle.get(Building_Min_dist.get(i).index).Angle_a;
            LWTM building_b = Building_Min_angle.get(Building_Min_dist.get(i).index).Angle_b;
            //건물의 각도
            point_dd = new LWPoint(building_a.getX(), building_a.getY());
            point_ff = new LWPoint(building_b.getX(), building_b.getY());

            if (triangle.in(point_dd) && triangle.in(point_ff)) {   //건물 전체가 시야안에 들어와있는 경우
                double dir_a = distance(l.getX(), l.getY(), building_a.getX(), building_a.getY());
                double dir_b = distance(l.getX(), l.getY(), building_b.getX(), building_b.getY());
                if (dir_a <= dir_b) {   //a가 왼쪽 시야와 가까운 경우 (0번으로 저장)
                    Angle_for_search_building.add(new BuildingAngleVertex(0.0, Building_Min_dist.get(i).index, LWTM_LineInterSection(v1, building_a, l, r, 0), LWTM_LineInterSection(v1, building_b, l, r, 0)));
                    Recursion_search(l, LWTM_LineInterSection(v1, building_a, l, r, 0), v1);
                    Recursion_search(LWTM_LineInterSection(v1, building_b, l, r, 0), r, v1);
                    Sight_Search_Building.add(new BuildingAngleVertex(0.0, 0, building_a, building_b));
                } else {  //b 가 왼쪽에 있는 경우     (1번으로 저장)
                    Angle_for_search_building.add(new BuildingAngleVertex(1.0, Building_Min_dist.get(i).index, LWTM_LineInterSection(v1, building_b, l, r, 0), LWTM_LineInterSection(v1, building_a, l, r, 0)));
                    Recursion_search(l, LWTM_LineInterSection(v1, building_b, l, r, 0), v1);
                    Recursion_search(LWTM_LineInterSection(v1, building_a, l, r, 0), r, v1);
                    Sight_Search_Building.add(new BuildingAngleVertex(0.0, 0, building_a, building_b));
                }
                break;
            } else if (!triangle.in(point_ff) && !triangle.in(point_dd)) {    //건물 각도가 시야보다 큰 경우 or 건물이 시야에 아예 들어오지 않는 경우  1번으로 저장
                if (LineCross(v1, r, building_a, building_b) && LineCross(v1, l, building_a, building_b)) { //2번으로 저장 ( 건물이 더 큰경우)
                    Angle_for_search_building.add(new BuildingAngleVertex(2.0, Building_Min_dist.get(i).index, new LWTM(0.0, 0.0), new LWTM(1900.0, 0.0)));
                    Sight_Search_Building.add(new BuildingAngleVertex(0.0, 0, LWTM_LineInterSection(v1, l, building_a, building_b, 0), LWTM_LineInterSection(v1, r, building_a, building_b, 0)));
                    break;
                }
            } else if (triangle.in(point_ff) || triangle.in(point_dd)) { //부분이 시야에 들어와있는 경우
                if (triangle.in(point_dd)) {
                    if (LineCross(v1, l, building_a, building_b)) {  //A점이 안쪽에 있고, 왼쪽 시야에 가까이 있는 경우 (3번으로 저장)
                        Angle_for_search_building.add(new BuildingAngleVertex(3.0, Building_Min_dist.get(i).index, LWTM_LineInterSection(v1, l, building_a, building_b, 0), building_a));

                        Recursion_search(LWTM_LineInterSection(v1, building_a, l, r, 0), r, v1);
                        Sight_Search_Building.add(new BuildingAngleVertex(0.0, 0, LWTM_LineInterSection(v1, l, building_a, building_b, 0), building_a));
                    } else if (LineCross(v1, r, building_a, building_b)) { //A점이 안쪽에 있고, 오른쪽 시야에 가까이 있는 경우 (4번으로 저장)
                        Angle_for_search_building.add(new BuildingAngleVertex(4.0, Building_Min_dist.get(i).index, building_a, LWTM_LineInterSection(v1, r, building_a, building_b, 0)));
                        Recursion_search(l, LWTM_LineInterSection(v1, building_a, l, r, 0), v1);
                        Sight_Search_Building.add(new BuildingAngleVertex(0.0, 0, building_a, LWTM_LineInterSection(v1, r, building_a, building_b, 0)));
                    } else {
                        Log.e("error","error");
                    }
                } else {
                    if (LineCross(v1, l, building_a, building_b)) {  //B점이 안쪽에 있고, 왼쪽 시야와 가까운 경우 (5번으로 저장)
                        Angle_for_search_building.add(new BuildingAngleVertex(5.0, Building_Min_dist.get(i).index, LWTM_LineInterSection(v1, l, building_a, building_b, 0), building_b));
                        Recursion_search(LWTM_LineInterSection(v1, building_b, l, r, 0), r, v1);
                        Sight_Search_Building.add(new BuildingAngleVertex(0.0, 0, LWTM_LineInterSection(v1, l, building_a, building_b, 0), building_b));
                    } else {       //B점이 안쪽에 있고, 오른쪽 시야와 가까운 경우( 6번으로 저장)
                        Angle_for_search_building.add(new BuildingAngleVertex(6.0, Building_Min_dist.get(i).index, building_b, LWTM_LineInterSection(v1, r, building_a, building_b, 0)));
                        Recursion_search(l, LWTM_LineInterSection(v1, building_b, l, r, 0), v1);
                        Sight_Search_Building.add(new BuildingAngleVertex(0.0, 0, LWTM_LineInterSection(v1, r, building_a, building_b, 0), building_b));
                    }
                }
                break;
            } else {
                Log.e("error_situation","error");
            }
        }
    }
    public int Search() {   //건물 중앙에 있는 거 찾는거
        int ccnt = 0;
        int find = 0;
        crossvertex = new ArrayList<ArrayList<CrossVertex>>();
        ArrayIndex[] line_cnt = new ArrayIndex[Triangle_in_building_number];    //건물이 직선 가지는 개수
        for (int dim = 51; dim <= 100; dim++) //시야를 100등분 한다.
        {
            LWTM mypos = v1;  //내위치
            LWTM Sighta = v2;
            LWTM Sightb = v3;
            //여기서는 50에서 100 방향으로 가는 구간
            ArrayList<CrossVertex> templist = new ArrayList<CrossVertex>();
            LWTM Sight = new LWTM(Sighta.getX() + (Sightb.getX() - Sighta.getX()) / 100.0 * (double) dim, Sighta.getY() + (Sightb.getY() - Sighta.getY()) / 100.0 * (double) dim);

            for (int i = 0; i < Triangle_in_building.length; i++) //시야안에 들어오는 건물을 가져온다
            {
                if (Triangle_in_building[i] == true) {  //시야안에 들어온 건물이면
                    LWTM a, b, c, d;       //건물의 꼭지점 4개
                    if (dim == 51) {
                        line_cnt[ccnt] = new ArrayIndex(ccnt, i);
                        ccnt++;
                    }
                    a = LWLatLngtoTM(new LWLatLng(RRMBR.get(i).getLU().latitude, RRMBR.get(i).getLU().longitude));
                    b = LWLatLngtoTM(new LWLatLng(RRMBR.get(i).getRD().latitude, RRMBR.get(i).getRD().longitude));
                    c = LWLatLngtoTM(new LWLatLng(RRMBR.get(i).getLD().latitude, RRMBR.get(i).getLD().longitude));
                    d = LWLatLngtoTM(new LWLatLng(RRMBR.get(i).getRU().latitude, RRMBR.get(i).getRU().longitude));
                    //중앙부터 좌우로 직선이 퍼지게 검사를 하는데, 이 직선이 닿는 건물을 찾는 과정임
                    if (LineCross(mypos, Sight, a, b)) {
                        double dis = LineInterSection(mypos, Sight, a, b, dim); //dim은 test 용
                        LWTM dis_pos = LWTM_LineInterSection(mypos, Sight, a, b, dim);
                        CrossVertex temp = new CrossVertex(dis, i, dis_pos);
                        templist.add(temp);
                    }
                    if (LineCross(mypos, Sight, b, c)) {
                        double dis = LineInterSection(mypos, Sight, b, c, dim);
                        LWTM dis_pos = LWTM_LineInterSection(mypos, Sight, b, c, dim);
                        CrossVertex temp = new CrossVertex(dis, i, dis_pos);
                        templist.add(temp);
                    }
                    if (LineCross(mypos, Sight, c, d)) {
                        double dis = LineInterSection(mypos, Sight, c, d, dim);
                        LWTM dis_pos = LWTM_LineInterSection(mypos, Sight, c, d, dim);
                        CrossVertex temp = new CrossVertex(dis, i, dis_pos);
                        templist.add(temp);
                    }
                    if (LineCross(mypos, Sight, d, a)) {
                        double dis = LineInterSection(mypos, Sight, d, a, dim);
                        LWTM dis_pos = LWTM_LineInterSection(mypos, Sight, d, a, dim);
                        CrossVertex temp = new CrossVertex(dis, i, dis_pos);
                        templist.add(temp);
                    }
                }
            }
            distanceSort distancesort = new distanceSort();
            Collections.sort(templist, distancesort);
            crossvertex.add(templist);
            if (templist.size() != 0) {
                find = (dim - 51) * 2;
                break;
            }
            // 여기까지 51 - 100까지 구간이다.
            // 여기부터는 50 - 1 구간
            templist = new ArrayList<CrossVertex>();
            Sight = new LWTM(Sighta.getX() + (Sightb.getX() - Sighta.getX()) / 100.0 * (double) (101 - dim), Sighta.getY() + (Sightb.getY() - Sighta.getY()) / 100.0 * (double) (101 - dim));

            for (int i = 0; i < Triangle_in_building.length; i++) //시야안에 들어오는 건물을 가져온다
            {
                if (Triangle_in_building[i] == true) {  //시야안에 들어온 건물이면
                    LWTM a, b, c, d;       //건물의 꼭지점 4개

                    a = LWLatLngtoTM(new LWLatLng(RRMBR.get(i).getLU().latitude, RRMBR.get(i).getLU().longitude));
                    b = LWLatLngtoTM(new LWLatLng(RRMBR.get(i).getRD().latitude, RRMBR.get(i).getRD().longitude));
                    c = LWLatLngtoTM(new LWLatLng(RRMBR.get(i).getLD().latitude, RRMBR.get(i).getLD().longitude));
                    d = LWLatLngtoTM(new LWLatLng(RRMBR.get(i).getRU().latitude, RRMBR.get(i).getRU().longitude));

                    if (LineCross(mypos, Sight, a, b)) {
                        double dis = LineInterSection(mypos, Sight, a, b, dim); //dim은 test 용
                        LWTM dis_pos = LWTM_LineInterSection(mypos, Sight, a, b, dim);
                        CrossVertex temp = new CrossVertex(dis, i, dis_pos);
                        templist.add(temp);
                    }
                    if (LineCross(mypos, Sight, b, c)) {
                        double dis = LineInterSection(mypos, Sight, b, c, dim);
                        LWTM dis_pos = LWTM_LineInterSection(mypos, Sight, b, c, dim);
                        CrossVertex temp = new CrossVertex(dis, i, dis_pos);
                        templist.add(temp);
                    }
                    if (LineCross(mypos, Sight, c, d)) {
                        double dis = LineInterSection(mypos, Sight, c, d, dim);
                        LWTM dis_pos = LWTM_LineInterSection(mypos, Sight, c, d, dim);
                        CrossVertex temp = new CrossVertex(dis, i, dis_pos);
                        templist.add(temp);
                    }
                    if (LineCross(mypos, Sight, d, a)) {
                        double dis = LineInterSection(mypos, Sight, d, a, dim);
                        LWTM dis_pos = LWTM_LineInterSection(mypos, Sight, d, a, dim);
                        CrossVertex temp = new CrossVertex(dis, i, dis_pos);
                        templist.add(temp);
                    }
                }
            }
            Collections.sort(templist, distancesort);
            crossvertex.add(templist);
            if (templist.size() != 0) {
                find = (dim - 51) * 2 + 1;
                break;
            }
        }
        //각 100등분된 선분이 몇개의 선분을 지나치는지 개수 획득완료
        //지나가는 선분의 좌표를 얻어야 하고, 그 선분이 속한 건물의 정보도 알아야 한다.
        int empty_cnt = 0;
        Finding = -1;
        LWTM Finding_pos = null;
        for (int j = 0; j < line_cnt.length; j++) {
            if (line_cnt[j].buildingindex == crossvertex.get(find).get(0).BuildingIndex) {
                Finding = line_cnt[j].buildingindex;
                Finding_pos = crossvertex.get(find).get(0).X;
            }
        }
        return Finding;
    }
    public void Draw_Building(BuildingAngleVertex ba) {
        LWTM draw_v1 = LWLatLngtoTM(new LWLatLng(Viewrange.get(0).latitude, Viewrange.get(0).longitude)); //각 변수에 삼각형 x,y TM좌표를 저장함
        LWTM draw_v2 = LWLatLngtoTM(new LWLatLng(Viewrange.get(2).latitude, Viewrange.get(2).longitude));
        LWTM draw_v3 = LWLatLngtoTM(new LWLatLng(Viewrange.get(1).latitude, Viewrange.get(1).longitude));

        LWPoint draw_a = new LWPoint(ba.Angle_a.getX(), ba.Angle_a.getY());
        LWPoint draw_b = new LWPoint(ba.Angle_b.getX(), ba.Angle_b.getY());
        LWPoint draw_c = new LWPoint(draw_v1.getX(), draw_v1.getY());
        LWPoint draw_d = new LWPoint(draw_v2.getX(), draw_v2.getY());
        LWPoint draw_e = new LWPoint(draw_v3.getX(), draw_v3.getY());
        Triangle triangle1 = new Triangle(draw_c, draw_d, draw_e);

        Display display = getWindowManager().getDefaultDisplay();
        Point psize = new Point();
        display.getSize(psize);

        if (ba.center == 0.0 || ba.center == 1.0) { //건물이 시야 전체에 들어온 경우
            if (ba.center == 0.0) { //a점이 왼쪽 시선과 가까운 경우
                LWTM ctm = LWTM_LineInterSection(draw_v2, draw_v3, draw_v1, new LWTM(draw_a.getX(), draw_a.getY()), 0);
                LWTM dtm = LWTM_LineInterSection(draw_v2, draw_v3, draw_v1, new LWTM(draw_b.getX(), draw_b.getY()), 0);
                LWPoint c = new LWPoint(ctm.getX(), ctm.getY());
                LWPoint d = new LWPoint(dtm.getX(), dtm.getY());

                double ratio_a, ratio_b, ratio_c, ratio_total; // ratio_a : 왼쪽 시야에서 a 까지, ratio_b : a에서 b까지 ratio_c : b에서 오른쪽 시야까지 ratio_total : 왼쪽시야에서 오른쪽 시야까지
                ratio_total = distance(draw_v2.x, draw_v2.y, draw_v3.x, draw_v3.y);
                ratio_a = distance(draw_v2.x, draw_v2.y, c.getX(), c.getY()) / ratio_total;
                ratio_b = distance(c.getX(), c.getY(), d.getX(), d.getY()) / ratio_total;
                ratio_c = distance(d.getX(), d.getY(), draw_v3.getX(), draw_v3.getY()) / ratio_total;

                building_line_a = new Point((int) (ratio_a * psize.x), 1);
                building_line_b = new Point((int) (ratio_b * psize.x + ratio_a * psize.x), 1);
            } else {      //a점이 오른쪽 시선과 가까운 경우
                LWTM ctm = LWTM_LineInterSection(draw_v2, draw_v3, draw_v1, new LWTM(draw_b.getX(), draw_b.getY()), 0);
                LWTM dtm = LWTM_LineInterSection(draw_v2, draw_v3, draw_v1, new LWTM(draw_a.getX(), draw_a.getY()), 0);
                LWPoint c = new LWPoint(ctm.getX(), ctm.getY());
                LWPoint d = new LWPoint(dtm.getX(), dtm.getY());

                double ratio_a, ratio_b, ratio_c, ratio_total; // ratio_a : 왼쪽 시야에서 a 까지, ratio_b : a에서 b까지 ratio_c : b에서 오른쪽 시야까지 ratio_total : 왼쪽시야에서 오른쪽 시야까지
                ratio_total = distance(draw_v2.x, draw_v2.y, draw_v3.x, draw_v3.y);
                ratio_a = distance(draw_v2.x, draw_v2.y, c.getX(), c.getY()) / ratio_total;
                ratio_b = distance(c.getX(), c.getY(), d.getX(), d.getY()) / ratio_total;
                building_line_a = new Point((int) (ratio_a * psize.x), 1);
                building_line_b = new Point((int) (ratio_b * psize.x + ratio_a * psize.x), 1);
            }
        } else if (ba.center == 2.0) { //건물이 시야보다 큰 경우
            building_line_a = new Point(1, 1);
            building_line_b = psize;
        } else if (ba.center == 3.0 || ba.center == 4.0 || ba.center == 5.0 || ba.center == 6.0) {   //건물의 부분이 시야에 들어온 경우
            if (ba.center == 3.0) {   // ()는 시야 안  b----(a)
                LWTM ctm = LWTM_LineInterSection(draw_v1, new LWTM(draw_b.getX(), draw_b.getY()), draw_v2, draw_v3, 0);
                LWPoint c = new LWPoint(ctm.getX(), ctm.getY());
                double ratio_a, ratio_b, ratio_total;
                ratio_total = distance(draw_v2.x, draw_v2.y, draw_v3.x, draw_v3.y);
                ratio_a = distance(draw_v2.x, draw_v2.y, c.getX(), c.getY()) / ratio_total;

                building_line_a = new Point(1, 1);
                building_line_b = new Point((int) (ratio_a * psize.x), 1);
            } else if (ba.center == 4.0) {  // ()는 시야 안  (a)----b
                LWTM ctm = LWTM_LineInterSection(draw_v1, new LWTM(draw_a.getX(), draw_a.getY()), draw_v2, draw_v3, 0);
                LWPoint c = new LWPoint(ctm.getX(), ctm.getY());
                double ratio_a, ratio_b, ratio_total;
                ratio_total = distance(draw_v2.x, draw_v2.y, draw_v3.x, draw_v3.y);
                ratio_a = distance(draw_v2.x, draw_v2.y, c.getX(), c.getY()) / ratio_total;

                building_line_b = new Point(psize.x, 1);
                building_line_a = new Point((int) (ratio_a * psize.x), 1);
            } else if (ba.center == 5.0) {    // ()는 시야 안  a----(b)
                LWTM ctm = LWTM_LineInterSection(draw_v1, new LWTM(draw_b.getX(), draw_b.getY()), draw_v2, draw_v3, 0);
                LWPoint c = new LWPoint(ctm.getX(), ctm.getY());
                double ratio_a, ratio_b, ratio_total;
                ratio_total = distance(draw_v2.x, draw_v2.y, draw_v3.x, draw_v3.y);
                ratio_a = distance(draw_v2.x, draw_v2.y, c.getX(), c.getY()) / ratio_total;
                building_line_a = new Point(1, 1);
                building_line_b = new Point((int) (ratio_a * psize.x), 1);
            } else if (ba.center == 6.0) {    // ()는 시야 안 (b)----a
                LWTM ctm = LWTM_LineInterSection(draw_v1, new LWTM(draw_a.getX(), draw_a.getY()), draw_v2, draw_v3, 0);
                LWPoint c = new LWPoint(ctm.getX(), ctm.getY());
                double ratio_a, ratio_b, ratio_total;
                ratio_total = distance(draw_v2.x, draw_v2.y, draw_v3.x, draw_v3.y);
                ratio_a = distance(draw_v2.x, draw_v2.y, c.getX(), c.getY()) / ratio_total;
                building_line_a = new Point((int) (ratio_a * psize.x), 1);
                building_line_b = new Point(psize.x, 1);
            } else {
                Log.e("ratio_error","error");
            }
        }
    }
    boolean CheckPosMbr(){
        // 현재 좌표를 받았는 지와 건물 정보를 받았는지 확인하는 함수
        if (cnt_latitude == 0.0 && cnt_longitude == 0.0) {   //현재 위치를 받지 못한 경우 - 이경우도 그렇고
            searchButton.setPress(0);               //다시 기본 건물 탐색 사진으로 바꾼다
            android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(MainActivity.this,R.style.My_dialog);    //위치 설정한 후 다시 시도하라고 dialog 알림창 표시
            dialog.setTitle("위치 확인")
                    .setMessage("현재 위치를 다시 확인해주십시오.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).create().show();
            return false;
        }
        if (RRMBR == null || RRMBR.isEmpty()) {     //서버로부터 받은 건물정보가 없을 경우 - 이경우도 그렇고
            searchButton.setPress(0);
            //      Server_send.run();
            android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(MainActivity.this,R.style.My_dialog);    //위치 설정한 후 다시 시도하라고 dialog 알림창 표시
            dialog.setTitle("알림")
                    .setMessage("건물 정보가 없습니다! 다시 확인해주십시오.")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    }).create().show();
            return false;
        }
        return true;
    }

    /********* 센서 관련 함수(시야각) **********/
    private void calculateFOV(CameraManager cManager) { //카메라의 화각을 가져오는 함수 ( horizonalAngle을 구하기 위한 함수)
        try {
            for (final String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    float[] maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                    SizeF size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                    float w = size.getWidth();
                    float h = size.getHeight();
                    horizonalAngle = (float) (2 * Math.atan(w / (maxFocus[0] * 2))) / 3 * 2 * 3 / 2;
                    verticalAngle = (float) (2 * Math.atan(h / (maxFocus[0] * 2)));
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void onPause() {
        super.onPause();
        manager.unregisterListener(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
        sensorManager.registerListener(this, gsensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, msensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(this, mAccelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(this, mMagnetometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(this, manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {        //현재 위치와 카메라가 쳐다보는 각도를 이용해 시야각을 구함// 기존에 돌아가는 코드
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        }
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I,
                    mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                // orientation contains: azimut, pitch  and roll
                azimut = orientation[0];
                ///////////////////////////////////////////////////////Compass
                final float alpha = 0.97f;

                synchronized (this) {
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        mmGravity[0] = alpha * mmGravity[0] + (1 - alpha)
                                * event.values[0];
                        mmGravity[1] = alpha * mmGravity[1] + (1 - alpha)
                                * event.values[1];
                        mmGravity[2] = alpha * mmGravity[2] + (1 - alpha)
                                * event.values[2];
                        // Log.e(TAG, Float.toString(mGravity[0]));
                    }
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                        mmGeomagnetic[0] = alpha * mmGeomagnetic[0] + (1 - alpha)
                                * event.values[0];
                        mmGeomagnetic[1] = alpha * mmGeomagnetic[1] + (1 - alpha)
                                * event.values[1];
                        mmGeomagnetic[2] = alpha * mmGeomagnetic[2] + (1 - alpha)
                                * event.values[2];
                        // Log.e(TAG, Float.toString(event.values[0]));
                    }
                    float RR[] = new float[9];
                    float II[] = new float[9];
                    boolean ssuccess = SensorManager.getRotationMatrix(RR, II, mmGravity,
                            mmGeomagnetic);
                    if (ssuccess) {
                        float oorientation[] = new float[3];
                        SensorManager.getOrientation(RR, oorientation);
                        azimuth = -oorientation[0] + 0.3;
                    }
                }
                /////////////////////////////////////////
                LatLng currentLocation1 = new LatLng(cnt_latitude, cnt_longitude);
                LWTM LWTM_currentLocation1 = LWLatLngtoTM(new LWLatLng(currentLocation1.latitude, currentLocation1.longitude));
                // double d = 0.0025;
                double d = 150;
                double ac = d * Math.tan(horizonalAngle / 2);
                double oa = ac / sin(horizonalAngle / 2);
                Ax = LWTM_currentLocation1.getX() + oa * cos(azimuth - (horizonalAngle / 2));
                Ay = LWTM_currentLocation1.getY() + oa * sin(azimuth - (horizonalAngle / 2));
                Bx = LWTM_currentLocation1.getX() + oa * cos(azimuth + (horizonalAngle / 2));
                By = LWTM_currentLocation1.getY() + oa * sin(azimuth + (horizonalAngle / 2));


                LatLng currentLocation3 = new LatLng(LWTMtoLatLng(new LWTM(Ax, Ay)).getLat(), LWTMtoLatLng(new LWTM(Ax, Ay)).getLng());
                LatLng currentLocation4 = new LatLng(LWTMtoLatLng(new LWTM(Bx, By)).getLat(), LWTMtoLatLng(new LWTM(Bx, By)).getLng());

                //시야의 3 좌표를 저장함
                Viewrange = new ArrayList<LatLng>();
                Viewrange.add(currentLocation1);
                Viewrange.add(currentLocation3);
                Viewrange.add(currentLocation4);

                if (draw_view != null)     //시야각을 그림
                    draw_view.remove();
                draw_view = mGoogleMap.addPolygon(new PolygonOptions().add(currentLocation1, currentLocation3, currentLocation4).strokeColor(Color.BLACK).strokeWidth(5));
                /*if(draw_view2 != null)
                    draw_view2.remove();
                PolylineOptions rectOptions = new PolylineOptions().add(currentLocation1).add(currentLocation3).add(currentLocation4).add(currentLocation1).width(5).color(Color.BLACK);
                draw_view2 = mGoogleMap.addPolyline(rectOptions);*/
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /*********구글맵 지도 관련 함수 *****/
    public void onMapReady(final GoogleMap map) {   //구글맵 그릴 때 초기값들 설정
        mGoogleMap = map;
        LatLng SEOUL = new LatLng(37.56, 126.97);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(SEOUL);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        map.addMarker(markerOptions);
        map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL));
        map.animateCamera(CameraUpdateFactory.zoomTo(15));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.setOnMapClickListener((GoogleMap.OnMapClickListener) this);
        mGoogleMap.setOnMapLongClickListener(this);
        mGoogleMap.setOnMyLocationButtonClickListener(this);
    }
    private void settingGPS(){
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {  //위치가 변경될 때마다 실행되는 함수
                cnt_latitude = location.getLatitude();              //위치가 갱신되면 위도와 경도,고도를 다시 저장한다
                cnt_longitude = location.getLongitude();
                if(Once == false) {
                    Once = true;
                    NetworkTask nt = new NetworkTask();
                    nt.execute(String.valueOf(cnt_latitude), String.valueOf(cnt_longitude));        //서버로 현재 위치 전송
                }
                setCurrentLocation(location);
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            public void onProviderEnabled(String provider) {
            }
            public void onProviderDisabled(String provider) {
            }
        };
    }
    public void setCurrentLocation(Location location ) {   //위치 바뀔 경우 구글맵 설정 변경하는 함수
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (location != null) {
            if (currentMarker != null)
                currentMarker.remove();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            markerOptions.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED));
            currentMarker = mGoogleMap.addMarker(markerOptions);
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            return;
        }
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
    }
    private Location getMyLocation() {  //권한 요청 후 현재 위치 가져오는 함수
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        return currentLocation;
    }
    @Override
    public void onMapClick(LatLng latLng) {
        mGoogleMap.clear();
        cnt_latitude = latLng.latitude;
        cnt_longitude = latLng.longitude;
        LatLng currentLocation = new LatLng(latLng.latitude, latLng.longitude);
        NetworkTask nt = new NetworkTask();
        nt.execute(String.valueOf(cnt_latitude), String.valueOf(cnt_longitude));
        if (currentMarker != null)
            currentMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLocation);
        markerOptions.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(cnt_latitude,cnt_longitude)));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
    }
    @Override
    public void onMapLongClick(LatLng latLng) {
        android.support.v7.app.AlertDialog.Builder ab = new android.support.v7.app.AlertDialog.Builder(this);
        ab.setTitle("Test 모드");
        ab.setMessage("Test 모드 시작");
        ab.show();
        Once = false;
        if(locationManager != null)
            locationManager.removeUpdates(locationListener);
    }
    @Override
    public boolean onMyLocationButtonClick(){
        android.support.v7.app.AlertDialog.Builder ab = new android.support.v7.app.AlertDialog.Builder(this);
        ab.setTitle("Test 모드");
        ab.setMessage("Test 모드 종료");
        ab.show();
        if(locationListener != null)
            locationManager.removeUpdates(locationListener);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                cnt_latitude = location.getLatitude();
                cnt_longitude = location.getLongitude();
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if(Once == false) {
                    Once = true;
                    NetworkTask nt = new NetworkTask();
                    nt.execute(String.valueOf(cnt_latitude), String.valueOf(cnt_longitude));        //서버로 현재 위치 전송
                }
                if (currentMarker != null)
                    currentMarker.remove();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(currentLocation);
                markerOptions.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                currentMarker = mGoogleMap.addMarker(markerOptions);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }
            @Override
            public void onProviderEnabled(String s) {

            }
            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        return false;
    }

    /*********권한 설정 함수 **********/
    public void CheckPermission(){
        int hasCameraPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION);
        if( hasCameraPermission != PackageManager.PERMISSION_GRANTED  || hasFineLocationPermission != PackageManager.PERMISSION_GRANTED
                || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){
            //카메라 권한이나 위치 권한 둘 중 하나라도 없는 경우
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults.length > 0){
                for(int i=0;i<grantResults.length;i++){
                    if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                        // 권한 요청중 하나라도 거부된다면
                        new AlertDialog.Builder(this).setTitle("알림").setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다.")
                                .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).setNegativeButton("권한 설정", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                getApplicationContext().startActivity(intent);
                            }
                        }).setCancelable(false).show();
                        return;
                    }
                }
                recreate();
            }
        }
    }

    /********카메라 관련 함수 *********/
    private void Camera_init() {    //카메라 세팅 함수
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(0);
        // surfaceview setting
        mCameraHolder = mCameraView.getHolder();
        mCameraHolder.addCallback(this);
        mCameraHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Display display1 = getWindowManager().getDefaultDisplay();
        Point psize1 = new Point();
        display1.getSize(psize1);
        mapButton = new GraphicButton(new Rect(10, 10, 150, 150));
        searchButton = new GraphicButton(new Rect(10, psize1.y - 160, 160, psize1.y - 10));
        Bitmap upimage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_open);
        Bitmap downimage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.map_close);
        Bitmap upimage1 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.play);
        Bitmap downimage1 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.stop);
        Bitmap reimage1 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.replay);
        mapButton.setImages(upimage, downimage, null);
        searchButton.setImages(upimage1, downimage1, reimage1);
        DrawOnTop mDraw = new DrawOnTop(mContext);
        addContentView(mDraw, new android.support.v7.app.ActionBar.LayoutParams(android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT, android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT));

    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) { //카메라 Surface 콜백 함수
        try {
            if (mCamera == null) {
                Camera_init();
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //카메라 Surface 콜백 함수
        // View 가 존재하지 않을 때
        if (mCameraHolder.getSurface() == null) {
            return;
        }
        // 작업을 위해 잠시 멈춘다
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // 에러가 나더라도 무시한다.
        }
        // 카메라 설정을 다시 한다.
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(parameters);
        // View 를 재생성한다.
        try {
            mCamera.setPreviewDisplay(mCameraHolder);
            mCamera.startPreview();
        } catch (Exception e) {
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {//카메라 Surface 콜백 함수
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchx = (int) event.getX();
        int touchy = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_CANCEL:
                //제스처를 취하다가 중단했을 경우 발생하는 이벤트
                break;
            case MotionEvent.ACTION_UP:
                //화면을 누르고 있다가 뗄 경우 발생하는 이벤트
                break;
            case MotionEvent.ACTION_MOVE:// 화면을 누르고 있는 상태에서 움직이는 경우 발생하는 이벤트
                break;
            case MotionEvent.ACTION_DOWN:// 화면 터치했을 경우 발생하는 이벤트
                if (mapButton.touch(touchx, touchy)) {   //이미지 영역 터치했을 경우 if문 안으로 들어감
                    View innerview = getLayoutInflater().inflate(R.layout.seek_bar,null);
                    AlertDialog.Builder adialog =new AlertDialog.Builder(this);
                    adialog.setView(innerview);
                    seekSize = (SeekBar) innerview.findViewById(R.id.SeekBar_Size);
                    setSeekbar();

                    AlertDialog alert = adialog.create();
                    alert.setTitle("지도 크기 설정");

                    alert.show();
                } else if (searchButton.touch(touchx, touchy)) {
                    if (searchButton.mImageNum == 0) {        // 탐색 시작
                        searchButton.setPress(1);
                        if(CheckPosMbr()){
                            timer = timerTask();
                            Timer tm = new Timer();
                            tm.schedule(timer,0,1000);
                        }
                        DrawOnTop mDraw = new DrawOnTop(mContext);
                        addContentView(mDraw, new android.support.v7.app.ActionBar.LayoutParams(android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT, android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT));
                    } else if (searchButton.mImageNum == 1) {    // 탐색 정지
                        searchButton.setPress(0);

                        DrawOnTop mDraw = new DrawOnTop(mContext);
                        addContentView(mDraw, new android.support.v7.app.ActionBar.LayoutParams(android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT, android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT));
                    } else {                                        //탐색 취소
                        searchButton.setPress(1);

                        DrawOnTop mDraw = new DrawOnTop(mContext);
                        addContentView(mDraw, new android.support.v7.app.ActionBar.LayoutParams(android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT, android.support.v7.app.ActionBar.LayoutParams.WRAP_CONTENT));
                    }
                }
        }
        return super.onTouchEvent(event);
    }

    /***********뷰 설정 ***************/
    class DrawOnTop extends View {
        public DrawOnTop(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
        }
        @Override
        protected void onDraw(Canvas canvas) {      //건물 탐색 중이거나 탐색 후 변경 된 UI를 그림
            // TODO Auto-generated method stub
            Display display = getWindowManager().getDefaultDisplay();   //화면에 대한 정보를 가져옴

            Paint paint2 = new Paint();
            Point psize = new Point();
            Paint paint3 = new Paint();

            paint2.setStyle(Paint.Style.STROKE);
            paint2.setColor(Color.RED);                    // 적색
            paint2.setTextSize(80);                    // 적색
            paint2.setStrokeWidth(5);
            display.getSize(psize);
            paint3.setStyle(Paint.Style.FILL);
            paint3.setColor(Color.RED);                    // 적색
            paint3.setTextSize(80);                    // 적색
            paint3.setStrokeWidth(5);
            mapButton.draw(canvas);
            searchButton.draw(canvas);

            canvas.drawCircle(psize.x / 2, psize.y / 2, 30, paint2);
            canvas.drawLine(psize.x / 2 - 30, psize.y / 2, psize.x / 2 + 30, psize.y / 2, paint2);
            canvas.drawLine(psize.x / 2, psize.y / 2 - 30, psize.x / 2, psize.y / 2 + 30, paint2);
            if (view_timer == 1)            //건물 찾은 타이머의 상태에 따라 화면에 표시된 탐색 게이지(?)의 모양이 달라짐
                canvas.drawArc(psize.x / 2 - 30, psize.y / 2 - 30, psize.x / 2 + 30, psize.y / 2 + 30, 270, 90, true, paint3);
            else if (view_timer == 2)
                canvas.drawArc(psize.x / 2 - 30, psize.y / 2 - 30, psize.x / 2 + 30, psize.y / 2 + 30, 270, 180, true, paint3);
            else if (view_timer == 3)
                canvas.drawArc(psize.x / 2 - 30, psize.y / 2 - 30, psize.x / 2 + 30, psize.y / 2 + 30, 270, 270, true, paint3);
            else if (view_timer == 4) {
                canvas.drawArc(psize.x / 2 - 30, psize.y / 2 - 30, psize.x / 2 + 30, psize.y / 2 + 30, 270, 360, true, paint3);
            }
            invalidate();   //화면 갱신
            super.onDraw(canvas);
        }
    }
    private void setSeekbar(){
        int nMax = 1000;
        ViewGroup.LayoutParams params = relativeLayout.getLayoutParams();

        int nCurrentVolumn = params.height;
        seekSize.setMax(nMax);
        seekSize.setProgress(nCurrentVolumn);
        seekSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                ViewGroup.LayoutParams params = relativeLayout.getLayoutParams();
                params.width = progress;
                params.height= progress;
                relativeLayout.setLayoutParams(params);
            }
        });

    }
    /***********위도,경도 --> 주소 변환 ***/
    protected void startIntentService() {   //찾은 건물의 위도,경도를 메인 쓰레드가 아닌 백그라운드에서 주소로 변경하는 작업을 하게 하는 함수
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        //intent에 위도,경도, 결과를 받는 변수를 저장한 후 Service로 처리하게 함
        intent.putExtra("Latitude", RRMBR.get(Finding).getCenter().latitude);
        intent.putExtra("Longitude", RRMBR.get(Finding).getCenter().longitude);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        startService(intent);
    }
    private class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }
        int select = 0;
        void Dialog(String mAddressOutput){
            final String a = mAddressOutput;
            android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(MainActivity.this,R.style.My_dialog);    //위치 설정한 후 다시 시도하라고 dialog 알림창 표시
            if(Search_Building_name_view.equals("null")) {
                dialog.setTitle("\n\n               탐색 건물")
                        .setMessage("            "+mAddressOutput.substring(0,13)+"\n           "+ mAddressOutput.substring(13))
                        .setCancelable(false)
                        .setNeutralButton("웹 검색", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + a));
                                startActivity(intent);
                            }});
                Dialog dialog0 = dialog.create();
                dialog0.getWindow().setGravity(Gravity.LEFT|Gravity.TOP);
                dialog0.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog0.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog0.setCanceledOnTouchOutside(true);
                dialog0.show();
            }else{
                dialog.setTitle("\n\n               탐색 건물")
                        .setMessage("          (" + Search_Building_name_view +
                                ")\n            "+mAddressOutput.substring(0,13)+"\n           "+ mAddressOutput.substring(13))
                        .setCancelable(false)
                        .setNeutralButton("웹 검색", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=1&ie=utf8&query=" + a));
                                startActivity(intent);
                            }
                        });
                Dialog dialog0 = dialog.create();
                dialog0.getWindow().setGravity(Gravity.LEFT|Gravity.TOP);
                dialog0.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog0.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog0.setCanceledOnTouchOutside(true);
                dialog0.show();
            }

        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);   //바뀐 주소 결과값을 저장함if (ss != null)

            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.i("help", "Founding Address");
            }
            Dialog(mAddressOutput);
        }
    }
    /*********기타 기능 함수 **********/
    public void onBackPressed() {   //뒤로 가기 2번 누를 경우 app 종료
        if (pressedTime == 0) {
            Toast.makeText(MainActivity.this, " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show();
            pressedTime = System.currentTimeMillis();
        } else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);

            if (seconds > 2000) {
                Toast.makeText(MainActivity.this, " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show();
                pressedTime = 0;
            } else {
                super.onBackPressed();
                finish(); // app 종료 시키기
            }
        }
    }

    /*********서버로부터 데이터 받아오기 *****/
    public class NetworkTask extends AsyncTask<String, Void, String> {  //서버와 통신하는 함수
        String receiveMsg;
        @Override
        protected String doInBackground(String... strings) {
            recieve_data = new ArrayList<LatLng_name>();    //서버로부터 받은 건물이름과 해당 건물의 전체 좌표를 저장할 배열
            HttpURLConnection conn = null;
            try {
                String url = "http://210.119.30.213:8080/Ambient_browser/ambient_server.jsp";
                /* 통신 부분*/
                URL _url = new URL(url);
                conn = (HttpURLConnection) _url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                StringBuffer buffer = new StringBuffer();
                buffer.append("xpos").append("=").append(strings[0]).append("&ypos").append("=").append(strings[1]).append("&send").append("=").append("yes");
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
                outputStreamWriter.write(buffer.toString());
                outputStreamWriter.flush();
                /*통신 부분 종료*/
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.e("HTTP_OK", "연결 성공");
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "utf-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuilder builder = new StringBuilder();
                    String _str;
                    while ((_str = reader.readLine()) != null) {   //서버에 출력부분을 읽어드림
                        builder.append(_str + "\n");
                    }
                    /* body 부분에 출력되는 문장을 가져온다 ( json 형식으로 건물이름과 건물 좌표가 저장되어 있음) */
                    String res = builder.toString();
                    int w = res.indexOf("<body>");
                    int q = res.indexOf("</body>");

                    String body = res.substring(w + 6, q);
                    JSONObject json = new JSONObject(body);
                    JSONArray jArr = json.getJSONArray("dataSend");
                    /* json 파싱 시작*/
                    for (int i = 0; i < jArr.length(); i++) {
                        String aa = jArr.getJSONObject(i).getString("Building_name");
                        StringBuilder total_string = new StringBuilder();
                        ArrayList<LWLatLng> rec_temp = new ArrayList<LWLatLng>();
                        for (int j = 0; j < jArr.getJSONObject(i).getJSONArray("latlng").length(); j++) {
                            String bb = jArr.getJSONObject(i).getJSONArray("latlng").getJSONObject(j).getString("lat");
                            String cc = jArr.getJSONObject(i).getJSONArray("latlng").getJSONObject(j).getString("lng");
                            total_string.append(bb).append(",").append(cc).append("\n");
                            rec_temp.add(new LWLatLng(Double.parseDouble(bb), Double.parseDouble(cc)));
                        }
                        recieve_data.add(new LatLng_name(rec_temp, aa)); //파싱한 건물 데이터를 저장
                    }
                    /* json 파싱 종료 및 건물 데이터 저장 완료 */
                    /*recieve와 동일한 기능을 하는 함수지만, 기존의 코드를 유지해서 사용하기 위해서 recieve_data를 그대로 옮김*/

                    TMmap = new ArrayList<BuildingData>();
                    BuildingData buildingData;
                    Building_name = new ArrayList<>();
                    for (int i = 0; i < recieve_data.size(); i++) {
                        buildingData = new BuildingData(0, 0, "building", recieve_data.get(i).name, recieve_data.get(i).a.size(), recieve_data.get(i).a);
                        Building_name.add(recieve_data.get(i).name);
                        TMmap.add(buildingData);
                    }
                    GetRotatedMBR();    //서버로부터 받은 건물정보를 이용해서 RotatedMBR을 구함
                    Log.i("TAG","RRMBR Size : "+ RRMBR.size());
                    Log.i("TAG","TMmap Size : "+ TMmap.size());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {     //RotatedMBR을 지도에 그림
                            draw_building2 = new Polyline[RRMBR.size()];
                            for (int i = 0; i < RRMBR.size(); i++) {
                                PolylineOptions rectOptions = new PolylineOptions().add(RRMBR.get(i).getLU()).add(RRMBR.get(i).getRD()).add(RRMBR.get(i).getLD()).add(RRMBR.get(i).getRU()).add(RRMBR.get(i).getLU()).width(5).color(Color.RED);
                                draw_building2[i] = mGoogleMap.addPolyline(rectOptions);
                            }
                        }
                    });
                }
            } catch (MalformedURLException e) {
                Log.e("mal", "Malformed_error");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e("IOzxc", "IOException");
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e("IOzxc", "Json_exception");
                e.printStackTrace();
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
            return receiveMsg;
        }
    }

    /*********건물 MBR -> Rotated MBR ********/
    void GetRotatedMBR() {      //서버에서부터 받은 건물정보의 RotatedMBR을 구하는 함수 !!
        RRMBR = new ArrayList<>();   //회전시키기고 난 후의 MBR
        for (int s = 0; s < TMmap.size(); s++) {    //서버로부터 받은 건물 이름과 건물 좌표들
            ArrayList<LWLatLng> temp = new ArrayList<>();
            temp = TMmap.get(s).getVertex_LatLng();     //temp에 한 건물에 대한 Vertex 좌표를 건네줌
            RMBR = new ArrayList<>();
            ArrayList<Double> degree = new ArrayList<>();           //건물의 각 Vertex 간의 각도를 저장하기 위한 배열
            ArrayList<ArrayList<LWLatLng>> all_rotated_vertex = new ArrayList<ArrayList<LWLatLng>>();
            ArrayList<Double> square_area = new ArrayList<>();
            ArrayList<LWTM> cateria = new ArrayList<>();

            for (int i = 0; i < temp.size(); i++) {     //건물의 Vertex의 좌표 개수만큼 반복함
                LWLatLng latlng_p, latlng_q;       //각도를 구하기 위한 두 Vertex, 위도와 경도이다
                LWTM tm_p, tm_q;                 //위도,경도를 TM 좌표로 바꿨을 때 저장할 변수
                double temp_degree = 0.0;             //두 Vertex간의 각도를 저장할 변수
                if (i == temp.size() - 1) {   //마지막 Vertex의 경우, 첫 번째 Vertex와의 각도를 계산한다.
                    latlng_p = new LWLatLng(temp.get(i).lat, temp.get(i).lng);      //위도를 TM 좌표계로 변경하기 위해 객체 생성하는 과정
                    latlng_q = new LWLatLng(temp.get(0).lat, temp.get(0).lng);
                } else {                 //마지막 Vertex가 아닌 경우
                    latlng_p = new LWLatLng(temp.get(i).lat, temp.get(i).lng);
                    latlng_q = new LWLatLng(temp.get(i + 1).lat, temp.get(i + 1).lng);
                }
                tm_p = LWLatLngtoTM(latlng_p);      //위도와 경도를 , TM 좌표로 바꿔준다 ( 이유 : 거리나 각도 계산에 좀 더 정밀도가 높기 떄문이다)
                tm_q = LWLatLngtoTM(latlng_q);      //반전이다 .. 10시간을 헤맨 이유가 나왓다.. 실제로 위도는 y , 경도는 x를 가르킨다
                cateria.add(tm_p);
                //당연히 TM 좌표로 바꿧을 때도 동일하게 바뀔줄 알았는데, TM좌표로 바꾸게 되면, x좌표 자리에 x,y좌표 자리에 y가 들어간다..
                //알아서 변환해서 들어간다는 거.... ㅅㅂㅂ...
                if (tm_p.getX() < tm_q.getX() && tm_p.getY() > tm_q.getY()) {       //두 Vertex의 방향이 오른쪽 아래인 경우, tm_p의 getX는 latitude를 나타내고 있으므로, 실제로 y축을 의미한다
                    temp_degree = atan((tm_p.getY() - tm_q.getY()) / (tm_q.getX() - tm_p.getX()));
                    //Java 는 삼각함수를 라디안으로 받는다.
                } else if (tm_p.getX() < tm_q.getX() && tm_p.getY() < tm_q.getY()) { //두 Vertex의 방향이 오른쪽 윗인 경우
                    temp_degree = atan((tm_q.getX() - tm_p.getX()) / (tm_q.getY() - tm_p.getY()));
                } else if (tm_p.getX() > tm_q.getX() && tm_p.getY() > tm_q.getY()) { //두 Vertex의 방향이 왼쪽 아래인 경우
                    temp_degree = atan((tm_p.getX() - tm_q.getX()) / (tm_p.getY() - tm_q.getY()));
                } else if (tm_p.getX() > tm_q.getX() && tm_p.getY() < tm_q.getY()) { //두 Vertex의 방향이 왼쪽 윗인 경우
                    temp_degree = atan((tm_q.getY() - tm_p.getY()) / (tm_p.getX() - tm_q.getX()));
                }
                degree.add(temp_degree);
                ArrayList<LWLatLng> rotated_compare_square = new ArrayList<>();
                for (int j = 0; j < temp.size(); j++) {     //자신을 제외한 나머지 Vertex 회전 시키기
                    if (i != j) {      //기준이 되는 Vertex라면 회전을 시키지 않는다.
                        double rotate_x, rotate_y;  //회전시킨 좌표 저장할 변수 ( TM 좌표로 저장함)
                        LWTM TM_temp;
                        TM_temp = LWLatLngtoTM(new LWLatLng(temp.get(j).lat, temp.get(j).lng));
                        rotate_x = (Math.cos(temp_degree) * (TM_temp.getX() - tm_p.getX())) - (Math.sin(temp_degree) * (TM_temp.getY() - tm_p.getY())) + tm_p.getX();
                        rotate_y = (Math.sin(temp_degree) * (TM_temp.getX() - tm_p.getX())) + (Math.cos(temp_degree) * (TM_temp.getY() - tm_p.getY())) + tm_p.getY();
                        //각도에 따라 회전시킨다
                        LWTM temp_TM = new LWTM(rotate_x, rotate_y);
                        LWLatLng temp_LatLng = LWTMtoLatLng(temp_TM);
                        rotated_compare_square.add(temp_LatLng);
                        //회전된
                    } else {  //기준이 되는 점일 경우에는 기준되는 점을 고정해 놓는다.
                        LWTM aaaaa = new LWTM(tm_p.getX(), tm_p.getY());
                        LWLatLng bbbbb = LWTMtoLatLng(aaaaa);
                        rotated_compare_square.add(bbbbb);
                    }
                }
                all_rotated_vertex.add(rotated_compare_square);
                //모든 vertex를 회전시킨다음에 MBR을 구함
                double Max_x = 0.0, Max_y = 0.0, Min_x = 0.0, Min_y = 0.0;
                for (int k = 0; k < rotated_compare_square.size(); k++) {
                    if (k == 0) {
                        Max_x = rotated_compare_square.get(k).getLng();
                        Min_x = rotated_compare_square.get(k).getLng();
                        Max_y = rotated_compare_square.get(k).getLat();
                        Min_y = rotated_compare_square.get(k).getLat();
                    } else {
                        if (Max_x < rotated_compare_square.get(k).getLng())
                            Max_x = rotated_compare_square.get(k).getLng();
                        if (Min_x > rotated_compare_square.get(k).getLng())
                            Min_x = rotated_compare_square.get(k).getLng();
                        if (Max_y < rotated_compare_square.get(k).getLat())
                            Max_y = rotated_compare_square.get(k).getLat();
                        if (Min_y > rotated_compare_square.get(k).getLat())
                            Min_y = rotated_compare_square.get(k).getLat();
                    }
                }
                LatLng temp_LU, temp_RU, temp_RD, temp_LD;
                temp_LU = new LatLng(Max_y, Min_x);
                temp_RU = new LatLng(Max_y, Max_x);
                temp_RD = new LatLng(Min_y, Max_x);
                temp_LD = new LatLng(Min_y, Min_x);
                RMBR.add(new MBR(temp_LU, temp_RU, temp_RD, temp_LD));
                double area = (Max_x - Min_x) * (Max_y - Min_y);
                square_area.add(area);  //구한 MBR들의 면적들을 저장함
            }
            int Minimum = 0;            // 면적이 가장 작은 square가 몇 번째 Index 인지 저장하는 변수
            double min = 0.0;
            for (int k = 0; k < square_area.size(); k++) {  //면적이 가장 작은 square가 있는 Index를 찾아내는 코드
                if (k == 0)
                    min = square_area.get(k);
                else {
                    if (min > square_area.get(k)) {
                        min = square_area.get(k);
                        Minimum = k;
                    }
                }
            }
            //최소 면적인 BoundRect를 찾았으니까, 다시 각도를 되돌린다.
            double rotate_x1, rotate_y1, rotate_x2, rotate_y2, rotate_x3, rotate_y3, rotate_x4, rotate_y4;  //회전시킨 좌표 저장할 변수 ( TM 좌표로 저장함)
            LWTM TM_LU, TM_RU, TM_RD, TM_LD;
            TM_LU = LWLatLngtoTM(new LWLatLng(RMBR.get(Minimum).getLU().latitude, RMBR.get(Minimum).getLU().longitude));
            TM_RU = LWLatLngtoTM(new LWLatLng(RMBR.get(Minimum).getRU().latitude, RMBR.get(Minimum).getRU().longitude));
            TM_RD = LWLatLngtoTM(new LWLatLng(RMBR.get(Minimum).getRD().latitude, RMBR.get(Minimum).getRD().longitude));
            TM_LD = LWLatLngtoTM(new LWLatLng(RMBR.get(Minimum).getLD().latitude, RMBR.get(Minimum).getLD().longitude));

            rotate_x1 = (Math.cos(-degree.get(Minimum)) * (TM_LU.getX() - cateria.get(Minimum).getX())) - (Math.sin(-degree.get(Minimum)) * (TM_LU.getY() - cateria.get(Minimum).getY())) + cateria.get(Minimum).getX();
            rotate_y1 = (Math.sin(-degree.get(Minimum)) * (TM_LU.getX() - cateria.get(Minimum).getX())) + (Math.cos(-degree.get(Minimum)) * (TM_LU.getY() - cateria.get(Minimum).getY())) + cateria.get(Minimum).getY();
            rotate_x2 = (Math.cos(-degree.get(Minimum)) * (TM_RU.getX() - cateria.get(Minimum).getX())) - (Math.sin(-degree.get(Minimum)) * (TM_RU.getY() - cateria.get(Minimum).getY())) + cateria.get(Minimum).getX();
            rotate_y2 = (Math.sin(-degree.get(Minimum)) * (TM_RU.getX() - cateria.get(Minimum).getX())) + (Math.cos(-degree.get(Minimum)) * (TM_RU.getY() - cateria.get(Minimum).getY())) + cateria.get(Minimum).getY();
            rotate_x3 = (Math.cos(-degree.get(Minimum)) * (TM_RD.getX() - cateria.get(Minimum).getX())) - (Math.sin(-degree.get(Minimum)) * (TM_RD.getY() - cateria.get(Minimum).getY())) + cateria.get(Minimum).getX();
            rotate_y3 = (Math.sin(-degree.get(Minimum)) * (TM_RD.getX() - cateria.get(Minimum).getX())) + (Math.cos(-degree.get(Minimum)) * (TM_RD.getY() - cateria.get(Minimum).getY())) + cateria.get(Minimum).getY();
            rotate_x4 = (Math.cos(-degree.get(Minimum)) * (TM_LD.getX() - cateria.get(Minimum).getX())) - (Math.sin(-degree.get(Minimum)) * (TM_LD.getY() - cateria.get(Minimum).getY())) + cateria.get(Minimum).getX();
            rotate_y4 = (Math.sin(-degree.get(Minimum)) * (TM_LD.getX() - cateria.get(Minimum).getX())) + (Math.cos(-degree.get(Minimum)) * (TM_LD.getY() - cateria.get(Minimum).getY())) + cateria.get(Minimum).getY();

            LWTM temp_TM1 = new LWTM(rotate_x1, rotate_y1);
            LWTM temp_TM2 = new LWTM(rotate_x2, rotate_y2);
            LWTM temp_TM3 = new LWTM(rotate_x3, rotate_y3);
            LWTM temp_TM4 = new LWTM(rotate_x4, rotate_y4);
            LWLatLng temp_LatLng1 = LWTMtoLatLng(temp_TM1);
            LWLatLng temp_LatLng2 = LWTMtoLatLng(temp_TM2);
            LWLatLng temp_LatLng3 = LWTMtoLatLng(temp_TM3);
            LWLatLng temp_LatLng4 = LWTMtoLatLng(temp_TM4);

            MBR_Name temp_mbr = new MBR_Name(new LatLng(temp_LatLng1.getLat(), temp_LatLng1.getLng()), new LatLng(temp_LatLng2.getLat(), temp_LatLng2.getLng())
                    , new LatLng(temp_LatLng3.getLat(), temp_LatLng3.getLng()), new LatLng(temp_LatLng4.getLat(), temp_LatLng4.getLng()), TMmap.get(s).Object_name);
            RRMBR.add(temp_mbr);    //가장 작은 면적의 MBR을 원래 각도로 되돌린 다음에 해당 좌표들을 저장함
        }
        Triangle_in_building = new Boolean[RRMBR.size()];   //RotatedMBR을 구한 건물이 총 개수를 저장함
    }

    /******* TM -> LatLng , LatLng -> TM ****/
    public LWTM LWLatLngtoTM(LWLatLng zx_input) {
        double zx_phi = zx_input.lat / 180.0 * Math.PI;
        double zx_lambda = zx_input.lng / 180.0 * Math.PI;
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
    public LWLatLng LWTMtoLatLng(LWTM zx_input) {
        double zx_M = 4207498.019150324 + (zx_input.y - 500000.0);
        double zx_mu1 = zx_M / 6367449.145908449;
        double zx_phi1 = zx_mu1 + 0.0025188265967581876 * sin(2.0 * zx_mu1) + 0.0000037009490719640127 * sin(4.0 * zx_mu1) + 7.447813877211132e-9 * sin(6.0 * zx_mu1) + 1.7035993573185923e-11 * sin(8.0 * zx_mu1);
        double zx_R1 = 6335439.327083876 / Math.pow(1.0 - 0.006694380022900686 * Math.pow(sin(zx_phi1), 2.0), 1.5);
        double zx_C1 = 0.006739496775478856 * Math.pow(cos(zx_phi1), 2.0);
        double zx_T1 = Math.pow(Math.tan(zx_phi1), 2.0);
        double zx_N1 = 6378137.0 / Math.sqrt(1.0 - 0.006694380022900686 * Math.pow(sin(zx_phi1), 2.0));
        double zx_D = (zx_input.x - 200000.0) / (zx_N1 * 1.0);

        // 위도 출력
        double zx_phi = (zx_phi1 - (zx_N1 * Math.tan(zx_phi1) / zx_R1) * (Math.pow(zx_D, 2.0) / 2.0 - Math.pow(zx_D, 4.0) / 24.0 * (5.0 + 3.0 * zx_T1 + 10.0 * zx_C1 - 4.0 * Math.pow(zx_C1, 2.0) - 0.060655470979309706) + Math.pow(zx_D, 6.0) / 720.0 * (61.0 + 90.0 * zx_T1 + 298.0 * zx_C1 + 45.0 * Math.pow(zx_T1, 2.0) - 1.6983531874206717 - 3.0 * Math.pow(zx_C1, 2.0)))) * 180.0 / Math.PI;
        // 경도 출력
        double zx_lambda = 127.0 + ((1.0 / cos(zx_phi1)) * (zx_D - (Math.pow(zx_D, 3.0) / 6.0) * (1.0 + 2.0 * zx_T1 + zx_C1) + (Math.pow(zx_D, 5.0) / 120.0) * (5.0 - 2.0 * zx_C1 + 28.0 * zx_T1 - 3.0 * Math.pow(zx_C1, 2.0) + 0.053915974203830846 + 24.0 * Math.pow(zx_T1, 2.0)))) * 180.0 / Math.PI;

        return new LWLatLng(zx_phi, zx_lambda);
    }   //TM 좌표를 위도 경도로 변경

    /******* 수학 / 정렬 함수 ***********************/
    double cos_law(LWTM A, LWTM B, LWTM my) {
        double a, b, c;
        a = Math.sqrt(Math.pow(my.getX() - A.getX(), 2) + Math.pow(my.getY() - A.getY(), 2));
        b = Math.sqrt(Math.pow(my.getX() - B.getX(), 2) + Math.pow(my.getY() - B.getY(), 2));
        c = Math.sqrt(Math.pow(A.getX() - B.getX(), 2) + Math.pow(A.getY() - B.getY(), 2));

        double seta = Math.acos(((Math.pow(a, 2)) + Math.pow(b, 2) - Math.pow(c, 2)) / (2 * a * b));
        return seta;
    }
    class descending implements Comparator<BuildingCenterIndex> {
        //내림차순
        @Override
        public int compare(BuildingCenterIndex o1, BuildingCenterIndex o2) {
            return o2.center.compareTo(o1.center);
        }
    }
    class Ascending implements Comparator<BuildingCenterIndex> {
        //올림차순
        @Override
        public int compare(BuildingCenterIndex o1, BuildingCenterIndex o2) {
            return o1.center.compareTo(o2.center);
        }
    }
    class Distance_Ascending implements Comparator<BuildingCenterIndex> {
        //거리 올림차순
        @Override
        public int compare(BuildingCenterIndex o1, BuildingCenterIndex o2) {
            return o1.center.compareTo(o2.center);
        }
    }
    double distance(double a, double b, double c, double d) {
        //(a,b) (c,d) 거리 구하는 함수
        double dis = Math.sqrt(Math.pow(a - c, 2) + Math.pow(b - d, 2));
        return dis;
    }
    LWTM LWTM_LineInterSection(LWTM mypos, LWTM sight, LWTM a, LWTM b, int dim) {
        //두 직선 사이의 거리
        LWTM b_a = new LWTM(sight.getX() - mypos.getX(), sight.getY() - mypos.getY());
        LWTM d_c = new LWTM(b.getX() - a.getX(), b.getY() - a.getY());
        LWTM c_a = new LWTM(a.getX() - mypos.getX(), a.getY() - mypos.getY());
        double det = (b_a.getX() * d_c.getY()) - (b_a.getY() * d_c.getX());
        double right = ((c_a.getX() * d_c.getY()) - (c_a.getY() * d_c.getX())) / det;

        LWTM x = new LWTM(mypos.getX() + b_a.getX() * right, mypos.getY() + b_a.getY() * right);
        return x;
    }
    Boolean LineCross(LWTM mypos, LWTM sight, LWTM a, LWTM b) { //직선이 겹치는 지 확인하는 함수
        double ab = ccw(mypos, sight, a) * ccw(mypos, sight, b);
        double cd = ccw(a, b, mypos) * ccw(a, b, sight);
        return ab <= 0 && cd <= 0;
    }
    double ccw(LWTM a, LWTM b) {
        return (a.getX() * b.getY()) - (a.getY() * b.getX());
    }
    double ccw(LWTM p, LWTM a, LWTM b) {
        LWTM d = new LWTM(a.getX() - p.getX(), a.getY() - p.getY());
        LWTM f = new LWTM(b.getX() - p.getX(), b.getY() - p.getY());
        return ccw(d, f);
    }
    double LineInterSection(LWTM mypos, LWTM sight, LWTM a, LWTM b, int dim) {
        LWTM b_a = new LWTM(sight.getX() - mypos.getX(), sight.getY() - mypos.getY());
        LWTM d_c = new LWTM(b.getX() - a.getX(), b.getY() - a.getY());
        LWTM c_a = new LWTM(a.getX() - mypos.getX(), a.getY() - mypos.getY());
        double det = (b_a.getX() * d_c.getY()) - (b_a.getY() * d_c.getX());
        double right = ((c_a.getX() * d_c.getY()) - (c_a.getY() * d_c.getX())) / det;

        LWTM x = new LWTM(mypos.getX() + b_a.getX() * right, mypos.getY() + b_a.getY() * right);
        double dist = Math.sqrt(Math.pow(mypos.getX() - x.getX(), 2) + Math.pow(mypos.getY() - x.getY(), 2));

        return dist;
    }
    class distanceSort implements Comparator<CrossVertex> {
        @Override
        public int compare(CrossVertex o1, CrossVertex o2) {
            return o1.Distance.compareTo(o2.Distance);
        }
    }
}
