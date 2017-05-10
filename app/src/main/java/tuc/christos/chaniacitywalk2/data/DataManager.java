package tuc.christos.chaniacitywalk2.data;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
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
import tuc.christos.chaniacitywalk2.model.Period;
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

    private HashMap<String, Scene> scenes= new HashMap<>();
    private HashMap<String, Period> periods= new HashMap<>();

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

    /*
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
*/

    /*public Player getPlayer(){
        Player player = new Player();
        Cursor c = mDBh.getActivePlayer();
        while(c.moveToNext()){
            player.setEmail(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_EMAIL)));
            player.setPassword(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_PASSWORD)));
        }

        return player;
    }*/

    /**********************************************************PLAYER METHODS*****************************************************************/

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

    public void insertUser(Player player) {
        mDBh.insertUser(player);
    }

    /**********************************************************PERIOD METHODS*****************************************************************/

    public List<Period> getPeriods() {
        Cursor c = mDBh.getPeriods();
        List<Period> periodsGet = new ArrayList<>();
        int i = 0;
        Log.i(TAG,"Fetching Periods from local db");
        while(c.moveToNext()){
            i++;
            String name = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_NAME));
            long id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_ID));
            String description = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_DESCRIPTION));
            Period temp = new Period(id, name, description);
            periodsGet.add(temp);
            //periods.put(name,temp);
        }
        Log.i(TAG,"Fetched: "+i+" Periods");
        return periodsGet;
    }

    public Period getPeriod(String name){
        Cursor c = mDBh.getPeriod(name);
        Period p = new Period();
        if(c.moveToNext()){
            p.setId(c.getLong(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_ID)));
            p.setName(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_NAME)));
            p.setDescription(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_DESCRIPTION)));
        }
        return p;
    }
    public Period getPeriod(int position){
        Period period;
        Log.i(TAG,"Fragment position: "+position);
        switch(position){
            case 0:
                period =  periods.get("HELLENISTIC");
                Log.i(TAG,"Period: "+period.getName());
                break;
            case 1:
                period = periods.get("BYZANTINE");
                Log.i(TAG,"Period: "+period.getName());
                break;
            case 2:
                period =  periods.get("VENETIAN");
                Log.i(TAG,"Period: "+period.getName());
                break;
            case 3:
                period =  periods.get("OTTOMAN");
                Log.i(TAG,"Period: "+period.getName());
                break;
            case 4:
                period =  periods.get("MODERN");
                Log.i(TAG,"Period: "+period.getName());
                break;
            default:
                period =  new Period();
        }
        return period;
    }

    public int getPeriodCount(){
        return periods.values().size();
    }

    public List<Scene> getScenesFromPeriod(String page){
        List<Scene> pScenes = new ArrayList<>();
        Period period = periods.get(page);
        if(period == null)
            return pScenes;
        for(Scene temp : scenes.values()){
           if(temp.getPeriod_id() == period.getId())
               pScenes.add(temp);
        }
        return pScenes;
    }

    /**********************************************************SCENE METHODS*****************************************************************/


    public Scene getScene(String id) {
        return scenes.get(id);
    }

    public List<Scene> getScenes() {
        Cursor c = mDBh.getScenes();
        int i = 0;
        Log.i(TAG,"Fetching Scenes from local db");
        while(c.moveToNext()){
            i++;
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
            scenes.put(String.valueOf(id),temp);
        }
        Log.i(TAG,"Fetched: "+i+" Scenes");
        return new ArrayList<>(scenes.values());
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

    /**********************************************************DOWNLOAD*************************************************************************/

    public void downloadPeriods(final ContentListener cl) {
        Log.i(TAG,"Downloading Periods");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.URL_PERIODS, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                    PopulateDBTask myTask = new PopulateDBTask(json,cl,mDBHelper.PeriodEntry.TABLE_NAME);
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

    public void downloadScenes(final ContentListener cl) {
        Log.i(TAG,"Downloading Scenes");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.URL_SCENES, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                    PopulateDBTask myTask = new PopulateDBTask(json,cl, mDBHelper.SceneEntry.TABLE_NAME);
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

    private class PopulateDBTask extends AsyncTask<Void, Double, Boolean> {

        private final JSONArray jsonArray;
        private final ContentListener cl;
        private final String tableName;

        PopulateDBTask(JSONArray json,ContentListener cl, String tableName) {
            this.jsonArray = json;
            this.cl=cl;
            this.tableName = tableName;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                switch (tableName) {

                    case  mDBHelper.SceneEntry.TABLE_NAME:

                        for (int i = 0; i < jsonArray.length(); i++) {
                            Scene scene = new Scene();
                            publishProgress((double) (i + 1) / jsonArray.length() * 100);
                            JSONObject obj = new JSONObject(jsonArray.get(i).toString());
                            scene.setId(obj.getLong("id"));
                            scene.setPeriod_id(obj.getLong("periodId"));
                            scene.setName(obj.getString("name"));
                            scene.setDescription(obj.getString("description"));
                            scene.setLatitude(obj.getDouble("latitude"));
                            scene.setLongitude(obj.getDouble("longitude"));
                            if (!mDBh.insertScene(scene)) {
                                Log.i(TAG, "DELETED SCENES");
                                mDBh.clearScenes();
                                return false;
                            }
                        }
                        break;

                    case mDBHelper.PeriodEntry.TABLE_NAME:

                        for(int i= 0; i< jsonArray.length();i++){
                            Period period = new Period();
                            publishProgress((double) (i + 1) / jsonArray.length() * 100);
                            JSONObject obj = new JSONObject(jsonArray.get(i).toString());

                            period.setId(obj.getLong("id"));
                            period.setName(obj.getString("name"));
                            period.setDescription(obj.getString("description"));

                            JSONArray links = obj.getJSONArray("links");
                           for(int j = 0; j< links.length();j++){
                               JSONObject json = new JSONObject(links.get(j).toString());
                               period.addLink(json.getString("rel"),json.getString("url"));
                           }
                            if (!mDBh.insertPeriod(period)) {
                                Log.i(TAG, "DELETED PERIODS");
                                mDBh.clearPeriods();
                                return false;
                            }
                            periods.put(period.getName(),period);
                        }
                        break;
                }
            } catch (JSONException e) {
                Log.i(TAG,e.getMessage());
                mDBh.clearScenes();
                return false;
            }
            return true;
        }

        @Override
        protected  void onProgressUpdate(Double... progress){
            Log.i(TAG,"Downloading :"+progress[0]);
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if(success){
                Log.i(TAG,"Download Complete :");
                cl.downloadComplete();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }


}
