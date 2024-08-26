package com.example.smart_class_project;

import java.util.Date;

public class uploadFileClass {
    public String name;
    public String url;
    private Date timestamp;

    public uploadFileClass() {
    }

    public uploadFileClass(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
