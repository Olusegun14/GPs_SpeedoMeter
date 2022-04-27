package com.ist.gpscounter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ist.onlinemovieapplication.databinding.ActivitySplashBinding;

public class SplashActivty extends AppCompatActivity {


    ActivitySplashBinding activitySplashBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activitySplashBinding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(activitySplashBinding.getRoot());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        new Handler().postDelayed(() -> {

            if (user != null) {
                //User is Logged in
                Intent homePage = new Intent(SplashActivty.this, MainActivity.class);
                startActivity(homePage);
                finish();
            } else {

                Intent registerPage = new Intent(SplashActivty.this, RegisterActivty.class);
                startActivity(registerPage);
                finish();
                //No User is Logged in
            }
        }, 4000);


    }
}
