package com.example.dylan.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

/**
 * A login screen that offers login via email/password.
 */
public class MainActivity extends AppCompatActivity {

    private ApplicationParent applicationParent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applicationParent = (ApplicationParent) getApplicationContext();
        handleAutomaticLogin();
    }

    private void handleAutomaticLogin() {
        applicationParent.getHttpClient().get(applicationParent.getLoginUrl(), null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("stay_logged_in") && response.getBoolean("stay_logged_in")) {
                        gotoHome();
                    }
                } catch (Exception e) {
                    applicationParent.log("exception while checking stay_logged_in_key in json object");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                applicationParent.log("sending get request to /login failed");
            }
        });
    }

    public void login(View view) {
        String usernameText = ((EditText) findViewById(R.id.input_username)).getText().toString();
        String passwordText = ((EditText) findViewById(R.id.input_password)).getText().toString();
        CheckBox stayLoggedIn = findViewById(R.id.checkbox_stay_logged_in);
        try {
            sendLoginCredentials(usernameText, passwordText, stayLoggedIn.isChecked() + "", this);
        } catch (Exception ex) {
            Log.i("Main activity", "Exception being thrown on login");
            ex.printStackTrace();
        }
    }

    public void gotoSignupIntent(View view) {
        Intent signupIntent = new Intent(this, SignupAccept.class);
        startActivity(signupIntent);
    }

    public void gotoHome() {
        Intent homeIntent = new Intent(this, WriteActivity.class);
        startActivity(homeIntent);
    }

    // Login to Server
    public void sendLoginCredentials(final String un, String password, String stayLoggedIn, final Context context) {
        RequestParams loginParams = createLoginParams(un, password, stayLoggedIn);
        applicationParent.getHttpClient().post(applicationParent.getLoginUrl(), loginParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                try {
                    if (response.has("exception") && response.getBoolean("exception")) {
                        applicationParent.displayToast("Server error", context);
                    } else if (response.has("error") && response.getBoolean("error")) {
                        applicationParent.displayToast(response.getString("message"), context);
                    } else if (response.has("logged_in") && response.getBoolean("logged_in")) {
                        applicationParent.setUsername(un);
                        gotoHome();
                    } else {
                        applicationParent.displayToast("Invalid Login", context);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                applicationParent.log("[+] " + responseString);
            }
        });
    }

    private RequestParams createLoginParams(String un, String password, String stayLoggedIn) {
        RequestParams loginParams = new RequestParams();
        loginParams.put("un", un);
        loginParams.put("pw", password);
        loginParams.put("stay_logged_in", stayLoggedIn);
        loginParams.put("login_type", "un-pw-droid");
        return loginParams;
    }

    public void handleStayLoggedIn(View view) {
        CheckBox cb = findViewById(R.id.checkbox_stay_logged_in);
        if (cb.isChecked()) {
            applicationParent.log("[+] Stay logged in checkbox is checked");
            applicationParent.setStayLoggedIn(true);
        } else {
            applicationParent.log("[+] Stay logged in checkbox ");
            applicationParent.setStayLoggedIn(false);
        }
    }
}
