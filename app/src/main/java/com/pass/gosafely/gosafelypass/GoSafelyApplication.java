package com.pass.gosafely.gosafelypass;

import android.app.Application;

/**
 * Created by Jovan on 3/17/2015.
 */
public class GoSafelyApplication extends Application {

    private String token;

    private String userId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


}
