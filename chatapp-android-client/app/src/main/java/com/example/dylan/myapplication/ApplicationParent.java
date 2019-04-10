package com.example.dylan.myapplication;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import com.loopj.android.http.*;

import javax.net.ssl.HttpsURLConnection;

import cz.msebera.android.httpclient.Header;

/**
 * Created by dylan on 1/7/2018.
 */

public class ApplicationParent extends Application {
    private String baseUrl;
    private String conversationUrl;
    private String contactHandlerUrl;
    private String logoutUrl;
    private String predefinedMessagesUrl;
    private boolean stayLoggedIn;
    private boolean receiveSms;
    private boolean tcChecked;
    private JSONObject inboxMessagesData;
    private String loginUrl;
    private String username;
    private ConnectivityManager connectivityManager;
    private String emailPostUrl;
    private AsyncHttpClient httpClient;
    private Context context;
    private boolean loggedInState = false;
    private ApplicationParent self;
    private String updateSmsEnabledUrl;
    private String updateMessageReadUrl;
    public SimpleDateFormat sdf;
    public Calendar calendar;

    public String getUpdateMessageReadUrl() {
        return updateMessageReadUrl;
    }
    public String getLoginUrl() {
        return loginUrl;
    }
    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }
    public String getPredefinedMessagesUrl() { return this.predefinedMessagesUrl; }
    public void setPredefinedMessagesUrl(String url) {this.predefinedMessagesUrl = url;}
    public String getEmailPostUrl() {
        return emailPostUrl;
    }
    public void setEmailPostUrl(String emailPostUrl) {
        this.emailPostUrl = emailPostUrl;
    }
    public AsyncHttpClient getHttpClient() {
        return httpClient;
    }
    public void setHttpClient(AsyncHttpClient httpClient) {
        this.httpClient = httpClient;
    }
    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
    public boolean getLoggedInState() {return loggedInState;}
    public void setLoggedInState(boolean status) {loggedInState = status;}
    public void setInboxMessages(JSONObject messages) {this.inboxMessagesData = messages;}
    public JSONObject getInboxMessages() {return this.inboxMessagesData;}
    public String getConversationUrl() {
        return conversationUrl;
    }
    public void setStayLoggedIn(boolean stayLoggedIn) {
        this.stayLoggedIn = stayLoggedIn;
    }
    public boolean getStayLoggedIn() {
        return this.stayLoggedIn;
    }
    public String getLogoutUrl() {
        return logoutUrl;
    }
    public boolean getReceiveSms() {
        return receiveSms;
    }
    public void setReceiveSms(boolean doReceiveSms) {
        this.receiveSms = doReceiveSms;
    }
    public void setTcChecked(boolean checked) {
        this.tcChecked = checked;
    }
    public boolean getTcChecked() {
        return this.tcChecked;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        httpClient = new AsyncHttpClient();
        PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        httpClient.setCookieStore(myCookieStore);
        sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        calendar = Calendar.getInstance();
        self = this;
        if (baseUrl == null) {
            setUrlsForProduction();
        }
    }

    public void setUrlsForDevelopment() {
        baseUrl = "http://192.168.4.12:8080";
        conversationUrl = baseUrl + "/get_conversations";
        loginUrl = baseUrl + "/login";
        logoutUrl = baseUrl + "/logout";
        emailPostUrl = baseUrl + "/email_message";
    }

    public void setUrlsForProduction() {
        baseUrl = "https://chatappbackend.appspot.com";
        conversationUrl = baseUrl + "/get_conversations";
        loginUrl = baseUrl + "/login";
        logoutUrl = baseUrl + "/logout";
        emailPostUrl = baseUrl + "/email_message";
        contactHandlerUrl = baseUrl + "/contact_handler";
        predefinedMessagesUrl = baseUrl + "/predefined_messages";
        updateSmsEnabledUrl = baseUrl + "/update_sms_enabled";
        updateMessageReadUrl = baseUrl + "/update_message_read";
    }

    public boolean isConnectedToInternet(){
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null){
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null){
                for (int i = 0; i < info.length; i++){
                    if (info[i].getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void displayToast(String message, Context context) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }

    public void log(String message) {
        Log.i("Application Parent [++]", message);
    }

    public void addEmail(final String emailTo) {
        RequestParams newContactParams = new RequestParams();
        newContactParams.put("option_type", "add_contact");
        newContactParams.put("new_contact_email", emailTo);

        getHttpClient().post(contactHandlerUrl, newContactParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("successful") && response.getBoolean("successful")) {
                        displayToast("added email", self);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                log("[+] onfailure response from server");
                log(errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                log("[+] " + responseString);
            }
        });
    }

    public void addCode(final String codeTo) {
        RequestParams newContactParams = new RequestParams();
        newContactParams.put("option_type", "add_code");
        newContactParams.put("new_write_code", codeTo);

        getHttpClient().post(contactHandlerUrl, newContactParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("successful") && response.getBoolean("successful")) {
                        displayToast("added code", self);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                log("[+] onfailure response from server");
                log(errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                log("Failed request");
                log("[+] " + responseString);
            }
        });
    }

    public void addEmailAndCode(final String emailTo, final String codeTo) {
        RequestParams newContactParams = new RequestParams();
        newContactParams.put("option_type", "add_contact_add_code");
        newContactParams.put("new_write_code", codeTo);
        newContactParams.put("new_contact_email", emailTo);

        getHttpClient().post(contactHandlerUrl, newContactParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.has("successful") && response.getBoolean("successful")) {
                        displayToast("added code and email", self);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                log("[+] onfailure response from server");
                log(errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                log("Failed request");
                log("[+] " + responseString);
            }
        });
    }

    public String formatTimeFromServer(String miliTimeStamp) {
        calendar.setTimeInMillis(Long.parseLong(miliTimeStamp));
        return sdf.format(calendar.getTime());
    }

    public String getUpdateSmsEnabledUrl() {
        return updateSmsEnabledUrl;
    }

    public void setTextColor(TextView tv, boolean read) {
        if (read) {
            tv.setTextColor(getResources().getColor(R.color.colorInboxMessageRead));
        }
        else {
            tv.setTextColor(getResources().getColor(R.color.colorInboxMessageUnread));
            tv.setTypeface(null, Typeface.BOLD);
        }
    }
}
