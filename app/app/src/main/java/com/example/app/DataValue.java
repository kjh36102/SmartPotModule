package com.example.app;

public class DataValue {
    String date,time, value;
    public DataValue() {    }
    public DataValue(String date, String time, String value) {
        this.date = date;
        this.time = time;
        this.value = value;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
