package tuc.christos.chaniacitywalk2.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tuc.christos.chaniacitywalk2.mInterfaces.ContentListener;
import tuc.christos.chaniacitywalk2.mInterfaces.LocalDBWriteListener;
import tuc.christos.chaniacitywalk2.model.ArScene;
import tuc.christos.chaniacitywalk2.model.Level;
import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;

/**
 * Created by Christos on 24/1/2017.
 * Data Manager for local DB access and content providing
 */

public class DataManager {

    private static DataManager INSTANCE = null;
    private boolean instantiated = false;
    private Player activePlayer;

    private boolean scenesLoaded = false;
    private Level currentLevel;

    private HashMap<String, ArScene> Route = new HashMap<>();
    private HashMap<String, Scene> ScenesMap = new HashMap<>();

    //private HashMap<Polyline, Scene> lineToSceneMap = new HashMap<>();
    //private HashMap<Scene, Polyline> sceneToLineMap = new HashMap<>();

    //private HashMap<Scene, ArrayList<LatLng>> sceneToPointsMap = new HashMap<>();
    //private HashMap<ArrayList<LatLng>, Scene> pointsToSceneMap = new HashMap<>();

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
            initRoute();
        }
    }

    public boolean isInstantiated() {
        return instantiated;
    }

    public boolean isContentReady(){
        return !mDBh.isScenesEmpty();
    }

    /**********************************************************MODIFICATIONS******************************************************************/

    public Timestamp getLastUpdate(String table_name) {
        Cursor c = mDBh.getModification(table_name);
        String update = "";
        if (c.moveToNext()) {
            update = c.getString(c.getColumnIndexOrThrow(mDBHelper.ModificationsEntry.COLUMN_LAST_MODIFIED));
        }
        Log.i(TAG, Timestamp.valueOf(update).toString());
        return Timestamp.valueOf(update);
    }

    public void printModsTable() {
        mDBh.printModsTable();
    }

    /**********************************************************DB SYNCING********************************************************************/

    public boolean isInitialised() {
        return !isScenesEmpty() && !isPeriodsEmpty();
    }

  /*  public void syncLocalToRemote() {
        RestClient rs = RestClient.getInstance();
        rs.getPlayerData(new ClientListener() {
            @Override
            public void onCompleted(boolean success, int httpCode, String msg) {
                Log.i(TAG, msg);
            }

            @Override
            public void onUpdate(int progress, String msg) {
                Log.i(TAG, msg);
            }
        });
        Log.i("DB_SYNC", "UPDATED LOCAL DATABASE");
    }

    public void syncRemoteToLocal() {

        Log.i("DB_SYNC", "UPDATED REMOTE DATABASE");
    }
  */

    public ArrayList<Scene> getActiveMapContent() {
        Log.i(TAG,""+scenesLoaded);
        if( getActivePlayer().getScore() < 1000 && !getActivePlayer().getUsername().equals("Guest"))
            return new ArrayList<Scene>(getRoute().values());
        else
            return new ArrayList<>(getScenes());
    }

  /*  /*********************************************************CONTENT METHODS**************************************************************/
   /* public void getContent(Player player) {
        ArrayList<Scene> scenes = new ArrayList<>(getScenes());
        for (Scene temp : scenes) {

        }
    }*/

    /**********************************************************PLAYER METHODS*****************************************************************/

    //Login and autocomplete methods
    public String getAutoLoginCredentials() {
        String credentials;
        String email;
        String username;
        String password;

        Cursor c = mDBh.getActivePlayer();
        if (c.moveToNext()) {
            email = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_EMAIL));
            username = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_USERNAME));
            password = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_PASSWORD));
            credentials = email + ":" + password + ":" + username;
            return credentials;
        }
        return null;
    }

    public void clearActivePlayer() {
        activePlayer = null;
        mDBh.clearActivePlayer();
    }

    public Player getActivePlayer() {
        if (this.activePlayer != null)
            return this.activePlayer;

        return this.getPlayer();

    }

    public void setActivePlayer(Player player) {
        activePlayer = player;
        mDBh.setActivePlayer(player.getUsername());
    }

    public List<String> getEmailsForAutoComplete() {
        List<String> emails = new ArrayList<>();
        Cursor c = mDBh.getPlayers();
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

    public Timestamp getPlayerLastActivity(String uname) {
        Cursor c = mDBh.getPlayer(uname);
        Timestamp time = new Timestamp(0);
        if (c.moveToNext()) {
            time = Timestamp.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_RECENT_ACTIVITY)));
        }
        return time;
    }


    // Player Methods

    public boolean playerExists(String username) {
        return mDBh.getPlayer(username).moveToNext();
    }

   /* public boolean isPlayersEmpty() {
        return mDBh.isPlayersEmpty();
    }

    public void updatePlayer(Player player, Context context) {
        mDBh.updatePlayer(player);
        RestClient rs = RestClient.getInstance();
        rs.putPlayer(player, context);
    }*/

    public void insertPlayer(Player player) {
        activePlayer = player;
        mDBh.insertPlayer(player);
    }

    public Player getPlayer() {
        Player player = new Player();
        Cursor c = mDBh.getActivePlayer();

        while (c.moveToNext()) {
            player.setEmail(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_EMAIL)));
            player.setUsername(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_USERNAME)));
            player.setPassword(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_PASSWORD)));
            player.setFirstname(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_FIRST_NAME)));
            player.setLastname(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_LAST_NAME)));
            player.setRegion(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_REGION)));
            player.setScore(c.getLong(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_SCORE)));
            try {
                player.setCreated(Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_CREATED))));
            }catch(IllegalArgumentException e){
                player.setCreated(new java.util.Date(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_CREATED))));
            }
            try {
                player.setRecentActivity(Timestamp.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_RECENT_ACTIVITY))));
            }catch(IllegalArgumentException e){
                player.setCreated(new java.util.Date(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_RECENT_ACTIVITY))));
            }
        }
        activePlayer = player;
        return player;
    }

    public void printPlayers() {
        Cursor c = mDBh.getPlayers();
        Player player = new Player();
        while (c.moveToNext()) {
            player.setEmail(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_EMAIL)));
            player.setUsername(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_USERNAME)));
            player.setPassword(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_PASSWORD)));
            player.setFirstname(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_FIRST_NAME)));
            player.setLastname(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_LAST_NAME)));
            player.setRegion(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_REGION)));
            int active = c.getInt(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_ACTIVE));
            player.setScore(c.getLong(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_SCORE)));

            try {
                player.setCreated(Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_CREATED))));
            }catch(IllegalArgumentException e){
                player.setCreated(new java.util.Date(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_CREATED))));
            }
            try {
                player.setRecentActivity(Timestamp.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_RECENT_ACTIVITY))));
            }catch(IllegalArgumentException e){
                player.setCreated(new java.util.Date(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_RECENT_ACTIVITY))));
            }
            Log.i("Players", player.getEmail() + player.getUsername() + player.getRecentActivity() + " ACTIVE?: " + String.valueOf(active));
        }
    }

    /**********************************************************PERIOD METHODS*****************************************************************/

    private boolean isPeriodsEmpty() {
        return mDBh.isPeriodsEmpty();
    }

    boolean populatePeriods(JSONArray jsonArray, LocalDBWriteListener l) {
        PopulateDBTask myTask = new PopulateDBTask(jsonArray, mDBHelper.PeriodEntry.TABLE_NAME, l);
        myTask.execute();
        return true;
    }

    public List<Period> getPeriods() {
        Cursor c = mDBh.getPeriods();
        List<Period> periodsGet = new ArrayList<>();
        int i = 0;
        Log.i(TAG, "Fetching Periods from local db");
        while (c.moveToNext()) {
            i++;
            String name = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_NAME));
            long id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_ID));
            String description = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_DESCRIPTION));
            String images = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_IMAGES_URL));
            String logo = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_LOGO_URL));
            String started = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_STARTED));
            String ended = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_ENDED));

            Period temp = new Period(id, name, description);
            temp.setUriLogo(logo);
            temp.setUriImages(images);
            temp.setStarted(started);
            temp.setEnded(ended);
            temp.setScenes(getPeriodScenes(temp.getId()));

            periodsGet.add(temp);
        }
        Log.i(TAG, "Fetched: " + i + " Periods");
        return periodsGet;
    }

    public Period getPeriod(String period_id) {
        Cursor c = mDBh.getPeriod(period_id);
        Period p = new Period();
        if (c.moveToNext()) {
            p.setId(c.getLong(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_ID)));
            p.setName(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_NAME)));
            p.setDescription(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_DESCRIPTION)));
            p.setUriImages(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_IMAGES_URL)));
            p.setUriLogo(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_LOGO_URL)));
            p.setStarted(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_STARTED)));
            p.setEnded(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_ENDED)));

            p.setScenes(getPeriodScenes(p.getId()));
        }
        return p;
    }

    public List<Scene> getPeriodScenes(long periodid) {
        List<Scene> scenes = new ArrayList<>();
        if (!scenesLoaded) {
            Cursor c = mDBh.getPeriodScenes(periodid);
            while (c.moveToNext()) {
                String name = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_NAME));
                double lat = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LATITUDE));
                double lon = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LONGITUDE));
                int id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_ID));
                int period_id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_PERIOD_ID));
                String description = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_DESCRIPTION));
                String images = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_IMAGES_URL));
                String thumbnail = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_THUMBNAIL_URL));

                Scene temp = new Scene(lat, lon, id, period_id, name, description);
                if(!Route.containsKey(String.valueOf(temp.getId())))
                    temp.setHasAR(false);
                else temp.setHasAR(true);

                temp.setSaved(hasSaved(temp.getId()));
                temp.setVisited(hasVisited(temp.getId()));
                temp.setUriImages(images);
                temp.setUriThumb(thumbnail);
                scenes.add(temp);
            }
        } else {
            for (Scene temp : ScenesMap.values()) {
                if (temp.getPeriod_id() == periodid)
                    scenes.add(temp);
            }

        }
        return scenes;
    }

    /**********************************************************SCENE METHODS*****************************************************************/

    private boolean isScenesEmpty() {
        return mDBh.isScenesEmpty();
    }

    void populateScenes(JSONArray jsonArray, LocalDBWriteListener l) {
        PopulateDBTask myTask = new PopulateDBTask(jsonArray, mDBHelper.SceneEntry.TABLE_NAME, l);
        myTask.execute();
    }

    public Scene getScene(String id) {
        if (!scenesLoaded) {
            Cursor c = mDBh.getScene(Long.valueOf(id));
            Scene s = new Scene();
            if (c.moveToNext()) {
                s.setId(c.getLong(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_ID)));
                s.setName(c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_NAME)));
                s.setDescription(c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_DESCRIPTION)));
                s.setLatitude(c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LATITUDE)));
                s.setLongitude(c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LONGITUDE)));
                s.setPeriod_id(c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_PERIOD_ID)));
                s.setUriImages(c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_IMAGES_URL)));
                s.setUriThumb(c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_THUMBNAIL_URL)));
                s.setSaved(hasSaved(s.getId()));
                s.setVisited(hasVisited(s.getId()));
                if(!Route.containsKey(String.valueOf(s.getId())))
                    s.setHasAR(false);
                else s.setHasAR(true);
            }
            return s;
        } else
            return ScenesMap.get(id);
    }


    public List<Scene> getScenes() {
        if (!scenesLoaded) {
            Cursor c = mDBh.getScenes();
            List<Scene> scenes = new ArrayList<>();
            int i = 0;
            Log.i(TAG, "Fetching Scenes from local db");
            if(!isScenesEmpty()) {
                while (c.moveToNext()) {
                    i++;
                    String name = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_NAME));
                    double lat = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LATITUDE));
                    double lon = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LONGITUDE));
                    int id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_ID));
                    int period_id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_PERIOD_ID));
                    String description = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_DESCRIPTION));
                    String images = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_IMAGES_URL));
                    String thumbnail = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_THUMBNAIL_URL));

                    Scene temp = new Scene(lat, lon, id, period_id, name, description);
                    temp.setSaved(hasSaved(temp.getId()));
                    temp.setVisited(hasVisited(temp.getId()));
                    temp.setUriImages(images);
                    temp.setUriThumb(thumbnail);
                    if (!Route.containsKey(String.valueOf(temp.getId())))
                        temp.setHasAR(false);
                    else {
                        temp.setHasAR(true);
                        Route.get(String.valueOf(temp.getId())).setUriImages(temp.getUriImages().toString());
                        Route.get(String.valueOf(temp.getId())).setUriThumb(temp.getUriThumb().toString());
                    }
                    scenes.add(temp);
                    ScenesMap.put(String.valueOf(temp.getId()), temp);
                }
                scenesLoaded = true;
            }
            Log.i(TAG, "Fetched: " + i + " Scenes");
            return scenes;
        } else
            return new ArrayList<>(ScenesMap.values());
    }

    /*****************************************************PLACES METHODS************************************************************************/

    public void savePlace(long id) {
        Player p = getPlayer();
        mDBh.insertPlace(id, p.getUsername());
        if (scenesLoaded)
            ScenesMap.get(String.valueOf(id)).setSaved(true);
    }

    public void clearPlace(long id) {
        Player p = getPlayer();
        mDBh.deletePlace(id, p.getUsername());
        if (scenesLoaded)
            ScenesMap.get(String.valueOf(id)).setSaved(false);
    }

    public boolean hasSaved(long scene_id) {
        if (!scenesLoaded) {
            Player p = getPlayer();
            Cursor c = mDBh.getPlace(scene_id, p.getUsername());
            return c.moveToNext();
        } else
            return ScenesMap.get(String.valueOf(scene_id)).isSaved();
    }

 /*   public void printPlaces() {
        Player p = getPlayer();
        Cursor c = mDBh.getPlaces(p.getUsername());
        String username;
        int scene_id;
        Timestamp created;
        Log.i("Place", "Attempt Print: " + c.getCount());
        while (c.moveToNext()) {
            username = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_USERNAME));
            scene_id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_SCENE_ID));
            created = Timestamp.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_CREATED)));
            Log.i("Place", "Username: " + username + "\tScene: " + scene_id + "\tCreated: " + created.toString());
        }
    }*/

    public ArrayList<Scene> getPlaces(String username) {
        ArrayList<Scene> scenes = new ArrayList<>();
        if (!scenesLoaded) {
            Cursor c = mDBh.getPlaces(username);
            while (c.moveToNext()) {
                long scene_id = c.getLong(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_SCENE_ID));
                scenes.add(getScene(String.valueOf(scene_id)));
            }
        } else {
            for (Scene temp : ScenesMap.values()) {
                if (temp.isSaved()) scenes.add(temp);
            }
        }
        return scenes;
    }

    /*****************************************************VISITS METHODS************************************************************************/

    public void addVisit(long id) {
        Player p = getPlayer();
        mDBh.insertVisit(id, p.getUsername());
        if (scenesLoaded)
            ScenesMap.get(String.valueOf(id)).setVisited(true);
    }

    private boolean hasVisited(long scene_id) {
        Player p = getPlayer();
        if (!scenesLoaded) {
            Cursor c = mDBh.getVisit(scene_id, p.getUsername());
            return c.moveToNext();
        } else return ScenesMap.get(String.valueOf(scene_id)).isVisited();
    }
/*
    public void printVisits() {
        Player p = getPlayer();
        Cursor c = mDBh.getVisits(p.getUsername());
        String username;
        int scene_id;
        Timestamp created;
        Log.i("Visit", "Attempt Print: " + c.getCount());
        while (c.moveToNext()) {
            username = c.getString(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_USERNAME));
            scene_id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_SCENE_ID));
            created = Timestamp.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_CREATED)));
            Log.i("Visit", "Username: " + username + "\tScene: " + scene_id + "\tCreated: " + created.toString());
        }
    }*/

    public ArrayList<Scene> getVisits(String username) {
        ArrayList<Scene> scenes = new ArrayList<>();

        if (!scenesLoaded) {
            Cursor c = mDBh.getVisits(username);
            while (c.moveToNext()) {
                long scene_id = c.getLong(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_SCENE_ID));
                scenes.add(getScene(String.valueOf(scene_id)));
            }
        } else {
            for (Scene temp : ScenesMap.values()) {
                if (temp.isVisited()) scenes.add(temp);
            }

        }
        return scenes;
    }

   /* /*****************************************************POLYLINES************************************************************************/
/*
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

        for (Scene temp : Route.values()) {
            if (temp.getId() == 1) {
                sceneToPointsMap.put(temp, polyListGlass);
                //pointsToSceneMap.put(polyListGlass, temp);
            }
            if (temp.getId() == 2) {
                sceneToPointsMap.put(temp, polyListByz);
                //pointsToSceneMap.put(polyListByz, temp);
            }
            if (temp.getId() == 3) {
                sceneToPointsMap.put(temp, polyListKast);
                //pointsToSceneMap.put(polyListKast, temp);
            }
            if (temp.getId() == 4) {
                sceneToPointsMap.put(temp, polyListRocco);
                //pointsToSceneMap.put(polyListRocco, temp);
            }
        }

    }*/

    /**********************************************************ROUTE HARD CODED *************************************************************/
    private void initRoute() {
        ArrayList<ArScene> mRoute = new ArrayList<>();
        mRoute.add(new ArScene(35.517398, 24.01779, 36, 4, "Glass Mosque", "", "assets/earth.wt3"));
        mRoute.add(new ArScene(35.51711, 24.020557, 37, 2, "The Byzantine Wall", "", "assets/earth.wt3"));
        mRoute.add(new ArScene(35.5171461, 24.019581, 38, 1, "Minoiki Kidonia", "", "assets/earth.wt3"));
        mRoute.add(new ArScene(35.5164899, 24.021208, 39, 3, "Church of St. Rocco", "", "assets/earth.wt3"));
        for (ArScene temp : mRoute) {
            temp.setHasAR(true);
            Route.put(String.valueOf(temp.getId()), temp);
        }
    }

    public void setLevelLocality(Level level){
        if(activePlayer != null)
            this.activePlayer.setRegion(level.getAdminArea());
        else
            getPlayer().setRegion(level.getAdminArea());

        this.currentLevel = level;

    }
    public Level getCurrentLevel(){
        if(currentLevel!=null)
            return this.currentLevel;
        return new Level();
    }

    /*public ArrayList<ArScene> getRouteAsList() {
        return new ArrayList<>(Route.values());
    }*/

    private HashMap<String, ArScene> getRoute() {
        return Route;
    }

    /**********************************************************POPULATE DB TASK*************************************************************************/

    public void clearScenes(){
        mDBh.clearScenes();
        scenesLoaded =false;
    }
    public void clearPeriods(){
        mDBh.clearPeriods();
    }

    void populateUserData(JSONArray jsonArray, String tableName, LocalDBWriteListener l) {
        PopulateDBTask myTask = new PopulateDBTask(jsonArray, tableName, l);
        myTask.execute();
    }

    private class PopulateDBTask extends AsyncTask<Void, Double, Boolean> {

        private final JSONArray jsonArray;
        private final String tableName;
        private final LocalDBWriteListener listener;

        PopulateDBTask(JSONArray json, String tableName,LocalDBWriteListener l) {
            this.jsonArray = json;
            this.tableName = tableName;
            this.listener = l;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                switch (tableName) {

                    case mDBHelper.SceneEntry.TABLE_NAME:

                        for (int i = 0; i < jsonArray.length(); i++) {
                            publishProgress((double) (i + 1) / jsonArray.length() * 100);
                            JSONObject obj = new JSONObject(jsonArray.get(i).toString());
                            Scene scene = JsonHelper.parseSceneFromJson(obj);
                            if (!mDBh.insertScene(scene)) {
                                Log.i(TAG, "DELETED SCENES");
                                mDBh.clearScenes();
                                return false;
                            }
                        }
                        break;

                    case mDBHelper.PeriodEntry.TABLE_NAME:

                        for (int i = 0; i < jsonArray.length(); i++) {
                            publishProgress((double) (i + 1) / jsonArray.length() * 100);

                            JSONObject obj = new JSONObject(jsonArray.get(i).toString());
                            Period period = JsonHelper.parsePeriodFromJson(obj);
                            if (!mDBh.insertPeriod(period)) {
                                Log.i(TAG, "DELETED PERIODS");
                                mDBh.clearPeriods();
                                return false;
                            }
                        }
                        break;
                    case mDBHelper.VisitsEntry.TABLE_NAME:

                        for (int i = 0; i < jsonArray.length(); i++) {
                            publishProgress((double) (i + 1) / jsonArray.length() * 100);

                            JSONObject obj = new JSONObject(jsonArray.get(i).toString());

                            if (!mDBh.insertVisit(obj.getLong("scene_id"), obj.getString("username"))) {
                                Log.i(TAG, "ERROR ON INSERT PLACE");
                                return false;
                            }
                        }
                        break;
                    case mDBHelper.PlacesEntry.TABLE_NAME:

                        for (int i = 0; i < jsonArray.length(); i++) {
                            publishProgress((double) (i + 1) / jsonArray.length() * 100);

                            JSONObject obj = new JSONObject(jsonArray.get(i).toString());
                            if (!mDBh.insertPlace(obj.getLong("scene_id"), obj.getString("username"))) {
                                Log.i(TAG, "ERROR ON INSERT VISIT");
                                return false;
                            }
                        }
                        break;
                }
            } catch (JSONException e) {
                Log.i(TAG, e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(Double... progress) {
            Log.i(TAG, "Inserting :" + progress[0]);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            Log.i(TAG, "Insert Complete :");
            listener.OnWriteComplete(success);
        }

        @Override
        protected void onCancelled() {
        }
    }


}
