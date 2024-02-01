package com.example.zenbudget.classes;

public class User {
    private String first_name, last_name, email, provider;

    public User(String first_name, String last_name, String email, String provider) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.provider = provider;
    }

    public User() {
        this.first_name = "";
        this.last_name = "";
        this.email = "";
        this.provider = "";
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
