package com.chatting.makrandpawar.WildFire.model;

public class UsersChatActivityModelClass {
    String message;
    String from;
    long timestamp;
    String type;
    String seen;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public UsersChatActivityModelClass() {

    }

    public String getTimestamp() {
        return String.valueOf(timestamp);
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public UsersChatActivityModelClass(String message, String from, long timestamp, String seen , String type) {
        this.timestamp = timestamp;
        this.message = message;
        this.from = from;
        this.type = type;
        this.seen = seen;
    }
}
