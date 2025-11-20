package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class StatusDownloadRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("total_download")
    private String total_download;

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

    public String getTotal_download() {
        return total_download;
    }
}
