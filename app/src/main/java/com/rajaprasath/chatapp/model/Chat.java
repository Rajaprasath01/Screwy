package com.rajaprasath.chatapp.model;

public class Chat {
    private String sender;
    private String receiver;
    private String message;
    private String messageid;
    private boolean isseen;

    public Chat() {
    }

    public Chat(String sender, String receiver, String message, boolean isseen, String messageid) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.messageid=messageid;
        this.isseen = isseen;
    }



    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getMessageid() {
        return messageid;
    }

    public void setMessageid(String messageid) {
        this.messageid = messageid;
    }
}
