package com.example.andeca1;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class EventFragment extends Fragment {
    private TextView selectedDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();
    private String selectedCalendarDate;
    private FloatingActionButton addButton;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);
        MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);
        selectedDate = view.findViewById(R.id.selected_date);
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        selectedDate.setText(dateFormat.format(calendar.getTime()));
        addButton = view.findViewById(R.id.floatingActionButton);

        List<DateRange> dateRanges = new ArrayList<>();
        dateRanges.add(new DateRange(CalendarDay.from(2024, 1, 10), CalendarDay.from(2024, 1, 20), Color.RED));
        dateRanges.add(new DateRange(CalendarDay.from(2024, 1, 24), CalendarDay.from(2024, 1, 27), Color.BLUE));
        for (DateRange range : dateRanges) {
            Drawable drawable = new ColorDrawable(range.getColor());
            calendarView.addDecorator(new EventDecorator(range, drawable));
        }
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                calendar.set(date.getYear(), date.getMonth()-1, date.getDay());
                SimpleDateFormat dateFormatForData = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                selectedCalendarDate = dateFormatForData.format(calendar.getTime());
                selectedDate.setText(dateFormat.format(calendar.getTime()));
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedCalendarDate != null){
                    Bundle bundle = new Bundle();
                    bundle.putString("selectedDate", selectedCalendarDate);
                    NewEventFragment newEventFragment = new NewEventFragment();
                    newEventFragment.setArguments(bundle);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, newEventFragment);
                    transaction.commit();
                }
                else{
                    Toast.makeText(getContext(),"Please select Event Start Date",Toast.LENGTH_LONG);
                }
            }
        });

        return view;
    }
    private void fetchEventsFromFirestore(String selectedDate){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Event> events = new ArrayList<>();
        db.collection("events")
                .whereLessThanOrEqualTo("startDate", selectedDate)
                .whereGreaterThanOrEqualTo("endDate", selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot eventDocument : task.getResult()) {
                            Event event = eventDocument.toObject(Event.class);
                            // Fetch sub-events for this event
                            eventDocument.getReference().collection("subEvents")
                                    .whereEqualTo("Date",selectedDate)
                                    .get()
                                    .addOnCompleteListener(subTask -> {
                                        if (subTask.isSuccessful()) {
                                            List<Event> subEventsList = new ArrayList<>();
                                            for (QueryDocumentSnapshot subEventDocument : subTask.getResult()) {
                                                Event subEvent = subEventDocument.toObject(Event.class);
                                                subEventsList.add(subEvent);
                                            }
                                            event.setSubEvents(subEventsList);
                                            events.add(event);
                                        }
                                        else{
                                            Toast.makeText(getContext(), "Server error", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "Server error", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
