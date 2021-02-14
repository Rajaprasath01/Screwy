package com.rajaprasath.chatapp.Notifications;

import java.util.ArrayList;

public class Data {

    private String user;
    private int icon;
    private  String title;
    private String body;
    private String sent;
    private Integer mode;
    private String category;
    public Data() {
    }


    public Data(String user, int icon, String title, String body, String sent, Integer mode) {
        this.user = user;
        this.icon = icon;
        this.title = title;
        this.body = body;
        this.sent = sent;
        this.mode = mode;
    }

    public Data(String user, int icon, String title, String body, String sent, Integer mode, String category) {
        this.user = user;
        this.icon = icon;
        this.title = title;
        this.body = body;
        this.sent = sent;
        this.mode = mode;
        this.category = category;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
