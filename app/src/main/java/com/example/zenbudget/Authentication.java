package com.example.zenbudget;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Authentication extends AppCompatActivity {

    AuthFragmentAdapter authFragmentAdapter;
    ViewPager2 viewPager;

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication); // Set the layout file

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if  (currentUser != null)   {
            Log.d("Authentication123", "onCreate: " + mAuth.getCurrentUser().getEmail());

            Intent i = new Intent(Authentication.this, BaseActivity.class);
            startActivity(i);
            return;
        }



        authFragmentAdapter = new AuthFragmentAdapter(this);
        viewPager = findViewById(R.id.authPager);
        viewPager.setAdapter(authFragmentAdapter);

        Log.d("Authentication12345", "onCreate: " + viewPager);


        TabLayout tabLayout = findViewById(R.id.authTab);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Login");
                    } else {
                        tab.setText("Register");
                    }
                }
        ).attach();
    }
}