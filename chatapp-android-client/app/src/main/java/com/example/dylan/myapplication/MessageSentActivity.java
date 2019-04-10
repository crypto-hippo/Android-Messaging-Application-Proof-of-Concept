package com.example.dylan.myapplication;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MessageSentActivity extends AppCompatActivity {

    private MessageSentActivity self = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_sent);
        waitAndReturnHome();
    }

    private void waitAndReturnHome() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(self, WriteActivity.class);
                startActivity(homeIntent);
            }
        }, 5000);
    }
}
