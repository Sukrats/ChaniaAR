package tuc.christos.chaniacitywalk2.utils;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import tuc.christos.chaniacitywalk2.MyApp;
import tuc.christos.chaniacitywalk2.mInterfaces.ContentListener;

/**
 * Created by Christos on 26-Oct-17.
 * asdasd
 */

public class RestClientV2 implements ContentListener {

    private static RestClientV2 INSTANCE = null;
    private ArrayList<Request> requestQueue = new ArrayList<>();
    private boolean isRunning = false;
    private AsyncHttpClient mClient;
    private MyResponseHandler mHandler;

    private RestClientV2() {
    }

    public static RestClientV2 getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RestClientV2();
        return INSTANCE;
    }

    @Override
    public void downloadComplete(boolean success, int httpCode, String TAG, String msg) {

    }

    public void makeNewRequest(String type, String contentType, String URI, JSONArray body){
        Request request = new Request(type,URI,contentType,body);
        if(requestQueue.isEmpty()){
            isRunning = true;
            AsyncHttpClient client = new AsyncHttpClient();
            switch(request.getOperation()){
            case "GET":
                client.get(MyApp.getAppContext(), request.getURI(), new MyResponseHandler(request.getOperation(),request.getContentType(),this));
                break;
            case "POST":
                client.get(MyApp.getAppContext(), request.getURI(), new MyResponseHandler(request.getOperation(),request.getContentType(),this));
                break;
            case "PUT":
                client.get(MyApp.getAppContext(), request.getURI(), new MyResponseHandler(request.getOperation(),request.getContentType(),this));
                break;
            case "DELETE":
                client.get(MyApp.getAppContext(), request.getURI(), new MyResponseHandler(request.getOperation(),request.getContentType(),this));
                break;
            default:
            }
        }else{
            requestQueue.add(request);
        }
    }

    public void makeNewAuthRequest(String type, String URI, JSONObject body, String auth){

    }

    private class MyResponseHandler extends AsyncHttpResponseHandler {
        private Request req;
        private String crudOperation;
        private String contentType;
        private ContentListener contentListener;

        private MyResponseHandler (String crud, String contentType, ContentListener cl){
            this.crudOperation = crud;
            this.contentType = contentType;
            this.contentListener = cl;
        }

        @Override
        public void onSuccess(int i, Header[] headers, byte[] bytes) {
            String code = "";
            Log.i("Success Response code: ", " code: " + i);
            for (Header head : headers) {
                Log.i("Response Headers: ", head.getName() + "->" + head.getValue() + "\n");
            }
            for (byte b : bytes) {
                code = code + ((char) b);
            }
            Log.i("Response Body: ", code);
            switch (contentType){
                case  mDBHelper.PlacesEntry.TABLE_NAME:
                    break;
                case  mDBHelper.VisitsEntry.TABLE_NAME:
                    break;
                case  mDBHelper.SceneEntry.TABLE_NAME:
                    break;
                case  mDBHelper.PeriodEntry.TABLE_NAME:
                    break;
                case  mDBHelper.PlayerEntry.TABLE_NAME:
                    break;

            }
            contentListener.downloadComplete(true, i, " ", code);
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            String code = "";
            if (bytes != null) {
                for (byte b : bytes) {
                    code = code + ((char) b);
                }
                Log.i("Response Body: ", code);
            }
            String result = "Error on Download";
            try {
                JSONObject errorMessage = new JSONObject(code);
                result = errorMessage.getString("message");
            } catch (JSONException ex) {
                Log.i("JSON EXCEPTION: ", ex.getMessage());
            }
            contentListener.downloadComplete(false, i, mDBHelper.PeriodEntry.TABLE_NAME, result);
        }
    }

}
