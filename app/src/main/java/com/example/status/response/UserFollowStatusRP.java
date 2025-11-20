package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserFollowStatusRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("activity_status")
    private String activity_status;

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

    public String getActivity_status() {
        return activity_status;
    }
}
