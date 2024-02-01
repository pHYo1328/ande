package com.example.zenbudget.classes;

public class SubEvent {
    private String id;
    private String subEvent_title;
    private String subEvent_date;
    private String start_time;
    private String end_time;
    private Double subEvent_budget;
    private String eventId = null;
    public SubEvent(String id, String subEvent_title, String subEvent_date, String start_time, String end_time, Double subEvent_budget, String eventId) {
        this.id = id;
        this.subEvent_title = subEvent_title;
        this.subEvent_date = subEvent_date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.subEvent_budget = subEvent_budget;
        this.eventId = eventId;
    }

    public SubEvent(String subEvent_title, String subEvent_date, String start_time, String end_time, Double subEvent_budget) {
        this.subEvent_title = subEvent_title;
        this.subEvent_date = subEvent_date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.subEvent_budget = subEvent_budget;
    }

    public SubEvent() {
        // Empty constructor required for Firestore data mapping
    }

    public String getSubEvent_title() {
        return subEvent_title;
    }

    public String getSubEvent_date() {
        return subEvent_date;
    }

    public String getStart_time() {
        return start_time;
    }

    public String getEnd_time() {
        return end_time;
    }


    public Double getSubEvent_budget() {
        return subEvent_budget;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
