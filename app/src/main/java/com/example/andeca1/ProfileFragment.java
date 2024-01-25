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

import com.example.andeca1.classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    TextView txtEmail, txtFirstName, txtDateJoined;
    Button btnLogout;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("ProfileFragment", "onCreateView: " + mAuth.getCurrentUser().getUid());

        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        btnLogout = view.findViewById(R.id.logoutButton);

        txtFirstName = view.findViewById(R.id.txtProfileFirstName);
        txtEmail = view.findViewById(R.id.profileEmail);
        txtDateJoined = view.findViewById(R.id.txtDateJoined);

        FirebaseUser currentUser = mAuth.getCurrentUser();

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    txtFirstName.setText(user.getFirst_name());
                    txtEmail.setText(currentUser.getEmail());
                });



        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            getActivity().finish();
            Intent i = new Intent(getActivity(), Authentication.class);
            startActivity(i);
        });
    }
}
