package com.pass.gosafely.gosafelypass;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity {
    // Progress Dialog Object
    ProgressDialog prgDialog;
    // Error Msg TextView Object
    TextView errorMsg;
    // Email Edit View Object
    EditText emailET;
    // Password Edit View Object
    EditText pwdET;

    GoSafelyApplication app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        app = (GoSafelyApplication) this.getApplication();
        // Find Error Msg Text View control by ID
        errorMsg = (TextView) findViewById(R.id.login_error);
        // Find Email Edit View control by ID
        emailET = (EditText) findViewById(R.id.loginEmail);
        // Find Password Edit View control by ID
        pwdET = (EditText) findViewById(R.id.loginPassword);
    }

    private class CallAPI extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params){
            String host = params[0]; // URL to call
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpHost target = new HttpHost(host, 443, "https");
            String result = "";
            // specify the get request
            HttpPost postRequest = new HttpPost("/api/user/login");
            List nameValuePairs = new ArrayList(2);
            nameValuePairs.add(new BasicNameValuePair("username", params[1]));
            nameValuePairs.add(new BasicNameValuePair("password", params[2]));
            HttpResponse httpResponse = null;
            try {
                postRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                httpResponse = httpclient.execute(target, postRequest);
                HttpEntity entity = httpResponse.getEntity();
                result = EntityUtils.toString(entity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String results) {
            if (results != null) {
                try {
                    JSONObject obj = new JSONObject(results);
                    if ((boolean)obj.get("success")){
                        app.setToken(obj.get("token").toString());
                        app.setUserId(obj.get("user_id").toString());
                        navigatetoMainActivity();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void loginUser(View view) {
        // Get Email Edit View Value
        String email = emailET.getText().toString();
        // Get Password Edit View Value
        String password = pwdET.getText().toString();
        // When Email Edit View and Password Edit View have values other than Null
        if (Utility.isNotNull(email) && Utility.isNotNull(password)) {
            // When Email entered is Valid
            if (Utility.validate(email)) {
                new CallAPI().execute("gosafely.com", email, password);
            }
        }
    }
    /**
     * Method which navigates from Login Activity to Home Activity
     */
    public void navigatetoMainActivity(){
        Intent homeIntent = new Intent(getApplicationContext(),MainActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}



