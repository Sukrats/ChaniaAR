package tuc.christos.chaniacitywalk2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import tuc.christos.chaniacitywalk2.model.Period;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.model.Scene;

/**
 * Created by Christos on 16-Feb-17.
 *
 */

final class mDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "myDBHelper";
    private static final int DB_VERSION = 1;
    private static String DB_PATH;// = "/data/data/tuc.christos.chaniacitywalk2/databases/";
    private static String DB_NAME = "scenesDBtest.db";
    private static String DB_NAME_c = "ARAppDB.db";


    mDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
    }


    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_EMAILS ="CREATE TABLE IF NOT EXISTS " + PlayerEntry.TABLE_NAME + " (" +
                PlayerEntry.COLUMN_EMAIL + " TEXT PRIMARY KEY," +
                PlayerEntry.COLUMN_USERNAME + " TEXT,"+
                PlayerEntry.COLUMN_PASSWORD + " TEXT,"+
                PlayerEntry.COLUMN_FIRST_NAME + " TEXT,"+
                PlayerEntry.COLUMN_LAST_NAME + " TEXT,"+
                PlayerEntry.COLUMN_CREATED + " TEXT,"+
                PlayerEntry.COLUMN_ACTIVE + " INTEGER "+
                ")";
        db.execSQL(SQL_CREATE_EMAILS);

        final String SQL_CREATE_SCENES ="CREATE TABLE IF NOT EXISTS " + SceneEntry.TABLE_NAME + " (" +
                SceneEntry.SCENES_COLUMN_ID + " INTEGER PRIMARY KEY," +
                SceneEntry.SCENES_COLUMN_PERIOD_ID + " INTEGER,"+
                SceneEntry.SCENES_COLUMN_NAME + " TEXT,"+
                SceneEntry.SCENES_COLUMN_DESCRIPTION + " TEXT,"+
                SceneEntry.SCENES_COLUMN_LATITUDE + " REAL,"+
                SceneEntry.SCENES_COLUMN_LONGITUDE + " REAL,"+
                SceneEntry.SCENES_COLUMN_VISITED + " INTEGER,"+
                SceneEntry.SCENES_COLUMN_PLACED + " INTEGER, "+
                SceneEntry.SCENES_COLUMN_IMAGES_URL + " TEXT,"+
                SceneEntry.SCENES_COLUMN_THUMBNAIL_URL + " TEXT"+
                ")";
        db.execSQL(SQL_CREATE_SCENES);

        final String SQL_CREATE_PERIODS ="CREATE TABLE IF NOT EXISTS " + PeriodEntry.TABLE_NAME + " (" +
                PeriodEntry.PERIODS_COLUMN_ID + " INTEGER PRIMARY KEY," +
                PeriodEntry.PERIODS_COLUMN_NAME + " TEXT,"+
                PeriodEntry.PERIODS_COLUMN_DESCRIPTION + " TEXT, "+
                PeriodEntry.PERIODS_COLUMN_STARTED + " TEXT, "+
                PeriodEntry.PERIODS_COLUMN_ENDED + " TEXT, "+
                PeriodEntry.PERIODS_COLUMN_LOGO_URL + " TEXT,"+
                PeriodEntry.PERIODS_COLUMN_IMAGES_URL + " TEXT "+
                ")";
        db.execSQL(SQL_CREATE_PERIODS);

        final String SQL_CREATE_MODIFICATIONS = "CREATE TABLE IF NOT EXISTS "+ModificationsEntry.TABLE_NAME+ " (" +
                ModificationsEntry.COLUMN_TABLE_NAME + " TEXT NOT NULL PRIMARY KEY," +
                ModificationsEntry.COLUMN_ACTION + " TEXT NOT NULL,"+
                ModificationsEntry.COLUMN_LAST_MODIFIED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP "+
                ")";
        db.execSQL(SQL_CREATE_MODIFICATIONS);

        final String SQL_TRIGGER_INSERT_SCENE = createTrigger(SceneEntry.TABLE_NAME,"INSERT");
        db.execSQL(SQL_TRIGGER_INSERT_SCENE);
        final String SQL_TRIGGER_UPDATE_SCENE = createTrigger(SceneEntry.TABLE_NAME,"UPDATE");
        db.execSQL(SQL_TRIGGER_UPDATE_SCENE);
        final String SQL_TRIGGER_DELETE_SCENE = createTrigger(SceneEntry.TABLE_NAME,"DELETE");
        db.execSQL(SQL_TRIGGER_DELETE_SCENE);

        final String SQL_TRIGGER_INSERT_PERIOD = createTrigger(PeriodEntry.TABLE_NAME,"INSERT");
        db.execSQL(SQL_TRIGGER_INSERT_PERIOD);
        final String SQL_TRIGGER_UPDATE_PERIOD = createTrigger(PeriodEntry.TABLE_NAME,"UPDATE");
        db.execSQL(SQL_TRIGGER_UPDATE_PERIOD);
        final String SQL_TRIGGER_DELETE_PERIOD = createTrigger(PeriodEntry.TABLE_NAME,"DELETE");
        db.execSQL(SQL_TRIGGER_DELETE_PERIOD);

        final String SQL_TRIGGER_INSERT_PLAYER = createTrigger(PlayerEntry.TABLE_NAME,"INSERT");
        db.execSQL(SQL_TRIGGER_INSERT_PLAYER);
        final String SQL_TRIGGER_UPDATE_PLAYER = createTrigger(PlayerEntry.TABLE_NAME,"UPDATE");
        db.execSQL(SQL_TRIGGER_UPDATE_PLAYER);
        final String SQL_TRIGGER_DELETE_PLAYER = createTrigger(PlayerEntry.TABLE_NAME,"DELETE");
        db.execSQL(SQL_TRIGGER_DELETE_PLAYER);

        //LOG DB SCHEMA
        String schemaQuery = "SELECT name FROM sqlite_master WHERE type='table';";
        Cursor c = db.rawQuery(schemaQuery,null);
        String schema = "";
        while(c.moveToNext()){
            schema+= c.getString(c.getColumnIndexOrThrow("name"))+"\n";
        }
        Log.i("Schema","DB Schema: " + schema);

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

    private String createTrigger(String table_name,String action){
        String trigger =
                " CREATE TRIGGER IF NOT EXISTS " + table_name + "_"+action+" AFTER "+ action +" ON "+table_name +
                        " BEGIN " +
                        " INSERT INTO "+ModificationsEntry.TABLE_NAME+" ( "+
                        ModificationsEntry.COLUMN_TABLE_NAME+","+
                        ModificationsEntry.COLUMN_ACTION + ")" +
                        " VALUES ( '"+table_name+"','"+action+"');" +
                        " END ;";

        Log.i("Schema","DB Schema: " + trigger);
        return trigger;
    }

    void printModsTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        String modificationsQuery = "SELECT * FROM "+ModificationsEntry.TABLE_NAME;
        Cursor cur = db.rawQuery(modificationsQuery,null);
        String mods = " TABLE_NAME \t"+"\t ACTION \t"+"\t MODIFIED\n";
        while(cur.moveToNext()){
            Log.i("Schema","NOT EMPTY");
            mods += cur.getString(cur.getColumnIndexOrThrow(ModificationsEntry.COLUMN_TABLE_NAME))+"\t";
            mods += cur.getString(cur.getColumnIndexOrThrow(ModificationsEntry.COLUMN_ACTION))+"\t";
            mods += cur.getString(cur.getColumnIndexOrThrow(ModificationsEntry.COLUMN_LAST_MODIFIED))+"\n";
        }
        Log.i("Schema","Modifications Table: \n" + mods);
    }

    public Cursor getModifications(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM "+ModificationsEntry.TABLE_NAME;
        return db.rawQuery(query, null);
    }

    public Cursor getModification(String table_name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = " SELECT * FROM "+ModificationsEntry.TABLE_NAME +
                       " WHERE "+ModificationsEntry.COLUMN_TABLE_NAME+" = '"+table_name+"';";
        return db.rawQuery(query, null);
    }

    public boolean isPlayersEmpty(){
        SQLiteDatabase db = this.getWritableDatabase();
        String countQ = "SELECT count(*) FROM "+PlayerEntry.TABLE_NAME;
        Cursor c = db.rawQuery(countQ, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count == 0;
    }

    public boolean isScenesEmpty(){
        SQLiteDatabase db = this.getWritableDatabase();
        String countQ = "SELECT count(*) FROM "+SceneEntry.TABLE_NAME;
        Cursor c = db.rawQuery(countQ, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count == 0;
    }

    public boolean isPeriodsEmpty(){
        SQLiteDatabase db = this.getWritableDatabase();
        String countQ = "SELECT count(*) FROM "+PeriodEntry.TABLE_NAME;
        Cursor c = db.rawQuery(countQ, null);
        c.moveToFirst();
        int count = c.getInt(0);
        c.close();
        return count == 0;
    }


    Cursor getEmails(){
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQ = "SELECT * FROM "+ PlayerEntry.TABLE_NAME;
        return db.rawQuery(selectQ,null);
    }

    Cursor getPlayer(){
        String selectQ = "SELECT * FROM " + PlayerEntry.TABLE_NAME + " WHERE " +
                PlayerEntry.COLUMN_ACTIVE + " == 1 ";

        SQLiteDatabase db = this.getWritableDatabase();

        return db.rawQuery(selectQ,null);
    }

    void insertUser(Player player){
        SQLiteDatabase db = this.getWritableDatabase();

        String checkEntriesQ = "SELECT * FROM "+ PlayerEntry.TABLE_NAME;
        Cursor p = db.rawQuery(checkEntriesQ,null);
        if( p.getCount() > 0 ) {
            String updateQ = "UPDATE " + PlayerEntry.TABLE_NAME + " SET " +
                    PlayerEntry.COLUMN_ACTIVE + " = "+0;
            db.rawQuery(updateQ,null);
        }
        p.close();

        ContentValues values = new ContentValues();
        values.put(PlayerEntry.COLUMN_EMAIL, player.getEmail());
        values.put(PlayerEntry.COLUMN_PASSWORD, player.getPassword());
        values.put(PlayerEntry.COLUMN_USERNAME, player.getUsername());
        values.put(PlayerEntry.COLUMN_FIRST_NAME, player.getFirstname());
        values.put(PlayerEntry.COLUMN_LAST_NAME, player.getLastname());
        values.put(PlayerEntry.COLUMN_CREATED, player.getCreated().toString());
        values.put(PlayerEntry.COLUMN_ACTIVE, 1);

        db.insertWithOnConflict(PlayerEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    Cursor getScenes(){
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQ = "SELECT * FROM " + SceneEntry.TABLE_NAME ;

        return db.rawQuery(selectQ,null);
    }

    Cursor getPeriodScenes(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + SceneEntry.TABLE_NAME + " WHERE " +SceneEntry.SCENES_COLUMN_PERIOD_ID+" = '"+id+"'" ;
        return db.rawQuery(selectQ,null);
    }

    Cursor getScene(long id){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + SceneEntry.TABLE_NAME + " WHERE " +SceneEntry.SCENES_COLUMN_ID+" = '"+id+"'" ;
        return db.rawQuery(selectQ,null);
    }

    boolean insertScene(Scene scene){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SceneEntry.SCENES_COLUMN_ID, scene.getId());
        values.put(SceneEntry.SCENES_COLUMN_PERIOD_ID, scene.getPeriod_id());
        values.put(SceneEntry.SCENES_COLUMN_NAME, scene.getName());
        values.put(SceneEntry.SCENES_COLUMN_DESCRIPTION, scene.getDescription());
        values.put(SceneEntry.SCENES_COLUMN_LATITUDE, scene.getLatitude());
        values.put(SceneEntry.SCENES_COLUMN_LONGITUDE, scene.getLongitude());
        values.put(SceneEntry.SCENES_COLUMN_IMAGES_URL, scene.getUriImages());
        values.put(SceneEntry.SCENES_COLUMN_THUMBNAIL_URL, scene.getUriThumb());

        long count = db.insertWithOnConflict(SceneEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return count != -1 ;

    }


    void clearScenes(){
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteQ = "DELETE FROM "+SceneEntry.TABLE_NAME;
        db.rawQuery(deleteQ,null);
    }

    Cursor getPeriods(){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + PeriodEntry.TABLE_NAME ;
        return db.rawQuery(selectQ,null);
    }

    Cursor getPeriod(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQ = "SELECT * FROM " + PeriodEntry.TABLE_NAME + " WHERE " +PeriodEntry.PERIODS_COLUMN_NAME+" = '"+name+"'" ;
        return db.rawQuery(selectQ,null);
    }

    boolean insertPeriod(Period period){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PeriodEntry.PERIODS_COLUMN_ID, period.getId());
        values.put(PeriodEntry.PERIODS_COLUMN_NAME, period.getName());
        values.put(PeriodEntry.PERIODS_COLUMN_DESCRIPTION, period.getDescription());
        values.put(PeriodEntry.PERIODS_COLUMN_IMAGES_URL, period.getUriImages().toString());
        values.put(PeriodEntry.PERIODS_COLUMN_LOGO_URL, period.getUriLogo().toString());

        long count = db.insertWithOnConflict(PeriodEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return count != -1 ;
    }

    void clearPeriods(){
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteQ = "DELETE FROM "+PeriodEntry.TABLE_NAME;
        db.rawQuery(deleteQ,null);
    }


    static class SceneEntry implements BaseColumns{
        static final String TABLE_NAME="Scenes";

        static final String SCENES_COLUMN_ID ="id";
        static final String SCENES_COLUMN_PERIOD_ID ="period_id";
        static final String SCENES_COLUMN_NAME ="name";
        static final String SCENES_COLUMN_DESCRIPTION="description";
        static final String SCENES_COLUMN_LATITUDE="latitude";
        static final String SCENES_COLUMN_LONGITUDE="longitude";
        static final String SCENES_COLUMN_VISITED="visited";
        static final String SCENES_COLUMN_PLACED="placed";
        static final String SCENES_COLUMN_THUMBNAIL_URL="thumbnail";
        static final String SCENES_COLUMN_IMAGES_URL="imagesurl";
    }

    static class PeriodEntry implements BaseColumns{
        static final String TABLE_NAME="Periods";

        static final String PERIODS_COLUMN_ID ="id";
        static final String PERIODS_COLUMN_NAME="name";
        static final String PERIODS_COLUMN_DESCRIPTION="description";
        static final String PERIODS_COLUMN_STARTED="started";
        static final String PERIODS_COLUMN_ENDED="ended";
        static final String PERIODS_COLUMN_LOGO_URL="logourl";
        static final String PERIODS_COLUMN_IMAGES_URL="imagesurl";
    }

    static class PlayerEntry implements BaseColumns{
        static final String TABLE_NAME="Player";

        static final String COLUMN_EMAIL = "email";
        static final String COLUMN_USERNAME = "username";
        static final String COLUMN_PASSWORD = "password";
        static final String COLUMN_FIRST_NAME = "firstname";
        static final String COLUMN_LAST_NAME = "lastname";
        static final String COLUMN_CREATED = "created";
        static final String COLUMN_ACTIVE = "active";
    }

    static class ModificationsEntry implements  BaseColumns{
        static final String TABLE_NAME="Modifications";

        static final String COLUMN_TABLE_NAME="table_name";
        static final String COLUMN_ACTION="action";
        static final String COLUMN_LAST_MODIFIED ="updated";
    }
}
