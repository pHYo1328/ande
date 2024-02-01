package com.example.zenbudget;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.widget.ImageView;

public class SplashScreen extends AppCompatActivity {
    ImageView art;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        Thread splashThread = new Thread() {

            public void run() {
                try {
                    sleep(1500);
                }  catch(InterruptedException e) {
                    e.printStackTrace();
                } finally
                {
                    Intent intent = new Intent(SplashScreen.this, Authentication.class);
                    startActivity(intent);
                    finish();
                }

            }
        };
        splashThread.start();
    }

}