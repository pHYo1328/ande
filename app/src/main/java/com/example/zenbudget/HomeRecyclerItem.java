package com.example.zenbudget;

import com.example.zenbudget.classes.Event;

public class HomeRecyclerItem {
    private Event event;
    private Integer type;
    private String eventId;
    private String infoMsg;

    public HomeRecyclerItem(int type, String infoMsg) {
        this.type = type;
        this.event = new Event();
        this.eventId = "";
        this.infoMsg = infoMsg;
    }

    public HomeRecyclerItem(int type) {
        this.type = type;
        this.event = new Event();
        this.eventId = "";
    }

    public static final int TYPE_CURRENT = 0;
    public static final int TYPE_UPCOMING = 1;

    public static final int TYPE_EVENT = 2;

    public static final int TYPE_INFO = 3;

    private Double totalSpent;
    private Double totalBudget;

    public HomeRecyclerItem(Event e, String eventId, double totalSpent, double totalBudget) {
        this.event = e;
        this.eventId = eventId;
        this.type = TYPE_EVENT;
        this.totalSpent = totalSpent;
        this.totalBudget = totalBudget;
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

    public Double getTotalSpent() {
        return totalSpent;
    }

    public String getInfoMsg() {
        return infoMsg;
    }

    public void setInfoMsg(String infoMsg) {
        this.infoMsg = infoMsg;
    }

    public void setTotalSpent(Double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(Double totalBudget) {
        this.totalBudget = totalBudget;
    }
}
