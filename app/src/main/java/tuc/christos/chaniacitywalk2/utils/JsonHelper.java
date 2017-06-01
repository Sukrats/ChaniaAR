package tuc.christos.chaniacitywalk2.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Timestamp;

import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;

/**
 * Created by Christos on 22-May-17.
 *
 *
 */

public class JsonHelper {

    public static Player parsePlayerFromJson(JSONObject json){
        Player player = new Player();
        Log.i("JSON PARSING COMMENCE: ", json.toString());
        try {
            player.setEmail(json.getString("email"));
            player.setUsername(json.getString("username"));
            player.setPassword(json.getString("password"));
            player.setFirstname(json.getString("firstname"));
            player.setLastname(json.getString("lastname"));
            player.setScore(json.getLong("score"));
            Log.i("json", "Created: " + json.getString("created"));
            player.setCreated(Date.valueOf(json.getString("created")));
            Log.i("json", "parsed Created: " + player.getCreated());

            Log.i("json", "Activity: " + json.getString("recentActivity"));
            player.setRecentActivity(Timestamp.valueOf(json.getString("recentActivity")));
            Log.i("json", "parsed Activity: " + player.getRecentActivity().toString());

            JSONArray links = json.getJSONArray("links");
            for (int i = 0; i < links.length(); i++) {
                JSONObject obj = new JSONObject(links.get(i).toString());
                player.addLink(obj.getString("rel"), obj.getString("url"));
            }
        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        Log.i("JSONParsed", "User info:\n Email:" + player.getEmail() + "\n" +
                "Username:" + player.getUsername() + "\n" +
                "Password:" + player.getPassword() + "\n" +
                "Firstname:" + player.getFirstname() + "\n" +
                "Lastname:" + player.getLastname() + "\n" +
                "Created:" + player.getCreated() + "\n");
        for (String str : player.getLinks().keySet()) {
            Log.i("Parsed Links", "key: " + str + "\tvalue: " + player.getLinks().get(str));
        }
        return player;
    }

    public static Scene parseSceneFromJson(JSONObject json){
        Scene scene = new Scene();
        try {
            scene.setId(json.getLong("id"));
            scene.setPeriod_id(json.getLong("periodId"));
            scene.setName(json.getString("name"));
            scene.setDescription(json.getString("description"));
            scene.setLatitude(json.getDouble("latitude"));
            scene.setLongitude(json.getDouble("longitude"));

            JSONArray links = json.getJSONArray("links");
            for (int j = 0; j < links.length(); j++) {
                JSONObject obj = new JSONObject(links.get(j).toString());
                scene.addLink(obj.getString("rel"), obj.getString("url"));
            }
            scene.setUriImages(scene.getLinks().get("images"));
            scene.setUriThumb(scene.getLinks().get("thumbnail"));
        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        return scene;
    }

    public static Period parsePeriodFromJson(JSONObject json) {
        Period period = new Period();
        try{
        period.setId(json.getLong("id"));
        period.setName(json.getString("name"));
        period.setDescription(json.getString("description"));
        period.setStarted(json.getString("started"));
        period.setEnded(json.getString("ended"));

        JSONArray links = json.getJSONArray("links");
        for (int j = 0; j < links.length(); j++) {
            JSONObject obj = new JSONObject(links.get(j).toString());
            period.addLink(obj.getString("rel"), obj.getString("url"));
        }
        period.setUriImages(period.getLinks().get("images"));
        period.setUriLogo(period.getLinks().get("logo"));
        }catch(JSONException e){
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        return period;
    }

    public static JSONObject playerToJson(Player player){
        JSONObject json = new JSONObject();
        try {
            json.put("username", player.getUsername());
            json.put("email", player.getEmail());
            json.put("password", player.getPassword());
            json.put("firstname", player.getFirstname());
            json.put("lastname", player.getLastname());
            json.put("score", player.getScore());
        }catch(JSONException e){
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        Log.i("Player Parsed:", json.toString());
        return json;
    }

    public static JSONObject sceneToJson(Scene scene){
        JSONObject json = new JSONObject();
        try {
            json.put("id", scene.getId());
            json.put("name", scene.getName());
            json.put("description", scene.getDescription());
            json.put("latitude", scene.getLatitude());
            json.put("longitude", scene.getLongitude());
            json.put("period_id", scene.getPeriod_id());
            json.put("thumb_uri", scene.getUriThumb());
            json.put("images_uri", scene.getUriImages());
        }catch(JSONException e){
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        Log.i("Scene parsed:", json.toString());
        return json;
    }

    public static JSONObject periodToJson(Period period){
        JSONObject json = new JSONObject();
        return json;
    }


}
