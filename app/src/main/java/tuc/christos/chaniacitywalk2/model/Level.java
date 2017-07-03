package tuc.christos.chaniacitywalk2.model;

import android.os.Bundle;

import java.util.Locale;

public class Level {

    private String city;
    private String country;
    private String country_code;
    private String adminArea;
    private String subAdminArea;
    private Double latitude;
    private Double longitude;

    public Level() {
    }

    public Level(String city, String country) {
        this.city = city;
        this.country = country;
    }

    public Level(String city, String country, String country_code) {
        this.city = city;
        this.country = country;
        this.country_code = country_code;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getAdminArea() {
        return adminArea;
    }

    public void setAdminArea(String adminArea) {
        this.adminArea = adminArea;
    }

    public String getSubAdminArea() {
        return subAdminArea;
    }

    public void setSubAdminArea(String subAdminArea) {
        this.subAdminArea = adminArea;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
