package tuc.christos.chaniacitywalk2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.*;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import tuc.christos.chaniacitywalk2.data.DataManager;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.utils.Constants;

import static tuc.christos.chaniacitywalk2.utils.Constants.PLAYERS_TABLE;


public class LoginActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private DataManager mDataManager;
    private Player mPlayer = Player.getInstance();
    // UI references.

    private AutoCompleteTextView mEmailView;
    private TextView panel_text;
    private TextView panel_swap;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View mRegisterFormView;
    private LinearLayout formsContainer;
    private LinearLayout btnPanel;
    private AppCompatCheckBox remember;

    private String mActiveView = "login";

    private TextView resultsView;

    private AsyncHttpClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        remember = (AppCompatCheckBox) findViewById(R.id.remember);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoSignIn = sharedPreferences.getBoolean(SettingsActivity.pref_key_auto_sign_in, false);
        remember.setChecked(autoSignIn);

        mDataManager = DataManager.getInstance();
        mDataManager.init(this);

        formsContainer = (LinearLayout) findViewById(R.id.forms_container);
        btnPanel = (LinearLayout) findViewById(R.id.btn_panel);
        mProgressView = findViewById(R.id.login_progress);

        resultsView = (TextView) findViewById(R.id.results);

        mLoginFormView = findViewById(R.id.login_form);
        mRegisterFormView = findViewById(R.id.register_form);
        mRegisterFormView.setVisibility(View.GONE);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        //swap Views pannel
        panel_text = (TextView) findViewById(R.id.panel_text);
        panel_swap = (TextView) findViewById(R.id.panel_swap);
        panel_swap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapForms(v);
            }
        });


        ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected)
            showNoConnectionDialog(this);

        if (autoSignIn) {
            String credentials = mDataManager.getAutoLoginCredentials();
            if (credentials != null) {
                String[] tokens = credentials.split(":");// 0 -> email, 1->password, 2-> username
                invokeWSLogin(tokens[0], tokens[1]);
            }
        }else{
            getAutoCompleteList();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mClient != null)
            mClient.cancelAllRequests(true);
    }

    public void getAutoCompleteList() {
        List<String> emails = mDataManager.getEmails();
        if (!emails.isEmpty()) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(LoginActivity.this,
                android.R.layout.simple_dropdown_item_1line, emails);
            mEmailView.setAdapter(adapter);
        }
    }

    public void attemptLogin(View view) {

        ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            // Reset errors.
            mEmailView.setError(null);
            mPasswordView.setError(null);

            // Store values at the time of the login attempt.
            String email = mEmailView.getText().toString();
            String password = mPasswordView.getText().toString();

            boolean cancel = false;
            View focusView = null;

            // Check for a valid password, if the user entered one.
            if (TextUtils.isEmpty(password)) {
                mPasswordView.setError(getString(R.string.error_field_required));
                focusView = mPasswordView;
                cancel = true;
            } else if (!isPasswordValid(password)) {
                mPasswordView.setError(getString(R.string.error_invalid_password));
                focusView = mPasswordView;
                cancel = true;
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                mEmailView.setError(getString(R.string.error_field_required));
                focusView = mEmailView;
                cancel = true;
            } else if (!isEmailValid(email) && !isUsernameValid(email)) {
                mEmailView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailView;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

                if (remember.isChecked()) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(SettingsActivity.pref_key_auto_sign_in, true);
                    editor.apply();
                }
                invokeWSLogin(email, password);
                //mAuthTask = new UserLoginTask(email, password);
                //mAuthTask.execute((Void) null);
            }

        } else {
            Snackbar.make(view, "You need an internet connection to continue", Snackbar.LENGTH_LONG).show();
        }
    }


    public void attemptRegister(View view) {

        ConnectivityManager cm = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            AutoCompleteTextView emailView = (AutoCompleteTextView) findViewById(R.id.reg_email);
            EditText usernameView = (EditText) findViewById(R.id.reg_username);
            EditText passwordView = (EditText) findViewById(R.id.reg_password);
            EditText cPasswordView = (EditText) findViewById(R.id.reg_password_conf);
            EditText fNameView = (EditText) findViewById(R.id.reg_fname);
            EditText lNameView = (EditText) findViewById(R.id.reg_lname);

            // Reset errors.
            emailView.setError(null);
            usernameView.setError(null);
            passwordView.setError(null);
            cPasswordView.setError(null);
            fNameView.setError(null);
            lNameView.setError(null);

            // Store values at the time of the login attempt.
            String email = emailView.getText().toString();
            String password = passwordView.getText().toString();
            String cPassword = cPasswordView.getText().toString();
            String username = usernameView.getText().toString();
            String fName = fNameView.getText().toString();
            String lName = lNameView.getText().toString();

            boolean cancel = false;
            View focusView = null;

            // Check for a valid password, if the user entered one.
            if (TextUtils.isEmpty(password)) {
                passwordView.setError(getString(R.string.error_field_required));
                focusView = passwordView;
                cancel = true;
            } else if (!password.equals(cPassword)) {
                passwordView.setError(getString(R.string.error_password_match));
                focusView = cPasswordView;
                cancel = true;
            } else if (!isPasswordValid(password)) {
                passwordView.setError(getString(R.string.error_invalid_password));
                focusView = passwordView;
                cancel = true;
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                emailView.setError(getString(R.string.error_field_required));
                focusView = emailView;
                cancel = true;
            } else if (!isEmailValid(email)) {
                emailView.setError(getString(R.string.error_invalid_email));
                focusView = emailView;
                cancel = true;
            }

            // Check for a valid username
            if (TextUtils.isEmpty(username)) {
                usernameView.setError(getString(R.string.error_field_required));
                focusView = usernameView;
                cancel = true;
            } else if (!isUsernameValid(username)) {
                usernameView.setError(getString(R.string.error_invalid_username));
                focusView = usernameView;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                try {
                    JSONObject json = new JSONObject();
                    json.put("username", username);
                    json.put("email", email);
                    json.put("password", password);
                    json.put("firstname", fName);
                    json.put("lastname", lName);
                    Log.i("Object Sent:", json.toString());
                    invokeWSRegister(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }//mAuthTask = new UserLoginTask(email, password);
                //mAuthTask.execute((Void) null);
            }
        } else
            Snackbar.make(view, "You need an internet connection to continue", Snackbar.LENGTH_LONG).show();
    }


    public void handleResponse(int responseCode, String message) {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        boolean cancel;
        View focusView = null;
        switch (responseCode) {
            case 200:
                resultsView.setText(R.string.action_sign_in_successful);
                Intent intent3 = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent3);
                cancel = false;
                break;
            case 204:
                resultsView.setText(R.string.action_register_successful);
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                cancel = false;
                break;
            case 401:
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                resultsView.setText(message);
                focusView = mPasswordView;
                cancel = true;
                break;
            case 403:
                resultsView.setText(message);
                cancel = true;
                break;
            case 404:
                mEmailView.setError(getString(R.string.error_incorrect_email));
                resultsView.setText(message);
                focusView = mEmailView;
                cancel = true;
                break;
            case 409:
                resultsView.setText(message);
                cancel = true;
                focusView = mEmailView;
                break;
            case 500:
                resultsView.setText(R.string.action_sign_in_server_error);
                cancel = true;
                break;
            default:
                resultsView.setText(message);
                cancel = true;
                break;
        }
        if (cancel) {
            showProgress(false);
            if (focusView != null) focusView.requestFocus();
        } else {
            Toast.makeText(this,mDataManager.getLastUpdate(PLAYERS_TABLE),Toast.LENGTH_LONG).show();
            mDataManager.insertUser(mPlayer);
        }
    }

    private void invokeWSLogin(String cred, String password) {
        showProgress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        mClient = client;
        final String login_url = Constants.URL_LOGIN_USER +"?auth="+cred;
        client.setBasicAuth(cred, password);
        client.setMaxRetriesAndTimeout(0, 200);
        client.get(login_url, null, new AsyncHttpResponseHandler() {
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

                try {
                    initPlayer(new JSONObject(code));
                    handleResponse(i, "ok");
                } catch (JSONException e) {
                    Log.i("JSON EXCEPTION: ", e.getMessage());
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                //Print Out response
                String code = "";
                Log.i("Failure Response code: ", " code: " + i);
                if (i == 0) {
                    final String result = throwable.getMessage();
                    handleResponse(i, result);
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
                try {
                    JSONObject errorMessage = new JSONObject(code);
                    final String result = errorMessage.getString("message");
                    handleResponse(i, result);
                } catch (JSONException ex) {
                    Log.i("JSON EXCEPTION: ", ex.getMessage());
                }
            }
        });

    }

    private void invokeWSRegister(JSONObject jsonObject) {
        try {
            ByteArrayEntity entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
            showProgress(true);
            AsyncHttpClient client = new AsyncHttpClient();
            mClient = client;
            client.setMaxRetriesAndTimeout(1, 20);
            client.post(this, Constants.URL_REGISTER_USER, entity, "application/json", new AsyncHttpResponseHandler() {
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

                    try {
                        initPlayer(new JSONObject(code));
                        handleResponse(i, "ok");
                    } catch (JSONException e) {
                        Log.i("JSON EXCEPTION: ", e.getMessage());
                    }
                }

                @Override
                public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                    //Print Out response
                    String code = "";
                    Log.i("Failure Response code: ", " code: " + i);
                    if (i == 0) {
                        final String result = throwable.getMessage();
                        handleResponse(i, result);
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
                    try {
                        JSONObject errorMessage = new JSONObject(code);
                        final String result = errorMessage.getString("message");
                        handleResponse(i, result);
                    } catch (JSONException ex) {
                        Log.i("JSON EXCEPTION: ", ex.getMessage());
                    }
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private void initPlayer(JSONObject json) {
        Log.i("JSON PARSING COMMENCE: ", json.toString());
        try {
            mPlayer.setEmail(json.getString("email"));
            mPlayer.setUsername(json.getString("username"));
            mPlayer.setPassword(json.getString("password"));
            mPlayer.setFirstname(json.getString("firstname"));
            mPlayer.setLastname(json.getString("lastname"));
            try {
                Log.i("Date","Json Date: "+ json.getString("created"));
                mPlayer.setCreated(new SimpleDateFormat("yyyy-MM-dd").parse(json.getString("created")));
                Log.i("Date","parsed Date: "+ mPlayer.getCreated());
            }catch(ParseException e){
                e.printStackTrace();
            }
            JSONArray links = json.getJSONArray("links");
            for (int i = 0; i < links.length(); i++) {
                JSONObject obj = new JSONObject(links.get(i).toString());
                mPlayer.addLink(obj.getString("rel"), obj.getString("url"));
            }
        } catch (JSONException e) {
            Log.i("JSON EXCEPTION", e.getMessage());
        }
        Log.i("JSONParsed", "User info:\n Email:" + mPlayer.getEmail() + "\n" +
                "Username:" + mPlayer.getUsername() + "\n" +
                "Password:" + mPlayer.getPassword() + "\n" +
                "Firstname:" + mPlayer.getFirstname() + "\n" +
                "Lastname:" + mPlayer.getLastname() + "\n" +
                "Created:" + mPlayer.getCreated() + "\n");
        for (String str : mPlayer.getLinks().keySet()) {
            Log.i("Parsed Links", "key: " + str + "\tvalue: " + mPlayer.getLinks().get(str));
        }
    }

    private boolean isUsernameValid(String username) {
        final String USERNAME_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*";
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private boolean isEmailValid(String email) {
        final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isPasswordValid(String password) {
        final String USERNAME_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*";
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return password.length() > 4 && matcher.matches() ;
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        if (show) {
            final String connecting = "Connecting...";
            resultsView.setText(connecting);
        }

        formsContainer.setVisibility(show ? View.GONE : View.VISIBLE);
        formsContainer.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                formsContainer.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        btnPanel.setVisibility(show ? View.GONE : View.VISIBLE);
        btnPanel.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                btnPanel.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });

    }

    private void swapViews(final View from, final View to) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        from.setVisibility(View.GONE);
        from.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                from.setVisibility(View.GONE);
            }
        });

        to.setVisibility(View.VISIBLE);
        to.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                to.setVisibility(View.VISIBLE);
            }
        });

    }

    public void swapForms(View view) {
        switch (mActiveView) {
            case "login":
                mActiveView = "register";
                panel_swap.setText(R.string.action_sign_in);
                panel_text.setText(R.string.text_register);
                swapViews(mLoginFormView, mRegisterFormView);
                mEmailView = (AutoCompleteTextView) findViewById(R.id.reg_email);
                mPasswordView = (EditText) findViewById(R.id.reg_password);
                break;
            case "register":
                mActiveView = "login";
                panel_swap.setText(R.string.action_register);
                panel_text.setText(R.string.text_login);
                swapViews(mRegisterFormView, mLoginFormView);
                mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
                mPasswordView = (EditText) findViewById(R.id.password);
                break;
        }
    }


    public static void showNoConnectionDialog(final Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setCancelable(true);
        builder.setMessage("No Internet Connection");
        builder.setTitle("Title: No Internet Connection");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ctx.startActivity(new Intent(Settings.ACTION_SETTINGS));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

}
