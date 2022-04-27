package com.ist.gpscounter.model;

import android.net.Uri;

public class UserModel {

    private String userName;
    private String userAge;
    private String userEmail;
    private String userPhone;
    private String userPassword;
    private String userProfile;

    public UserModel() {

    }

    public UserModel(String userName, String userAge, String userEmail, String userPhone, String userPassword, String userProfile) {
        this.userName = userName;
        this.userAge = userAge;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.userPassword = userPassword;
        this.userProfile = userProfile;
    }


    public String getUserName() {
        return userName;
    }


    public String getUserAge() {
        return userAge;
    }


    public String getUserEmail() {
        return userEmail;
    }


    public String getUserPhone() {
        return userPhone;
    }


    public String getUserPassword() {
        return userPassword;
    }

    public String getUserProfile() {
        return userProfile;
    }
}
