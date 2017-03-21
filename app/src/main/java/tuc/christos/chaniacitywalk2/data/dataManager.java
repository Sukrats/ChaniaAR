package tuc.christos.chaniacitywalk2.data;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import tuc.christos.chaniacitywalk2.model.Scene;

/**
 * Created by Christos on 24/1/2017.
 * Data Manager for local DB access and content providing
 */

 public class dataManager {

    private static dataManager INSTANCE = null;

    private boolean initiated = false;

    private ArrayList<Scene> Route = new ArrayList<>();
    private HashMap<String,Scene> routeMap = new HashMap<>();

    private ArrayList<Scene> Scenes = new ArrayList<>();
    private HashMap<String,Scene> scenesMap = new HashMap<>();


    private HashMap<Marker,Scene> markerSceneMap = new HashMap<>();
    private HashMap<Scene,Marker> sceneMarkerMap = new HashMap<>();

    private HashMap<Polyline, Scene> lineToSceneMap = new HashMap<>();
    private HashMap<Scene, Polyline> sceneToLineMap = new HashMap<>();
	
    private HashMap<Scene,ArrayList<LatLng>> sceneToPointsMap = new HashMap<>();
    private HashMap<ArrayList<LatLng>,Scene> pointsToSceneMap = new HashMap<>();

    private String TAG = "Data Manager";
    //private Context mContext;
    private mDBHelper mDBh;

    private dataManager(){}

    public static dataManager getInstance() {
        if(INSTANCE == null)
            INSTANCE = new dataManager();
        /* mDBh = new mDBHelper(context);
        mContext = context;
        initDBhelper();
        instantiate();*/
        return INSTANCE;
    }

    public boolean isInstantiated(){
        return (initiated);
    }
	
	public void init(Context context){
        this.initiated = true;
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

                Cursor c = mDBh.getEntries();
                while (c.moveToNext()) {

                    String TAG = c.getString(c.getColumnIndexOrThrow(mDBHelper.FeedEntry.TABLE_COLUMN_TAG));
                    String name = c.getString(c.getColumnIndexOrThrow(mDBHelper.FeedEntry.TABLE_COLUMN_NAME));
                    double lat = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.FeedEntry.TABLE_COLUMN_LATITUDE));
                    double lon = c.getDouble(c.getColumnIndexOrThrow(mDBHelper.FeedEntry.TABLE_COLUMN_LONGITUDE));
                    int id = c.getInt(c.getColumnIndexOrThrow(mDBHelper.FeedEntry._ID));
                    String descr = c.getString(c.getColumnIndexOrThrow(mDBHelper.FeedEntry.TABLE_COLUMN_DESCRIPTION));

                    boolean tVisible = intToBool(c.getInt(c.getColumnIndexOrThrow(mDBHelper.FeedEntry.TABLE_COLUMN_VISIBLE)));
                    boolean tHasAR = intToBool(c.getInt(c.getColumnIndexOrThrow(mDBHelper.FeedEntry.TABLE_COLUMN_HASAR)));
                    boolean tVisit = intToBool(c.getInt(c.getColumnIndexOrThrow(mDBHelper.FeedEntry.TABLE_COLUMN_VISITED)));


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


	public boolean mapMarkerToScene(Marker marker, Scene scene){
        markerSceneMap.put(marker,scene);
        return true;
    }

    public boolean mapScenetoMarker(Scene scene, Marker marker){
        sceneMarkerMap.put(scene,marker);
        return true;
    }

    public Scene getSceneFromMarker(Marker marker){
        return markerSceneMap.get(marker);
    }

    public Marker getMarkerFromScene(Scene scene){
        return sceneMarkerMap.get(scene);
    }


    public boolean mapLineToScene(Polyline line, Scene scene){
        lineToSceneMap.put(line,scene);
        return true;
    }

    public boolean mapScenetoLine(Scene scene, Polyline line){
        sceneToLineMap.put(scene,line);
        return true;
    }

    public Scene getSceneFromLine(Polyline line){
        return lineToSceneMap.get(line);
    }

    public Polyline getLineFromScene(Scene scene){
        return sceneToLineMap.get(scene);
    }


    public boolean clearMaps(){
        sceneToLineMap.clear();
        lineToSceneMap.clear();
        sceneMarkerMap.clear();
        markerSceneMap.clear();
        return true;
    }

    public Scene getScene(String id){
        return scenesMap.get(id);
    }

    public ArrayList<Scene> getScenes(){
                   return Scenes;
    }

    public ArrayList<Scene> getScenesFromTag(String tag){
        ArrayList<Scene> result = new ArrayList<>();
        for(Scene temp: Scenes){
            if(temp.getTAG().equalsIgnoreCase(tag)){
                result.add(temp);
            }
        }
        return result;
    }

    public ArrayList<Scene> getRoute() {
        return Route;
    }

    private void updateLocalDB(){
        mDBh.updateLocalDB(Scenes);
        Scenes.clear();
        instantiate();
    }

    public ArrayList<LatLng> getPolyPoints(Scene scene){

        return sceneToPointsMap.get(scene);
    }

    private void  setPolyPoints(){
        ArrayList<LatLng> polyListRocco = new ArrayList<>();
        ArrayList<LatLng> polyListByz = new ArrayList<>();
        ArrayList<LatLng> polyListKast = new ArrayList<>();
        ArrayList<LatLng> polyListGlass = new ArrayList<>();

        polyListRocco.add(new LatLng(35.5164899,24.021208));//stRocco
        polyListRocco.add(new LatLng(35.51711,24.020557));//ByzWall
        /*polyListRocco.add(new LatLng(35.516470,24.021102));//stRocco
        polyListRocco.add(new LatLng(35.517112,24.020858));//stRocco
        polyListRocco.add(new LatLng(35.517092,24.020705));//stRocco
        */
        polyListByz.add(new LatLng(35.51711,24.020557));//ByzWall
        polyListByz.add(new LatLng(35.5171461,24.019581));//kasteli

        /* polyListByz.add(new LatLng(35.517092,24.020705));//ByzWall
        polyListByz.add(new LatLng(35.517253,24.020791));//ByzWall
        polyListByz.add(new LatLng(35.517443,24.020788));//ByzWall
        polyListByz.add(new LatLng(35.517369,24.020397));//ByzWall
        polyListByz.add(new LatLng(35.517076,24.019614));//ByzWall
        */

        polyListKast.add(new LatLng(35.5171461,24.019581));//kasteli
        polyListKast.add(new LatLng(35.517398,24.01779));//Glass Mosque

        /*polyListKast.add(new LatLng(35.517076,24.019614));//kasteli
        polyListKast.add(new LatLng(35.516653,24.018527));//kasteli
        polyListKast.add(new LatLng(35.516522,24.017910));//kasteli
        polyListKast.add(new LatLng(35.516729,24.017768));//kasteli
        polyListKast.add(new LatLng(35.517356,24.017637));//kasteli
        */
        polyListGlass.add(new LatLng(35.517398,24.01779));//Glass Mosque

        for(Scene temp: Route){
            if(temp.getId() == 1 ){
                sceneToPointsMap.put(temp,polyListGlass);
                pointsToSceneMap.put(polyListGlass,temp);
            }
            if(temp.getId() == 2 ){
                sceneToPointsMap.put(temp,polyListByz);
                pointsToSceneMap.put(polyListByz,temp);
            }
            if(temp.getId() == 3 ){
                sceneToPointsMap.put(temp,polyListKast);
                pointsToSceneMap.put(polyListKast,temp);
            }
            if(temp.getId() == 4 ){
                sceneToPointsMap.put(temp,polyListRocco);
                pointsToSceneMap.put(polyListRocco,temp);
            }
        }

    }
    private boolean intToBool(int i){
        return (i != 0);
    }

}
