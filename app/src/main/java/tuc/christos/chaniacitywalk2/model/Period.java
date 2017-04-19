package tuc.christos.chaniacitywalk2.model;

import android.net.Uri;

/**
 * Created by Christos on 19-Apr-17.
 *
 */

public class Period {
    private String name;
    private String description;
    private Uri uriLogo;
    private Uri uriMap;

    public Period (){}

    public Period(String name, String description){
        this.name = name;
        this.description = description;
    }

    public Period(String name, String description, String uriLogo, String uriMap){
        this.name = name;
        this.description = description;
        this.uriLogo = Uri.parse(uriLogo);
        this.uriMap = Uri.parse(uriMap);
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
}
