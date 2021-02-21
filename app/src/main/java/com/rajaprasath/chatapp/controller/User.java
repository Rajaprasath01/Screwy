package com.rajaprasath.chatapp.controller;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class User extends Application {
    private String username;
    private String userid;
    private String imageurl;
    private String nickname;
    private String gender;
    private String about;
    private ArrayList<String> interest;
    private static User instance;
    private String status;
    private String typing;
    private Map messagetime;
    private List<String> permitted;
    private List<String> trusted;
    public User() {
    }



    public User(String username, String userid, String imageurl, String nickname, String gender, String about, ArrayList<String> interest, String status) {
        this.username = username;
        this.userid = userid;
        this.imageurl = imageurl;
        this.nickname = nickname;
        this.gender = gender;
        this.about = about;
        this.interest = interest;
        this.status=status;
    }

    public User(String username, String userid, String imageurl, String nickname, String gender, String about, ArrayList<String> interest, String status, String typing, Map messagetime) {
        this.username = username;
        this.userid = userid;
        this.imageurl = imageurl;
        this.nickname = nickname;
        this.gender = gender;
        this.about = about;
        this.interest = interest;
        this.status = status;
        this.typing = typing;
        this.messagetime = messagetime;
    }

    public User(List<String> permitted, List<String> trusted) {
        this.permitted = permitted;
        this.trusted = trusted;
    }

    public User(String username, String userid) {
        this.username = username;
        this.userid = userid;
    }

    public static User getInstance() {
        if (instance==null){
            instance=new User();
        }
        return instance;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public ArrayList<String> getInterest() {
        return interest;
    }

    public void setInterest(ArrayList<String> interest) {
        this.interest = interest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map getMessagetime() {
        return messagetime;
    }

    public void setMessagetime(Map messagetime) {
        this.messagetime = messagetime;
    }

    public String getTyping() {
        return typing;
    }

    public void setTyping(String typing) {
        this.typing = typing;
    }

    public List<String> getPermitted() {
        return permitted;
    }

    public void setPermitted(List<String> permitted) {
        this.permitted = permitted;
    }

    public List<String> getTrusted() {
        return trusted;
    }

    public void setTrusted(List<String> trusted) {
        this.trusted = trusted;
    }
}
