package com.example.andeca1.utils;

import android.text.Editable;
import android.text.TextWatcher;

import com.google.android.material.textfield.TextInputLayout;

public class ClearErrorTextWatcher implements TextWatcher {
    private final TextInputLayout textInputLayout;

    public ClearErrorTextWatcher(TextInputLayout textInputLayout) {
        this.textInputLayout = textInputLayout;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable editable) {
        // Clear errors when text changes
        textInputLayout.setError(null);
        textInputLayout.setErrorEnabled(false);
    }
}
