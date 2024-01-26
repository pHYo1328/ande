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

import com.example.andeca1.classes.User;
import com.example.andeca1.utils.ClearErrorTextWatcher;
import com.example.andeca1.utils.EmailValidator;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterFragment extends Fragment {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ViewPager2 viewPager;
    Button btnRegister;
    private FirebaseAuth mAuth;

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
        TextInputLayout layoutEmail = view.findViewById(R.id.layoutEmail);
        TextInputLayout layoutPassword = view.findViewById(R.id.layoutPassword);
        TextInputLayout layoutConfirmPassword = view.findViewById(R.id.layoutConfirmPassword);
        TextInputLayout layoutFirstName = view.findViewById(R.id.layoutFirstName);
        TextInputLayout layoutLastName = view.findViewById(R.id.layoutLastName);

        TextView txtEmail = view.findViewById(R.id.txtEmailRegister);
        TextView txtPassword = view.findViewById(R.id.txtPassword);
        TextView txtConfirmPassword = view.findViewById(R.id.txtConfirmPassword);
        TextView txtFirstName = view.findViewById(R.id.txtFirstName);
        TextView txtLastName = view.findViewById(R.id.txtLastName);

        txtFirstName.addTextChangedListener(new ClearErrorTextWatcher(layoutFirstName));
        txtLastName.addTextChangedListener(new ClearErrorTextWatcher(layoutLastName));
        txtEmail.addTextChangedListener(new ClearErrorTextWatcher(layoutEmail));
        txtPassword.addTextChangedListener(new ClearErrorTextWatcher(layoutPassword));
        txtConfirmPassword.addTextChangedListener(new ClearErrorTextWatcher(layoutConfirmPassword));

        btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String email = txtEmail.getText().toString();
            String password = txtPassword.getText().toString();
            String confirmPassword = txtConfirmPassword.getText().toString();
            String firstName = txtFirstName.getText().toString();
            String lastName = txtLastName.getText().toString();

            boolean hasError = false;

            if (firstName.isEmpty()) {
                layoutFirstName.setError("First name is required");
                layoutFirstName.setErrorEnabled(true);
                hasError = true;
            } else {
                layoutFirstName.setErrorEnabled(false);
            }

            if (lastName.isEmpty()) {
                layoutLastName.setError("Last name is required");
                layoutLastName.setErrorEnabled(true);
                hasError = true;
            } else {
                layoutLastName.setErrorEnabled(false);
            }

            if (!EmailValidator.isValidEmail(email)) {
                layoutEmail.setError("Please enter a valid email");
                layoutEmail.setErrorEnabled(true);
                hasError = true;
            } else {
                layoutEmail.setErrorEnabled(false);
            }

            if (password.isEmpty() || password.length() < 6) {
                layoutPassword.setError("Please make sure your password is at least 6 characters long");
                layoutPassword.setErrorEnabled(true);
                hasError = true;
            } else {
                layoutPassword.setErrorEnabled(false);
            }

            if (!confirmPassword.equals(password)) {
                layoutConfirmPassword.setError("Please make sure your passwords match");
                layoutConfirmPassword.setErrorEnabled(true);
                hasError = true;
            } else {
                layoutConfirmPassword.setErrorEnabled(false);
            }

            if (hasError) return;



            User user = new User();
            user.setFirst_name(firstName);
            user.setLast_name(lastName);
            user.setEmail(email);
            user.setProvider(EmailAuthProvider.PROVIDER_ID);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser created_user = mAuth.getCurrentUser();
                            String uid = created_user.getUid();

                            db.collection("users")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(getActivity(), "Successfully registered", Toast.LENGTH_SHORT).show();
                                        Log.d("RegisterFragment", "createUserWithEmail:success");

                                        txtFirstName.setText("");
                                        txtLastName.setText("");
                                        txtEmail.setText("");
                                        txtPassword.setText("");
                                        txtConfirmPassword.setText("");


                                        viewPager.setCurrentItem(0, true);

                                    })
                                    .addOnFailureListener(e -> Log.w("CREATE FAILURE", "Error adding document", e));


                        } else {
                            Exception e = task.getException();
                            Log.w("RegisterFragment", "createUserWithEmail:failure", e);

                            if (e instanceof FirebaseAuthUserCollisionException) {
                                layoutEmail.setError("Email already in use");
                                layoutEmail.setErrorEnabled(true);
                                Toast.makeText(getActivity(), "Failed to register", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        });
    }
}