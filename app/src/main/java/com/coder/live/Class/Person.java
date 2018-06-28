package com.coder.live.Class;

/**
 * Created by Rey on 2018/6/28.
 */

public class Person {

    private int getColor;
    private String Name;
    private String value;

    public Person(String name, String value, int color) {
        this.Name = name;
        this.value = value;
        this.getColor = color;
    }

    public int getColor() {
        return getColor;
    }

    public void setGetColor(int getColor) {
        this.getColor = getColor;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
