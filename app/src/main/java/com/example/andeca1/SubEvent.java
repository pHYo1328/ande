package com.example.andeca1;

public class SubEvent {
    private Integer id;
    private String subEvent_title;
    private String subEvent_date;
    private String start_time;
    private String end_time;
    private Double subEvent_budget;
    private Integer eventId;

    public SubEvent(Integer id, String subEvent_title, String subEvent_date, String start_time, String end_time, Double subEvent_budget, Integer eventId) {
        this.id = id;
        this.subEvent_title = subEvent_title;
        this.subEvent_date = subEvent_date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.subEvent_budget = subEvent_budget;
        this.eventId = eventId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public SubEvent(String subEvent_title, String subEvent_date, String start_time, String end_time, Double subEvent_budget,Integer eventId) {
        this.subEvent_title = subEvent_title;
        this.subEvent_date = subEvent_date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.subEvent_budget = subEvent_budget;
        this.eventId = eventId;
    }



    public String getSubEvent_title() {
        return subEvent_title;
    }

    public void setSubEvent_title(String subEvent_title) {
        this.subEvent_title = subEvent_title;
    }

    public String getSubEvent_date() {
        return subEvent_date;
    }

    public void setSubEvent_date(String subEvent_date) {
        this.subEvent_date = subEvent_date;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public Double getSubEvent_budget() {
        return subEvent_budget;
    }

    public void setSubEvent_budget(Double subEvent_budget) {
        this.subEvent_budget = subEvent_budget;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
