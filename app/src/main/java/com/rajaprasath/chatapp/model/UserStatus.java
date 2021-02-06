package com.rajaprasath.chatapp.model;

public class UserStatus {
    private String status;
    private String typing;
    private String receiver;

    public UserStatus() {
    }

    public UserStatus(String status) {
        this.status = status;
    }

    public UserStatus(String status, String typing, String receiver) {
        this.status = status;
        this.typing = typing;
        this.receiver = receiver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTyping() {
        return typing;
    }

    public void setTyping(String typing) {
        this.typing = typing;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
