package com.example.andeca1;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class AuthFragmentAdapter extends FragmentStateAdapter {
    public AuthFragmentAdapter(AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance in createFragment(int).
        Fragment render = new LoginFragment();
        Bundle args = new Bundle();

        if (position == 1) {
            render = new RegisterFragment();
        }

        args.putInt(LoginFragment.ARG_OBJECT, position + 1);
        render.setArguments(args);
        return render;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}