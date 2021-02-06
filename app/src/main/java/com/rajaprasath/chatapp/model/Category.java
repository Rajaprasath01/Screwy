package com.rajaprasath.chatapp.model;

import android.graphics.drawable.Drawable;

public class Category {
    private String name;
    private String color;
    private Drawable icon;

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Category(String name, String color, Drawable icon) {
        this.name = name;
        this.color = color;
        this.icon = icon;
    }

    public Category() {
    }

    public Category(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
