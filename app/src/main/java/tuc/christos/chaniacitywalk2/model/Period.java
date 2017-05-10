package tuc.christos.chaniacitywalk2.model;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christos on 19-Apr-17.
 *
 */

public class Period {
    private long id;
    private String name;
    private String description;
    private Uri uriLogo;
    private Uri uriMap;


    private Map<String,String> links = new HashMap<>();

    public Period (){}

    public Period(String name, String description){
        this.name = name;
        this.description = description;
    }
    public Period(long id, String name , String description){
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Period(String name, String description, String uriLogo, String uriMap){
        this.name = name;
        this.description = description;
        this.uriLogo = Uri.parse(uriLogo);
        this.uriMap = Uri.parse(uriMap);
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

    public void setUriLogo(Uri uriLogo) {
        this.uriLogo = uriLogo;
    }

    public Uri getUriMap() {
        return uriMap;
    }

    public void setUriMap(Uri uriMap) {
        this.uriMap = uriMap;
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

}
