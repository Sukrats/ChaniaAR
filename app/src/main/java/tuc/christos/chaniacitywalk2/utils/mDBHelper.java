package tuc.christos.chaniacitywalk2.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import tuc.christos.chaniacitywalk2.mInterfaces.LocalDBWriteListener;
import tuc.christos.chaniacitywalk2.model.Level;
import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Place;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;
import tuc.christos.chaniacitywalk2.model.Visit;


final class mDBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static String DB_NAME = "MyDB.db";


    mDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_PLAYERS = "CREATE TABLE IF NOT EXISTS " + PlayerEntry.TABLE_NAME + " (" +
                PlayerEntry.COLUMN_EMAIL + " TEXT PRIMARY KEY," +
                PlayerEntry.COLUMN_USERNAME + " TEXT," +
                PlayerEntry.COLUMN_PASSWORD + " TEXT," +
                PlayerEntry.COLUMN_FIRST_NAME + " TEXT," +
                PlayerEntry.COLUMN_LAST_NAME + " TEXT," +
                PlayerEntry.COLUMN_CREATED + " TEXT," +
                PlayerEntry.COLUMN_REGION + " TEXT," +
                PlayerEntry.COLUMN_SCORE + " INTEGER," +
                PlayerEntry.COLUMN_RECENT_ACTIVITY + " TIMESTAMP," +
                PlayerEntry.COLUMN_ACTIVE + " INTEGER " +
                ")";
        db.execSQL(SQL_CREATE_PLAYERS);

        final String SQL_CREATE_SCENES = "CREATE TABLE IF NOT EXISTS " + SceneEntry.TABLE_NAME + " (" +
                SceneEntry.SCENES_COLUMN_ID + " INTEGER PRIMARY KEY," +
                SceneEntry.SCENES_COLUMN_PERIOD_ID + " INTEGER," +
                SceneEntry.SCENES_COLUMN_NAME + " TEXT," +
                SceneEntry.SCENES_COLUMN_DESCRIPTION + " TEXT," +
                SceneEntry.SCENES_COLUMN_REGION + " TEXT," +
                SceneEntry.SCENES_COLUMN_LATITUDE + " REAL," +
                SceneEntry.SCENES_COLUMN_LONGITUDE + " REAL," +
                SceneEntry.SCENES_COLUMN_VISITED + " INTEGER," +
                SceneEntry.SCENES_COLUMN_PLACED + " INTEGER, " +
                SceneEntry.SCENES_COLUMN_IMAGES_URL + " TEXT," +
                SceneEntry.SCENES_COLUMN_THUMBNAIL_URL + " TEXT" +
                ")";
        db.execSQL(SQL_CREATE_SCENES);

        final String SQL_CREATE_PERIODS = "CREATE TABLE IF NOT EXISTS " + PeriodEntry.TABLE_NAME + " (" +
                PeriodEntry.PERIODS_COLUMN_ID + " INTEGER PRIMARY KEY," +
                PeriodEntry.PERIODS_COLUMN_NAME + " TEXT," +
                PeriodEntry.PERIODS_COLUMN_DESCRIPTION + " TEXT, " +
                PeriodEntry.PERIODS_COLUMN_STARTED + " TEXT, " +
                PeriodEntry.PERIODS_COLUMN_ENDED + " TEXT, " +
                PeriodEntry.PERIODS_COLUMN_LOGO_URL + " TEXT," +
                PeriodEntry.PERIODS_COLUMN_MAP_URL + " TEXT," +
                PeriodEntry.PERIODS_COLUMN_IMAGES_URL + " TEXT " +
                ")";
        db.execSQL(SQL_CREATE_PERIODS);

        final String SQL_CREATE_VISITS = "CREATE TABLE IF NOT EXISTS " + VisitsEntry.TABLE_NAME + " (" +
                VisitsEntry.COLUMN_SCENE_ID + " INTEGER NOT NULL, " +
                VisitsEntry.COLUMN_SCENE_NAME + " TEXT NOT NULL, " +
                VisitsEntry.COLUMN_THUMB + " TEXT , " +
                VisitsEntry.COLUMN_ADMIN_AREA + " TEXT , " +
                VisitsEntry.COLUMN_COUNTRY + " TEXT , " +
                VisitsEntry.COLUMN_CREATED + " TEXT DEFAULT CURRENT_DATE, " +
                " PRIMARY KEY (  " + VisitsEntry.COLUMN_SCENE_ID + " )" +
                ");";
        db.execSQL(SQL_CREATE_VISITS);


        final String SQL_CREATE_PLACES = "CREATE TABLE IF NOT EXISTS " + PlacesEntry.TABLE_NAME + " (" +
                PlacesEntry.COLUMN_SCENE_ID + " INTEGER NOT NULL," +
                PlacesEntry.COLUMN_SCENE_NAME + " TEXT NOT NULL," +
                PlacesEntry.COLUMN_COMMENT + " TEXT," +
                PlacesEntry.COLUMN_ADMIN_AREA + " TEXT , " +
                PlacesEntry.COLUMN_COUNTRY + " TEXT , " +
                PlacesEntry.COLUMN_THUMB + " TEXT," +
                PlacesEntry.COLUMN_CREATED + " TEXT DEFAULT CURRENT_DATE," +
                "PRIMARY KEY ( " + PlacesEntry.COLUMN_SCENE_ID + " )" +
                ");";
        db.execSQL(SQL_CREATE_PLACES);

        final String SQL_CREATE_LOCALITY = "CREATE TABLE IF NOT EXISTS " + LocalityEntry.TABLE_NAME + " (" +
                LocalityEntry.COLUMN_COUNTRY + " TEXT NOT NULL, " +
                LocalityEntry.COLUMN_COUNTRY_CODE + " TEXT NOT NULL, " +
                LocalityEntry.COLUMN_ADMIN_AREA + " TEXT NOT NULL, " +
                LocalityEntry.COLUMN_LOCALITY + " TEXT NOT NULL, " +
                LocalityEntry.COLUMN_UPDATED_LATITUDE + " REAL," +
                LocalityEntry.COLUMN_UPDATED_LONGITUDE + " REAL," +
                LocalityEntry.COLUMN_UPDATED + " TEXT DEFAULT CURRENT_DATE, " +
                " PRIMARY KEY ( " + LocalityEntry.COLUMN_COUNTRY + " , " + LocalityEntry.COLUMN_ADMIN_AREA + " )" +
                ");";
        db.execSQL(SQL_CREATE_LOCALITY);

        final String SQL_CREATE_MODIFICATIONS = "CREATE TABLE IF NOT EXISTS " + ModificationsEntry.TABLE_NAME + " (" +
                ModificationsEntry.COLUMN_TABLE_NAME + " TEXT NOT NULL PRIMARY KEY," +
                ModificationsEntry.COLUMN_ACTION + " TEXT NOT NULL," +
                ModificationsEntry.COLUMN_LAST_MODIFIED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP " +
                ")";
        db.execSQL(SQL_CREATE_MODIFICATIONS);

        final String SQL_TRIGGER_INSERT_SCENE = createTrigger(SceneEntry.TABLE_NAME, "INSERT");
        db.execSQL(SQL_TRIGGER_INSERT_SCENE);
        final String SQL_TRIGGER_UPDATE_SCENE = createTrigger(SceneEntry.TABLE_NAME, "UPDATE");
        db.execSQL(SQL_TRIGGER_UPDATE_SCENE);
        final String SQL_TRIGGER_DELETE_SCENE = createTrigger(SceneEntry.TABLE_NAME, "DELETE");
        db.execSQL(SQL_TRIGGER_DELETE_SCENE);

        final String SQL_TRIGGER_INSERT_PERIOD = createTrigger(PeriodEntry.TABLE_NAME, "INSERT");
        db.execSQL(SQL_TRIGGER_INSERT_PERIOD);
        final String SQL_TRIGGER_UPDATE_PERIOD = createTrigger(PeriodEntry.TABLE_NAME, "UPDATE");
        db.execSQL(SQL_TRIGGER_UPDATE_PERIOD);
        final String SQL_TRIGGER_DELETE_PERIOD = createTrigger(PeriodEntry.TABLE_NAME, "DELETE");
        db.execSQL(SQL_TRIGGER_DELETE_PERIOD);

        final String SQL_TRIGGER_INSERT_PLAYER = createTrigger(PlayerEntry.TABLE_NAME, "INSERT");
        db.execSQL(SQL_TRIGGER_INSERT_PLAYER);
        final String SQL_TRIGGER_UPDATE_PLAYER = createTrigger(PlayerEntry.TABLE_NAME, "UPDATE");
        db.execSQL(SQL_TRIGGER_UPDATE_PLAYER);
        final String SQL_TRIGGER_DELETE_PLAYER = createTrigger(PlayerEntry.TABLE_NAME, "DELETE");
        db.execSQL(SQL_TRIGGER_DELETE_PLAYER);


        final String SQL_TRIGGER_INSERT_PLACES = placesTrigger("INSERT");
        db.execSQL(SQL_TRIGGER_INSERT_PLACES);
        final String SQL_TRIGGER_UPDATE_PLACES = placesTrigger("UPDATE");
        db.execSQL(SQL_TRIGGER_UPDATE_PLACES);
        final String SQL_TRIGGER_DELETE_PLACES = placesDeleteTrigger("DELETE");
        db.execSQL(SQL_TRIGGER_DELETE_PLACES);

        final String SQL_TRIGGER_VISITS = visitsTrigger();
        db.execSQL(SQL_TRIGGER_VISITS);

        //LOG DB SCHEMA
        String schemaQuery = "SELECT name FROM sqlite_master WHERE type='table';";
        Cursor c = db.rawQuery(schemaQuery, null);
        String schema = "";
        while (c.moveToNext()) {
            schema += c.getString(c.getColumnIndexOrThrow("name")) + "\n";
        }
        c.close();
        Log.i("Schema", "DB Schema: " + schema);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        //db.execSQL(SQL_DELETE_ENTRIES);
        //onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // onUpgrade(db, oldVersion, newVersion);
    }

    /**************************************************************************************TRIGGERS****************************************************************************************/

    private String visitsTrigger() {
        String trigger =
                " CREATE TRIGGER IF NOT EXISTS visits_insert AFTER INSERT ON Visits" +
                        " BEGIN " +
                        " UPDATE " + PlayerEntry.TABLE_NAME + " SET " +
                        PlayerEntry.COLUMN_RECENT_ACTIVITY + " = current_timestamp ;" +
                        " END ;";

        Log.i("Schema", "DB Schema: " + trigger);
        return trigger;
    }

    private String placesTrigger(String action) {
        String trigger =
                " CREATE TRIGGER IF NOT EXISTS " + PlacesEntry.TABLE_NAME + "_" + action + " AFTER " + action + " ON " + PlacesEntry.TABLE_NAME +
                        " BEGIN " +
                        " UPDATE " + PlayerEntry.TABLE_NAME + " SET " +
                        PlayerEntry.COLUMN_RECENT_ACTIVITY + " = current_timestamp ;" +
                        " END ;";

        Log.i("Schema", "DB Schema: " + trigger);
        return trigger;
    }

    private String placesDeleteTrigger(String action) {
        String trigger =
                " CREATE TRIGGER IF NOT EXISTS " + PlacesEntry.TABLE_NAME + "_" + action + " AFTER " + action + " ON " + PlacesEntry.TABLE_NAME +
                        " BEGIN " +
                        " UPDATE " + PlayerEntry.TABLE_NAME + " SET " +
                        PlayerEntry.COLUMN_RECENT_ACTIVITY + " = current_timestamp ;" +
                        " END ;";

        Log.i("Schema", "DB Schema: " + trigger);
        return trigger;
    }

    private String createTrigger(String table_name, String action) {
        String trigger =
                " CREATE TRIGGER IF NOT EXISTS " + table_name + "_" + action + " AFTER " + action + " ON " + table_name +
                        " BEGIN " +
                        " INSERT OR REPLACE INTO " + ModificationsEntry.TABLE_NAME + " ( " +
                        ModificationsEntry.COLUMN_TABLE_NAME + "," +
                        ModificationsEntry.COLUMN_ACTION + ")" +
                        " VALUES ( '" + table_name + "','" + action + "');" +
                        " END ;";

        Log.i("Schema", "DB Schema: " + trigger);
        return trigger;
    }


    /**************************************************************************************MODIFICATIONS****************************************************************************************/

    void printModsTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String modificationsQuery = "SELECT * FROM " + ModificationsEntry.TABLE_NAME;
        Cursor cur = db.rawQuery(modificationsQuery, null);
        String mods = " TABLE_NAME \t" + "\t ACTION \t" + "\t MODIFIED\n";
        while (cur.moveToNext()) {
            Log.i("Schema", "NOT EMPTY");
            mods += cur.getString(cur.getColumnIndexOrThrow(ModificationsEntry.COLUMN_TABLE_NAME)) + "\t";
            mods += cur.getString(cur.getColumnIndexOrThrow(ModificationsEntry.COLUMN_ACTION)) + "\t";
            mods += cur.getString(cur.getColumnIndexOrThrow(ModificationsEntry.COLUMN_LAST_MODIFIED)) + "\n";
        }
        cur.close();
        Log.i("Schema", "Modifications Table: \n" + mods);
    }

    public Cursor getModifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + ModificationsEntry.TABLE_NAME;
        return db.rawQuery(query, null);
    }

    Cursor getModification(String table_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = " SELECT * FROM " + ModificationsEntry.TABLE_NAME +
                " WHERE " + ModificationsEntry.COLUMN_TABLE_NAME + " = '" + table_name + "';";
        return db.rawQuery(query, null);
    }

    boolean isPlayersEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        String countQ = "SELECT count(*) FROM " + PlayerEntry.TABLE_NAME;
        Cursor c = db.rawQuery(countQ, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count == 0;
    }

    boolean isScenesEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        String countQ = "SELECT count(*) FROM " + SceneEntry.TABLE_NAME;
        Cursor c = db.rawQuery(countQ, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count == 0;
    }

    boolean isPeriodsEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        String countQ = "SELECT count(*) FROM " + PeriodEntry.TABLE_NAME;
        Cursor c = db.rawQuery(countQ, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count == 0;
    }

    /***************************************************************LOCALITY*****************************************************************************/


    Cursor getLocality() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + LocalityEntry.TABLE_NAME;
        return db.rawQuery(selectQ, null);
    }

    boolean insertLocality(Level level) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + LocalityEntry.TABLE_NAME);
        ContentValues values = new ContentValues();
        values.put(LocalityEntry.COLUMN_COUNTRY, level.getCountry());
        values.put(LocalityEntry.COLUMN_COUNTRY_CODE, level.getCountry_code());
        values.put(LocalityEntry.COLUMN_ADMIN_AREA, level.getAdminArea());
        values.put(LocalityEntry.COLUMN_UPDATED_LATITUDE, level.getLatitude());
        values.put(LocalityEntry.COLUMN_UPDATED_LONGITUDE, level.getLongitude());
        values.put(LocalityEntry.COLUMN_LOCALITY, level.getCity());

        long count = db.insertWithOnConflict(LocalityEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return count != -1;
    }

    void clearLocality() {
        SQLiteDatabase db = this.getWritableDatabase();
        /*String deleteQ = "DELETE FROM " + SceneEntry.TABLE_NAME;
        db.rawQuery(deleteQ, null);*/
        Log.i("DB_CLEAR", "DELETE QUERY EXECUTED");
        db.delete(LocalityEntry.TABLE_NAME, null, null);
    }
    /**************************************************************************************PLAYERS****************************************************************************************/


    Cursor getPlayers() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + PlayerEntry.TABLE_NAME;
        return db.rawQuery(selectQ, null);
    }

    Cursor getActivePlayer() {
        String selectQ = "SELECT * FROM " + PlayerEntry.TABLE_NAME + " WHERE " +
                PlayerEntry.COLUMN_ACTIVE + " = 1 ";

        SQLiteDatabase db = this.getWritableDatabase();

        return db.rawQuery(selectQ, null);
    }

    void setActivePlayer(String uname) {
        SQLiteDatabase db = this.getWritableDatabase();
        clearActivePlayer();

        ContentValues values = new ContentValues();
        values.put(PlayerEntry.COLUMN_ACTIVE, 1);

        String[] args = new String[]{uname};

        db.update(PlayerEntry.TABLE_NAME, values, PlayerEntry.COLUMN_USERNAME + " = ? ", args);
    }

    void clearActivePlayer() {
        SQLiteDatabase db = this.getWritableDatabase();

        String checkEntriesQ = "SELECT * FROM " + PlayerEntry.TABLE_NAME;
        Cursor p = db.rawQuery(checkEntriesQ, null);
        if (p.getCount() > 0) {
            ContentValues cv = new ContentValues();
            cv.put(PlayerEntry.COLUMN_ACTIVE, 0);
            db.update(PlayerEntry.TABLE_NAME, cv, null, null);
        }
        p.close();

    }

    Cursor getPlayer(String username) {

        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + PlayerEntry.TABLE_NAME + " WHERE " + PlayerEntry.COLUMN_USERNAME + " = " + "'" + username + "'";
        return db.rawQuery(query, null);
    }

    void insertPlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();

        clearActivePlayer();
        ContentValues values = new ContentValues();
        values.put(PlayerEntry.COLUMN_EMAIL, player.getEmail());
        values.put(PlayerEntry.COLUMN_PASSWORD, player.getPassword());
        values.put(PlayerEntry.COLUMN_USERNAME, player.getUsername());
        values.put(PlayerEntry.COLUMN_FIRST_NAME, player.getFirstname());
        values.put(PlayerEntry.COLUMN_LAST_NAME, player.getLastname());
        values.put(PlayerEntry.COLUMN_CREATED, player.getCreated().toString());
        values.put(PlayerEntry.COLUMN_SCORE, player.getScore());
        values.put(PlayerEntry.COLUMN_RECENT_ACTIVITY, player.getRecentActivity().toString());
        values.put(PlayerEntry.COLUMN_ACTIVE, 1);

        db.insertWithOnConflict(PlayerEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    void updatePlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();

        String updateQ = "UPDATE " + PlayerEntry.TABLE_NAME + " SET " +
                PlayerEntry.COLUMN_EMAIL + " = '" + player.getEmail() + "'," +
                PlayerEntry.COLUMN_PASSWORD + " = '" + player.getPassword() + "'," +
                PlayerEntry.COLUMN_USERNAME + " = '" + player.getUsername() + "'," +
                PlayerEntry.COLUMN_FIRST_NAME + " = '" + player.getFirstname() + "'," +
                PlayerEntry.COLUMN_LAST_NAME + " = '" + player.getLastname() + "'," +
                PlayerEntry.COLUMN_SCORE + " = '" + player.getScore() + "'," +
                PlayerEntry.COLUMN_RECENT_ACTIVITY + " =  current_timestamp " +
                " WHERE " + PlayerEntry.COLUMN_ACTIVE + " = '1' ";
        db.execSQL(updateQ);

        /*ContentValues values = new ContentValues();
        values.put(PlayerEntry.COLUMN_EMAIL, player.getEmail());
        values.put(PlayerEntry.COLUMN_PASSWORD, player.getPassword());
        values.put(PlayerEntry.COLUMN_USERNAME, player.getUsername());
        values.put(PlayerEntry.COLUMN_FIRST_NAME, player.getFirstname());
        values.put(PlayerEntry.COLUMN_LAST_NAME, player.getLastname());
        values.put(PlayerEntry.COLUMN_ACTIVE, 1);

        String[] args = new String[]{player.getUsername()};
        db.update(PlayerEntry.TABLE_NAME,values, PlayerEntry.COLUMN_USERNAME +" = ? " ,args);*/
    }

    public void clearPlayersAndData() {
        SQLiteDatabase db = this.getWritableDatabase();
        /*String deleteQ = "DELETE FROM " + SceneEntry.TABLE_NAME;
        db.rawQuery(deleteQ, null);*/
        Log.i("DB_CLEAR", "DELETE PLAYERS AND DATA");
        db.delete(PlayerEntry.TABLE_NAME, null, null);
        db.delete(PlacesEntry.TABLE_NAME, null, null);
        db.delete(VisitsEntry.TABLE_NAME, null, null);
    }

    /**************************************************************************************SCENES****************************************************************************************/


    Cursor getScenes() {
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQ = "SELECT * FROM " + SceneEntry.TABLE_NAME;

        return db.rawQuery(selectQ, null);
    }

    Cursor getPeriodScenes(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + SceneEntry.TABLE_NAME + " WHERE " + SceneEntry.SCENES_COLUMN_PERIOD_ID + " = '" + id + "'";
        return db.rawQuery(selectQ, null);
    }

    Cursor getScene(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + SceneEntry.TABLE_NAME + " WHERE " + SceneEntry.SCENES_COLUMN_ID + " = '" + id + "'";
        return db.rawQuery(selectQ, null);
    }

    boolean insertScene(Scene scene) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SceneEntry.SCENES_COLUMN_ID, scene.getId());
        values.put(SceneEntry.SCENES_COLUMN_PERIOD_ID, scene.getPeriod_id());
        values.put(SceneEntry.SCENES_COLUMN_NAME, scene.getName());
        values.put(SceneEntry.SCENES_COLUMN_DESCRIPTION, scene.getDescription());
        values.put(SceneEntry.SCENES_COLUMN_PLACED, scene.getNumOfSaves());
        values.put(SceneEntry.SCENES_COLUMN_VISITED, scene.getNumOfVisits());
        values.put(SceneEntry.SCENES_COLUMN_LATITUDE, scene.getLatitude());
        values.put(SceneEntry.SCENES_COLUMN_LONGITUDE, scene.getLongitude());
        values.put(SceneEntry.SCENES_COLUMN_REGION, scene.getRegion());
        values.put(SceneEntry.SCENES_COLUMN_IMAGES_URL, scene.getUriImages().toString());
        values.put(SceneEntry.SCENES_COLUMN_THUMBNAIL_URL, scene.getUriThumb().toString());


        long count = db.insertWithOnConflict(SceneEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return count != -1;

    }


    void clearScenes() {
        SQLiteDatabase db = this.getWritableDatabase();
        /*String deleteQ = "DELETE FROM " + SceneEntry.TABLE_NAME;
        db.rawQuery(deleteQ, null);*/
        Log.i("DB_CLEAR", "DELETE QUERY EXECUTED");
        db.delete(SceneEntry.TABLE_NAME, null, null);
    }

    /**************************************************************************************PERIODS****************************************************************************************/

    Cursor getPeriods() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + PeriodEntry.TABLE_NAME;
        return db.rawQuery(selectQ, null);
    }

    Cursor getPeriod(String period_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + PeriodEntry.TABLE_NAME + " WHERE " + PeriodEntry.PERIODS_COLUMN_ID + " = '" + period_id + "'";
        return db.rawQuery(selectQ, null);
    }

    boolean insertPeriod(Period period) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PeriodEntry.PERIODS_COLUMN_ID, period.getId());
        values.put(PeriodEntry.PERIODS_COLUMN_NAME, period.getName());
        values.put(PeriodEntry.PERIODS_COLUMN_DESCRIPTION, period.getDescription());
        values.put(PeriodEntry.PERIODS_COLUMN_STARTED, period.getStarted());
        values.put(PeriodEntry.PERIODS_COLUMN_ENDED, period.getEnded());
        values.put(PeriodEntry.PERIODS_COLUMN_IMAGES_URL, period.getUriImages().toString());
        values.put(PeriodEntry.PERIODS_COLUMN_LOGO_URL, period.getUriLogo().toString());
        values.put(PeriodEntry.PERIODS_COLUMN_MAP_URL, period.getUriMap().toString());

        long count = db.insertWithOnConflict(PeriodEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return count != -1;
    }

    void clearPeriods() {
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteQ = "DELETE FROM " + PeriodEntry.TABLE_NAME;
        db.rawQuery(deleteQ, null);
    }

    /**************************************************************************************PLACES****************************************************************************************/

    Cursor getPlaces() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + PlacesEntry.TABLE_NAME;
        Log.i("Place", selectQ);
        return db.rawQuery(selectQ, null);
    }

    Cursor getPlace(long scene_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + PlacesEntry.TABLE_NAME +
                " WHERE " +
                PlacesEntry.COLUMN_SCENE_ID + " = '" + scene_id + "';";
        Log.i("Place", selectQ);
        return db.rawQuery(selectQ, null);
    }

    boolean insertPlace(Place place) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PlacesEntry.COLUMN_SCENE_ID, place.getScene_id());
        values.put(PlacesEntry.COLUMN_SCENE_NAME, place.getScene_name());
        values.put(PlacesEntry.COLUMN_COMMENT, place.getComment());
        values.put(PlacesEntry.COLUMN_ADMIN_AREA, place.getRegion());
        values.put(PlacesEntry.COLUMN_COUNTRY, place.getCountry());
        if (place.getCreated() != null)
            values.put(PlacesEntry.COLUMN_CREATED, String.valueOf(place.getCreated()));
        values.put(PlacesEntry.COLUMN_THUMB, String.valueOf(place.getThumb()));

        long count = db.insertWithOnConflict(PlacesEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return count != -1;
        //db.insert(PlacesEntry.TABLE_NAME, null, values);

    }

    void deletePlace(long scene_id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String WHERE = PlacesEntry.COLUMN_SCENE_ID + "= ?";
        String[] args = new String[]{String.valueOf(scene_id)};
        db.delete(PlacesEntry.TABLE_NAME, WHERE, args);

    }

    /**************************************************************************************VISITS****************************************************************************************/

    Cursor getVisits() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + VisitsEntry.TABLE_NAME;
        Log.i("Place", selectQ);
        return db.rawQuery(selectQ, null);
    }

    Cursor getVisit(long scene_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + VisitsEntry.TABLE_NAME +
                " WHERE " +
                VisitsEntry.COLUMN_SCENE_ID + " = '" + scene_id + "';";
        Log.i("Place", selectQ);
        return db.rawQuery(selectQ, null);
    }

    boolean insertVisit(Visit visit) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(VisitsEntry.COLUMN_SCENE_ID, visit.getScene_id());
        values.put(VisitsEntry.COLUMN_SCENE_NAME, visit.getScene_name());
        values.put(VisitsEntry.COLUMN_ADMIN_AREA, visit.getRegion());
        values.put(VisitsEntry.COLUMN_COUNTRY, visit.getCountry());
        if (visit.getCreated() != null)
            values.put(VisitsEntry.COLUMN_CREATED, String.valueOf(visit.getCreated()));
        values.put(VisitsEntry.COLUMN_THUMB, String.valueOf(visit.getThumb()));

        long count = db.insertWithOnConflict(VisitsEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        //db.insert(PlacesEntry.TABLE_NAME, null, values);
        return count != -1;

    }

    /*****************************************************************TABLES********************************************************************/

    static class SceneEntry implements BaseColumns {
        static final String TABLE_NAME = "Scenes";

        static final String SCENES_COLUMN_ID = "id";
        static final String SCENES_COLUMN_PERIOD_ID = "period_id";
        static final String SCENES_COLUMN_NAME = "name";
        static final String SCENES_COLUMN_DESCRIPTION = "description";
        static final String SCENES_COLUMN_LATITUDE = "latitude";
        static final String SCENES_COLUMN_LONGITUDE = "longitude";
        static final String SCENES_COLUMN_VISITED = "visited";
        static final String SCENES_COLUMN_PLACED = "placed";
        static final String SCENES_COLUMN_REGION = "region";
        static final String SCENES_COLUMN_THUMBNAIL_URL = "thumbnail";
        static final String SCENES_COLUMN_IMAGES_URL = "imagesurl";
    }

    static class PeriodEntry implements BaseColumns {
        static final String TABLE_NAME = "Periods";

        static final String PERIODS_COLUMN_ID = "id";
        static final String PERIODS_COLUMN_NAME = "name";
        static final String PERIODS_COLUMN_DESCRIPTION = "description";
        static final String PERIODS_COLUMN_STARTED = "started";
        static final String PERIODS_COLUMN_ENDED = "ended";
        static final String PERIODS_COLUMN_LOGO_URL = "logourl";
        static final String PERIODS_COLUMN_MAP_URL = "mapurl";
        static final String PERIODS_COLUMN_IMAGES_URL = "imagesurl";
    }

    static class PlayerEntry implements BaseColumns {
        static final String TABLE_NAME = "Player";

        static final String COLUMN_EMAIL = "email";
        static final String COLUMN_USERNAME = "username";
        static final String COLUMN_PASSWORD = "password";
        static final String COLUMN_FIRST_NAME = "firstname";
        static final String COLUMN_LAST_NAME = "lastname";
        static final String COLUMN_CREATED = "created";
        static final String COLUMN_SCORE = "score";
        static final String COLUMN_REGION = "region";
        static final String COLUMN_RECENT_ACTIVITY = "recent_activity";
        static final String COLUMN_ACTIVE = "active";
    }

    static class VisitsEntry implements BaseColumns {
        static final String TABLE_NAME = "Visits";

        static final String COLUMN_SCENE_ID = "scene_id";
        static final String COLUMN_SCENE_NAME = "scene_name";
        static final String COLUMN_ADMIN_AREA = "admin";
        static final String COLUMN_COUNTRY = "country";
        static final String COLUMN_CREATED = "created";
        static final String COLUMN_THUMB = "thumb";
    }

    static class PlacesEntry implements BaseColumns {
        static final String TABLE_NAME = "Places";

        static final String COLUMN_SCENE_ID = "scene_id";
        static final String COLUMN_SCENE_NAME = "scene_name";
        static final String COLUMN_COMMENT = "comment";
        static final String COLUMN_ADMIN_AREA = "admin";
        static final String COLUMN_COUNTRY = "country";
        static final String COLUMN_CREATED = "created";
        static final String COLUMN_THUMB = "thumb";
    }

    static class ModificationsEntry implements BaseColumns {
        static final String TABLE_NAME = "Modifications";

        static final String COLUMN_TABLE_NAME = "table_name";
        static final String COLUMN_ACTION = "action";
        static final String COLUMN_LAST_MODIFIED = "updated";
    }

    static class LocalityEntry implements BaseColumns {
        static final String TABLE_NAME = "Locality";

        static final String COLUMN_LOCALITY = "locality";
        static final String COLUMN_ADMIN_AREA = "admin";
        static final String COLUMN_COUNTRY = "country";
        static final String COLUMN_COUNTRY_CODE = "code";
        static final String COLUMN_UPDATED = "updated";
        static final String COLUMN_UPDATED_LATITUDE = "latitude";
        static final String COLUMN_UPDATED_LONGITUDE = "longitude";
    }

}
