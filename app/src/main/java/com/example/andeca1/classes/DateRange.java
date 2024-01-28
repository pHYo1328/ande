package com.example.andeca1.classes;

import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;

public class DateRange {
    private final CalendarDay startDate;
    private final CalendarDay endDate;

    public DateRange(CalendarDay startDate, CalendarDay endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean isWithinRange(CalendarDay day) {
        return (day.isAfter(startDate) && day.isBefore(endDate)) || day.equals(startDate) || day.equals(endDate);
    }
}
