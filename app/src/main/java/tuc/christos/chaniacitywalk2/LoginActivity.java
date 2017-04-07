package tuc.christos.chaniacitywalk2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;


public class LoginActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private TextView resultsView;

    private final String[] DUMMY_CREDENTIALS = new String[]{
            "cautionfail@gmail.com:hello"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoSignIn = sharedPreferences.getBoolean(SettingsActivity.pref_key_auto_sign_in,false);


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        resultsView = (TextView)findViewById(R.id.results);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);

        //add emails to autocomplete
        String[] pieces = DUMMY_CREDENTIALS[0].split(":");
        pieces[1] = "";
        ArrayAdapter<String> adapter =new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, pieces);
        mEmailView.setAdapter(adapter);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                attemptLogin();
            }
            });

        if(autoSignIn){
            mLoginFormView.setVisibility(View.GONE);
            //TODO:READ DATABASE, GET LOGIN INFO, ACCESS REMOTE SERVER AND VALIDATE
            //see alse Auth Task implementation
            invokeWSLogin("cautionfail@gmail.com", "hello");
            //mAuthTask = new UserLoginTask("cautionfail@gmail.com", "hello");
            //mAuthTask.execute((Void) null);
        }
    }

    public void invokeWSGet(){
        showProgress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://10.0.25.102:8080/Jersey/rest/contacts", null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                String response = "";
                for(byte b: bytes){
                    response = response +((char) b);
                }
                try{
                    JSONObject json = new JSONObject(response);
                    JSONArray contacts = json.getJSONArray("contact");

                    String items = "";
                    for(int j = 0; j < contacts.length(); j++) {
                        //Decode contacts
                        JSONObject contact = contacts.getJSONObject(j);
                        String id = contact.getString("id");
                        String name = contact.getString("name");
                        int z = j+1;
                        String temp= "\n\ncontact:"+z+"\n"+"name: "+ name+"\n"+"id: "+id;

                        if( !contact.isNull("address") ) {
                            //Decode Addresses
                            JSONArray addresses = contact.getJSONArray("address");
                            temp = temp +"\n"+"Addresses:";
                            for (int y = 0; y < addresses.length(); y++) {
                                JSONObject address = addresses.getJSONObject(y);
                                String city = address.getString("city");
                                String street = address.getString("street");
                                int o = y + 1;
                                String add = "\n\t" + o + ":" + city + "\t" + street;
                                temp = temp + add;
                            }
                        }
                        items = items +temp;
                    }
                    //handleResponse(true, items);
                }catch(JSONException e){
                   // handleResponse(true, e.getMessage());
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
               // handleResponse(false,throwable.getMessage());
            }
        });
    }

    private void attemptLogin(){
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
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
            invokeWSLogin(email,password);
            //mAuthTask = new UserLoginTask(email, password);
            //mAuthTask.execute((Void) null);
        }


    }

    private void invokeWSLogin(String email,String password){
        showProgress(true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://10.0.25.102:8080/Jersey/rest/players/login&"+email+"&"+password, null , new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {
                String code = "";
                for(byte b:bytes){
                    code = code + ((char)b);
                }
                handleResponse(code);
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                String code ="301";
                    handleResponse(code);
            }
        });

    }
    public void handleResponse(String response){
        showProgress(false);
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        boolean cancel = false;
        View focusView = null;

        switch(response){
            case "102":
                mEmailView.setError(getString(R.string.error_incorrect_email));
                resultsView.setText(R.string.action_sign_in_wrong);
                focusView = mEmailView;
                cancel = true;
                break;
            case "201":
                resultsView.setText(R.string.action_sign_in_successful);
                Intent intent = new Intent( getApplicationContext(), MapsActivity.class);
                startActivity(intent);
                break;
            case "202":
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                resultsView.setText(R.string.action_sign_in_wrong);
                focusView = mPasswordView;
                cancel = true;
                break;
        }
        if(cancel)
            focusView.requestFocus();

    }

    private boolean isEmailValid(String email) {
        final String EMAIL_PATTERN ="^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        if(!(mLoginFormView.getVisibility()==View.GONE)) {
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });
        }
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        private String error = "";

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }
            // TODO: attempt authentication against a network service.

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                error = "email";
                if (pieces[0].equals(mEmail)) {
                    error = "Password";
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }
            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent( getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            } else {
                if(error.equals("Password")) {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }else{
                    mEmailView.setError(getString(R.string.error_incorrect_email));
                    mEmailView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
