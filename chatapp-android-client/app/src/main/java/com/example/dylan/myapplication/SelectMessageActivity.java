package com.example.dylan.myapplication;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import cz.msebera.android.httpclient.Header;

public class SelectMessageActivity extends AppCompatActivity {

    private ArrayList<String> predefinedMessages = new ArrayList<>();
    private String emailTo;
    private String codeTo;
    private String messageToCompose;
    private TextView lastClicked;
    private ApplicationParent applicationParent;
    private NestedScrollView scrollView;
    private LinearLayout messageLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_message);
        applicationParent = (ApplicationParent) getApplicationContext();
        scrollView = findViewById(R.id.defined_message_scrollview);
        messageLinearLayout = findViewById(R.id.message_linear_layout);
        emailTo = getIntent().getStringExtra("emailTo");
        codeTo = getIntent().getStringExtra("codeTo");
        setMessageBoxHeight();
        populateDefinedMessages();
    }

    public void populateScrollView() {
        int messageIndex;
        for (messageIndex = 0; messageIndex < predefinedMessages.size(); messageIndex++) {
            View v = getLayoutInflater().inflate(R.layout.message_view, null);
            TextView tv = v.findViewById(R.id.dynamic_message);
            setMessageOnClickListener(tv);
            tv.setText(predefinedMessages.get(messageIndex));
            ViewGroup messageLayout = findViewById(R.id.message_linear_layout);
            messageLayout.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    private void setMessageOnClickListener(final TextView tv) {
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastClicked != null && !lastClicked.getText().toString().equals(tv.getText().toString())) {
                    lastClicked.setTextColor(getResources().getColor(R.color.colorDefinedMessageLink));
                    lastClicked.setTypeface(Typeface.DEFAULT);
                }
                lastClicked = tv;
                messageToCompose = tv.getText().toString();
                tv.setTextColor(getResources().getColor(R.color.colorDefinedMessageLinkWhenClicked));
                tv.setTypeface(null, Typeface.BOLD);
            }
        });
    }

    private void populateDefinedMessages() {
        applicationParent.getHttpClient().get(applicationParent.getPredefinedMessagesUrl(), null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray messages = response.getJSONArray("defined_messages");
                    int messageCount = messages.length();
                    int messageIndex;
                    for (messageIndex = 0; messageIndex < messageCount; messageIndex++) {
                        JSONObject currentMessageObject = messages.getJSONObject(messageIndex);
                        String currentMessage = currentMessageObject.getString("message");
                        predefinedMessages.add(currentMessage);
                    }
                    populateScrollView();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    private void setMessageBoxHeight() {
        int inboxHeight = this.getResources().getDisplayMetrics().heightPixels;
        NestedScrollView inboxLayout = findViewById(R.id.defined_message_scrollview);
        ViewGroup.LayoutParams params = inboxLayout.getLayoutParams();
        params.height = inboxHeight / 3;
        inboxLayout.setLayoutParams(params);
    }

    public void sendMessage(View view) {
        if (messageToCompose == null) {
            applicationParent.displayToast("Please select a message", this);
        } else {

            RequestParams emailParams = createEmailParams();
            applicationParent.getHttpClient().post(applicationParent.getEmailPostUrl(), emailParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    applicationParent.log(response.toString());
                    try {
                        if (response.has("message_sent") && response.getBoolean("message_sent")) {
                            loadMessageSentScreen();
                        } else {
                            applicationParent.displayToast("error. logging data", getApplicationContext());
                            applicationParent.log("emailTo: " + emailTo + " code: " + codeTo + " message " + messageToCompose);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    applicationParent.log("[+] error " + responseString);
                }

            });
        }
    }
    private RequestParams createEmailParams() {
        RequestParams emailParams = new RequestParams();
        emailParams.put("emailTo", emailTo);
        emailParams.put("code", codeTo);
        emailParams.put("message", messageToCompose);
        return emailParams;
    }

    private void loadMessageSentScreen() {
        Intent messageScreenIntent = new Intent(this, MessageSentActivity.class);
        startActivity(messageScreenIntent);
    }
}
