package com.example.dylan.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

public class SignupActivity extends AppCompatActivity {

    private ProgressDialog progress;
    private ApplicationParent applicationParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        applicationParent = (ApplicationParent) getApplicationContext();
        initializeCheckBoxes();
    }

    public void initializeCheckBoxes() {
        CheckBox tcCheckbox = findViewById(R.id.tc_checkbox_signup);
        CheckBox smsCheckbox = findViewById(R.id.receive_sms_signup);
        tcCheckbox.setChecked(applicationParent.getTcChecked());
        smsCheckbox.setChecked(applicationParent.getReceiveSms());
    }

    public void signup(View view) {
        EditText emailInput = findViewById(R.id.input_email);
        EditText passwordInput = findViewById(R.id.input_password);
        EditText usernameInput = findViewById(R.id.input_username);
        EditText phoneInput = findViewById(R.id.input_phone);

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String username = usernameInput.getText().toString();
        String phoneText = phoneInput.getText().toString();
        sendSignupCredentials(email, password, username, phoneText, applicationParent.getReceiveSms(), applicationParent.getTcChecked());
    }

    private void gotoLogin() {
        Intent loginIntent = new Intent(this, MainActivity.class);
        startActivity(loginIntent);
    }

    private void sendSignupCredentials(String email, String password, String username, String phoneNumber, boolean receiveSms, boolean tcAccepted) {
        String url = "https://chatappbackend.appspot.com/register";
        RequestParams signupParams = createSignupParams(email, password, username, phoneNumber, receiveSms, tcAccepted);
        applicationParent.getHttpClient().post(url, signupParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("success") && response.getBoolean("success")) {
                        displayToast("Signup Successful. Please login");
                        gotoLogin();
                    } else {
                        if (response.has("error") && response.has("message"))
                            displayToast(response.getString("message"));
                        else if (response.has("exception"))
                            displayToast("The server was unable to handle your request");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                applicationParent.log(responseString);
            }
        });

    }

    private RequestParams createSignupParams(String email, String password, String username, String phoneNumber, boolean receiveSms, boolean tcAccepted) {
        RequestParams signupParams = new RequestParams();
        signupParams.put("email", email);
        signupParams.put("password", password);
        signupParams.put("username", username);
        signupParams.put("phone", phoneNumber);
        signupParams.put("receive_sms", receiveSms);
        signupParams.put("tc_accepted", tcAccepted);

        return signupParams;
    }

    private void displayToast(String toastText) {
        Toast toast = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_LONG);
        toast.show();
    }
}
