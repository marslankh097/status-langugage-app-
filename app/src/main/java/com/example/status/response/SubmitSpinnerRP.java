package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SubmitSpinnerRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("daily_spinner_limit")
    private String daily_spinner_limit;

    @SerializedName("remain_spin")
    private String remain_spin;

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

    public String getDaily_spinner_limit() {
        return daily_spinner_limit;
    }

    public String getRemain_spin() {
        return remain_spin;
    }
}
