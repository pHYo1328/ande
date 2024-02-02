package com.example.zenbudget;

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

import com.example.zenbudget.classes.Event;
import com.example.zenbudget.utils.FirestoreUtils;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ExpenseFragment extends Fragment {

    private EditText editAmount,editTextDate,editDesc,editTitle;
    private Spinner spinnerCategory,spinnerEvent;
    private String category;
    private String event;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    public static ExpenseFragment newInstance(String amount, String description) {
        ExpenseFragment fragment = new ExpenseFragment();
        Bundle args = new Bundle();
        args.putString("amount_key", amount);
        args.putString("description_key", description);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("checkData","i m testing"+currentUser.getUid());
        View view = inflater.inflate(R.layout.fragment_expense, container, false);
        editTextDate = view.findViewById(R.id.dateEditText);
        editAmount = view.findViewById(R.id.spentEditText);
        editDesc = view.findViewById(R.id.descEditText);
        editTitle = view.findViewById(R.id.expenseTitle);
        Button receiptButton = view.findViewById(R.id.btnReceipt);

        Button saveButton = view.findViewById(R.id.btnSaveSubEvent);
        editTextDate.setOnClickListener(v -> showMaterialDatePicker());

        saveButton.setOnClickListener(view1 -> saveButtonOnClickListener());

        if (getArguments() != null) {
            String amount = getArguments().getString("amount_key");
            String description = getArguments().getString("description_key");

            editAmount.setText(amount);
            editDesc.setText(description);
        }

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

    // open the fragment and pass the image uri for google photos/gallery
    private void openReceiptFragmentWithImage(Uri imageUri) {
        // Create a new instance of your fragment
        ReceiptFragment receiptFragment = ReceiptFragment.newInstance(imageUri.toString());
        // Fragment transaction, change out fragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.content_frame, receiptFragment)
                .commit();
    }

    //Image bitmap if camera used
    private void openReceiptFragmentWithImage(Bitmap imageBitmap) {

        ReceiptFragment receiptFragment = ReceiptFragment.newInstance(imageBitmap);
        // Begin a fragment transaction, change out fragment
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

        FirestoreUtils.getAllEvents(currentUser.getUid(),new FirestoreUtils.FirestoreCallback<List<Event>>() {
            @Override
            public void onSuccess(List<Event> eventList) {

                ArrayAdapter<Event> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, eventList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerEvent.setAdapter(adapter);
                spinnerEvent.setSelection(0);
                spinnerEvent.setOnItemSelectedListener(new EventSelectedListener());
            }

            @Override
            public void onError(Exception e) {

            }
        });

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
            Event selectedEvent =(Event) parent.getItemAtPosition(position);
            event = selectedEvent.getId();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Interface callback for when nothing is selected
        }
    }

    private void saveButtonOnClickListener(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String expenseTitle = editTitle.getText().toString().trim();
        String amount = editAmount.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String desc = editDesc.getText().toString().trim();

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

        if (expenseTitle.isEmpty() ){
            editTitle.setError("Please enter expense title");
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Map<String, Object> expense = new HashMap<>();
        expense.put("title",expenseTitle);
        expense.put("amount", amount);
        expense.put("date", date);
        expense.put("category",category);
        expense.put("description",desc);
        expense.put("event",event);
        assert currentUser != null;
        expense.put("userID", currentUser.getUid());
        db.collection("expenses").add(expense)
                .addOnSuccessListener(documentReference -> {
                    editTitle.setText("");
                    editAmount.setText("");
                    editTextDate.setText("");
                    editDesc.setText("");
                    spinnerCategory.setSelection(0);
                    spinnerEvent.setSelection(0);

                    Toast.makeText(getContext(), "Expense saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Server error", Toast.LENGTH_SHORT).show());

    }

}
