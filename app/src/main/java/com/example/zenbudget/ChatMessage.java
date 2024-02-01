package com.example.zenbudget;

public class ChatMessage {
    public static final int TYPE_SENDER = 0;
    public static final int TYPE_RECIPIENT = 1;

    private final String message;
    private final String timestamp;
    private final int type;
    private boolean isSelected;

    private boolean isIgnored;

    public ChatMessage(String message, String timestamp, int type) {
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.isSelected = false;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getType() {
        return type;
    }

    public boolean getIsSelected() {
        return isSelected;
    }

    public boolean getIsIgnored() {
        return isIgnored;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public void setIsIgnored(boolean isIgnored) {
        this.isIgnored = isIgnored;
    }


}
