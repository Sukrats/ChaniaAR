package tuc.christos.chaniacitywalk2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import tuc.christos.chaniacitywalk2.data.DataManager;
import tuc.christos.chaniacitywalk2.data.RestClient;
import tuc.christos.chaniacitywalk2.model.Player;
import tuc.christos.chaniacitywalk2.utils.Constants;
import tuc.christos.chaniacitywalk2.utils.JsonHelper;


public class LoginActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private DataManager mDataManager;
    private RestClient mRestClient;
    private Player mPlayer = new Player();
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
    private int downloads = 0;

    private String mActiveView = "login";

    private TextView resultsView;
    private TextView progressView;

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

        mRestClient = RestClient.getInstance();

        formsContainer = (LinearLayout) findViewById(R.id.forms_container);
        btnPanel = (LinearLayout) findViewById(R.id.btn_panel);
        mProgressView = findViewById(R.id.login_progress);

        resultsView = (TextView) findViewById(R.id.results);
        progressView = (TextView) findViewById(R.id.progress);

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

        getAutoCompleteList();
        if (autoSignIn) {
            showProgress(true);
            String credentials = mDataManager.getAutoLoginCredentials();
            if (credentials != null) {
                String[] tokens = credentials.split(":");// 0 -> email, 1->password, 2-> username
                Log.i("REMEMBER", "uname: " + tokens[2] + "pass: " + tokens[1]);
                mRestClient.login(tokens[0], tokens[1],new ClientListener() {
                    @Override
                    public void onCompleted(boolean success, int httpCode, String code) {
                        if(success) {
                            try {
                                mPlayer = JsonHelper.parsePlayerFromJson(new JSONObject(code));
                                handleResponse(httpCode, "ok");
                            } catch (JSONException e) {
                                resultsView.setText(e.getMessage());
                            }
                        }
                        else{
                            handleResponse(httpCode,code);
                        }
                    }
                    @Override
                    public void onUpdate(int progress, String msg) {
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mClient != null)
            mClient.cancelAllRequests(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mClient != null) {
            mClient.cancelAllRequests(true);
        }
    }

    public void getAutoCompleteList() {
        List<String> emails = mDataManager.getEmailsForAutoComplete();
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
                showProgress(true);
                mRestClient.login(email, password,new ClientListener() {
                    @Override
                    public void onCompleted(boolean success, int httpCode, String code) {
                        if(success) {
                            try {
                                mPlayer = JsonHelper.parsePlayerFromJson(new JSONObject(code));
                                handleResponse(httpCode, "ok");
                                } catch (JSONException e) {
                                    resultsView.setText(e.getMessage());
                                }
                        }
                        else{
                            handleResponse(httpCode,code);
                        }
                    }

                    @Override
                    public void onUpdate(int progress, String msg) {

                    }
                });
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
                JSONObject json = JsonHelper.playerToJson(new Player(email,username,password,fName,lName));
                showProgress(true);
                mRestClient.register(json, new ClientListener() {
                    @Override
                    public void onCompleted(boolean success, int httpCode, String code) {
                        if(success) {
                            try {
                                mPlayer = JsonHelper.parsePlayerFromJson(new JSONObject(code));
                                handleResponse(httpCode, "ok");
                            } catch (JSONException e) {
                                resultsView.setText(e.getMessage());
                            }
                        }
                        else{
                            handleResponse(httpCode,code);
                        }
                    }

                    @Override
                    public void onUpdate(int progress, String msg) {

                    }
                }, this);
                //mAuthTask = new UserLoginTask(email, password);
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
                cancel = false;
                break;
            case 201 :
                resultsView.setText(R.string.action_register_successful);
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
            mDataManager.clearActivePlayer();
        } else {
            mDataManager.printPlayers();
            if (mDataManager.isPlayersEmpty()) {
                Log.i("DB_SYNC", "Inserted new Player on: " + mPlayer.getRecentActivity().toString());
                mDataManager.insertPlayer(mPlayer);
            } else {
                Log.i("DB_SYNC", "MYSQL last update: " + mPlayer.getRecentActivity().toString());
                Log.i("DB_SYNC", "SQLite last update: " + mDataManager.getPlayerLastActivity(mPlayer.getUsername()));
                if (mPlayer.getRecentActivity().after(mDataManager.getPlayerLastActivity(mPlayer.getUsername()))) {
                    mDataManager.insertPlayer(mPlayer);
                    mDataManager.syncLocalToRemote();
                } else if (mPlayer.getRecentActivity().before(mDataManager.getPlayerLastActivity(mPlayer.getUsername()))) {
                    mDataManager.setActivePlayer(mPlayer);
                    mDataManager.syncRemoteToLocal();
                }
            }
            if (mDataManager.isInitialised()) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }else{
                mRestClient.getInitialContent( new ClientListener() {
                    @Override
                    public void onCompleted(boolean success, int httpCode, String msg) {
                        if(success) {
                            resultsView.setText(msg);
                            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                            startActivity(intent);
                        }else{
                            handleResponse(httpCode,msg);
                        }
                    }

                    @Override
                    public void onUpdate(int progress, String msg) {
                        resultsView.setText(msg+progress+"%");
                    }
                });
            }

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
        return password.length() > 4 && matcher.matches();
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
