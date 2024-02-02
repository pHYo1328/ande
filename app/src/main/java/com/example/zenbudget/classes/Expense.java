package com.example.zenbudget.classes;

public class Expense {
    private String userID, event, title, date, description, category, amount;

    public Expense() {
        // Empty constructor required for Firestore data mapping
    }

    public Expense(String date, String amount, String description, String title, String category, String event, String userId) {
        this.userID = userId;
        this.event = event;
        this.title = title;
        this.date = date;
        this.description = description;
        this.category = category;
        this.amount = amount;
    }

    public Double getAmountDouble() {
        return Double.valueOf(amount);
    }


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Double getDoubleAmount() {
        return Double.valueOf(amount);
    }
}
