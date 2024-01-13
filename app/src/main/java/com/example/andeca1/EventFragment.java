package com.example.andeca1;

import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import java.util.ArrayList;
import java.util.List;


public class EventFragment extends Fragment {


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event, container, false);


        MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);
        List<CalendarDay> eventDays = new ArrayList<>();

        CalendarDay day = CalendarDay.from(2024,1,17);
        CalendarDay endDay = CalendarDay.from(2024,1,28);

        while (!day.isAfter(endDay)) {
            eventDays.add(day);
            day = CalendarDay.from(2024,1,day.getDay()+1);
        }

        calendarView.addDecorator(new EventDecorator(eventDays, getContext()));
        return view;
    }
}
