package com.example.smart_class_project;

public class userHelperClass {
    private String rollNo;
    private String fullName;
    private String email;
    private String userClass;

    public userHelperClass() {
        // Default constructor
    }

    public userHelperClass(String rollNo, String fullName, String email, String userClass) {
        this.rollNo = rollNo;
        this.fullName = fullName;
        this.email = email;
        this.userClass = userClass;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserClass() {
        return userClass;
    }

    public void setUserClass(String userClass) {
        this.userClass = userClass;
    }
}
