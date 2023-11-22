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
        TextView textView = findViewById(R.id.titleSplash);
        String html = "<b><font color='" + String.format("#%06X", (0xFFFFFF & Color.BLACK))
                + "'><big>zen</big></font></b><b><font color='"
                + "#9835D0" + "'><big>Budget</big></font></b>";
        textView.setText(Html.fromHtml(html));
        Thread splashThread = new Thread() {

            public void run() {
                try {
                    sleep(5000);
                }  catch(InterruptedException e) {
                    e.printStackTrace();
                } finally
                {
                    Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        };
        splashThread.start();
    }

}