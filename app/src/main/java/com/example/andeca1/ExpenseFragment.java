package com.example.andeca1;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
    private Spinner spinnerCategory,spinnerEvent;
    private String category;
    private String event;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);
        editTextDate = view.findViewById(R.id.dateEditText);
        editAmount = view.findViewById(R.id.spentEditText);
        editNote = view.findViewById(R.id.descEditText);
        Button receiptButton = view.findViewById(R.id.btnReceipt);
        editTextDate = (EditText) view.findViewById(R.id.dateEditText);
        editAmount = (EditText) view.findViewById(R.id.spentEditText);
        editNote = (EditText) view.findViewById(R.id.descEditText);

        Button saveButton = view.findViewById(R.id.btnSaveSubEvent);
        editTextDate.setOnClickListener(v -> showMaterialDatePicker());

        saveButton.setOnClickListener(view1 -> saveButtonOnClickListener());

        receiptButton.setOnClickListener(view12 -> showCamera());

        setupCategorySpinner(view);
        setUpEventSpinner(view);
        return view;
    }

    public void showCamera(){
        // Create an intent to open the camera or gallery
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");

        // Create a chooser intent to let the user select between camera and gallery
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
        chooser.putExtra(Intent.EXTRA_TITLE, "Choose an app to proceed");

        // Create a list for the camera intent and add it to the chooser
        Intent[] intentArray = { cameraIntent };
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

        // Start the activity with the chooser
        mGetContent.launch(chooser);
    }

    // Register activity result callbacks
    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        // Handle the image uri from gallery
                        Uri selectedImageUri = data.getData();
                        openReceiptFragmentWithImage(selectedImageUri);
                    } else if (data != null && data.getExtras() != null) {
                        // Handle the image from camera
                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        openReceiptFragmentWithImage(imageBitmap);
                    }
                }
            });

    // Method to open the fragment and pass the image
    private void openReceiptFragmentWithImage(Uri imageUri) {
        // Create a new instance of your fragment
        ReceiptFragment receiptFragment = ReceiptFragment.newInstance(imageUri.toString());
        // Begin a fragment transaction, replace the container with your fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.content_frame, receiptFragment) // Use the correct container ID
                .commit();
    }

    private void openReceiptFragmentWithImage(Bitmap imageBitmap) {
        // Create a new instance of your fragment
        ReceiptFragment receiptFragment = ReceiptFragment.newInstance(imageBitmap);
        // Begin a fragment transaction, replace the container with your fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.content_frame, receiptFragment)
                .commit();
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setSelection(0);
        spinnerCategory.setOnItemSelectedListener(new CategorySelectedListener());
    }

    private void setUpEventSpinner(View view){
        spinnerEvent = view.findViewById(R.id.eventSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
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
        String amount = editAmount.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String note = editNote.getText().toString().trim();

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
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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
        expense.put("amount", amount);
        expense.put("date", date);
        expense.put("category",category);
        expense.put("note", note);
        expense.put("event",event);

        Log.d("SaveExpense", "Sending data to Firestore: " + expense);
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
