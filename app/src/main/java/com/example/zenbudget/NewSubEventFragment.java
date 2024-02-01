package com.example.zenbudget;

import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.zenbudget.classes.DateRange;
import com.example.zenbudget.classes.SubEvent;
import com.example.zenbudget.utils.FirestoreUtils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewSubEventFragment extends Fragment {
    private EditText editTitle, editBudget;
    private TextView selectedDate, startTime, endTime, pageTitle;
    private Date selectedCalendarDate;
    private final Calendar calendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private MaterialCalendarView calendarView;
    private Button saveButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_sub_event, container, false);
        initializeViews(view);
        setupCalendarView();
        setupTimePickers();

        String txtStartDate, txtEndDate,eventId,subEventId;
        Date startDate = null, endDate = null;
        if (getArguments() != null) {
            txtStartDate = getArguments().getString("startDate");
            txtEndDate = getArguments().getString("endDate");
            eventId = getArguments().getString("eventId");
            subEventId = getArguments().getString("subEventId");
            try {
                assert txtStartDate != null;
                startDate = formatter.parse(txtStartDate);
                assert txtEndDate != null;
                endDate = formatter.parse(txtEndDate);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            calendar.setTime(startDate);
            CalendarDay startCalendarDay = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            calendar.setTime(endDate);
            CalendarDay endCalendarDay = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            calendarView.setSelectedDate(startCalendarDay);
            DateRange dateRange = new DateRange(startCalendarDay, endCalendarDay);
            calendarView.addDecorator(new EventDecorator(dateRange));

            if (subEventId!=null && eventId!=null) {
                pageTitle.setText(R.string.edit_sub_event);
                FirestoreUtils.getSubEventById(eventId, subEventId, new FirestoreUtils.FirestoreCallback<SubEvent>() {
                    @Override
                    public void onSuccess(SubEvent subEvent) {
                        if (subEvent != null) {
                            editTitle.setText(subEvent.getSubEvent_title());
                            editBudget.setText(String.valueOf(subEvent.getSubEvent_budget()));
                            startTime.setText(subEvent.getStart_time());
                            endTime.setText(subEvent.getEnd_time());
                            try {
                                selectedCalendarDate = formatter.parse(subEvent.getSubEvent_date());
                                calendar.setTime(selectedCalendarDate);
                                CalendarDay selectedDateDB = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                                calendarView.setSelectedDate(selectedDateDB);
                                selectedDate.setText(dateFormat.format(selectedCalendarDate));
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(getContext(),"fail to get sub event",Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            eventId = null;
            subEventId = null;
        }

        Date finalStartDate = startDate;
        Date finalEndDate = endDate;

        saveButton.setOnClickListener(view1 -> {
            String title = editTitle.getText().toString().trim();
            String budget = editBudget.getText().toString().trim();
            String startTimeStr = startTime.getText().toString().trim();
            String endTimeStr = endTime.getText().toString().trim();

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

            if (selectedCalendarDate == null) {
                Log.d("checkData", "it is null");
                Toast.makeText(getContext(), "Please select date between start date and end date of the event", Toast.LENGTH_LONG).show();
                return;
            }

            if (selectedCalendarDate.before(finalStartDate) || selectedCalendarDate.after(finalEndDate)) {
                Toast.makeText(getContext(), "Please select date between start date and end date of the event", Toast.LENGTH_LONG).show();
                return;
            }
            if (title.isEmpty()) {
                editTitle.setError("Please enter a event title");
                return;
            }

            try {
                Date startTimeDate = timeFormat.parse(startTimeStr);
                Date endTimeDate = timeFormat.parse(endTimeStr);
                if (startTimeDate != null && endTimeDate != null) {
                    if (endTimeDate.before(startTimeDate)) {
                        Toast.makeText(getContext(), "End time must be after start time", Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter valid start and end times", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Invalid time format", Toast.LENGTH_LONG).show();
                return;
            }


            SimpleDateFormat dateFormatForData = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String subEventDateStr = dateFormatForData.format(selectedCalendarDate);
            if (subEventId != null && eventId != null) {
                FirestoreUtils.updateSubEvent(eventId, subEventId, title, subEventDateStr, startTimeStr, endTimeStr, budgetValue, new FirestoreUtils.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                        transaction.replace(R.id.content_frame,new EventFragment());
                        transaction.commit();
                    }
                    @Override
                    public void onError(Exception e) {
                        // Handle errors if any
                    }
                });
            } else {
                FirestoreUtils.createSubEvent(title, subEventDateStr, startTimeStr, endTimeStr, budgetValue, eventId, new FirestoreUtils.FirestoreCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        editTitle.setText("");
                        editBudget.setText("");
                        startTime.setText("");
                        endTime.setText("");
                    }

                    @Override
                    public void onError(Exception e) {
                        // Handle the error if sub-event creation fails
                        Toast.makeText(getContext(),"fail to create sub event",Toast.LENGTH_LONG).show();
                    }
                });

            }
        });
        return view;


    }
    private void initializeViews(View view) {
        startTime = view.findViewById(R.id.editStartTime);
        endTime = view.findViewById(R.id.editEndTime);
        saveButton = view.findViewById(R.id.btnSaveSubEvent);
        editTitle = view.findViewById(R.id.editSubEventTitle);
        editBudget = view.findViewById(R.id.editSubBudget);
        selectedDate = view.findViewById(R.id.selected_date);
        calendarView = view.findViewById(R.id.calendarView);
        ImageButton closeButton = view.findViewById(R.id.exit_selection_mode);
        pageTitle = view.findViewById(R.id.toolbar_title);
        closeButton.setOnClickListener(v -> closeFragment());
    }

    private void setupCalendarView() {
        resetCalendarToCurrentDate();
        selectedDate.setText(dateFormat.format(calendar.getTime()));
        selectedCalendarDate = calendar.getTime();
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            calendar.set(date.getYear(), date.getMonth() - 1, date.getDay());
            selectedCalendarDate = calendar.getTime();
            widget.invalidateDecorators();
            selectedDate.setText(dateFormat.format(calendar.getTime()));
        });
    }

    private void resetCalendarToCurrentDate() {
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setupTimePickers() {
        setupTimePicker(startTime);
        setupTimePicker(endTime);
    }

    private void setupTimePicker(TextView timeView) {
        timeView.setOnClickListener(view -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getActivity(),
                    R.style.CustomMaterialDatePickerTheme,
                    (view1, selectedHour, selectedMinute) -> updateTimeView(timeView, selectedHour, selectedMinute),
                    hour,
                    minute,
                    false
            );
            timePickerDialog.show();
        });
    }

    private void updateTimeView(TextView timeView, int hour, int minute) {
        Calendar tempCalendar = Calendar.getInstance();
        tempCalendar.set(Calendar.HOUR_OF_DAY, hour);
        String amPm = tempCalendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
        if (amPm.equals("PM")) {
            hour -= 12;
        }
        String timeString = String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm);
        timeView.setText(timeString);
    }

    private void closeFragment() {
        getParentFragmentManager().beginTransaction().replace(R.id.content_frame, new EventFragment()).commit();
    }
}
