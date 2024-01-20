package com.example.andeca1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ExpenseFragment extends Fragment {


    private EditText editAmount,editTextDate,editNote;
    private Button saveButton;
    private Spinner spinnerCategory,spinnerEvent;
    private String amount,date,note,category,event;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);
        editTextDate = (EditText) view.findViewById(R.id.dateEditText);
        editAmount = (EditText) view.findViewById(R.id.spentEditText);
        editNote = (EditText) view.findViewById(R.id.descEditText);

        saveButton = (Button) view.findViewById(R.id.btnSaveSubEvent);
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMaterialDatePicker();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveButtonOnClickListener();
            }
        });

        setupCategorySpinner(view);
        setUpEventSpinner(view);
        return view;
    }

    public void showMaterialDatePicker() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");
        int materialTheme = R.style.CustomMaterialDatePickerTheme;
        builder.setTheme(materialTheme);

        MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            // Use the selected date here
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTimeInMillis(selection);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(calendar.getTime());
            editTextDate.setText(formattedDate);
        });
        // Show the MaterialDatePicker
        materialDatePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void setupCategorySpinner(View view) {
        spinnerCategory = view.findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setSelection(0);
        spinnerCategory.setOnItemSelectedListener(new CategorySelectedListener());
    }

    private void setUpEventSpinner(View view){
        spinnerEvent = view.findViewById(R.id.eventSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.events, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEvent.setAdapter(adapter);
        spinnerEvent.setSelection(0);

        spinnerEvent.setOnItemSelectedListener(new EventSelectedListener());
    }

    private class CategorySelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(position != 0){
                category = (String) parent.getItemAtPosition(position);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Interface callback for when nothing is selected
        }
    }

    private class EventSelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(position != 0){
                event = (String) parent.getItemAtPosition(position);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Interface callback for when nothing is selected
        }
    }

    private void saveButtonOnClickListener(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        amount = editAmount.getText().toString().trim();
        date = editTextDate.getText().toString().trim();
        note = editNote.getText().toString().trim();

        double amountValue;
        try {
            amountValue = Double.parseDouble(amount);
            if (amountValue <= 0) {
                throw new NumberFormatException("Amount must be greater than zero");
            }
        } catch (NumberFormatException e) {
            editAmount.setError("Please enter a valid amount");
            return;
        }

        // Validate the date - make sure it's a valid date
        SimpleDateFormat format = new SimpleDateFormat("dd/mm/yyyy", Locale.getDefault());
        try {
            Date parsedDate = format.parse(date);
            if (parsedDate == null) {
                throw new ParseException("Date format is incorrect", 0);
            }
        } catch (ParseException e) {
            editTextDate.setError("Please enter a valid date in format yyyy-MM-dd");
            return;
        }

        // Check if category and event are not empty
        if (category== null) {
            // Show some error or make a Toast
            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        if (event==null) {
            // Show some error or make a Toast
            Toast.makeText(getContext(), "Please enter an event", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> expense = new HashMap<>();
        expense.put("amount", amount );
        expense.put("date", date);
        expense.put("category",category);
        expense.put("note",note);
        expense.put("event",event);

        Log.d("SaveExpense", "Sending data to Firestore: " + expense.toString());
        db.collection("expenses").add(expense)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId());
                    editAmount.setText("");
                    editTextDate.setText("");
                    editNote.setText("");
                    spinnerCategory.setSelection(0);
                    spinnerEvent.setSelection(0);

                    Toast.makeText(getContext(), "Expense saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding document", e);
                    Toast.makeText(getContext(), "Server error", Toast.LENGTH_SHORT).show();
                });

    }

}
