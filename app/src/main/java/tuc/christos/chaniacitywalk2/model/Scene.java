package tuc.christos.chaniacitywalk2.model;

import android.net.Uri;

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
    private double latitude;
    private double longitude;
    private boolean visited;
    private boolean saved;
    private boolean hasAR;

    private Map<String,String> links = new HashMap<>();

    private String briefDesc;
    private String description;
    private Uri uriImages;
    private Uri uriThumb;
    private String TAG;

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

    public Scene( double latitude, double longitude, int id,int periodid, String name, String description) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.period_id = periodid;
        this.description = description;
    }

    public Scene( double latitude, double longitude, long id, String name, boolean visited, boolean saved, boolean hasAR, String description, String TAG) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.visited = visited;
        this.saved = saved;
        this.briefDesc = description;
        this.hasAR = hasAR;
        this.TAG = TAG;
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

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
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

}
