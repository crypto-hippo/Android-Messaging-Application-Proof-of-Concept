package com.example.dylan.myapplication;

import android.content.Intent;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InboxActivity extends AppCompatActivity {

    private ApplicationParent applicationParent;
    private InboxActivity self = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        applicationParent = (ApplicationParent) getApplicationContext();
        populateInbox();
        setMessageBoxHeight();
    }

    public void populateInbox() {
        int messageIndex;
        JSONObject userData = applicationParent.getInboxMessages();
        if (userData != null) {
            try {
                JSONArray inboxMessages = userData.getJSONArray("messages");
                setInboxSubtitle(inboxMessages.length());
                for (messageIndex = 0; messageIndex < inboxMessages.length(); messageIndex++) {
                    addMessageToInbox(inboxMessages.getJSONObject(messageIndex));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMessageToInbox(JSONObject messageObject) throws JSONException {
        View v = getLayoutInflater().inflate(R.layout.inbox_message_view, null);
        TextView tv = v.findViewById(R.id.dynamic_inbox_message);
        tv.setText(applicationParent.formatTimeFromServer(messageObject.getString("time_sent")) + " " + messageObject.getString("message_text"));
        setMessageOnClickListener(tv, messageObject);
        applicationParent.setTextColor(tv, messageObject.getBoolean("read"));
        ViewGroup messageLayout = findViewById(R.id.message_linear_layout_inbox);
        messageLayout.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void setMessageOnClickListener(TextView tv, final JSONObject messageObject) {
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inboxMessageActivity = new Intent(self, InboxMessageActivity.class);
                inboxMessageActivity.putExtra("message_json_string", messageObject.toString());
                startActivity(inboxMessageActivity);
            }
        });
    }

    private void setInboxSubtitle(int numMessages) {
        TextView inboxSubtitle = findViewById(R.id.inbox_subtitle);
        inboxSubtitle.setText("Inbox (" + numMessages + ")");
    }

    private void setMessageBoxHeight() {
        int inboxHeight = this.getResources().getDisplayMetrics().heightPixels;
        NestedScrollView inboxLayout = findViewById(R.id.inbox_message_scrollview);
        ViewGroup.LayoutParams params = inboxLayout.getLayoutParams();
        params.height = inboxHeight / 3;
        inboxLayout.setLayoutParams(params);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ViewGroup currentMessageView = findViewById(R.id.message_linear_layout_inbox);
        currentMessageView.removeAllViews();
        populateInbox();
    }
}
