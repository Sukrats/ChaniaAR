package tuc.christos.chaniacitywalk2.model;


import android.net.Uri;

import java.sql.Date;

public class Place {

    private long scene_id;
    private String scene_name;
    private Uri thumb;
    private String region;
    private String country;
    private String comment;
    private Date created;

    public Place() {
    }

    public long getScene_id() {
        return scene_id;
    }

    public void setScene_id(long scene_id) {
        this.scene_id = scene_id;
    }

    public String getScene_name() {
        return scene_name;
    }

    public void setScene_name(String scene_name) {
        this.scene_name = scene_name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Uri getThumb() {
        return thumb;
    }

    public void setThumb(Uri thumb) {
        this.thumb = thumb;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
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
}
