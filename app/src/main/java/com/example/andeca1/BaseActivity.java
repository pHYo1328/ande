package com.example.andeca1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jakewharton.threetenabp.AndroidThreeTen;

import java.util.HashMap;

public class BaseActivity extends AppCompatActivity {
    public interface KeyboardVisibilityListener {
        void onKeyboardVisibilityChanged(boolean keyboardVisible);
    }
    private KeyboardVisibilityListener keyboardVisibilityListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidThreeTen.init(this);
        setContentView(R.layout.activity_base);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupKeyboardVisibilityListener();
        bottomNavigationView.setOnItemSelectedListener(item -> {
            HashMap<Integer, Fragment> fragmentMap = new HashMap<>();
            fragmentMap.put(R.id.navigation_expense, new ExpenseFragment());
            fragmentMap.put(R.id.navigation_event, new EventFragment());
            fragmentMap.put(R.id.navigation_chat, new ChatFragment());
            fragmentMap.put(R.id.navigation_profile, new ProfileFragment());

            //Switch isn't viable cause R.id is no longer final
            //And I don't want a long if else
            Fragment selectedFragment = fragmentMap.get(item.getItemId());
            if (selectedFragment == null) {
                selectedFragment = new HomeFragment(); // Fallback to home fragment
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, selectedFragment)
                    .commit();

            return true;
        });


        // Set default fragment
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }
    private void setupKeyboardVisibilityListener() {
        final View contentView = findViewById(android.R.id.content);
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            contentView.getWindowVisibleDisplayFrame(r);
            int screenHeight = contentView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            boolean isKeyboardVisible = keypadHeight > screenHeight * 0.15;
            if (keyboardVisibilityListener != null) {
                keyboardVisibilityListener.onKeyboardVisibilityChanged(isKeyboardVisible);
            }
        });
    }

    public void setKeyboardVisibilityListener(KeyboardVisibilityListener listener) {
        this.keyboardVisibilityListener = listener;
    }

}
