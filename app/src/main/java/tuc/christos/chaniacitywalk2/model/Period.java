package tuc.christos.chaniacitywalk2.model;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Christos on 19-Apr-17.
 */

public class Period {
    private long id;
    private String name;
    private String description;
    private String started;


    private String ended;
    private Uri uriLogo;
    private Uri uriMap;
    private Uri uriImages;

    private Map<String, String> links = new HashMap<>();
    private Map<Long, Scene> scenes = new HashMap<>();

    public Period() {
    }

    public Period(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Period(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Period(String name, String description, String uriLogo, String uriImages) {
        this.name = name;
        this.description = description;
        this.uriLogo = Uri.parse(uriLogo);
        this.uriImages = Uri.parse(uriImages);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public Uri getUriLogo() {
        return uriLogo;
    }

    public void setUriLogo(String uriLogo) {
        this.uriLogo = Uri.parse(uriLogo);
    }
    public Uri getUriMap() {
        return uriMap;
    }

    public void setUriMap(String uriMap) {
        this.uriMap = Uri.parse(uriMap);
    }

    public Uri getUriImages() {
        return uriImages;
    }

    public void setUriImages(String uriImages) {
        this.uriImages = Uri.parse(uriImages);
    }

    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }

    public String getEnded() {
        return ended;
    }

    public void setEnded(String ended) {
        this.ended = ended;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public void addLink(String rel, String url) {
        this.links.put(rel, url);
    }

    public Map<Long, Scene> getScenes() {
        return scenes;
    }
    public List<Scene> getScenesAsList(){
        return new ArrayList<>(scenes.values());
    }

    public void setScenes(Map<Long, Scene> scenes) {
        this.scenes = scenes;
    }
    public void setScenes(List<Scene> scene){
        for(Scene s : scene){
            this.scenes.put(s.getId(),s);
        }
    }


}
