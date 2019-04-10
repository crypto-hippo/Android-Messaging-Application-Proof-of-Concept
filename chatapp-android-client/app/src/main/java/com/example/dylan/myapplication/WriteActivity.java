package com.example.dylan.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import cz.msebera.android.httpclient.Header;

public class WriteActivity extends AppCompatActivity {

    private ApplicationParent applicationParent;
    private ArrayAdapter<String> emailToArrayAdapter;
    private ArrayAdapter<String> codeToArrayAdapter;
    private List<String> myEmails = new ArrayList<>();
    private List<String> myCodes = new ArrayList<>();
    private AutoCompleteTextView emailToInput;
    private AutoCompleteTextView codeToInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        setInstanceMembers();
        loadInboxMessages();
        setButtonsWidth();
    }

    public void createArrayAdapterForCodesTo() {
        codeToArrayAdapter = new ArrayAdapter<>(
                this, R.layout.code_to_list_item, myCodes);
        codeToInput.setAdapter(codeToArrayAdapter);
    }

    public void setInstanceMembers() {
        applicationParent = (ApplicationParent) getApplicationContext();
        emailToInput = findViewById(R.id.autocomplete_emailto);
        codeToInput = findViewById(R.id.autocomplete_codeTo);
    }

    public void createArrayAdapterForEmailsTo() {
        emailToArrayAdapter = new ArrayAdapter<>(
                this, R.layout.email_to_list_item, myEmails);
        emailToInput.setAdapter(emailToArrayAdapter);
    }

    public void loadInboxMessages() {
        applicationParent.getHttpClient().get(applicationParent.getConversationUrl(), null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (response.has("messages_length") && response.has("messages") && response.has("messages_sent") && response.has("contacts")) {
                    applicationParent.setInboxMessages(response);
                    applicationParent.log(response.toString());
                    extractEmailsFromJson();
                    extractCodesFromJson();
                    createArrayAdapterForEmailsTo();
                    createArrayAdapterForCodesTo();
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
    public void extractEmailsFromJson() {
        applicationParent.log(applicationParent.getInboxMessages().toString());
        int contactObjectIndex;
        try {
            JSONArray contactEmails = applicationParent.getInboxMessages().getJSONArray("contacts");
            for (contactObjectIndex = 0; contactObjectIndex < contactEmails.length(); contactObjectIndex++) {
                JSONObject currentContact = contactEmails.getJSONObject(contactObjectIndex);
                String currentContactEmail = currentContact.getString("email");
                myEmails.add(currentContactEmail);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void extractCodesFromJson() {
        int codeObjectIndex;
        try {
            JSONArray contactCodes = applicationParent.getInboxMessages().getJSONArray("codes");
            for (codeObjectIndex = 0; codeObjectIndex < contactCodes.length(); codeObjectIndex++) {
                JSONObject currentCode = contactCodes.getJSONObject(codeObjectIndex);
                String code = currentCode.getString("write_code");
                myCodes.add(code);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void startWrite(View view) {
        String emailTo = ((EditText) findViewById(R.id.autocomplete_emailto)).getText().toString();
        String codeTo = ((EditText) findViewById(R.id.autocomplete_codeTo)).getText().toString();

        if (!myEmails.contains(emailTo) && !myCodes.contains(codeTo)) {
            applicationParent.addEmailAndCode(emailTo, codeTo);
            emailToArrayAdapter.add(emailTo);
            codeToArrayAdapter.add(codeTo);
            myEmails.add(emailTo);
            myCodes.add(codeTo);

        } else if (!myEmails.contains(emailTo)) {
            applicationParent.addEmail(emailTo);
            emailToArrayAdapter.add(emailTo);
            myEmails.add(emailTo);

        } else if (!myCodes.contains(codeTo)) {
            applicationParent.addCode(codeTo);
            codeToArrayAdapter.add(codeTo);
            myCodes.add(codeTo);
        }

        if (codeTo.length() > 0 && emailTo.length() > 0)
            loadSelectMessage(codeTo, emailTo);
        else
            applicationParent.displayToast("Email and Code are required fields", this);
    }

    public void setButtonsWidth() {
        double widthForBtns = this.getResources().getDisplayMetrics().widthPixels / 2.5;
        Button writeBtn = findViewById(R.id.btn_write);
        Button inboxBtn = findViewById(R.id.btn_inbox);
        ViewGroup.LayoutParams writeBtnParams = writeBtn.getLayoutParams();
        ViewGroup.LayoutParams inboxBtnParams = inboxBtn.getLayoutParams();
        writeBtnParams.width = (int) widthForBtns;
        inboxBtnParams.width = (int) widthForBtns;
        writeBtn.setLayoutParams(writeBtnParams);
        inboxBtn.setLayoutParams(inboxBtnParams);
    }

    private void loadSelectMessage(String codeTo, String emailTo) {
        Intent selectMessageActivity = new Intent(this, SelectMessageActivity.class);
        selectMessageActivity.putExtra("codeTo", codeTo);
        selectMessageActivity.putExtra("emailTo", emailTo);
        startActivity(selectMessageActivity);
    }

    public void gotoProfile(View v) {
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        startActivity(profileIntent);
    }
}
