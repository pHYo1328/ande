package com.example.andeca1;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import android.content.Intent;
import android.text.Html;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

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