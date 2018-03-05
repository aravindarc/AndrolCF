package com.example.aravinda.androlcf;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {
    private static final String myPreferences = "myUser";
    private static final String[] storedValuesIds = {"userId", "userName", "password"};
    SharedPreferences sharedPreferences;
    boolean startLoginActivity = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);

        for(int i=0; i<3; i++)
            if(sharedPreferences.getString(storedValuesIds[i], "") == "") {
                startLoginActivity = true;
                break;
            }

        if(startLoginActivity) {
            finish();
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        } else {
            finish();
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }
    }
}
