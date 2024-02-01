package com.example.zenbudget;

import com.example.zenbudget.classes.Event;

public class HomeRecyclerItem {
    private Event event;
    private Integer type;
    private String eventId;

    public HomeRecyclerItem(int type) {
        this.type = type;
        this.event = new Event();
        this.eventId = "";
    }

    public static final int TYPE_CURRENT = 0;
    public static final int TYPE_UPCOMING = 1;

    public static final int TYPE_EVENT = 2;

    public HomeRecyclerItem(Event e, String eventId) {
        this.event = e;
        this.eventId = eventId;
        this.type = TYPE_EVENT;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
