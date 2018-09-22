package com.example.iju95.ambient_browser;


import java.util.ArrayList;

/**
 * Created by iju95 on 2018-06-14.
 */

public class BuildingData {
    int Object_id;
    int Map_id;
    String Object_type;
    String Object_name;
    int Vertex_count;
    ArrayList<LWLatLng> Vertex_LatLng;

    public BuildingData(int object_id, int map_id, String object_type, String object_name, int vertex_count, ArrayList<LWLatLng> vertex_LatLng) {
        Object_id = object_id;
        Map_id = map_id;
        Object_type = object_type;
        Object_name = object_name;
        Vertex_count = vertex_count;
        Vertex_LatLng = vertex_LatLng;
    }
    public ArrayList<LWLatLng> getVertex_LatLng() {
        return Vertex_LatLng;
    }

}
