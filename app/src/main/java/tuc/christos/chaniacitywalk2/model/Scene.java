package tuc.christos.chaniacitywalk2.model;

import android.net.Uri;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christos on 28-Jan-17.
 *
 *
 */

public class Scene {
    private long id;
    private long period_id;
    private String name;
    private String region;
    private String country;
    private String comment;
    private Date created;
    private double latitude;
    private double longitude;
    private int numOfVisits;
    private int numOfSaves;
    private boolean hasAR;

    private Map<String,String> links = new HashMap<>();

    private String briefDesc;
    private String description;
    private Uri uriImages = Uri.parse("");
    private Uri uriThumb = Uri.parse("");
    private String TAG;

    private int numOfSlamScenes = 0;
    private int numOfScenes = 0;


    private ArrayList<ArScene> geoScenes =new ArrayList<>();
    private ArrayList<ArScene> slamScenes =new ArrayList<>();
    private ArrayList<Viewport> viewports = new ArrayList<>();

    public boolean hasAR() {
        return hasAR;
    }

    public void setHasAR(boolean hasAR) {
        this.hasAR = hasAR;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTAG() {
        return TAG;
    }

    public void setTAG(String TAG) {
        this.TAG = TAG;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Scene(){

    }

    public Scene(long id, String name, Uri thumb){
        this.id = id;
        this.name = name;
        this.uriThumb = thumb;
    }
    public Scene( double latitude, double longitude, int id,int periodid, String name, String description) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.period_id = periodid;
        this.description = description;
    }

    public long getPeriod_id() {
        return period_id;
    }

    public void setPeriod_id(long period_id) {
        this.period_id = period_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public String getBriefDesc() {
        return briefDesc;
    }

    public void setBriefDesc(String briefDesc) {
        this.briefDesc = briefDesc;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public void addLink(String rel, String url){
        this.links.put(rel, url);
    }

    public Uri getUriImages() {
        return uriImages;
    }

    public void setUriImages(String uriImages) {
        this.uriImages = Uri.parse(uriImages);
    }

    public Uri getUriThumb() {
        return uriThumb;
    }

    public void setUriThumb(String uriThumb) {
        this.uriThumb = Uri.parse(uriThumb);
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public ArrayList<ArScene> getArScene() {
        return geoScenes;
    }

    public void setArScene(ArrayList<ArScene> scenes) {
        this.numOfScenes = scenes.size();
        this.geoScenes = scenes;
    }

    public void addArScene(ArScene scene){
        this.numOfScenes+=numOfScenes;
        this.geoScenes.add(scene);
    }

    public int getNumOfGeoScenes() {
        return numOfScenes;
    }

    public void setNumOfScenes(int numOfScenes) {
        this.numOfScenes = numOfScenes;
    }


    public ArrayList<ArScene> getSlamScene() {
        return slamScenes;
    }

    public void setSlamScene(ArrayList<ArScene> scenes) {
        this.numOfSlamScenes = scenes.size();
        this.slamScenes = scenes;
    }

    public void addSlamScene(ArScene scene){
        this.numOfSlamScenes+=numOfSlamScenes;
        this.slamScenes.add(scene);
    }

    public int getNumOfSlamScenes() {
        return numOfSlamScenes;
    }

    public void setNumOfSlamScenes(int numOfScenes) {
        this.numOfSlamScenes = numOfScenes;
    }


    public ArrayList<Viewport> getViewports() {
        return viewports;
    }

    public void setViewports(ArrayList<Viewport> viewports) {
        this.viewports = viewports;
    }

    public void addViewport(Viewport viewport){
        this.viewports.add(viewport);
    }

    public Viewport getViewport(String id){
        for(Viewport v : viewports){
            if(v.getId().equals(id))
                return v;
        }
        return null;
    }

    public int getNumOfVisits() {
        return numOfVisits;
    }

    public void setNumOfVisits(int numOfVisits) {
        this.numOfVisits = numOfVisits;
    }

    public int getNumOfSaves() {
        return numOfSaves;
    }

    public void setNumOfSaves(int numOfSaves) {
        this.numOfSaves = numOfSaves;
    }
}
