package com.example.dylan.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class InboxMessageActivity extends AppCompatActivity {

    private ApplicationParent applicationParent;
    private InboxMessageActivity self = this;
    private JSONObject messageObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox_message);
        applicationParent = (ApplicationParent) getApplicationContext();
        try {
            messageObject = new JSONObject(getIntent().getStringExtra("message_json_string"));
            markAsRead(messageObject);
            showMessage(messageObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showMessage(JSONObject messageObject) throws JSONException {
        TextView emailFromTextView = findViewById(R.id.email_code_from);
        TextView datetimeSentFromTextView = findViewById(R.id.datetime_sent_from);
        TextView messageBodyFromTextView = findViewById(R.id.message_body_from);

        emailFromTextView.setText(
                "Sent from " + messageObject.getString("sender_email"));

        datetimeSentFromTextView.setText(applicationParent.formatTimeFromServer(
                messageObject.getString("time_sent")));

        messageBodyFromTextView.setText(
                messageObject.getString("message_text"));
    }

    private void markAsRead(final JSONObject messageObject) throws JSONException {
        RequestParams markMessageReadParams = new RequestParams();
        boolean alreadyRead = messageObject.getBoolean("read");
        if (!alreadyRead) {
            markMessageReadParams.put("message_id", messageObject.getString("id"));
            markMessageReadParams.put("read", "true");
            applicationParent.getHttpClient().post(applicationParent.getUpdateMessageReadUrl(), markMessageReadParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (response.has("successful") && response.getBoolean("successful")) {
                            applicationParent.displayToast("Message marked as read", self);
                            setMessageJsonMarkedAsRead(messageObject.getString("id"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    applicationParent.log("[+] onfailure response from server");
                    applicationParent.log(errorResponse.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    applicationParent.log("[+] " + responseString);
                }
            });
        }
    }

    private void setMessageJsonMarkedAsRead(String id) throws JSONException {
        JSONObject userData = applicationParent.getInboxMessages();
        if (userData != null) {
            JSONArray messages = userData.getJSONArray("messages");
            for (int i = 0; i < messages.length(); i++) {
                JSONObject currentMessage = messages.getJSONObject(i);
                if (currentMessage.getString("id").equals(id))
                    currentMessage.put("read", true);
            }

        } else {
            applicationParent.displayToast("User data is showing up as null. Needs fix", self);
        }
    }

    public void goReply(View view) {
        Intent replyIntent = new Intent(this, ReplyActivity.class);
        replyIntent.putExtra("message_json_string", messageObject.toString());
        startActivity(replyIntent);
    }
}
