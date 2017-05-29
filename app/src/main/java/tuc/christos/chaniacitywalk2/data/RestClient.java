package tuc.christos.chaniacitywalk2.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import tuc.christos.chaniacitywalk2.ClientListener;
import tuc.christos.chaniacitywalk2.ContentListener;
import tuc.christos.chaniacitywalk2.LoginActivity;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.utils.Constants;
import tuc.christos.chaniacitywalk2.utils.JsonHelper;

/**
 * Created by Christos on 29-May-17.
 * <p>
 * REST CLIENT
 */

public class RestClient implements ContentListener {

    private final String TAG = "REST CLIENT";

    private static RestClient INSTANCE = null;

    private DataManager mDataManager = DataManager.getInstance();
    private boolean isLoading = false;
    private ClientListener currentListener;

    private RestClient() {
    }


    public static RestClient getInstance() {
        if (INSTANCE == null)
            INSTANCE = new RestClient();
        return INSTANCE;
    }

    public void getInitialContent(ClientListener clientListener) {
        if (!isLoading) {
            currentListener = clientListener;
            currentListener.onUpdate(0, "Downloading Periods...");
            isLoading = true;
            downloadPeriods(this);
        }else{
           Log.i(TAG,"Client is Already running...");
        }
    }

    public void login(String uname, String pass, ClientListener clientListener ){
        if (!isLoading) {
            currentListener = clientListener;
            currentListener.onUpdate(0, "Logging in...");
            isLoading = true;
            invokeWSLogin(uname, pass, this);
        }else{
            Log.i("REST","Client is Already running...");
        }

    }
    public void register(JSONObject jsonObject, ClientListener clientListener, Context context){
        if (!isLoading) {
            currentListener = clientListener;
            currentListener.onUpdate(0, "Logging in...");
            isLoading = true;
            invokeWSRegister(jsonObject, this, context);
        }else{
            Log.i("REST","Client is Already running...");
        }
    }

    public void getPlayerData( ClientListener clientListener){
        Context context = (Context) clientListener;
        if (!isLoading) {
            currentListener = clientListener;
            currentListener.onUpdate(0, "Downloading Visits...");
            isLoading = true;
            downloadData(mDataManager.getActivePlayer().getLinks().get("visits"),this, mDBHelper.VisitsEntry.TABLE_NAME);
        }else{
            Toast.makeText(context,"Client is Already running...",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void downloadComplete(boolean success, int httpCode, String tag, String msg) {
        if (success) {

            switch (tag) {
                case mDBHelper.PeriodEntry.TABLE_NAME:
                    currentListener.onUpdate(50, "Downloading Scenes...");
                    downloadScenes(this);
                    break;
                case mDBHelper.SceneEntry.TABLE_NAME:
                    currentListener.onCompleted(true, httpCode, msg);
                    isLoading = false;
                    break;
                case mDBHelper.PlayerEntry.TABLE_NAME:
                    currentListener.onCompleted(true, httpCode, msg);
                    isLoading = false;
                    break;
                case mDBHelper.VisitsEntry.TABLE_NAME:
                    currentListener.onUpdate(50, "Downloading Places...");
                    downloadData(mDataManager.getActivePlayer().getLinks().get("places"),this,mDBHelper.PlacesEntry.TABLE_NAME);
                    break;
                case mDBHelper.PlacesEntry.TABLE_NAME:
                    currentListener.onCompleted(true, httpCode, msg);
                    isLoading = false;
                    break;
            }

        }else{
            currentListener.onCompleted(false, httpCode , msg);
            isLoading = false;
        }
    }
    /**********************************************************GET PERIODS***********************************************************************/

    private void downloadPeriods(final ContentListener cl) {
        Log.i(TAG, "Downloading Periods");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.URL_PERIODS, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                    mDataManager.populatePeriods(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cl.downloadComplete(true, i, mDBHelper.PeriodEntry.TABLE_NAME, "Download Periods Complete");
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
                cl.downloadComplete(false, i, mDBHelper.PeriodEntry.TABLE_NAME,result);
            }
        });
    }

    /**********************************************************GET SCENES*************************************************************************/

    public void downloadScenes(final ContentListener cl) {
        Log.i(TAG, "Downloading Scenes");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.URL_SCENES, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                    mDataManager.populateScenes(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cl.downloadComplete(true, i, mDBHelper.SceneEntry.TABLE_NAME, "Downloading Scenes Complete");
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
                cl.downloadComplete(false, i, mDBHelper.SceneEntry.TABLE_NAME, result);
            }
        });
    }
    /**********************************************************UPDATE PLAYER*************************************************************************/

    void putPlayer(Player player, Context context) {
        Log.i(TAG, "STARTING...");
        ByteArrayEntity entity = null;
        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("username", player.getUsername());
            json.put("email", player.getEmail());
            json.put("password", player.getPassword());
            json.put("firstname", player.getFirstname());
            json.put("lastname", player.getLastname());
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
        try {
            entity = new ByteArrayEntity(json.toString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException es) {
            Log.i(TAG, es.getMessage());
        }
        client.setBasicAuth(player.getUsername(), player.getPassword());
        if (entity != null)
            client.put(context, Constants.URL_PUT_USER + player.getUsername(), entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    Log.i("PUT", "SUCCESS" + i);
                    String code = "";
                    for (byte b : bytes) {
                        code = code + ((char) b);
                    }
                    Log.i("Response Body: ", code);
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    Log.i("PUT", "NOP" + i);
                    String code = "";
                    for (byte b : bytes) {
                        code = code + ((char) b);
                    }
                    Log.i("Response Body: ", code);
                }
            });

    }

    /*********************************************LOGIN/GET PLAYER***********************************************************************/

    private void invokeWSLogin(String cred, String password ,final ContentListener contentListener) {
        AsyncHttpClient client = new AsyncHttpClient();
        final String login_url = Constants.URL_LOGIN_USER + "?auth=" + cred;
        client.setBasicAuth(cred, password);
        client.setMaxRetriesAndTimeout(4, 20000);
        client.get(login_url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                //Print Out response
                isLoading = false;
                String code = "";
                Log.i("Success Response code: ", " code: " + i);
                for (Header head : headers) {
                    Log.i("Response Headers: ", head.getName() + "->" + head.getValue() + "\n");
                }
                for (byte b : bytes) {
                    code = code + ((char) b);
                }
                Log.i("Response Body: ", code);
                contentListener.downloadComplete(true, i, mDBHelper.PlayerEntry.TABLE_NAME, code);
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                //Print Out response
                String code = "";
                isLoading = false;
                Log.i("Failure Response code: ", " code: " + i);
                if (i == 0) {
                    final String result = throwable.getMessage();
                    contentListener.downloadComplete(false,i, mDBHelper.PlayerEntry.TABLE_NAME ,result);
                    return;
                }

                if (headers != null)
                    for (Header head : headers) {
                        Log.i("Response Headers: ", head.getName() + "->" + head.getValue() + "\n");
                    }
                if (bytes != null) {
                    for (byte b : bytes) {
                        code = code + ((char) b);
                    }
                    Log.i("Response Body: ", code);
                }
                String result = "404 NOT FOUND! Service unreachable. :(";
                try {
                    JSONObject errorMessage = new JSONObject(code);
                    result = errorMessage.getString("message");
                    contentListener.downloadComplete(false,i, mDBHelper.PlayerEntry.TABLE_NAME ,result);
                } catch (JSONException ex) {
                    Log.i("JSON EXCEPTION: ", ex.getMessage());
                    contentListener.downloadComplete(false,505, mDBHelper.PlayerEntry.TABLE_NAME ,result);
                }
            }
        });

    }
    //GET PLAYER VISITS


    //GET PLAYER PLACES



    /*********************************************REGISTER/POST PLAYER***********************************************************************/


    public void invokeWSRegister(JSONObject jsonObject,final ContentListener ContentListener, Context context) {
        try {
            ByteArrayEntity entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
            AsyncHttpClient client = new AsyncHttpClient();
            client.setMaxRetriesAndTimeout(4, 20000);
            client.post(context, Constants.URL_REGISTER_USER, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    //Print Out response
                    String code = "";
                    Log.i("Success Response code: ", " code: " + i);
                    for (Header head : headers) {
                        Log.i("Response Headers: ", head.getName() + "->" + head.getValue() + "\n");
                    }
                    for (byte b : bytes) {
                        code = code + ((char) b);
                    }
                    Log.i("Response Body: ", code);
                    ContentListener.downloadComplete(true,i, mDBHelper.PlayerEntry.TABLE_NAME , code);

                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    //Print Out response
                    String code = "";
                    Log.i("Failure Response code: ", " code: " + i);
                    if (i == 0) {
                        final String result = throwable.getMessage();
                        ContentListener.downloadComplete(false,i, mDBHelper.PlayerEntry.TABLE_NAME ,result);
                        return;
                    }

                    if (headers != null)
                        for (Header head : headers) {
                            Log.i("Response Headers: ", head.getName() + "->" + head.getValue() + "\n");
                        }
                    if (bytes != null) {
                        for (byte b : bytes) {
                            code = code + ((char) b);
                        }
                        Log.i("Response Body: ", code);
                    }
                    String result = "404 NOT FOUND! Service unreachable. :(";
                    try {
                        JSONObject errorMessage = new JSONObject(code);
                        result = errorMessage.getString("message");
                        ContentListener.downloadComplete(false,i, mDBHelper.PlayerEntry.TABLE_NAME ,result);
                    } catch (JSONException ex) {
                        Log.i("JSON EXCEPTION: ", ex.getMessage());
                        ContentListener.downloadComplete(false,i, mDBHelper.PlayerEntry.TABLE_NAME ,result);
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void downloadData(String uri,final ContentListener contentListener,final String tag){
        Log.i(TAG, "Downloading Player Data: "+ tag);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(uri, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                        mDataManager.populateUserData(json, tag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                contentListener.downloadComplete(true, i, tag, "Downloading Complete");
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
                contentListener.downloadComplete(false, i, tag, result);
            }
        });

    }
}
