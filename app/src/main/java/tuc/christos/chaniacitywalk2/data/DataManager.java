package tuc.christos.chaniacitywalk2.data;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import tuc.christos.chaniacitywalk2.ContentListener;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.utils.Constants;

/**
 * Created by Christos on 24/1/2017.
 * Data Manager for local DB access and content providing
 */

public class DataManager {

    private static DataManager INSTANCE = null;
    private boolean instantiated = false;

    private ArrayList<Scene> Route = new ArrayList<>();
    private HashMap<String, Scene> routeMap = new HashMap<>();

    private ArrayList<Scene> Scenes = new ArrayList<>();
    private HashMap<String, Scene> scenesMap = new HashMap<>();


    private HashMap<Marker, Scene> markerSceneMap = new HashMap<>();
    private HashMap<Scene, Marker> sceneMarkerMap = new HashMap<>();

    private HashMap<Polyline, Scene> lineToSceneMap = new HashMap<>();
    private HashMap<Scene, Polyline> sceneToLineMap = new HashMap<>();

    private HashMap<Scene, ArrayList<LatLng>> sceneToPointsMap = new HashMap<>();
    private HashMap<ArrayList<LatLng>, Scene> pointsToSceneMap = new HashMap<>();

    private String TAG = "Data Manager";
    private mDBHelper mDBh;

    private DataManager() {
    }

    public static DataManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DataManager();
        return INSTANCE;
    }

    public void init(Context context) {
        if (!instantiated && INSTANCE != null) {
            mDBh = new mDBHelper(context);
            instantiated = true;
        }
    }

    /*
    public static DataManager getInstance() {
        if(INSTANCE == null)
            INSTANCE = new DataManager();
        return INSTANCE;
    }

    public boolean isInstantiated(){
        return (initiated);
    }

	public void init(Context context){
        this.initiated = true;
        mContext = context;
        initDBhelper(context);
        instantiate();
        mDBh.closeDataBase();
    }

	private void initDBhelper(Context context){
        this.mDBh = new mDBHelper(context);
        try {
            mDBh.createDataBase();
        }catch(IOException e){
            Log.i(TAG,"Unable to create database");
        }
        try{
            mDBh.openDataBase();
        }catch(SQLException e){
            Log.i(TAG,e.getMessage());
        }

    }

    private void instantiate(){

                Cursor c = mDBh.getScenes();
                while (c.moveToNext()) {

                    String TAG = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_TAG));
                    String name = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_NAME));
                    double lat = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LATITUDE));
                    double lon = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LONGITUDE));
                    int id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry._ID));
                    String descr = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_DESCRIPTION));

                    boolean tVisible = intToBool(c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_VISIBLE)));
                    boolean tHasAR = intToBool(c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_HASAR)));
                    boolean tVisit = intToBool(c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_VISITED)));


                    Scene temp = new Scene(lat, lon, id, name, tVisit, tVisible, tHasAR, descr, TAG);

                    this.Scenes.add(temp);
                    this.scenesMap.put(Integer.toString(id),temp);
                    if(temp.isHasAR()){
                        this.Route.add(temp);
                        this.routeMap.put(Integer.toString(id),temp);
                    }

                }
                setPolyPoints();
    }
*/
    public List<String> getEmails() {
        List<String> emails = new ArrayList<>();
        Cursor c = mDBh.getEmails();
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                emails.add(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_EMAIL)));
                emails.add(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_USERNAME)));
            }
        }
        String ems = "Emails: ";
        for (String temp : emails)
            ems += temp + "\n";
        Log.i(TAG, ems);
        return emails;
    }

    /*public Player getPlayer(){
        Player player = new Player();
        Cursor c = mDBh.getActivePlayer();
        while(c.moveToNext()){
            player.setEmail(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_EMAIL)));
            player.setPassword(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_PASSWORD)));
        }

        return player;
    }*/

    /*
    public void populateServerDB(){
        Cursor c = mDBh.getScenesTemp();

        JSONObject tempScene = new JSONObject();
        JSONArray scenes = new JSONArray();
        try {
            while(c.moveToNext()){
                JSONObject sc = new JSONObject();
                String name = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_NAME));
                String description =  c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_DESCRIPTION));
                double lat = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LATITUDE));
                double longi = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LONGITUDE));

                String period = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_TAG));
                long p_id = 1;
                switch(period){
                    case "Hellenistic" : p_id = 1; break;
                    case "Byzantine" :  p_id = 2; break;
                    case "Venetian" : p_id = 3; break;
                    case "Ottoman" : p_id = 4; break;
                    case "Modern" : p_id = 5; break;
                }
                if(name.equals("Municipal Garden's clock"))
                    name = "Municipal Garden\\'s clock";

                sc.put("name", name);
                sc.put("description", description);
                sc.put("latitude", lat);
                sc.put("longitude", longi);
                sc.put("periodId", p_id);
                scenes.put(sc);
            }
            tempScene.put("scenes",scenes);
            Log.i("REST FULL JSON: ", tempScene.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        InvokeRestfullScenes(tempScene);
    }

    private void InvokeRestfullScenes(JSONObject json){
        try {
            ByteArrayEntity entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"));
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(0,20);
            client.post(mContext, Constants.URL_TEMP_SCENES, entity, "application/json" , new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    if(i == 201){
                        Toast.makeText(mContext,"SUCCESS",Toast.LENGTH_SHORT).show();
                    }else if (i == 500){
                        Toast.makeText(mContext,"FUCK ME SOMETHING'S FISHY",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(mContext,"FUCK ME SOMETHING'S FISHY: "+i,Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    Toast.makeText(mContext,"FUCK ME SOMETHING'S FISHY: "+i,Toast.LENGTH_SHORT).show();
                }
            });

        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }

    */

    public String getAutoLoginCredentials() {
        String credentials;
        String email;
        String username;
        String password;

        Cursor c = mDBh.getPlayer();
        if (c.moveToNext()) {
            email = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_EMAIL));
            username = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_USERNAME));
            password = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_PASSWORD));
            credentials = email + ":" + password + ":" + username;
            return credentials;
        }
        return null;
    }

    public void insertUser(Player player) {
        mDBh.insertUser(player);
    }

    public boolean mapMarkerToScene(Marker marker, Scene scene) {
        markerSceneMap.put(marker, scene);
        return true;
    }

    public boolean mapScenetoMarker(Scene scene, Marker marker) {
        sceneMarkerMap.put(scene, marker);
        return true;
    }

    public Scene getSceneFromMarker(Marker marker) {
        return markerSceneMap.get(marker);
    }

    public Marker getMarkerFromScene(Scene scene) {
        return sceneMarkerMap.get(scene);
    }


    public boolean mapLineToScene(Polyline line, Scene scene) {
        lineToSceneMap.put(line, scene);
        return true;
    }

    public boolean mapScenetoLine(Scene scene, Polyline line) {
        sceneToLineMap.put(scene, line);
        return true;
    }

    public Scene getSceneFromLine(Polyline line) {
        return lineToSceneMap.get(line);
    }

    public Polyline getLineFromScene(Scene scene) {
        return sceneToLineMap.get(scene);
    }


    public boolean clearMaps() {
        sceneToLineMap.clear();
        lineToSceneMap.clear();
        sceneMarkerMap.clear();
        markerSceneMap.clear();
        return true;
    }

    public Scene getScene(String id) {
        return scenesMap.get(id);
    }

    public ArrayList<Scene> getScenes() {
        ArrayList<Scene> allScenes = new ArrayList<>();
        Cursor c = mDBh.getScenes();
        while(c.moveToNext()){
            String name = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_NAME));
            double lat = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LATITUDE));
            double lon = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LONGITUDE));
            int id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_ID));
            int period_id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_PERIOD_ID));
            String description = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_DESCRIPTION));

            Scene temp = new Scene(lat, lon, id, period_id, name, description);
            temp.setVisible(true);
            temp.setHasAR(false);
            temp.setVisited(false);
            allScenes.add(temp);
        }
        return allScenes;
    }

    public ArrayList<Scene> getScenesFromTag(String tag) {
        ArrayList<Scene> result = new ArrayList<>();
        for (Scene temp : Scenes) {
            if (temp.getTAG().equalsIgnoreCase(tag)) {
                result.add(temp);
            }
        }
        return result;
    }

    public ArrayList<Scene> getRoute() {
        return Route;
    }

    public ArrayList<LatLng> getPolyPoints(Scene scene) {
        return sceneToPointsMap.get(scene);
    }

    private void setPolyPoints() {
        ArrayList<LatLng> polyListRocco = new ArrayList<>();
        ArrayList<LatLng> polyListByz = new ArrayList<>();
        ArrayList<LatLng> polyListKast = new ArrayList<>();
        ArrayList<LatLng> polyListGlass = new ArrayList<>();

        polyListRocco.add(new LatLng(35.5164899, 24.021208));//stRocco
        polyListRocco.add(new LatLng(35.51711, 24.020557));//ByzWall

        polyListByz.add(new LatLng(35.51711, 24.020557));//ByzWall
        polyListByz.add(new LatLng(35.5171461, 24.019581));//kasteli

        polyListKast.add(new LatLng(35.5171461, 24.019581));//kasteli
        polyListKast.add(new LatLng(35.517398, 24.01779));//Glass Mosque

        polyListGlass.add(new LatLng(35.517398, 24.01779));//Glass Mosque

        for (Scene temp : Route) {
            if (temp.getId() == 1) {
                sceneToPointsMap.put(temp, polyListGlass);
                pointsToSceneMap.put(polyListGlass, temp);
            }
            if (temp.getId() == 2) {
                sceneToPointsMap.put(temp, polyListByz);
                pointsToSceneMap.put(polyListByz, temp);
            }
            if (temp.getId() == 3) {
                sceneToPointsMap.put(temp, polyListKast);
                pointsToSceneMap.put(polyListKast, temp);
            }
            if (temp.getId() == 4) {
                sceneToPointsMap.put(temp, polyListRocco);
                pointsToSceneMap.put(polyListRocco, temp);
            }
        }

    }


    private boolean intToBool(int i) {
        return (i != 0);
    }


    public String getPeriod(int position) {
        String page = "";
        switch (position) {
            case 0:
                page = Tags.HELLENISTIC.toString();
                break;
            case 1:
                page = Tags.BYZANTINE.toString();
                break;
            case 2:
                page = Tags.VENETIAN.toString();
                break;
            case 3:
                page = Tags.OTTOMAN.toString();
                break;
            case 4:
                page = Tags.MODERN.toString();
                break;
        }
        return page;
    }

    public int getPeriodCount() {
        return Tags.values().length;
    }

    private enum Tags {

        OTTOMAN,

        VENETIAN,

        MODERN,

        HELLENISTIC,

        BYZANTINE

    }

    public void downloadScenes(final ContentListener cl) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.URL_SCENES, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                    PopulateScenesTask myTask = new PopulateScenesTask(json,cl);
                    myTask.execute();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            }
        });
    }

    private class PopulateScenesTask extends AsyncTask<Void, Void, Boolean> {

        private final JSONArray scenes;
        private final ContentListener cl;

        PopulateScenesTask(JSONArray json,ContentListener cl) {
            this.scenes = json;
            this.cl=cl;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Scene scene = new Scene();
                for (int i = 0; i < scenes.length(); i++) {
                    JSONObject obj = new JSONObject(scenes.get(i).toString());
                    scene.setId(obj.getLong("id"));
                    scene.setPeriod_id(obj.getLong("periodId"));
                    scene.setName(obj.getString("name"));
                    scene.setDescription(obj.getString("description"));
                    scene.setLatitude(obj.getDouble("latitude"));
                    scene.setLongitude(obj.getDouble("longitude"));
                    if(!mDBh.insertScene(scene)){
                        Log.i(TAG,"DELETED SCENES");
                        mDBh.clearScenes();
                        return false;
                    }
                }
            } catch (JSONException e) {
                Log.i(TAG,e.getMessage());
                mDBh.clearScenes();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                cl.downloadComplete();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
