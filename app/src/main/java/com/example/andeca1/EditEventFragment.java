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
import androidx.fragment.app.FragmentTransaction;

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

public class EditEventFragment extends Fragment {


    private EditText editTitle,editStartDate,editEndDate,editBudget;
    private Button btnAddSubEvent,btnSaveEvent;
    DbHelper db;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);
        editTitle = view.findViewById(R.id.editEventTitle);
        editBudget = view.findViewById(R.id.editBudget);
        editStartDate = view.findViewById(R.id.editStartDate);
        editEndDate = view.findViewById(R.id.editEndDate);
        btnSaveEvent = view.findViewById(R.id.btnSaveEvent);
        btnAddSubEvent = view.findViewById(R.id.btnEditAddSubEvent);

        db = new DbHelper(getContext());


        Integer eventId=null;
        if (getArguments() != null) {
            eventId = getArguments().getInt("eventId");
            Event event=db.getEventByID(eventId);
            editTitle.setText(event.getEventName());
            editBudget.setText(String.valueOf(event.getBudget()));
            editStartDate.setText(event.getStartDate());
            editEndDate.setText(event.getEndDate());
        }
        editStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMaterialDatePicker(editStartDate);
            }
        });

        editEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMaterialDatePicker(editEndDate);
            }
        });

        Integer finalEventId = eventId;
        btnSaveEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eventTitle= editTitle.getText().toString().trim();
                String budget = editBudget.getText().toString().trim();
                String startDateStr = editStartDate.getText().toString().trim();
                String endDateStr = editEndDate.getText().toString().trim();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date finalStartDate,finalEndDate;
                try {
                    finalStartDate = formatter.parse(startDateStr);
                    finalEndDate = formatter.parse(endDateStr);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                double budgetValue;
                try {
                    budgetValue = Double.parseDouble(budget);
                    if (budgetValue <= 0) {
                        throw new NumberFormatException("Amount must be greater than zero");
                    }
                } catch (NumberFormatException e) {
                    editBudget.setError("Please enter a valid amount");
                    return;
                }

                if(eventTitle == null){
                    editTitle.setError("Please enter a event title");
                    return;
                }

                if(finalEndDate.before(finalStartDate)){
                    Toast.makeText(getContext(),"Event end date shouldn't before start date",Toast.LENGTH_LONG);
                    return;
                }
                db.updateEvent(finalEventId,eventTitle,startDateStr,endDateStr,budgetValue);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, new EventFragment());
                transaction.commit();
            }
        });


        btnAddSubEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String eventTitle= editTitle.getText().toString().trim();
                String budget = editBudget.getText().toString().trim();
                String startDateStr = editStartDate.getText().toString().trim();
                String endDateStr = editEndDate.getText().toString().trim();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                Date finalStartDate,finalEndDate;
                try {
                    finalStartDate = formatter.parse(startDateStr);
                    finalEndDate = formatter.parse(endDateStr);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                double budgetValue;
                try {
                    budgetValue = Double.parseDouble(budget);
                    if (budgetValue <= 0) {
                        throw new NumberFormatException("Amount must be greater than zero");
                    }
                } catch (NumberFormatException e) {
                    editBudget.setError("Please enter a valid amount");
                    return;
                }

                if(eventTitle == null){
                    editTitle.setError("Please enter a event title");
                    return;
                }

                if(finalEndDate.before(finalStartDate)){
                    Toast.makeText(getContext(),"Event end date shouldn't before start date",Toast.LENGTH_LONG);
                    return;
                }
                db.updateEvent(finalEventId,eventTitle,startDateStr,endDateStr,budgetValue);
                Bundle bundle = new Bundle();
                bundle.putInt("eventId",(int) finalEventId);
                bundle.putString("startDate",startDateStr);
                bundle.putString("endDate",endDateStr);
                NewSubEventFragment newSubEventFragment = new NewSubEventFragment();
                newSubEventFragment.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, newSubEventFragment);
                transaction.commit();
            }
        });

        return view;
    }

    public void showMaterialDatePicker(EditText editText) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");
        int materialTheme = R.style.CustomMaterialDatePickerTheme;
        builder.setTheme(materialTheme);

        MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            // Use the selected date here
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTimeInMillis(selection);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dateFormat.format(calendar.getTime());
            editText.setText(formattedDate);
        });
        // Show the MaterialDatePicker
        materialDatePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }
}
