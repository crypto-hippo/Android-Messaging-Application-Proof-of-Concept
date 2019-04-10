package com.example.dylan.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SentActivity extends AppCompatActivity {

    private ApplicationParent applicationParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent);
        applicationParent = (ApplicationParent) getApplicationContext();
        populateSent();
    }

    private void populateSent() {
        JSONObject userData = applicationParent.getInboxMessages();
        if (userData != null) {
            try {
                JSONArray sentMessages = userData.getJSONArray("messages_sent");
                int length = sentMessages.length();
                int counter;
                for (counter = 0; counter < length; counter++) {
                    JSONObject currentMessage = sentMessages.getJSONObject(counter);
                    applicationParent.log(currentMessage.toString());
                    addMessageToUi(currentMessage.getString("receiver"), currentMessage.getString("message_code"), currentMessage.getString("time_sent"), currentMessage.getString("message_text"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            applicationParent.displayToast("User Data is null. This needs fix", this);
        }
    }

    private void addMessageToUi(String toEmail, String code, String datetime_sent, String message) {
        LinearLayout parentLayout = findViewById(R.id.parent_layout_sent);
        View v = getLayoutInflater().inflate(R.layout.sent_message_layout, parentLayout, false);

        TextView emailAndCode = v.findViewById(R.id.email_code);
        emailAndCode.setText(toEmail + " " + code);

        TextView date = v.findViewById(R.id.datetime_sent);
        date.setText(applicationParent.formatTimeFromServer(datetime_sent));

        TextView messageBody = v.findViewById(R.id.messageBody);
        messageBody.setText(message);

        parentLayout.addView(v);
    }
}
