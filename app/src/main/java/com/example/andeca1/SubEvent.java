package com.example.andeca1;

public class SubEvent {
    private Integer id;
    private final String subEvent_title;
    private final String subEvent_date;
    private final String start_time;
    private final String end_time;
    private final Double subEvent_budget;
    private final Integer eventId;
    public SubEvent(Integer id, String subEvent_title, String subEvent_date, String start_time, String end_time, Double subEvent_budget, Integer eventId) {
        this.id = id;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
