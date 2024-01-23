package com.example.andeca1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {
    FirebaseAuth mAuth;
    TextView txtEmail;
    Button btnLogout;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        Log.d("ProfileFragment", "onCreateView: " + mAuth.getCurrentUser().getUid());

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        btnLogout = view.findViewById(R.id.logoutButton);
        txtEmail = view.findViewById(R.id.profileEmail);

        txtEmail.setText(mAuth.getCurrentUser().getEmail());
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            getActivity().finish();
            Intent i = new Intent(getActivity(), Authentication.class);
            startActivity(i);
        });
    }
}
