package com.example.andeca1;

import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewEventFragment extends Fragment {
    private EditText editEventTitle,editBudget;
    private Button addSubEventButton,saveButton;
    private CalendarView calendarView;
    private TextView selectedDateStr;
    private DbHelper db;
    private Date startDate;
    private Date endDate;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_event, container, false);

        addSubEventButton = view.findViewById(R.id.btnAddSubEvent);
        saveButton = view.findViewById(R.id.btnSaveEvent);
        editEventTitle = view.findViewById(R.id.editTxtEventTitle);
        editBudget = view.findViewById(R.id.editTxtBudget);
        calendarView = view.findViewById(R.id.calendarViewNewEvent);
        selectedDateStr = view.findViewById(R.id.selected_date);

        db = new DbHelper(getActivity());

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat dateFormatForDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(android.icu.util.Calendar.YEAR),calendar.get(android.icu.util.Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        selectedDateStr.setText(dateFormat.format(calendar.getTime()));
        endDate = calendar.getTime();

        String startDateStr = null;
        if (getArguments() != null) {
            startDateStr = getArguments().getString("selectedDate");
            try {
                startDate= dateFormatForDB.parse(startDateStr);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                String formattedDate = dateFormat.format(selectedDate.getTime());
                endDate = selectedDate.getTime();
                selectedDateStr.setText(formattedDate);
            }
        });
        String finalStartDateStr = startDateStr;
        addSubEventButton.setOnClickListener(new View.OnClickListener() {
            String eventTitle,budget;
            @Override
            public void onClick(View view) {
                eventTitle= editEventTitle.getText().toString().trim();
                budget = editBudget.getText().toString().trim();
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
                    editEventTitle.setError("Please enter a event title");
                    return;
                }
                if(endDate.before(startDate)){
                    Toast.makeText(getContext(),"Event end date shouldn't before start date",Toast.LENGTH_LONG);
                    return;
                }
                String endDateStr = dateFormatForDB.format(endDate.getTime());
                long eventId = db.createEvent(eventTitle, finalStartDateStr,endDateStr,budgetValue);
                Bundle bundle = new Bundle();
                bundle.putString("eventId",String.valueOf(eventId) );
                bundle.putString("startDate",finalStartDateStr);
                bundle.putString("endDate",endDateStr);
                NewSubEventFragment newSubEventFragment = new NewSubEventFragment();
                newSubEventFragment.setArguments(bundle);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, newSubEventFragment);
                transaction.commit();
            }
        });


        saveButton.setOnClickListener(new View.OnClickListener() {
            String eventTitle,budget;
            @Override
            public void onClick(View view) {
                eventTitle= editEventTitle.getText().toString().trim();
                budget = editBudget.getText().toString().trim();
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
                    editEventTitle.setError("Please enter a event title");
                    return;
                }

                if(endDate.before(startDate)){
                    Toast.makeText(getContext(),"Event end date shouldn't before start date",Toast.LENGTH_LONG);
                    return;
                }
                String endDateStr = dateFormatForDB.format(endDate.getTime());
                db.createEvent(eventTitle, finalStartDateStr,endDateStr,budgetValue);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, new EventFragment());
                transaction.commit();

            }
        });

        return view;
    }
}
