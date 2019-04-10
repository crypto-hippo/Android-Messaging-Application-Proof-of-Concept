package com.example.dylan.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private Toolbar toolbar;
    private ApplicationParent applicationParent;
    private ProfileActivity self = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        applicationParent = (ApplicationParent) getApplicationContext();
        setInboxHeight();
        setButtonsWidth();
        setMessageDataNums();
        initializeSmsCheckbox();
    }

    public void initializeSmsCheckbox() {
        CheckBox smsCheckbox = findViewById(R.id.check_sms);
        JSONObject inboxMessagesData = applicationParent.getInboxMessages();
        if (inboxMessagesData != null) {
            try {
                boolean smsEnabled = inboxMessagesData.getBoolean("sms_enabled");
                smsCheckbox.setChecked(smsEnabled);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void enableSms(View v) {
        final CheckBox checkSms = (CheckBox) v;
        RequestParams update_sms_params = new RequestParams();
        update_sms_params.put("sms_enabled", checkSms.isChecked());
        applicationParent.getHttpClient().post(applicationParent.getUpdateSmsEnabledUrl(), update_sms_params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("success") && response.getBoolean("success")) {
                        applicationParent.displayToast("SMS settings have been updated", self);
                        applicationParent.getInboxMessages().put("sms_enabled", checkSms.isChecked());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                applicationParent.log("error: " + responseString);
            }
        });
    }

    public void setButtonsWidth() {
        double widthForBtns = this.getResources().getDisplayMetrics().widthPixels / 2.5;
        Button homeBtn = findViewById(R.id.home_btn);
        Button signoutBtn = findViewById(R.id.signout_btn);
        ViewGroup.LayoutParams homeBtnParams = homeBtn.getLayoutParams();
        ViewGroup.LayoutParams signoutBtnParams = signoutBtn.getLayoutParams();
        homeBtnParams.width = (int) widthForBtns;
        signoutBtnParams.width = (int) widthForBtns;
        homeBtn.setLayoutParams(homeBtnParams);
        signoutBtn.setLayoutParams(signoutBtnParams);
    }

    public void setInboxHeight() {
        int inboxHeight = this.getResources().getDisplayMetrics().heightPixels;
        RelativeLayout inboxLayout = findViewById(R.id.inbox_relative_layout);
        ViewGroup.LayoutParams params = inboxLayout.getLayoutParams();
        params.height = inboxHeight / 3;
        inboxLayout.setLayoutParams(params);
    }

    public void signout(View v) {
        applicationParent.getHttpClient().get(applicationParent.getLogoutUrl(), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                applicationParent.log(response.toString());
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                applicationParent.log(errorResponse.toString());
            }
        });
    }

    public void setMessageDataNums() {
        setNumInbox();
        setNumSent();
    }

    public void setNumInbox() {
        TextView inboxView = findViewById(R.id.num_inbox);
        if (applicationParent.getInboxMessages() != null) {
            try {
                inboxView.setText(applicationParent.getInboxMessages().getJSONArray("messages").length() + "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setNumSent() {
        TextView inboxView = findViewById(R.id.num_sent);
        if (applicationParent.getInboxMessages() != null) {
            try {
                inboxView.setText(applicationParent.getInboxMessages().getJSONArray("messages_sent").length() + "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void goHome(View v) {
        Intent homeIntent = new Intent(this, WriteActivity.class);
        startActivity(homeIntent);
    }

    public void goInbox(View v) {
        applicationParent.log("inbox is not loading");
        Intent inboxIntent = new Intent(this, InboxActivity.class);
        startActivity(inboxIntent);
    }

    public void goSent(View view) {
        Intent sentIntent = new Intent(this, SentActivity.class);
        startActivity(sentIntent);
    }
}
//    private void setFloatingActionBarOnClick() {
//        FloatingActionButton fab = findViewById(R.id.new_message_fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startComposeMessageActivity();
//            }
//        });
//    }
//
//    private void startComposeMessageActivity() {
//        Intent composeMsgIntent = new Intent(this, ComposeMessageActivity.class);
//        startActivity(composeMsgIntent);
//    }
//

//
//    private void addMessageToUI(String fromEmail, String fromUsername, String subject, String body, String timeSent) {
//        applicationParent.log("[+] adding message to UI");
//        View v = getLayoutInflater().inflate(R.layout.message_view, null);
//        TextView tv = (TextView) v.findViewById(R.id.dynamic_message);
//        tv.setText("From Username: " + fromUsername + ", Subject: " + subject + ", Body: " + body + ", Time Sent: " + timeSent);
//        ViewGroup scrollview = (ViewGroup) findViewById(R.id.scrollview_linearlayout);
//        scrollview.addView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//    }
//
//    private void setupToolbar() {
//        toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setBackgroundColor(Color.BLACK);
//        toolbar.setTitleTextColor(Color.parseColor("#f5f5dc"));
//        Drawable menuOverflowIcon = toolbar.getOverflowIcon();
//        if (menuOverflowIcon != null) {
//            menuOverflowIcon = DrawableCompat.wrap(menuOverflowIcon);
//            DrawableCompat.setTint(menuOverflowIcon.mutate(), Color.parseColor("#f5f5dc"));
//            toolbar.setOverflowIcon(menuOverflowIcon);
//        }
//    }
//
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.contacts) {
//            Toast.makeText(this, "Loading Contacts", Toast.LENGTH_LONG).show();
//            return true;
//        } else if (id == R.id.conversations) {
//            Toast.makeText(this, "Loading Conversations", Toast.LENGTH_LONG).show();
//            return true;
//        } else if (id == R.id.default_messages) {
//            Toast.makeText(this, "Loading Pre Defined Messages", Toast.LENGTH_LONG).show();
//            return true;
//        } else if (id == R.id.search) {
//            Toast.makeText(this, "Loading Search", Toast.LENGTH_LONG).show();
//            return true;
//        } else if (id == R.id.logout) {
//            Toast.makeText(this, "Logging out", Toast.LENGTH_LONG).show();
//            logout();
//            return true;
//        } else {
//            Log.i(TAG, "If this line is printed out then the world is close to ending");
//        }
//
//        Log.i(TAG, "Testing Menu");
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        applicationParent.log("[+] Restarting profileactivity");
//        ViewGroup scrollviewLinearLayout = (ViewGroup) findViewById(R.id.scrollview_linearlayout);
//        scrollviewLinearLayout.removeAllViews();
//        loadConversations();
//    }
//
//
//}

