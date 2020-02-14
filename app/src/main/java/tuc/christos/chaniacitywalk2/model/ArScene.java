package tuc.christos.chaniacitywalk2.model;

import java.util.ArrayList;


public class ArScene {
    private String path;
    private double latitude;
    private double longitude;


    public ArScene() {
    }

    public ArScene(String path){
        this.path = path;
    }

    public ArScene(String path, double latitude, double longitude) {
        this.path = path;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
