package com.example.andeca1.classes;

public class User {
    private String first_name, last_name;

    public User(String uid, String first_name, String last_name) {
        this.first_name = first_name;
        this.last_name = last_name;
    }

    public User() {
        this.first_name = "";
        this.last_name = "";
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }
}
