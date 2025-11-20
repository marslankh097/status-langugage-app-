package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("user_image")
    private String user_image;

    @SerializedName("user_youtube")
    private String user_youtube;

    @SerializedName("user_instagram")
    private String user_instagram;

    @SerializedName("user_code")
    private String user_code;

    @SerializedName("total_point")
    private String total_point;

    @SerializedName("total_status")
    private String total_status;

    @SerializedName("total_following")
    private String total_following;

    @SerializedName("total_followers")
    private String total_followers;

    @SerializedName("is_verified")
    private String is_verified;

    @SerializedName("already_follow")
    private String already_follow;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getUser_image() {
        return user_image;
    }

    public String getUser_youtube() {
        return user_youtube;
    }

    public String getUser_instagram() {
        return user_instagram;
    }

    public String getUser_code() {
        return user_code;
    }

    public String getTotal_point() {
        return total_point;
    }

    public String getTotal_status() {
        return total_status;
    }

    public String getTotal_following() {
        return total_following;
    }

    public String getTotal_followers() {
        return total_followers;
    }

    public String getIs_verified() {
        return is_verified;
    }

    public String getAlready_follow() {
        return already_follow;
    }
}
