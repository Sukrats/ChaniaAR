package tuc.christos.chaniacitywalk2.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import tuc.christos.chaniacitywalk2.MyApp;
import tuc.christos.chaniacitywalk2.mInterfaces.ClientListener;
import tuc.christos.chaniacitywalk2.mInterfaces.ContentListener;
import tuc.christos.chaniacitywalk2.mInterfaces.LocalDBWriteListener;
import tuc.christos.chaniacitywalk2.model.Player;

/**
 * Created by Christos on 29-May-17.
 * <p>
 * REST CLIENT
 */

public class RestClient implements ContentListener {

    private final String TAG = "REST CLIENT";

    private static RestClient INSTANCE = null;

    private AsyncHttpClient mClient;
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

    @Override
    public void downloadComplete(boolean success, int httpCode, String tag, String msg) {
        if (success) {
            switch (tag) {
                case mDBHelper.PeriodEntry.TABLE_NAME:
                    //currentListener.onUpdate(50, "Downloading Scenes...");
                    currentListener.onCompleted(true, httpCode, msg);
                    //downloadScenes(this);
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
                    downloadData(mDataManager.getActivePlayer().getLinks().get("places"), this, mDBHelper.PlacesEntry.TABLE_NAME);
                    break;
                case mDBHelper.PlacesEntry.TABLE_NAME:
                    currentListener.onCompleted(true, httpCode, msg);
                    isLoading = false;
                    break;
            }

        } else {
            currentListener.onCompleted(false, httpCode, msg);
            isLoading = false;
        }
    }

    public void getInitialContent(ClientListener clientListener) {
        //if (!isLoading) {
        currentListener = clientListener;
        currentListener.onUpdate(0, "Downloading Periods...");
        isLoading = true;
        downloadPeriods(this);
        // }else{
        // Log.i(TAG,"Can't Load Content Client is Already running...");
        //}
    }

    public void login(String uname, String pass, ClientListener clientListener) {
        //if (!isLoading) {
        currentListener = clientListener;
        currentListener.onUpdate(0, "Logging in...");
        isLoading = true;
        invokeWSLogin(uname, pass, this);
        //}else{
        //    Log.i("REST","Can't Login Client is Already running...");
        //}

    }

    public void register(JSONObject jsonObject, ClientListener clientListener, Context context) {
        //if (!isLoading) {
        currentListener = clientListener;
        currentListener.onUpdate(0, "Logging in...");
        isLoading = true;
        invokeWSRegister(jsonObject, this, context);
        //}else{
        //    Log.i("REST","Can't Register Client is Already running...");
        //}
    }

    public void getPlayerData(ClientListener clientListener) {
        //if (!isLoading) {
        currentListener = clientListener;
        currentListener.onUpdate(0, "Downloading Visits...");
        isLoading = true;
        downloadData(mDataManager.getActivePlayer().getLinks().get("visits"), this, mDBHelper.VisitsEntry.TABLE_NAME);
        //}else{
        //    Log.i("REST","Cant get Player Data Client is Already running...");
        //}
    }






    /**********************************************************GET PERIODS***********************************************************************/

    public void downloadPeriods(final ContentListener cl) {
        Log.i(TAG, "Downloading Periods");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.URL_PERIODS, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(final int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                    mDataManager.populatePeriods(json, new LocalDBWriteListener() {
                        @Override
                        public void OnWriteComplete(boolean success) {
                            cl.downloadComplete(success, i, mDBHelper.PeriodEntry.TABLE_NAME, "Download Periods Complete");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                cl.downloadComplete(false, i, mDBHelper.PeriodEntry.TABLE_NAME, result);
            }
        });
        mClient = client;
    }

    /**********************************************************GET SCENES*************************************************************************/

    public void downloadScenes(final ContentListener cl) {
        Log.i(TAG, "Downloading Scenes");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.URL_SCENES, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(final int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                    mDataManager.populateScenes(json, new LocalDBWriteListener() {
                        @Override
                        public void OnWriteComplete(boolean success) {
                            cl.downloadComplete(success, i, mDBHelper.SceneEntry.TABLE_NAME, "Downloading Scenes Complete");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
        mClient = client;
    }

    public void downloadScenesView(final ContentListener cl) {
        Log.i(TAG, "Downloading Scenes");
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constants.URL_SCENES, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(final int i, Header[] headers, byte[] bytes) {
                cl.downloadComplete(true,i,"scenes",new String(bytes, StandardCharsets.UTF_8));
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
        mClient = client;
    }

    /**********************************************************UPDATE PLAYER*************************************************************************/

    void putPlayer(Player player, Context context) {
        ByteArrayEntity entity = null;
        AsyncHttpClient client = new AsyncHttpClient();
        JSONObject json = JsonHelper.playerToJson(player);
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
                    Log.i("PUT", "BODY: " + code);
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    Log.i("PUT", "NOPE" + i);
                    String code = "";
                    for (byte b : bytes) {
                        code = code + ((char) b);
                    }
                    Log.i("PUT", "BODY: " + code);
                }
                @Override
                public boolean getUseSynchronousMode() {
                    return false;
                }
            });
        mClient = client;

    }

    /*********************************************LOGIN/GET PLAYER***********************************************************************/

    private void invokeWSLogin(String cred, String password, final ContentListener contentListener) {
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
                    contentListener.downloadComplete(false, i, mDBHelper.PlayerEntry.TABLE_NAME, result);
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
                    contentListener.downloadComplete(false, i, mDBHelper.PlayerEntry.TABLE_NAME, result);
                } catch (JSONException ex) {
                    Log.i("JSON EXCEPTION: ", ex.getMessage());
                    contentListener.downloadComplete(false, 505, mDBHelper.PlayerEntry.TABLE_NAME, result);
                }
            }
        });
        mClient = client;

    }

    public void downloadPlayers(final ContentListener contentListener){
        //mClient.cancelAllRequests(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.setMaxRetriesAndTimeout(4, 20000);
        client.get(Constants.URL_USERS, null, new AsyncHttpResponseHandler() {
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
                contentListener.downloadComplete(true, i, mDBHelper.PlayerEntry.TABLE_NAME, code);

            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {String code = "";
                isLoading = false;
                Log.i("Failure Response code: ", " code: " + i);
                if (i == 0) {
                    final String result = throwable.getMessage();
                    contentListener.downloadComplete(false, i, mDBHelper.PlayerEntry.TABLE_NAME, result);
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
                    contentListener.downloadComplete(false, i, mDBHelper.PlayerEntry.TABLE_NAME, result);
                } catch (JSONException ex) {
                    Log.i("JSON EXCEPTION: ", ex.getMessage());
                    contentListener.downloadComplete(false, 505, mDBHelper.PlayerEntry.TABLE_NAME, result);
                }

            }
        });
        //mClient = client;

    }

    /*********************************************REGISTER/POST PLAYER***********************************************************************/


    public void invokeWSRegister(JSONObject jsonObject, final ContentListener ContentListener, Context context) {
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
                    ContentListener.downloadComplete(true, i, mDBHelper.PlayerEntry.TABLE_NAME, code);

                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    //Print Out response
                    String code = "";
                    Log.i("Failure Response code: ", " code: " + i);
                    if (i == 0) {
                        final String result = throwable.getMessage();
                        ContentListener.downloadComplete(false, i, mDBHelper.PlayerEntry.TABLE_NAME, result);
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
                        ContentListener.downloadComplete(false, i, mDBHelper.PlayerEntry.TABLE_NAME, result);
                    } catch (JSONException ex) {
                        Log.i("JSON EXCEPTION: ", ex.getMessage());
                        ContentListener.downloadComplete(false, i, mDBHelper.PlayerEntry.TABLE_NAME, result);
                    }
                }
            });
            mClient = client;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void downloadData(String uri, final ContentListener contentListener, final String tag) {
        Log.i("DATA", "DOWLOADING: " + tag);
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(mDataManager.getActivePlayer().getUsername(), mDataManager.getActivePlayer().getPassword());
        client.setMaxRetriesAndTimeout(4, 20000);
        client.get(uri, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(final int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                    mDataManager.populateUserData(json, tag, new LocalDBWriteListener() {
                        @Override
                        public void OnWriteComplete(boolean success) {
                            contentListener.downloadComplete(success, i, tag, "Downloading Complete");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                currentListener.onUpdate((int) (bytesWritten / totalSize), "");
            }
        });
        mClient = client;

    }

    public void downloadScenesForLocation(String country, String area, final ContentListener contentListener) {
        Log.i(TAG, "Downloading Scenes For Location");
        AsyncHttpClient client = new AsyncHttpClient();
        client.setMaxRetriesAndTimeout(4, 20000);
        client.get(Constants.URL_SCENES + "?country=" + country + "&area=" + area, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(final int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                    mDataManager.populateUserData(json, "Scenes", new LocalDBWriteListener() {
                        @Override
                        public void OnWriteComplete(boolean success) {
                            contentListener.downloadComplete(success, i, "", "Download Complete!");
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                contentListener.downloadComplete(false, i, "Guest", result);
            }
        });
        mClient = client;
    }

    void postVisit(long scene_id, final Context context) {
        try {
            Player player = mDataManager.getActivePlayer();
            Log.i(TAG, "Posting Visit");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("scene_id", scene_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ByteArrayEntity entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
            AsyncHttpClient client = new AsyncHttpClient();
            client.setBasicAuth(player.getUsername(), player.getPassword());
            client.setMaxRetriesAndTimeout(4, 20000);
            client.post(context, Constants.URL_USER + player.getUsername() + "/visits", entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    Toast.makeText(context, "updated visits", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                }
                @Override
                public boolean getUseSynchronousMode() {
                    return false;
                }
            });
            mClient = client;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    void postPlace(long scene_id, final Context context) {
        try {
            Player player = mDataManager.getActivePlayer();
            Log.i(TAG, "Posting Place");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("scene_id", scene_id);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ByteArrayEntity entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
            AsyncHttpClient client = new AsyncHttpClient();
            client.setBasicAuth(player.getUsername(),player.getPassword());
            client.setMaxRetriesAndTimeout(4, 20000);
            client.post(context, Constants.URL_USER + player.getUsername() + "/places", entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int i, Header[] headers, byte[] bytes) {
                    Toast.makeText(context, "updated places", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
                }
                @Override
                public boolean getUseSynchronousMode() {
                    return false;
                }
            });
            mClient = client;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    void deletePlace(long scene_id,final Context context){
        Log.i(TAG, "Deleting Place");
        Player player = mDataManager.getActivePlayer();
        AsyncHttpClient client = new AsyncHttpClient();
        client.setBasicAuth(player.getUsername(),player.getPassword());
        client.delete(context, Constants.URL_USER + player.getUsername() + "/places/"+scene_id, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                Toast.makeText(context, "Cleared Place", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
            }
            @Override
            public boolean getUseSynchronousMode() {
                return false;
            }
        });
        mClient = client;
    }
    public void getImagesUris(Uri uri, final ContentListener contentListener){
        //mDataManager.clearLocality();
        AsyncHttpClient client = new AsyncHttpClient();
        client.setMaxRetriesAndTimeout(4, 20000);
        client.get(uri.toString(), null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(final int i, Header[] headers, byte[] bytes) {
                try {
                    JSONArray json = new JSONArray(new String(bytes, StandardCharsets.UTF_8));
                    contentListener.downloadComplete(true,i,"Images",json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                contentListener.downloadComplete(false, i, "Images", result);
            }
        });
    }

    public void cancel() {
        if (mClient != null)
            mClient.cancelAllRequests(true);
    }
}
