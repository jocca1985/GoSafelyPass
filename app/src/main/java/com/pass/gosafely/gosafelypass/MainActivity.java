package com.pass.gosafely.gosafelypass;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends ActionBarActivity {
    GoSafelyApplication app;
    final Context context = this;

    private class CustomHttpGet extends HttpGet{
        public user user = new user();
        public class user{
            public String id = "";
        }

        public CustomHttpGet(String url, String userId){
            super(url);
            setUserId(userId);
        }


        public void setUserId(String userId){
            this.user.id = userId;
        }
    }
    private class CallAPI extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params){
            String host = "gosafely.com"; // URL to call
            DefaultHttpClient httpclient = new DefaultHttpClient();
            String result = "";
            String url = params[0];
            try {
                HttpHost target = new HttpHost(host, 443, "https");
                List<NameValuePair> nameValuePairs = new ArrayList(1);
                HttpGet getRequest;
                if(!"".equals(params[1])) {
                    nameValuePairs.add(new BasicNameValuePair("user_id", params[1]));
                    String paramString = URLEncodedUtils.format(nameValuePairs, "utf-8");
                    url += paramString;
                    getRequest = new HttpGet(url);
                } else {
                    getRequest = new CustomHttpGet(url, params[3]);
                }
                // specify the get request

                getRequest.setHeader("x-access-token", params[2]);
                HttpResponse httpResponse = null;
                Log.v("getReq", url);
                httpResponse = httpclient.execute(target, getRequest);
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
                JSONObject obj = null;
                JSONArray data = null;
                final TextView et = (TextView) findViewById(R.id.editResult);
                try {
                    obj = new JSONObject(results);
                    if ((boolean)obj.get("success")){
                        data = (JSONArray) obj.get("data");
                    }
                    Set<String> s = new HashSet<>();

                    for (int i=0;i<data.length();i++){
                        s.add(((JSONObject)(data.get(i))).get("domain").toString()+"\n");
                    }
                    for (String l:s){
                        et.append(l);
                    }
                    et.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            // custom dialog
                            new CallAPI(){
                                protected void onPostExecute(String results) {
                                    if (results != null) {
                                        JSONObject obj = null;
                                        JSONObject data = null;
                                        final Dialog dialog = new Dialog(context);
                                        dialog.setContentView(R.layout.custom);
                                        TextView text = (TextView) dialog.findViewById(R.id.text);
                                        try {
                                            obj = new JSONObject(results);
                                            if ((boolean) obj.get("success")) {
                                                data = (JSONObject) obj.get("data");

                                                text.setText(data.getJSONObject("www.facebook.com").getString("username")+"/"+data.getJSONObject("www.facebook.com").getString("password"));
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonUsername);
                                        dialogButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                            }
                                        });

                                        dialog.show();
                                    }
                                }
                            }.execute("/api/resource/sso_credential/email", "", app.getToken(), app.getUserId());


                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = ((GoSafelyApplication)this.getApplication());
        if (((GoSafelyApplication)this.getApplication()).getToken() == null)
            navigatetoLoginActivity();
        else {
            setContentView(R.layout.activity_main);
            new CallAPI().execute("/api/resource/sso_url/query?", app.getUserId(), app.getToken(),"sso_url");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method which navigates from Login Activity to Home Activity
     */
    public void navigatetoLoginActivity(){
        Intent homeIntent = new Intent(getApplicationContext(),LoginActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }


}
