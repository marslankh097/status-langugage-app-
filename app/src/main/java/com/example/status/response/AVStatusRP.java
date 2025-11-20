package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AVStatusRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("document_img")
    private String document_img;

    @SerializedName("request_date")
    private String request_date;

    @SerializedName("response_date")
    private String response_date;

    @SerializedName("user_message")
    private String user_messagesg;

    @SerializedName("admin_message")
    private String admin_message;

    @SerializedName("user_full_name")
    private String user_full_name;

    @SerializedName("av_status")
    private String av_status;

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

    public String getDocument_img() {
        return document_img;
    }

    public String getRequest_date() {
        return request_date;
    }

    public String getResponse_date() {
        return response_date;
    }

    public String getUser_messagesg() {
        return user_messagesg;
    }

    public String getAdmin_message() {
        return admin_message;
    }

    public String getUser_full_name() {
        return user_full_name;
    }

    public String getAv_status() {
        return av_status;
    }
}
