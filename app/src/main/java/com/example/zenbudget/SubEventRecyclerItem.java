package com.example.zenbudget;

import com.example.zenbudget.classes.SubEvent;

public class SubEventRecyclerItem {
    private SubEvent s;
    private String subEventId;
    public SubEventRecyclerItem(SubEvent s, String subEventId) {
        this.s = s;
        this.subEventId = subEventId;
    }

    public SubEvent getSubEvent() {
        return s;
    }

    public void setSubEvent(SubEvent s) {
        this.s = s;
    }

    public String getSubEventId() {
        return subEventId;
    }

    public void setSubEventId(String subEventId) {
        this.subEventId = subEventId;
    }
}
