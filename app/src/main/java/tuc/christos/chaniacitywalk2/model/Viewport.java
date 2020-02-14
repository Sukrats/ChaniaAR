package tuc.christos.chaniacitywalk2.model;

import android.location.Location;

public class Viewport {

    private String id;
    private double latitude;
    private double longitude;
    private int radius;
    private int rotation;
    private float translateX;
    private float translateY;

    private Location location = new Location("");

    public Viewport() {
    }

    public Viewport(double latitude, double longitude, int rotation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.rotation = rotation;
        this.radius = 3;
        this.location.setLatitude(latitude);
        this.location.setLongitude(longitude);
    }

    public Viewport(double latitude, double longitude, int rotation, float translateX, float translateY) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.rotation = rotation;
        this.radius = 3;
        this.translateX = translateX;
        this.translateY = translateY;
        this.location.setLatitude(latitude);
        this.location.setLongitude(longitude);
    }

    public Viewport(String id, double latitude, double longitude, int rotation, float translateX, float translateY) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rotation = rotation;
        this.radius = 3;
        this.translateX = translateX;
        this.translateY = translateY;
        this.location.setLatitude(latitude);
        this.location.setLongitude(longitude);
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public float getDistanceFromLocation(Location location) {
        return this.location.distanceTo(location);
    }

    public float getTranslateX() {
        return translateX;
    }

    public void setTranslateX(float translateX) {
        this.translateX = translateX;
    }

    public float getTranslateY() {
        return translateY;
    }

    public void setTranslateY(float translateY) {
        this.translateY = translateY;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
