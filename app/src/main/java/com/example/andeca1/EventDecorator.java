package com.example.andeca1;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class EventDecorator implements DayViewDecorator {

    private final DateRange dateRange;

    private final Drawable backgroundDrawable;
    private CalendarDay currentDate;

    public EventDecorator(DateRange dateRange,Drawable backgroundDrawable) {
        this.dateRange = dateRange;
        this.backgroundDrawable = backgroundDrawable;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dateRange.isWithinRange(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        // Apply the drawable which uses the color of the range
        view.setBackgroundDrawable(backgroundDrawable);
    }

}
