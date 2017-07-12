package tuc.christos.chaniacitywalk2.utils;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tuc.christos.chaniacitywalk2.MyApp;
import tuc.christos.chaniacitywalk2.mInterfaces.ContentListener;
import tuc.christos.chaniacitywalk2.mInterfaces.LocalDBWriteListener;
import tuc.christos.chaniacitywalk2.model.ArScene;
import tuc.christos.chaniacitywalk2.model.Level;
import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Place;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.model.Viewport;
import tuc.christos.chaniacitywalk2.model.Visit;

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

    private HashMap<String, Scene> Route = new HashMap<>();
    private HashMap<Long, Scene> ScenesMap = new HashMap<>();

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

    public boolean isContentReady() {
        return !mDBh.isScenesEmpty() && activePlayer != null;
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
        Log.i(TAG, "" + scenesLoaded);
        getScenes();            //DUMMY FOR ROUTE IMPL
        if (getActivePlayer().getUsername().equals("Guest"))
            return new ArrayList<>(Route.values());

        if (getActivePlayer().getScore() < 750)
            return new ArrayList<>(getRoute());
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

    public void initNewPlayer(Player newPlayer) {
        mDBh.clearPlayersAndData();
        this.activePlayer = newPlayer;
        mDBh.insertPlayer(newPlayer);
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

    public boolean isPlayersEmpty() {
        return mDBh.isPlayersEmpty();
    }

    public void updatePlayer(boolean success, Context context) {
        if (activePlayer.getUsername().contains("Guest"))
            return;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            getActivePlayer().updateScore(success);
            mDBh.updatePlayer(getActivePlayer());
            RestClient rs = RestClient.getInstance();
            rs.putPlayer(getActivePlayer(), context);
            return;
        }
        Toast.makeText(context, "No Internet Connection cannot update your Progress :(", Toast.LENGTH_SHORT).show();
    }

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
            player.setScore(c.getLong(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_SCORE)));
            try {
                player.setCreated(Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_CREATED))));
            } catch (IllegalArgumentException e) {
                player.setCreated(new java.util.Date(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_CREATED))));
            }
            try {
                player.setRecentActivity(Timestamp.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_RECENT_ACTIVITY))));
            } catch (IllegalArgumentException e) {
                player.setCreated(new java.util.Date(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_RECENT_ACTIVITY))));
            }

            for (Place p : getPlaces()) {
                player.addPlace(p);
            }
            for (Visit v : getVisits()) {
                player.addVisit(v);
            }
            activePlayer = player;
        }
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
            int active = c.getInt(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_ACTIVE));
            player.setScore(c.getLong(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_SCORE)));

            try {
                player.setCreated(Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_CREATED))));
            } catch (IllegalArgumentException e) {
                player.setCreated(new java.util.Date(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_CREATED))));
            }
            try {
                player.setRecentActivity(Timestamp.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlayerEntry.COLUMN_RECENT_ACTIVITY))));
            } catch (IllegalArgumentException e) {
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
            String map = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_MAP_URL));
            String started = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_STARTED));
            String ended = c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_ENDED));

            Period temp = new Period(id, name, description);
            temp.setUriLogo(logo);
            temp.setUriMap(map);
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
            p.setUriMap(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_MAP_URL)));
            p.setStarted(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_STARTED)));
            p.setEnded(c.getString(c.getColumnIndexOrThrow(mDBHelper.PeriodEntry.PERIODS_COLUMN_ENDED)));

            p.setScenes(getPeriodScenes(p.getId()));
        }
        return p;
    }

    public List<Scene> getPeriodScenes(long periodid) {
        List<Scene> scenes = new ArrayList<>();
        List<Scene> guestScenes = new ArrayList<>();
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
                if (!Route.containsKey(String.valueOf(temp.getId())))
                    temp.setHasAR(false);
                else temp.setHasAR(true);
                temp.setUriImages(images);
                temp.setUriThumb(thumbnail);
                scenes.add(temp);

                if(Route.containsKey(String.valueOf(temp.getId()))){
                    guestScenes.add(temp);
                }
            }
        } else {
            for (Scene temp : ScenesMap.values()) {
                if (temp.getPeriod_id() == periodid) {
                    scenes.add(temp);
                    if(Route.containsKey(String.valueOf(temp.getId()))){
                        guestScenes.add(temp);
                    }
                }
            }

        }
        if(getActivePlayer().getUsername().contains("Guest"))
            return guestScenes;

        return scenes;
    }

    /**********************************************************SCENE METHODS*****************************************************************/

    public boolean isScenesEmpty() {
        return mDBh.isScenesEmpty();
    }

    void populateScenes(JSONArray jsonArray, LocalDBWriteListener l) {
        PopulateDBTask myTask = new PopulateDBTask(jsonArray, mDBHelper.SceneEntry.TABLE_NAME, l);
        myTask.execute();
    }

    public Scene getScene(long id) {
        if (getActivePlayer().getUsername().contains("Guest"))
            return Route.get(String.valueOf(id));

        if (!scenesLoaded) {
            Cursor c = mDBh.getScene(id);
            Scene s = new Scene();
            if (c.moveToNext()) {
                s.setId(c.getLong(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_ID)));
                s.setName(c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_NAME)));
                s.setDescription(c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_DESCRIPTION)));
                s.setLatitude(c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LATITUDE)));
                s.setLongitude(c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LONGITUDE)));
                s.setPeriod_id(c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_PERIOD_ID)));
                s.setNumOfSaves(c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_PLACED)));
                s.setNumOfVisits(c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_VISITED)));
                s.setUriImages(c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_IMAGES_URL)));
                s.setUriThumb(c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_THUMBNAIL_URL)));
                //s.setVisited(hasVisited(s.getId()));
                if (!Route.containsKey(String.valueOf(s.getId())))
                    s.setHasAR(false);
                else {
                    s.setHasAR(true);
                    Route.get(String.valueOf(s.getId())).setUriImages(s.getUriImages().toString());
                    Route.get(String.valueOf(s.getId())).setUriThumb(s.getUriThumb().toString());
                    Route.get(String.valueOf(s.getId())).setDescription(s.getDescription());
                    Route.get(String.valueOf(s.getId())).setNumOfSaves(s.getNumOfSaves());
                    Route.get(String.valueOf(s.getId())).setNumOfVisits(s.getNumOfVisits());
                    s = Route.get(String.valueOf(s.getId()));
                }
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
            if (!isScenesEmpty()) {
                while (c.moveToNext()) {
                    i++;
                    String name = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_NAME));
                    double lat = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LATITUDE));
                    double lon = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_LONGITUDE));
                    int id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_ID));
                    int period_id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_PERIOD_ID));
                    int saved = (c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_PLACED)));
                    int visited = (c.getInt(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_VISITED)));
                    String description = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_DESCRIPTION));
                    String images = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_IMAGES_URL));
                    String thumbnail = c.getString(c.getColumnIndexOrThrow(mDBHelper.SceneEntry.SCENES_COLUMN_THUMBNAIL_URL));

                    Scene temp = new Scene(lat, lon, id, period_id, name, description);
                    temp.setUriImages(images);
                    temp.setUriThumb(thumbnail);
                    temp.setNumOfVisits(visited);
                    temp.setNumOfSaves(saved);
                    if (!Route.containsKey(String.valueOf(temp.getId())))
                        temp.setHasAR(false);
                    else {
                        temp.setHasAR(true);
                        Route.get(String.valueOf(temp.getId())).setUriImages(temp.getUriImages().toString());
                        Route.get(String.valueOf(temp.getId())).setUriThumb(temp.getUriThumb().toString());
                        Route.get(String.valueOf(temp.getId())).setDescription(description);
                        Route.get(String.valueOf(temp.getId())).setNumOfSaves(saved);
                        Route.get(String.valueOf(temp.getId())).setNumOfVisits(visited);
                        temp = Route.get(String.valueOf(temp.getId()));
                    }
                    scenes.add(temp);
                    ScenesMap.put(temp.getId(), temp);
                }
                Log.i(TAG, "DB Queried");
                scenesLoaded = true;
            }
            Log.i(TAG, "Fetched: " + i + " Scenes");
            return scenes;
        } else
            return new ArrayList<>(ScenesMap.values());
    }

    /*****************************************************PLACES METHODS************************************************************************/

    public void savePlace(long id, Context context) {
        Place p = new Place();
        Scene temp = getScene(id);
        p.setScene_id(id);
        p.setThumb(temp.getUriThumb());
        p.setComment("");
        p.setRegion(getCurrentLevel().getAdminArea());
        p.setScene_name(temp.getName());
        p.setCountry(getCurrentLevel().getCountry());
        mDBh.insertPlace(p);
        activePlayer.addPlace(p);

        RestClient rs = RestClient.getInstance();
        rs.postPlace(id, context);
    }

    public void clearPlace(long id, Context context) {
        mDBh.deletePlace(id);
        activePlayer.removePlace(id);

        RestClient rs = RestClient.getInstance();
        rs.deletePlace(id, context);
    }

    /*
    public boolean hasSaved(long scene_id) {
        if (!scenesLoaded) {
            Player p = getPlayer();
            Cursor c = mDBh.getPlace(scene_id);
            return c.moveToNext();
        } else
            return ScenesMap.get(scene_id).isSaved();
    }*/

    public void printPlaces() {
        Cursor c = mDBh.getPlaces();
        int scene_id;
        String sceneName;
        String thumb;
        String comment;
        Date created;
        Log.i("Place", "Attempt Print: " + c.getCount());
        while (c.moveToNext()) {
            scene_id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_SCENE_ID));
            sceneName = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_SCENE_NAME));
            thumb = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_THUMB));
            comment = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_COMMENT));
            created = Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_CREATED)));
            Log.i("Place", "Scene: " + scene_id + "\tCreated: " + created.toString());
        }
    }

    private ArrayList<Place> getPlaces() {
        ArrayList<Place> places = new ArrayList<>();
        Log.i(TAG, "Places From Local DB");
        Cursor c = mDBh.getPlaces();
        while (c.moveToNext()) {
            long scene_id = c.getLong(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_SCENE_ID));
            String sceneName = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_SCENE_NAME));
            String admin = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_ADMIN_AREA));
            String country = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_COUNTRY));
            String comment = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_COMMENT));
            String thumb = c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_THUMB));
            Date created = Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.PlacesEntry.COLUMN_CREATED)));

            Place place = new Place();
            place.setScene_id(scene_id);
            place.setScene_name(sceneName);
            place.setRegion(admin);
            place.setCountry(country);
            place.setThumb(Uri.parse(thumb));
            place.setCreated(created);
            place.setComment(comment);

            places.add(place);
        }

        return places;
    }

    /*****************************************************VISITS METHODS************************************************************************/

    public void addVisit(long scene_id, Context context) {
        Visit v = new Visit();
        Scene temp = getScene(scene_id);
        v.setScene_id(scene_id);
        v.setThumb(temp.getUriThumb());
        v.setRegion(getCurrentLevel().getAdminArea());
        v.setScene_name(temp.getName());
        v.setCountry(getCurrentLevel().getCountry());
        v.setCreated(new java.sql.Date(new java.util.Date().getTime()));
        mDBh.insertVisit(v);
        activePlayer.addVisit(v);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
            RestClient rs = RestClient.getInstance();
            rs.postVisit(scene_id, context);
            return;
        }
        Toast.makeText(MyApp.getAppContext(), "No intenret Connection", Toast.LENGTH_SHORT).show();
    }

    public void addGuestVisit(long scene_id) {
        Visit v = new Visit();

        Scene temp = getScene(scene_id);
        v.setScene_id(scene_id);
        v.setThumb(temp.getUriThumb());
        v.setRegion(getCurrentLevel().getAdminArea());
        v.setScene_name(temp.getName());
        v.setCountry(getCurrentLevel().getCountry());
        v.setCreated(new java.sql.Date(new java.util.Date().getTime()));
        mDBh.insertVisit(v);
        activePlayer.addVisit(v);
    }

   /*
    public boolean hasVisited(long scene_id) {
        Player p = getPlayer();
        if (!scenesLoaded) {
            Cursor c = mDBh.getVisit(scene_id);
            return c.moveToNext();
        } else return ScenesMap.get(scene_id).isVisited();
    }*/

    public void printVisits() {
        Cursor c = mDBh.getVisits();
        String username;
        int scene_id;
        String sceneName;
        String thumb;
        String comment;
        Date created;
        Log.i("Visit", "Attempt Print: " + c.getCount());
        while (c.moveToNext()) {
            scene_id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_SCENE_ID));
            sceneName = c.getString(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_SCENE_NAME));
            thumb = c.getString(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_THUMB));
            created = Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_CREATED)));
            Log.i("Visit", "Scene: " + scene_id + "\tCreated: " + created.toString());
        }
    }

    private ArrayList<Visit> getVisits() {
        ArrayList<Visit> visits = new ArrayList<>();

        Log.i(TAG, "VISITS FROM LOCAL DB");
        Cursor c = mDBh.getVisits();
        while (c.moveToNext()) {
            long scene_id = c.getLong(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_SCENE_ID));
            String sceneName = c.getString(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_SCENE_NAME));
            String admin = c.getString(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_ADMIN_AREA));
            String country = c.getString(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_COUNTRY));
            String thumb = c.getString(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_THUMB));
            Date created = Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.VisitsEntry.COLUMN_CREATED)));
            Visit visit = new Visit();
            visit.setScene_id(scene_id);
            visit.setScene_name(sceneName);
            visit.setRegion(admin);
            visit.setCountry(country);
            visit.setThumb(Uri.parse(thumb));
            visit.setCreated(created);
            visits.add(visit);
        }
        return visits;
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
        Route = new HashMap<>();
        ArrayList<Scene> mRoute = new ArrayList<>();
        //KIDONIA PENDING
        //Scene minoiki = new Scene(35.5171461, 24.019581, 38, 1, "Minoan Kydonia", "");
        //mRoute.add(new Scene(35.5171461, 24.019581, 38, 1, "Minoan Kydonia", ""));

        //BYZANTINE WALL PENDING
        Scene wall = new Scene(35.516904, 24.020253, 37, 2, "The Byzantine Wall", "");//35.516954, 24.020359

        wall.addSlamScene(new ArScene("assets/wall/wall_Slam_2048.wt3", 35.516904, 24.020253));
        wall.addViewport(new Viewport("1", 35.516778, 24.020272, 0, 0.0f, 0.0f));
        //wall.addViewport(new Viewport("2", 35.516908, 24.020405, 0, -9.0f, 0.0f));

        wall.addArScene(new ArScene("assets/wall/wall_Geo_2048.wt3", 35.516934, 24.020343));
        //wall.addArScene(new ArScene("assets/wall/wall.wt3", 35.516934, 24.020343));
        mRoute.add(wall);


        //GLASS MOSQUE ASSETS INITIALIZATION
        Scene mosque = new Scene(35.517398, 24.01779, 36, 4, "Glass Mosque", "");
        mosque.addViewport(new Viewport("1", 35.517327, 24.017647, 0, 0.0f, 0.0f));//west 35.517327, 24.017647
        //mosque.addViewport(new Viewport("2", 35.517548, 24.017727, 0, 0.0f, 0.0f));//north 35.517548, 24.017727
        //mosque.addViewport(new Viewport("3", 35.517239, 24.017891, -180, 0.0f, 0.0f));//south 35.517239, 24.017891
        //mosque.addViewport(new Viewport("4", 35.517458, 24.018026, -270, 0.0f, 0.0f));//east 35.517458, 24.018026
        mosque.addSlamScene(new ArScene("assets/mosque/giali_slam_geo_centered.wt3", 35.517398, 24.01779));
        //mosque.addSlamScene(new ArScene("assets/mosque/minaret_Slam.wt3", 35.517394, 24.017851));
        mosque.addArScene(new ArScene("assets/mosque/Giali_Geo_4096_jpeg.wt3", 35.517394, 24.017851));
        //mosque.addArScene(new ArScene("assets/mosque/minaret_Geo_png.wt3", 35.517394, 24.017851));
        //mosque.addArScene(new ArScene("assets/mosque/minaret_Geo_no_base.wt3", 35.517394, 24.017851));
        mRoute.add(mosque);

        // Saint Rocco asset initialization
        Scene rocco = new Scene(35.5164899, 24.021208, 39, 3, "Church of St. Rocco", "");//35.516551, 24.021191
        rocco.addViewport(new Viewport("1", 35.516459, 24.021050, 0, 0.0f, 0.0f));
        //rocco.addViewport(new Viewport("2", 35.516419, 24.021270, -90, (float) -5.3, 8.0f));
        rocco.addSlamScene(new ArScene("assets/rocco/rocco_slam_geo_centered.wt3", 35.516551, 24.021191));
        //rocco.addSlamScene(new ArScene("assets/rocco/rocco_1024_slam_skt.wt3", 35.516551, 24.021191));
        //rocco.addSlamScene(new ArScene("assets/rocco/rocco_2048_slam.wt3",35.516551, 24.021191));
        //rocco.addArScene(new ArScene("assets/rocco/rocco_1024_geo.wt3",35.516551, 24.021191));
        rocco.addArScene(new ArScene("assets/rocco/rocco_2048_geo.wt3", 35.516551, 24.021191));
        mRoute.add(rocco);

        for (Scene temp : mRoute) {
            temp.setHasAR(true);
            Route.put(String.valueOf(temp.getId()), temp);
        }
    }

    public Scene getArScene(String scene_id) {
        return Route.get(scene_id);
    }


    public void setLevelLocality(Level level) {
        if (mDBh.insertLocality(level)) {
            Toast.makeText(MyApp.getAppContext(), "Updated Level", Toast.LENGTH_SHORT).show();
            this.currentLevel = level;
            return;
        }
        Toast.makeText(MyApp.getAppContext(), "Can't find Locality", Toast.LENGTH_SHORT).show();
    }

    public boolean hasLocality() {
        return mDBh.getLocality().moveToNext();
    }

    public Date getLastLocalityUpdate() {
        Cursor c = mDBh.getLocality();
        if (!c.moveToNext())
            return null;
        String adminArea = c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_ADMIN_AREA));
        Date updated = Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_UPDATED)));
        double lat = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_UPDATED_LATITUDE));
        double lon = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_UPDATED_LONGITUDE));
        Log.i("Geocoder", "SQLite Locality: " + adminArea + " \nLocation: " + lat + "," + lon + " \nUpdated:" + updated.toString());
        return Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_UPDATED)));
    }

    public Location getLastLocalityLocationUpdate() {
        Cursor c = mDBh.getLocality();
        if (!c.moveToNext())
            return null;

        Location loc = new Location("");
        loc.setLatitude(c.getDouble(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_UPDATED_LATITUDE)));
        loc.setLongitude(c.getDouble(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_UPDATED_LONGITUDE)));
        return loc;
    }

    public boolean compareExistingLocality(Level level) {
        Cursor c = mDBh.getLocality();
        if (level != null) {
            if (!c.moveToNext())
                return true;
            else if (level.getAdminArea().equals(c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_ADMIN_AREA))))
                return false;
            String adminArea = c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_ADMIN_AREA));
            String country = c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_COUNTRY));
            String city = c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_LOCALITY));
            Date updated = Date.valueOf(c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_UPDATED)));
            Log.i("Geocoder", "SQLite Locality: " + country + " " + adminArea + " " + city + " \nUpdated:" + updated.toString());
            return true;
        } else {
            return false;
        }
    }

    public void printExistingLocality() {
        Cursor c = mDBh.getLocality();
        while (c.moveToNext()) {
            Log.i("Geocoder", "\nCountry: " + c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_COUNTRY)) +
                    "\nCode: " + c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_COUNTRY_CODE)) +
                    "\nAdmin Area: " + c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_ADMIN_AREA)) +
                    "\nLocality: " + c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_LOCALITY)));
        }
    }

    public Level getCurrentLevel() {
        if (currentLevel != null)
            return this.currentLevel;
        Cursor c = mDBh.getLocality();
        currentLevel = new Level();
        if (c.moveToNext()) {
            currentLevel.setAdminArea(c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_ADMIN_AREA)));
            currentLevel.setCountry(c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_COUNTRY)));
            currentLevel.setCountry_code(c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_COUNTRY_CODE)));
            currentLevel.setCity(c.getString(c.getColumnIndexOrThrow(mDBHelper.LocalityEntry.COLUMN_LOCALITY)));
            return currentLevel;
        }
        return null;
    }

    /*public ArrayList<ArScene> getRouteAsList() {
        return new ArrayList<>(Route.values());
    }*/

    private ArrayList<Scene> getRoute() {
        return new ArrayList<>(Route.values());
    }

    /****************************************** Images for Fullscreen gallery view*****************************************************/
    private ArrayList<Uri> imgs = new ArrayList<>();

    public void setImages(ArrayList<Uri> images) {
        imgs = images;
    }

    public ArrayList<Uri> getImages() {
        return imgs;
    }

    /**********************************************************POPULATE DB TASK*************************************************************************/

    public void clearScenes() {
        mDBh.clearScenes();
        scenesLoaded = false;
    }
    /*public void clearLocality(){
        mDBh.clearLocality();
    }*/

    public void clearPeriods() {
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

        PopulateDBTask(JSONArray json, String tableName, LocalDBWriteListener l) {
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
                            scene.setRegion(getCurrentLevel().getAdminArea());

                            if (Route.containsKey(String.valueOf(scene.getId()))) {
                                Route.get(String.valueOf(scene.getId())).setUriImages(scene.getUriImages().toString());
                                Route.get(String.valueOf(scene.getId())).setUriThumb(scene.getUriThumb().toString());
                                Route.get(String.valueOf(scene.getId())).setDescription(scene.getDescription());
                                Route.get(String.valueOf(scene.getId())).setNumOfSaves(scene.getNumOfSaves());
                                Route.get(String.valueOf(scene.getId())).setNumOfVisits(scene.getNumOfVisits());
                                scene = Route.get(String.valueOf(scene.getId()));
                                scene.setHasAR(true);
                            }else scene.setHasAR(false);

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
                            Visit visit = new Visit();
                            visit.setScene_id(obj.getLong("scene_id"));
                            visit.setScene_name(obj.getString("scene_name"));
                            visit.setRegion(obj.getString("admin_area_name"));
                            visit.setScene_saves(obj.getInt("scene_saves"));
                            visit.setScene_visits(obj.getInt("scene_visits"));
                            visit.setCreated(Date.valueOf(obj.getString("created")));
                            activePlayer.addVisit(visit);
                            Log.i("Visit", "inserted: " + activePlayer.getVisit(visit.getScene_id()).getScene_name());
                            if (!mDBh.insertVisit(visit)) {
                                Log.i(TAG, "ERROR ON INSERT PLACE");
                                return false;
                            }
                        }
                        break;
                    case mDBHelper.PlacesEntry.TABLE_NAME:

                        for (int i = 0; i < jsonArray.length(); i++) {
                            publishProgress((double) (i + 1) / jsonArray.length() * 100);

                            JSONObject obj = new JSONObject(jsonArray.get(i).toString());
                            Place place = new Place();
                            place.setScene_id(obj.getLong("scene_id"));
                            place.setScene_name(obj.getString("scene_name"));
                            place.setRegion(obj.getString("admin_area_name"));
                            place.setScene_saves(obj.getInt("scene_saves"));
                            place.setScene_visits(obj.getInt("scene_visits"));
                            place.setComment(obj.getString("comment"));
                            activePlayer.addPlace(place);
                            Log.i("Place", "inserted: " + activePlayer.getPlace(place.getScene_id()).getScene_name());
                            if (!mDBh.insertPlace(place)) {
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
