package com.example.status.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserRMList implements Serializable {

    @SerializedName("redeem_id")
    private String redeem_id;

    @SerializedName("user_points")
    private String user_points;

    @SerializedName("redeem_price")
    private String redeem_price;

    @SerializedName("request_date")
    private String request_date;

    @SerializedName("status")
    private String status;

    public String getRedeem_id() {
        return redeem_id;
    }

    public String getUser_points() {
        return user_points;
    }

    public String getRedeem_price() {
        return redeem_price;
    }

    public String getRequest_date() {
        return request_date;
    }

    public String getStatus() {
        return status;
    }
}
