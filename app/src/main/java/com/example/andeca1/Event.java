package com.example.andeca1;

import java.util.List;

public class Event {
    private String startDate;
    private String endDate;
    private String eventName;
    private double budget;
    private List<Event> subEvents;

    public Event(String startDate, String endDate, String eventName, double budget) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.eventName = eventName;
        this.budget = budget;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public List<Event> getSubEvents() {
        return subEvents;
    }

    public void setSubEvents(List<Event> subEvents) {
        this.subEvents = subEvents;
    }
}
