package com.example.status.response;

import com.example.status.item.SpinnerList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import rubikstudio.library.model.LuckyItem;

public class SpinnerRP implements Serializable {

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

    @SerializedName("ad_on_spin")
    private String ad_on_spin;

    @SerializedName("remain_spin")
    private String remain_spin;

    @SerializedName("ANDROID_REWARDS_APP")
    private List<SpinnerList> spinnerLists;

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

    public String getAd_on_spin() {
        return ad_on_spin;
    }

    public String getRemain_spin() {
        return remain_spin;
    }

    public List<SpinnerList> getSpinnerLists() {
        return spinnerLists;
    }
}
