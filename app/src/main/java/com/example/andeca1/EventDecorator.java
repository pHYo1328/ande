package com.example.andeca1;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;


public class EventDecorator implements DayViewDecorator {

    private final DateRange dateRange;
    private CalendarDay currentDate;

    public EventDecorator(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dateRange.isWithinRange(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(10, Color.parseColor("#FF9835D0")));
    }

}
