package com.example.andeca1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EditEventFragment extends Fragment {
    private EditText editTitle,editStartDate,editEndDate,editBudget;
    DbHelper db;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);
        editTitle = view.findViewById(R.id.editEventTitle);
        editBudget = view.findViewById(R.id.editBudget);
        editStartDate = view.findViewById(R.id.editStartDate);
        editEndDate = view.findViewById(R.id.editEndDate);
        Button btnSaveEvent = view.findViewById(R.id.btnSaveEvent);
        Button btnAddSubEvent = view.findViewById(R.id.btnEditAddSubEvent);

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
        editStartDate.setOnClickListener(v -> showMaterialDatePicker(editStartDate));

        editEndDate.setOnClickListener(v -> showMaterialDatePicker(editEndDate));

        Integer finalEventId = eventId;
        btnSaveEvent.setOnClickListener(v -> {
            String eventTitle = editTitle.getText().toString().trim();
            String budget = editBudget.getText().toString().trim();
            String startDateStr = editStartDate.getText().toString().trim();
            String endDateStr = editEndDate.getText().toString().trim();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date finalStartDate, finalEndDate;
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

            if (eventTitle.isEmpty()) {
                editTitle.setError("Please enter an event title");
                return;
            }

            if (finalEndDate.before(finalStartDate)) {
                Toast.makeText(getContext(), "Event end date shouldn't be before the start date", Toast.LENGTH_LONG).show();
                return;
            }

            db.updateEvent(finalEventId, eventTitle, startDateStr, endDateStr, budgetValue);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, new EventFragment());
            transaction.commit();
        });


        btnAddSubEvent.setOnClickListener(v -> {
                String eventTitle= editTitle.getText().toString().trim();
                String budget = editBudget.getText().toString().trim();
                String startDateStr = editStartDate.getText().toString().trim();
                String endDateStr = editEndDate.getText().toString().trim();

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
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

                if(finalEndDate.before(finalStartDate)){
                    Toast.makeText(getContext(),"Event end date shouldn't before start date",Toast.LENGTH_LONG).show();
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
