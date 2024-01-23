package com.example.andeca1;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterFragment extends Fragment {
    private FirebaseAuth mAuth;
    ViewPager2 viewPager;
    Button btnRegister;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        viewPager = getActivity().findViewById(R.id.authPager);

        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        btnRegister = view.findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            TextView txtEmail = view.findViewById(R.id.txtEmailRegister);
            TextView txtPassword = view.findViewById(R.id.txtPassword);
            TextView txtConfirmPassword = view.findViewById(R.id.txtConfirmPassword);

            String email = txtEmail.getText().toString();
            String password = txtPassword.getText().toString();
            String confirmPassword = txtConfirmPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.equals(confirmPassword)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), task -> {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(getActivity(), "Successfully registered", Toast.LENGTH_SHORT).show();
                                Log.d("RegisterFragment", "createUserWithEmail:success");

                                txtEmail.setText("");
                                txtPassword.setText("");
                                txtConfirmPassword.setText("");


                                viewPager.setCurrentItem(0, true);

                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.w("RegisterFragment", "createUserWithEmail:failure", task.getException());

                                // updateUI(null);
                            }
                        });
            }
        });
    }
}