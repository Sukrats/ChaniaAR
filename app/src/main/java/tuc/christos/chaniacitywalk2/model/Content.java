package tuc.christos.chaniacitywalk2.model;


import org.json.JSONArray;

import java.util.ArrayList;

public class Content {
    private Level level;
    private JSONArray scenes;
    private JSONArray periods ;

    public Content() {
    }

    public Content(Level level, JSONArray scenes, JSONArray periods) {
        super();
        this.level = level;
        this.scenes = scenes;
        this.periods = periods;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public JSONArray getScenes() {
        return scenes;
    }

    public void setScenes(JSONArray scenes) {
        this.scenes = scenes;
    }

    public JSONArray getPeriods() {
        return periods;
    }

    public void setPeriods(JSONArray periods) {
        this.periods = periods;
    }

}