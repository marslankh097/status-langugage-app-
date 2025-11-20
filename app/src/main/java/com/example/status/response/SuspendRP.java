package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SuspendRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("user_image")
    private String user_image;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("is_verified")
    private String is_verified;

    @SerializedName("date")
    private String date;

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

    public String getUser_image() {
        return user_image;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getIs_verified() {
        return is_verified;
    }

    public String getDate() {
        return date;
    }
}
