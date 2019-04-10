package com.example.dylan.myapplication.firebase;

import android.app.Service;
import android.util.Log;
import android.widget.Toast;

import com.example.dylan.myapplication.ApplicationParent;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by dylan on 1/2/2018.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private final String TAG = "FirebaseIdService";
    private final String url = "http://chatappbackend.appspot.com/update_token";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        /** If you want to send messages to this application instance or
         * manage this apps subscriptions on the server side, send the
         * Instance ID token to app server
         */
        try {
            sendRegistrationToServer(refreshedToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendRegistrationToServer(String refreshedToken) throws JSONException {
        ((ApplicationParent) getApplicationContext()).log(refreshedToken);
    }
}
