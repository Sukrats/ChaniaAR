package tuc.christos.chaniacitywalk2.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
    private SQLiteDatabase myDataBase;

    private final Context mContext;

    /*private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SceneEntry.TABLE_NAME;
    */


    mDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
        DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        this.mContext = context;

    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    void createDataBase() throws IOException{

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{
            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                Log.i(TAG,"ERROR COPYING DATABASE");
                throw new Error("Error copying database");
            }
        }

    }

    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){
            //database does't exist yet.
            Log.i(TAG,"database doesn't exist yet");
        }
        if(checkDB != null){
            checkDB.close();
        }
        return checkDB != null;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = mContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    void openDataBase() throws SQLException {
        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }

    void closeDataBase(){
        myDataBase.close();
    }
	
    public void onCreate(SQLiteDatabase db) {
        //db.execSQL(SQL_CREATE_ENTRIES);
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


    Cursor getScenes(){

        String selectQ = "SELECT * FROM " + SceneEntry.TABLE_NAME;

        //return myDataBase.query(SceneEntry.TABLE_NAME, projection,null,null,null,null,null);
        return myDataBase.rawQuery(selectQ,null);
    }

    Cursor getEmails(){
        SQLiteDatabase db = this.getWritableDatabase();

        final String SQL_CREATE_EMAILS ="CREATE TABLE IF NOT EXISTS " + EmailsEntry.TABLE_NAME + " (" +
                        EmailsEntry.EMAIL_COLUMN_EMAIL + " TEXT PRIMARY KEY," +
                        EmailsEntry.EMAIL_COLUMN_PASSWORD + " TEXT,"+
                        EmailsEntry.EMAIL_COLUMN_ACTIVE + " INTEGER "+")";

        if(!checkEmails()){
            db.execSQL(SQL_CREATE_EMAILS);
            Log.i(TAG,"created Emails table");
        }

        String selectQ = "SELECT * FROM "+ EmailsEntry.TABLE_NAME;
        return db.rawQuery(selectQ,null);
    }

    private boolean checkEmails(){
        Log.i(TAG,"check for emails table");
        String checkEmails = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + EmailsEntry.TABLE_NAME + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor temp = db.rawQuery(checkEmails,null);

        if(temp.getCount() == 0){
            temp.close();
            return false;
        }
        else
            temp.close();
        return true;
    }

    Cursor getAutoLoginEmail(){
        String selectQ = "SELECT * FROM " + EmailsEntry.TABLE_NAME + " WHERE " +
                EmailsEntry.EMAIL_COLUMN_ACTIVE + " == 1 ";

        SQLiteDatabase db = this.getWritableDatabase();

        return db.rawQuery(selectQ,null);
    }

    public void insertCredentials(String email, String password){
        SQLiteDatabase db = this.getWritableDatabase();

        String checkEntriesQ = "SELECT * FROM "+ EmailsEntry.TABLE_NAME;
        Cursor p = db.rawQuery(checkEntriesQ,null);
        if( p.getCount() > 0 ) {
            String updateQ = "UPDATE " + EmailsEntry.TABLE_NAME + " SET " +
                    EmailsEntry.EMAIL_COLUMN_ACTIVE + " = "+0;
            db.execSQL(updateQ);
        }
        p.close();

        ContentValues values = new ContentValues();
        values.put(EmailsEntry.EMAIL_COLUMN_EMAIL, email);
        values.put(EmailsEntry.EMAIL_COLUMN_PASSWORD, password);
        values.put(EmailsEntry.EMAIL_COLUMN_ACTIVE, 1);

        db.insertWithOnConflict(EmailsEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    Cursor getActivePlayer(){

        String selectQ = "SELECT * FROM "+EmailsEntry.TABLE_NAME + " WHERE "+EmailsEntry.EMAIL_COLUMN_ACTIVE+"=="+1;
        SQLiteDatabase db = this.getWritableDatabase();

        return db.rawQuery(selectQ,null);
    }

    Cursor getPeriods(){
        String selectQ = "SELECT * FROM "+ PeriodEntry.TABLE_NAME;
        return myDataBase.rawQuery(selectQ,null);
    }

    void updateLocalDB(ArrayList<Scene> Scenes){

        String deleteQ = "DELETE * FROM Scenes";
        myDataBase.rawQuery(deleteQ, null);

        String insertQ = "INSERT INTO Scenes (" + SceneEntry._ID + ","
                + SceneEntry.SCENES_COLUMN_NAME + ","
                + SceneEntry.SCENES_COLUMN_LATITUDE + ","
                + SceneEntry.SCENES_COLUMN_LONGITUDE + ","
                + SceneEntry.SCENES_COLUMN_VISITED + ","
                + SceneEntry.SCENES_COLUMN_VISIBLE + ","
                + SceneEntry.SCENES_COLUMN_HASAR + ","
                + SceneEntry.SCENES_COLUMN_DESCRIPTION + ","
                + SceneEntry.SCENES_COLUMN_TAG + ")";

        for (Scene temp: Scenes) {

            String valuesQ =  "VALUES (" + temp.getId() +","
                    + temp.getName() + ","
                    + temp.getLatitude() + ","
                    + temp.getLongitude() + ","
                    + temp.isVisited() + ","
                    + temp.isVisible() + ","
                    + temp.isHasAR() + ","
                    + temp.getBriefDesc() + ","
                    + temp.getTAG() + ")";
            myDataBase.rawQuery( insertQ+valuesQ , null);
        }
    }

    static class SceneEntry implements BaseColumns{
        static final String TABLE_NAME="Scenes";
        static final String SCENES_COLUMN_NAME ="name";
        static final String SCENES_COLUMN_LATITUDE="latitude";
        static final String SCENES_COLUMN_LONGITUDE="longitude";
        static final String SCENES_COLUMN_VISITED="visited";
        static final String SCENES_COLUMN_VISIBLE="visible";
        static final String SCENES_COLUMN_HASAR="hasAR";
        static final String SCENES_COLUMN_DESCRIPTION="description";
        static final String SCENES_COLUMN_TAG="TAG";
    }

    static class PeriodEntry implements BaseColumns{
        static final String TABLE_NAME="Periods";
        static final String PERIODS_COLUMN_NAME="name";
        static final String PERIODS_COLUMN_DESCRIPTION="description";
    }

    static class EmailsEntry implements BaseColumns{
        static final String TABLE_NAME="Emails";
        static final String EMAIL_COLUMN_EMAIL = "email";
        static final String EMAIL_COLUMN_PASSWORD = "password";
        static final String EMAIL_COLUMN_ACTIVE = "active";
    }

}
