package com.example.andeca1;

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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.LocalDate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class EventFragment extends Fragment implements EventsAdapter.OnEventEditListener,EventsAdapter.OnSubEventEditListener {
    private TextView selectedDate,noEventsTextView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
    private final Calendar calendar = Calendar.getInstance();
    private String selectedCalendarDate;
    private List<Event> eventsOnSelectedDate;
    private RecyclerView recyclerView;
    private EventsAdapter adapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);

        MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);

        selectedDate = view.findViewById(R.id.selected_date);
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        selectedDate.setText(dateFormat.format(calendar.getTime()));
        ExtendedFloatingActionButton addButton = view.findViewById(R.id.floatingActionButton);
        recyclerView = view.findViewById(R.id.event_recycler_view);
        noEventsTextView = view.findViewById(R.id.txtView_no_event);

        LocalDate today = LocalDate.now();
        CalendarDay calendarDay = CalendarDay.from(today);
        calendarView.setSelectedDate(calendarDay);
        calendarView.invalidateDecorators();
        SimpleDateFormat dateFormatForData = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedCalendarDate = dateFormatForData.format(calendar.getTime());

        DbHelper db = new DbHelper(this.getContext());
        setRecyclerView(db,calendarView);
        addDecorator(db,calendarView);

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            calendar.set(date.getYear(), date.getMonth()-1, date.getDay());
            SimpleDateFormat dateFormatForData1 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedCalendarDate = dateFormatForData1.format(calendar.getTime());
            Log.d("checkDate",selectedCalendarDate);
            widget.invalidateDecorators();
            selectedDate.setText(dateFormat.format(calendar.getTime()));
            setRecyclerView(db,calendarView);
        });

        addButton.setOnClickListener(view1 -> {
            if(selectedCalendarDate != null){
                Bundle bundle = new Bundle();
                bundle.putString("selectedDate", selectedCalendarDate);
                NewEventFragment newEventFragment = new NewEventFragment();
                newEventFragment.setArguments(bundle);
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, newEventFragment);
                transaction.commit();
            }
            else{
                Toast.makeText(getContext(),"Please select Event Start Date",Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    @Override
    public void onEventEditClicked(int eventId, String startDate) {
        // Handle the edit button click
        Bundle bundle = new Bundle();
        bundle.putInt("eventId", eventId);
        EditEventFragment editEventFragment = new EditEventFragment();
        editEventFragment.setArguments(bundle);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, editEventFragment);
        transaction.commit();
    }

    @Override
    public void onSubEventEditClicked(int subEventId, String startDate, String endDate){
        Bundle bundle = new Bundle();
        bundle.putInt("subEventId", subEventId);
        bundle.putString("startDate", startDate);
        bundle.putString("endDate",endDate);
        NewSubEventFragment newSubEventFragment = new NewSubEventFragment();
        newSubEventFragment.setArguments(bundle);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame,newSubEventFragment);
        transaction.commit();
    }

    private void setRecyclerView(DbHelper db,MaterialCalendarView calendarView){
        eventsOnSelectedDate = db.getAllEventsOnSelectedDate(selectedCalendarDate);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));


        if (eventsOnSelectedDate.isEmpty()) {
            // If there are no events, show the no events message and hide the RecyclerView
            noEventsTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            // If there are events, set up the RecyclerView
            noEventsTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    // not important if we're not implementing drag and drop
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    Event eventToRemove = eventsOnSelectedDate.get(position);
                    eventsOnSelectedDate.remove(position);
                    db.deleteEvent(eventToRemove.getId());
                    adapter.notifyItemRemoved(position);
                    addDecorator(db, calendarView);
                    if(eventsOnSelectedDate.size()==0){
                        noEventsTextView.setVisibility(View.VISIBLE);
                    }
                }
            };
            adapter = new EventsAdapter(eventsOnSelectedDate,this,this);
            recyclerView.setAdapter(adapter);
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
    }

    private void addDecorator(DbHelper db, MaterialCalendarView calendarView) {
        calendarView.removeDecorators();
        List<DateRange> dateRanges = new ArrayList<>();
        SimpleDateFormat dateFormatForData = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        List<Event> events = db.getAllEvents();

        for (Event event : events) {
            Date startDate, endDate;
            try {
                startDate = dateFormatForData.parse(event.getStartDate());
                endDate = dateFormatForData.parse(event.getEndDate());
                calendar.setTime(startDate);
                CalendarDay startCalendarDay = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                calendar.setTime(endDate);
                CalendarDay endCalendarDay = CalendarDay.from(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                dateRanges.add(new DateRange(startCalendarDay, endCalendarDay));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        for (DateRange range : dateRanges) {
            calendarView.addDecorator(new EventDecorator(range));
        }
    }

}
