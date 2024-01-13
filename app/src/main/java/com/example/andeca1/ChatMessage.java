package com.example.andeca1;

public class ChatMessage {
    public static final int TYPE_SENDER = 0;
    public static final int TYPE_RECIPIENT = 1;

    private String message;
    private String timestamp;
    private int type;

    public ChatMessage(String message, String timestamp, int type) {
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
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
}
