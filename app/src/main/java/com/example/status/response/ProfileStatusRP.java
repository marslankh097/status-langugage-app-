package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileStatusRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("profile_status")
    private String profile_status;

    @SerializedName("profile_message")
    private String profile_message;

    @SerializedName("name")
    private String name;

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

    public String getProfile_status() {
        return profile_status;
    }

    public String getProfile_message() {
        return profile_message;
    }

    public String getName() {
        return name;
    }
}
