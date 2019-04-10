package com.example.dylan.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;

public class SignupAccept extends AppCompatActivity {

    private ApplicationParent applicationParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_accept);
        applicationParent = (ApplicationParent) getApplicationContext();

    }

    public void acceptTC(View view) {
        CheckBox tcCheckbox = (CheckBox) view.findViewById(R.id.tc_checkbox);
        applicationParent.setTcChecked(tcCheckbox.isChecked());
        WebView tcWebView = (WebView) findViewById(R.id.tc_webview);

        if (tcCheckbox.isChecked()) {
            tcWebView.setVisibility(view.VISIBLE);
            tcWebView.loadUrl("https://chatappbackend.appspot.com/terms_and_conditions");
        }
        else
            tcWebView.setVisibility(view.GONE);
    }

    public void acceptAndContinue(View view) {
        if (!applicationParent.getTcChecked()) {
            applicationParent.displayToast("You must accept Terms and Conditions in order to signup for this application", this);
        } else {
            Intent signupIntent = new Intent(this, SignupActivity.class);
            startActivity(signupIntent);
        }
    }

    public void receiveSms(View view) {
        CheckBox receiveSmsCheckbox = (CheckBox) view.findViewById(R.id.receiveSmsCheckbox);
        applicationParent.setReceiveSms(receiveSmsCheckbox.isChecked());
    }
}
