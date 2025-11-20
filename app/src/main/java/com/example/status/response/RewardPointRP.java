package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RewardPointRP implements Serializable {

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

    @SerializedName("total_point")
    private String total_point;

    @SerializedName("redeem_points")
    private String redeem_points;

    @SerializedName("redeem_money")
    private String redeem_money;

    @SerializedName("minimum_redeem_points")
    private String minimum_redeem_points;

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

    public String getTotal_point() {
        return total_point;
    }

    public String getRedeem_points() {
        return redeem_points;
    }

    public String getRedeem_money() {
        return redeem_money;
    }

    public String getMinimum_redeem_points() {
        return minimum_redeem_points;
    }
}
