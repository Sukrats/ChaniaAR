package tuc.christos.chaniacitywalk2.utils;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;

import tuc.christos.chaniacitywalk2.model.ArScene;
import tuc.christos.chaniacitywalk2.model.Content;
import tuc.christos.chaniacitywalk2.model.Level;
import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.model.Viewport;

/**
 * Created by Christos on 22-May-17.
 */

public class JsonHelper {

    public static Player parsePlayerFromJson(JSONObject json) {
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

    public static ArrayList<Player> parsePlayersFromJson(String jsonObject) {
        ArrayList<Player> players = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(jsonObject);
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                players.add(parsePlayerFromJsonNoPass(json));
            }
            Log.i("JSON", "parsed: " + players.size() + " players");
        } catch (JSONException e) {
            Log.i("JSON", e.getMessage());
        }
        return players;
    }
    public static Player parsePlayerFromJsonNoPass(JSONObject json) {
        Player player = new Player();
        Log.i("JSON PARSING COMMENCE: ", json.toString());
        try {
            player.setEmail(json.getString("email"));
            player.setUsername(json.getString("username"));
            //player.setPassword(json.getString("password"));
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

    public static Level parseLevelFromJson(JSONObject json){
        Level level = new Level();

        try {
            level.setCountry(json.getString("country_name"));
            level.setCountry_code(json.getString("country_id"));
            level.setAdminArea(json.getString("admin_area_name"));
            level.setCity(json.getString("admin_area_name"));
            level.setAdminAreaID(json.getString("admin_area_id"));
            level.setBoundLongitude(json.getDouble("longitude"));
            level.setBoundLatitude(json.getDouble("latitude"));
            level.setBound(json.getDouble("bound"));
            Log.i("JSON", "parsed: " + level.getCountry() + "/n"+
                            level.getCountry_code()+
                            level.getAdminArea()+
                            level.getAdminAreaID()+
                            level.getCity()+
                            level.getBound());
        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }

        return level;
    }

    public static Content parseContentFromJson(JSONObject json){
        Content content = new Content();
        try {
            JSONObject jsonLevel = json.getJSONObject("level");
            JSONArray jsonScenes = json.getJSONArray("scenes");
            JSONArray jsonPeriods = json.getJSONArray("periods");
            content.setLevel(parseLevelFromJson(jsonLevel));
            content.setPeriods(jsonPeriods);
            content.setScenes(jsonScenes);
        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }

        return content;
    }

    public static Scene parseSceneFromJson(JSONObject json) {
        Scene scene = new Scene();
        try {
            scene.setId(json.getLong("id"));
            scene.setPeriod_id(json.getLong("periodId"));
            scene.setName(json.getString("name"));
            scene.setDescription(json.getString("description"));
            scene.setLatitude(json.getDouble("latitude"));
            scene.setLongitude(json.getDouble("longitude"));
            scene.setNumOfSaves(json.getInt("numOfPlaces"));
            scene.setNumOfVisits(json.getInt("numOfVisits"));

            JSONArray links = json.getJSONArray("links");
            for (int j = 0; j < links.length(); j++) {
                JSONObject obj = new JSONObject(links.get(j).toString());
                scene.addLink(obj.getString("rel"), obj.getString("url"));
            }
            if (scene.getLinks().containsKey("images"))
                scene.setUriImages(scene.getLinks().get("images"));
            else
                scene.setUriImages("");

            if (scene.getLinks().containsKey("thumbnail"))
                scene.setUriThumb(scene.getLinks().get("thumbnail"));
            else
                scene.setUriThumb("");

            Log.i("JSONHELPER", "Scene Parsed: " + scene.getUriThumb());
        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        return scene;
    }

    public static ArrayList<Scene> parseScenesFromJson(String jsonObject) {
        ArrayList<Scene> scenes = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(jsonObject);
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                scenes.add(parseSceneFromJson(json));
            }
            Log.i("JSON", "parsed: " + scenes.size() + " scenes");
        } catch (JSONException e) {
            Log.i("JSON", e.getMessage());
        }
        return scenes;
    }

    public static Period parsePeriodFromJson(JSONObject json) {
        Period period = new Period();
        try {
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
            period.setUriMap(period.getLinks().get("map"));
        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        return period;
    }

    public static ArrayList<Uri> parseImagesJsonArray(String JSONarray) {
        ArrayList<Uri> images = new ArrayList<>();
        try{
            JSONArray array = new JSONArray(JSONarray);
            for(int i = 0; i< array.length(); i++){
                JSONObject json = new JSONObject(array.get(i).toString());
                images.add(Uri.parse(json.getString("url")));
            }
        }catch (JSONException e){
            Log.i("JSON",e.getMessage());
        }
        return images;
    }


    public static JSONObject playerToJson(Player player) {
        JSONObject json = new JSONObject();
        try {
            json.put("username", player.getUsername());
            json.put("email", player.getEmail());
            json.put("password", player.getPassword());
            json.put("firstname", player.getFirstname());
            json.put("lastname", player.getLastname());
            json.put("score", player.getScore());
        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        Log.i("Player Parsed:", json.toString());
        return json;
    }

    public static JSONObject sceneToJson(Scene scene) {
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
        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        Log.i("JSON","thumb_uri: " + scene.getUriThumb());
        return json;
    }

    public static JSONObject arSceneToJson(Scene scene) {
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
            json.put("num", scene.getNumOfGeoScenes());
            JSONArray array = new JSONArray();
            for (ArScene s : scene.getArScene()) {
                JSONObject object = new JSONObject();
                object.put("path", s.getPath());
                object.put("latitude", s.getLatitude());
                object.put("longitude", s.getLongitude());
                array.put(object);
            }
            json.put("ar_scenes", array);

        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        Log.i("ARScene parsed:", json.toString());
        return json;
    }

    public static JSONObject slamSceneToJson(Scene scene) {
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
            json.put("num", scene.getNumOfSlamScenes());
            JSONArray array = new JSONArray();
            for (ArScene s : scene.getSlamScene()) {
                JSONObject object = new JSONObject();
                object.put("path", s.getPath());
                object.put("latitude", s.getLatitude());
                object.put("longitude", s.getLongitude());
                array.put(object);
            }
            json.put("ar_scenes", array);

        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        Log.i("ARScene parsed:", json.toString());
        return json;
    }

    private static JSONObject viewportToJSON(Viewport viewport) {
        JSONObject json = new JSONObject();
        try {
            json.put("latitude", viewport.getLatitude());
            json.put("longitude", viewport.getLongitude());
            json.put("rotation", viewport.getRotation());
            json.put("radius", viewport.getRadius());
            json.put("translateX", viewport.getTranslateX());
            json.put("translateY", viewport.getTranslateY());
        } catch (JSONException e) {
            Log.i("JSON", e.getMessage());
        }
        Log.i("JSON", json.toString());
        return json;
    }

    public static JSONObject sceneWithViewportToJSON(Scene scene, Viewport viewport) {
        JSONObject json = slamSceneToJson(scene);
        try {
            json.put("viewport", viewportToJSON(viewport));
        } catch (JSONException e) {
            Log.i("JSON", e.getMessage());
        }
        Log.i("JSON", json.toString());
        return json;
    }

/****************************************************************************************************************GET AR SCENES FROM LOCAL ASSETS **********************************************************/

    public static ArrayList<Scene> parseArScenesFromLocalJson(String jsonObject) {
        ArrayList<Scene> scenes = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(jsonObject);
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                scenes.add(arSceneFromJson(json));
            }
            Log.i("JSON", "parsed: " + scenes.size() + " scenes");
        } catch (JSONException e) {
            Log.i("JSON", e.getMessage());
        }
        return scenes;
    }

    private static Scene arSceneFromJson(JSONObject jsonObject) {
        Scene scene = new Scene();
        try {
            scene.setId(jsonObject.getLong("id"));
            scene.setPeriod_id(jsonObject.getLong("periodId"));
            scene.setName(jsonObject.getString("name"));
            scene.setDescription(jsonObject.getString("description"));
            scene.setLatitude(jsonObject.getDouble("latitude"));
            scene.setLongitude(jsonObject.getDouble("longitude"));

            JSONArray array = jsonObject.getJSONArray("arScenes");
            for (int i = 0; i< array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                scene.addArScene(new ArScene(object.getString("path"),object.getDouble("latitude"),object.getDouble("longitude")));
                scene.addSlamScene(new ArScene(object.getString("path"),object.getDouble("latitude"),object.getDouble("longitude")));
            }

            JSONArray array1 = jsonObject.getJSONArray("viewports");
            Viewport viewport = new Viewport();
            for (int y = 0; y< array1.length(); y++) {
                JSONObject object = array.getJSONObject(y);
                //viewport.setId(object.getString("viewport_id"));
                viewport.setId("1");
                viewport.setLatitude(object.getDouble("latitude"));
                viewport.setLongitude(object.getDouble("longitude"));
                viewport.setRotation(0);
                viewport.setTranslateX(0.0f);
                viewport.setTranslateY(0.0f);
                scene.addViewport(viewport);
            }

        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }

        return scene;
    }

}
