package com.example.andeca1;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import android.content.Intent;
import android.text.Html;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Thread splashThread = new Thread(() -> {
            try {
                Thread.sleep(5000);
            }  catch(InterruptedException e) {
                e.printStackTrace();
            } finally
            {
                Intent intent = new Intent(SplashScreen.this, BaseActivity.class);
                startActivity(intent);
                finish();
            }

        });
        splashThread.start();
    }

}