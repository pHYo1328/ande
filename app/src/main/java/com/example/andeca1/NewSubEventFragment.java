package com.example.andeca1;

import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewSubEventFragment extends Fragment {
    private EditText editTitle, editBudget;
    private TextView selectedDate, startTime, endTime;
    private Date selectedCalendarDate;
    private Calendar calendar = Calendar.getInstance();
    private Button saveButton;
    private DbHelper db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
        View view = inflater.inflate(R.layout.fragment_new_sub_event, container, false);
        startTime = view.findViewById(R.id.editTextStartTime);
        endTime = view.findViewById(R.id.editTextEndTime);
        saveButton = view.findViewById(R.id.btnSaveSubEvent);
        editTitle = view.findViewById(R.id.editSubEventTitle);
        editBudget = view.findViewById(R.id.editSubBudget);
        selectedDate = view.findViewById(R.id.selected_date);
        MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);

        db = new DbHelper(getContext());
        String txtStartDate, txtEndDate;
        Date startDate = null, endDate = null;
        Integer eventId = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Integer subEventId;
        if (getArguments() != null) {
            txtStartDate = getArguments().getString("startDate");
            txtEndDate = getArguments().getString("endDate");
            String temp = requireArguments().getString("eventId");
            assert temp != null;
            eventId = getArguments().getInt("eventId");
            subEventId = getArguments().getInt("subEventId");
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
            if (subEventId != 0) {
                SubEvent subEvent = db.getSubEventById(subEventId);
                editTitle.setText(subEvent.getSubEvent_title());
                editBudget.setText(String.valueOf(subEvent.getSubEvent_budget()));
                startTime.setText(subEvent.getStart_time());
                endTime.setText(subEvent.getEnd_time());
                try {
                    selectedCalendarDate = formatter.parse(subEvent.getSubEvent_date());
                    calendar.setTime(selectedCalendarDate);
                    CalendarDay selectedDateDB = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                    calendarView.setSelectedDate(selectedDateDB);

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            }
        } else {
            subEventId = null;
        }

        startTime.setOnClickListener(view15 -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Create a TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getActivity(),
                    R.style.CustomMaterialDatePickerTheme,
                    (view14, selectedHour, selectedMinute) -> {
                        Calendar calendar12 = Calendar.getInstance();
                        calendar12.set(Calendar.HOUR_OF_DAY, selectedHour);
                        String amPm;
                        if (calendar12.get(Calendar.AM_PM) == Calendar.AM) {
                            amPm = "AM";
                        } else {
                            amPm = "PM";
                            selectedHour = selectedHour - 12;
                        }
                        String timeString = String.format(Locale.getDefault(), "%02d:%02d %s", selectedHour, selectedMinute, amPm);
                        startTime.setText(timeString);
                    },
                    hour,
                    minute,
                    false
            );
            timePickerDialog.show();

        });

        endTime.setOnClickListener(view13 -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Create a TimePickerDialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getActivity(),
                    R.style.CustomMaterialDatePickerTheme,
                    (view12, selectedHour, selectedMinute) -> {
                        Calendar calendar1 = Calendar.getInstance();
                        calendar1.set(Calendar.HOUR_OF_DAY, selectedHour);
                        String amPm;
                        if (calendar1.get(Calendar.AM_PM) == Calendar.AM) {
                            amPm = "AM";
                        } else {
                            amPm = "PM";
                            selectedHour = selectedHour - 12;
                        }
                        String timeString = String.format(Locale.getDefault(), "%02d:%02d %s", selectedHour, selectedMinute, amPm);
                        endTime.setText(timeString);
                    },
                    hour,
                    minute,
                    false
            );
            timePickerDialog.show();
        });

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            calendar.set(date.getYear(), date.getMonth() - 1, date.getDay());
            selectedCalendarDate = calendar.getTime();
            widget.invalidateDecorators();
            selectedDate.setText(dateFormat.format(calendar.getTime()));
        });

        Date finalStartDate = startDate;
        Date finalEndDate = endDate;
        Integer finalEventId = eventId;
        saveButton.setOnClickListener(view1 -> saveButton.setOnClickListener(view2 -> {
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
                Toast.makeText(getContext(), "Please select date between start date and end date of the event", Toast.LENGTH_LONG).show();
                return;
            }

            if (selectedCalendarDate.before(finalStartDate) && selectedCalendarDate.after(finalEndDate)) {
                Toast.makeText(getContext(), "Please select date between start date and end date of the event", Toast.LENGTH_LONG).show();
                return;
            }
            if (title.isEmpty()) {
                editTitle.setError("Please enter a event title");
            }

            try {
                budgetValue = Double.parseDouble(budget);
                if (budgetValue <= 0) {
                    throw new NumberFormatException("Amount must be greater than zero");
                }
            } catch (NumberFormatException e) {
                editBudget.setError("Please enter a valid amount");
                return;
            }
            if (selectedCalendarDate.before(finalStartDate) && selectedCalendarDate.after(finalEndDate)) {
                Toast.makeText(getContext(), "Please select date between start date and end date of the event", Toast.LENGTH_LONG).show();
                return;
            }
            if (title.equals("")) {
                editTitle.setError("Please enter a event title");
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
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

            if (finalEventId == null) {
                return;
            }

            SimpleDateFormat dateFormatForData = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String subEventDateStr = dateFormatForData.format(selectedCalendarDate);

            db.createSubEvent(title, subEventDateStr, startTimeStr, endTimeStr, budgetValue, finalEventId);
            editTitle.setText("");
            editBudget.setText("");
            startTime.setText("");
            endTime.setText("");
            if (!subEventId.equals(0)) {
                db.updateSubEvent(subEventId, title, subEventDateStr, startTimeStr, endTimeStr, budgetValue);
            } else {
                db.createSubEvent(title, subEventDateStr, startTimeStr, endTimeStr, budgetValue, finalEventId);
            }
            editTitle.setText("");
            editBudget.setText("");
            startTime.setText("");
            endTime.setText("");
        }));
        return view;


    }


}
