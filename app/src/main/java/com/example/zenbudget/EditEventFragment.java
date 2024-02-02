package com.example.zenbudget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.zenbudget.classes.Event;
import com.example.zenbudget.utils.FirestoreUtils;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EditEventFragment extends Fragment {
    private EditText editTitle,editStartDate,editEndDate,editBudget;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);
        editTitle = view.findViewById(R.id.editEventTitle);
        editBudget = view.findViewById(R.id.editBudget);
        editStartDate = view.findViewById(R.id.editStartDate);
        editEndDate = view.findViewById(R.id.editEndDate);
        Button btnSaveEvent = view.findViewById(R.id.btnSaveEvent);
        Button btnAddSubEvent = view.findViewById(R.id.btnEditAddSubEvent);
        ImageButton closeButton = view.findViewById(R.id.exit_selection_mode);

        String eventId=null;
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            FirestoreUtils.getEventByID(eventId, new FirestoreUtils.FirestoreCallback<Event>() {
                @Override
                public void onSuccess(Event event) {
                    if (event != null) {
                        editTitle.setText(event.getEventName());
                        editBudget.setText(String.valueOf(event.getBudget()));
                        editStartDate.setText(event.getStartDate());
                        editEndDate.setText(event.getEndDate());
                    } else {
                        Toast.makeText(requireContext(),"event is no longer available",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    // Handle errors
                }
            });

        }
        editStartDate.setOnClickListener(v -> showMaterialDatePicker(editStartDate));

        editEndDate.setOnClickListener(v -> showMaterialDatePicker(editEndDate));

        String finalEventId = eventId;
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

            assert finalEndDate != null;
            if (finalEndDate.before(finalStartDate)) {
                Toast.makeText(getContext(), "Event end date shouldn't be before the start date", Toast.LENGTH_LONG).show();
                return;
            }

            assert currentUser != null;
            FirestoreUtils.updateEvent(finalEventId,eventTitle,startDateStr,endDateStr,budgetValue,currentUser.getUid(),new FirestoreUtils.FirestoreCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                            transaction.replace(R.id.content_frame, new EventFragment());
                            transaction.commit();
                        }
                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(getContext(), "fail to update event", Toast.LENGTH_LONG).show();
                        }
                    }
            );
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

            assert finalEndDate != null;
                if(finalEndDate.before(finalStartDate)){
                    Toast.makeText(getContext(),"Event end date shouldn't before start date",Toast.LENGTH_LONG).show();
                    return;
                }
            assert currentUser != null;
            FirestoreUtils.updateEvent(finalEventId,eventTitle,startDateStr,endDateStr,budgetValue,currentUser.getUid(),new FirestoreUtils.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Bundle bundle = new Bundle();
                        bundle.putString("eventId",finalEventId);
                        bundle.putString("startDate",startDateStr);
                        bundle.putString("endDate",endDateStr);
                        NewSubEventFragment newSubEventFragment = new NewSubEventFragment();
                        newSubEventFragment.setArguments(bundle);
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.content_frame, newSubEventFragment);
                        transaction.commit();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(), "fail to update the event", Toast.LENGTH_LONG).show();
                    }
                }
            );
        });

        closeButton.setOnClickListener(view1 -> getParentFragmentManager().beginTransaction().replace(R.id.content_frame,new EventFragment()).commit());
        return view;
    }

    public void showMaterialDatePicker(EditText editText) {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select date");
        int materialTheme = R.style.CustomMaterialDatePickerTheme;
        builder.setTheme(materialTheme);

        MaterialDatePicker<Long> materialDatePicker = builder.build();

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTimeInMillis(selection);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dateFormat.format(calendar.getTime());
            editText.setText(formattedDate);
        });
        materialDatePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }
}
