package com.example.andeca1;

import java.util.Date;

public class recycleItem {
    private String eventTitle;
    private int progress;
    private String date;
    private int photoID;
    private String amount;
    public String getEventTitle() {
        return eventTitle;
    }

    public int getProgress() {
        return progress;
    }

    public recycleItem(String eventTitle, int progress, String date, int photoID,String amount) {
        this.eventTitle = eventTitle;
        this.progress = progress;
        this.date = date;
        this.photoID = photoID;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }
    public int getPhotoID() {return photoID;}
    public String getAmount() {return amount;}
}
