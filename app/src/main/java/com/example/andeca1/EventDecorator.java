package com.example.andeca1;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import java.util.Collection;
import java.util.HashSet;

public class EventDecorator implements DayViewDecorator {

    private final Drawable highlightDrawable;
    private final HashSet<CalendarDay> dates;

    public EventDecorator(Collection<CalendarDay> dates, Context context) {
        this.dates = new HashSet<>(dates);
        highlightDrawable = context.getResources().getDrawable(R.drawable.highlight_calendar_event);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(highlightDrawable);
    }
}
