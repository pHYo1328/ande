package com.example.zenbudget;

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

import com.example.zenbudget.classes.User;
import com.example.zenbudget.utils.ClearErrorTextWatcher;
import com.example.zenbudget.utils.EmailValidator;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginFragment extends Fragment {
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();


    private static final int RC_SIGN_IN = 69;
    private GoogleSignInClient gsc;
    private static final String TAG = "GOOGLE_SIGN_IN_TAG";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button btnLogin = view.findViewById(R.id.btnLogin);
        Button btnGoogleAuth = view.findViewById(R.id.btnGoogleAuth);

        TextView txtLoginEmail = view.findViewById(R.id.txtLoginEmail);
        TextView txtLoginPassword = view.findViewById(R.id.txtLoginPassword);

        TextInputLayout layoutLoginEmail = view.findViewById(R.id.layoutLoginEmail);
        TextInputLayout layoutLoginPassword = view.findViewById(R.id.layoutLoginPassword);

        txtLoginEmail.addTextChangedListener(new ClearErrorTextWatcher(layoutLoginEmail));
        txtLoginPassword.addTextChangedListener(new ClearErrorTextWatcher(layoutLoginPassword));


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(getActivity(), gso);

        btnGoogleAuth.setOnClickListener(v -> {
            Intent i = gsc.getSignInIntent();
            startActivityForResult(i, RC_SIGN_IN);
            Log.d(TAG, "onViewCreated: " + i.toString());
        });

        btnLogin.setOnClickListener(v -> {
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
                    .addOnCompleteListener(getActivity(), task -> {
                        if (task.isSuccessful()) {
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
                    });


        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "onActivityResult: " + account.getEmail());
                firebaseAuthWithGoogleAccount(account);
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, "onActivityResult: " + e);
            }
        }
    }

    private void firebaseAuthWithGoogleAccount(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: " + account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        db.collection("users")
                .whereEqualTo("email", account.getEmail())
                .whereNotEqualTo("provider", GoogleAuthProvider.PROVIDER_ID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    boolean exists = !documentSnapshot.isEmpty();
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: User exists: " + exists);
                    if (exists) {
                        Log.d(TAG, "firebaseAuthWithGoogleAccount: User exists");
                        Toast.makeText(getActivity(), "User with email: " + account.getEmail() + " already exists", Toast.LENGTH_LONG).show();
                    } else {
                        mAuth.signInWithCredential(credential)
                                .addOnSuccessListener(getActivity(), authResult -> {
                                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Logged in");

                                    FirebaseUser user = mAuth.getCurrentUser();

                                    User newUser = new User();
                                    newUser.setFirst_name(user.getDisplayName());
                                    newUser.setEmail(user.getEmail());
                                    newUser.setProvider(GoogleAuthProvider.PROVIDER_ID);

                                    db.collection("users")
                                            .document(user.getUid())
                                            .set(newUser)
                                            .addOnSuccessListener(documentReference -> {
                                                Log.d(TAG, "firebaseAuthWithGoogleAccount: User created");


                                                Intent baseActivity = new Intent(getActivity(), BaseActivity.class);
                                                startActivity(baseActivity);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "firebaseAuthWithGoogleAccount: Failed to create user", e);
                                                mAuth.signOut();
                                            });

                                })
                                .addOnFailureListener(getActivity(), e -> {
                                    Log.w(TAG, "firebaseAuthWithGoogleAccount: Login failed", e);
                                });
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "firebaseAuthWithGoogleAccount: Failed to check if user exists", e));


    }
}