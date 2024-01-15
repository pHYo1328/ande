package com.example.andeca1;

import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;

public class DateRange {
    private final CalendarDay startDate;
    private final CalendarDay endDate;
    private final int color; // Color for the range

    public DateRange(CalendarDay startDate, CalendarDay endDate, int color) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.color = color;
    }

    public boolean isWithinRange(CalendarDay day) {
        return (day.isAfter(startDate) && day.isBefore(endDate)) || day.equals(startDate) || day.equals(endDate);
    }

    public int getColor() {
        return color;
    }
}
