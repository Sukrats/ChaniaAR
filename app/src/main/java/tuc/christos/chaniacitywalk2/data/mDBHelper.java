package tuc.christos.chaniacitywalk2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
                PlayerEntry.COLUMN_ACTIVE + " INTEGER "+")";

        db.execSQL(SQL_CREATE_EMAILS);

        final String SQL_CREATE_SCENES ="CREATE TABLE IF NOT EXISTS " + SceneEntry.TABLE_NAME + " (" +
                SceneEntry.SCENES_COLUMN_ID + " INTEGER PRIMARY KEY," +
                SceneEntry.SCENES_COLUMN_PERIOD_ID + " INTEGER,"+
                SceneEntry.SCENES_COLUMN_NAME + " TEXT,"+
                SceneEntry.SCENES_COLUMN_DESCRIPTION + " TEXT,"+
                SceneEntry.SCENES_COLUMN_LATITUDE + " REAL,"+
                SceneEntry.SCENES_COLUMN_LONGITUDE + " REAL,"+
                SceneEntry.SCENES_COLUMN_VISITED + " INTEGER,"+
                SceneEntry.SCENES_COLUMN_PLACED + " INTEGER "+")";

        db.execSQL(SQL_CREATE_SCENES);
        final String SQL_CREATE_PERIODS ="CREATE TABLE IF NOT EXISTS " + PeriodEntry.TABLE_NAME + " (" +
                PeriodEntry.PERIODS_COLUMN_ID + " INTEGER PRIMARY KEY," +
                PeriodEntry.PERIODS_COLUMN_NAME + " TEXT,"+
                PeriodEntry.PERIODS_COLUMN_DESCRIPTION + " TEXT "+")";

        db.execSQL(SQL_CREATE_PERIODS);

        String schemaQuery = "SELECT name FROM sqlite_master WHERE type='table';";
        Cursor c = db.rawQuery(schemaQuery,null);
        String schema = "";
        while(c.moveToNext()){
            schema+= c.getString(c.getColumnIndexOrThrow("name"))+"\n";
        }
        Log.i(TAG,"DB Schema: " + schema);
        c.close();
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
            db.execSQL(updateQ);
        }
        p.close();

        ContentValues values = new ContentValues();
        values.put(PlayerEntry.COLUMN_EMAIL, player.getEmail());
        values.put(PlayerEntry.COLUMN_PASSWORD, player.getPassword());
        values.put(PlayerEntry.COLUMN_USERNAME, player.getUsername());
        values.put(PlayerEntry.COLUMN_FIRST_NAME, player.getFirstname());
        values.put(PlayerEntry.COLUMN_LAST_NAME, player.getLastname());
        values.put(PlayerEntry.COLUMN_CREATED, player.getCreated());
        values.put(PlayerEntry.COLUMN_ACTIVE, 1);

        db.insertWithOnConflict(PlayerEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
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

        long count = db.insertWithOnConflict(SceneEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return count != -1 ;

    }

    Cursor getScenes(){
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQ = "SELECT * FROM " + SceneEntry.TABLE_NAME ;

        return db.rawQuery(selectQ,null);
    }

    void clearScenes(){
        SQLiteDatabase db = this.getWritableDatabase();
        String deleteQ = "DELETE FROM "+SceneEntry.TABLE_NAME;
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
    }

    static class PeriodEntry implements BaseColumns{
        static final String TABLE_NAME="Periods";
        static final String PERIODS_COLUMN_ID ="id";
        static final String PERIODS_COLUMN_NAME="name";
        static final String PERIODS_COLUMN_DESCRIPTION="description";
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

}
