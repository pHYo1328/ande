package com.example.andeca1;

import java.util.ArrayList;
import java.util.List;

public class Event {
    private Integer id;
    private String startDate;
    private String endDate;
    private String eventName;
    private double budget;
    private List<SubEvent> subEvents;

    private Boolean isExpanded =false;

    public Event() {
        this.subEvents = new ArrayList<>();
    }

    public Boolean getExpanded() {
        return isExpanded;
    }

    public void setExpanded(Boolean expanded) {
        isExpanded = expanded;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Event(Integer id,String eventName,String startDate, String endDate,double budget) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.eventName = eventName;
        this.budget = budget;
        this.subEvents = new ArrayList<>();
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

    public List<SubEvent> getSubEvents() {
        return subEvents;
    }

    public void setSubEvents(List<SubEvent> subEvents) {
        this.subEvents = subEvents;
    }
    public void addSubEvent(SubEvent subEvent){
        this.subEvents.add(subEvent);
    }

    public Boolean IsExpanded(){
        return isExpanded;
    }
}