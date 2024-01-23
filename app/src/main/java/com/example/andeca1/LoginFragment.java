package com.example.andeca1;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andeca1.utils.EmailValidator;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {
    public static final String ARG_OBJECT = "object";
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button btnLogin = view.findViewById(R.id.btnLogin);

        TextView txtLoginEmail = view.findViewById(R.id.txtLoginEmail);
        TextView txtLoginPassword = view.findViewById(R.id.txtLoginPassword);

        TextInputLayout layoutLoginEmail = view.findViewById(R.id.layoutLoginEmail);
        TextInputLayout layoutLoginPassword = view.findViewById(R.id.layoutLoginPassword);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean error = false;
                String email = txtLoginEmail.getText().toString();
                String password = txtLoginPassword.getText().toString();
                Log.w("LoginFragment", "onViewCreated: " + email + " " + password);



                if (!EmailValidator.isValidEmail(email)) {
                    layoutLoginEmail.setError("Please enter a valid email address");
                    layoutLoginEmail.setErrorEnabled(true);
                    error = true;
                } else {
                    layoutLoginEmail.setErrorEnabled(false);
                }

                if (password.length() < 6) {
                    layoutLoginPassword.setError("Password must be at least 6 characters");
                    layoutLoginPassword.setErrorEnabled(true);
                    error = true;
                } else {
                    layoutLoginPassword.setErrorEnabled(false);
                }

                if (error) return;

                txtLoginEmail.setEnabled(false);
                txtLoginPassword.setEnabled(false);

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    Intent baseActivity = new Intent(getActivity(), BaseActivity.class);
                                    startActivity(baseActivity);
                                } else {
                                    Exception e = task.getException();
                                    Log.w("FirebaseAuthError", "signInWithEmail:failure", task.getException().getCause());
                                    Log.d("FirebaseAuthError", "onComplete: " + task.getException().getClass());

                                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                        layoutLoginPassword.setErrorEnabled(true);
                                        layoutLoginPassword.setError("Incorrect email or password");
                                        layoutLoginEmail.setErrorEnabled(true);
                                        layoutLoginEmail.setError("Incorrect email or password");

                                        Toast.makeText(getActivity(), "Incorrect email or password", Toast.LENGTH_LONG).show();
                                        Log.d("FirebaseAuthError", "onComplete: " + e.getMessage());
                                    }

                                    // Enable the text fields again in the case of an error
                                    txtLoginEmail.setEnabled(true);
                                    txtLoginPassword.setEnabled(true);
                                }
                            }
                        });


            }
        });
    }

}