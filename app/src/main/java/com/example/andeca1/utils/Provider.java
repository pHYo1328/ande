package com.example.andeca1.utils;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

public class Provider {
    public static String Determine() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        String providerId = null;
        for (UserInfo profile : user.getProviderData()) {
            if (profile.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID)) {
                providerId = GoogleAuthProvider.PROVIDER_ID;
                break;
            } else if (profile.getProviderId().equals(EmailAuthProvider.PROVIDER_ID)) {
                providerId = EmailAuthProvider.PROVIDER_ID;
                break;
            }
        }
        return providerId;
    }
}
