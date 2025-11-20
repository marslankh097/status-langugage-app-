package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TransactionDetailRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("redeem_id")
    private String redeem_id;

    @SerializedName("user_points")
    private String user_points;

    @SerializedName("redeem_price")
    private String redeem_price;

    @SerializedName("payment_mode")
    private String payment_mode;

    @SerializedName("bank_details")
    private String bank_details;

    @SerializedName("request_date")
    private String request_date;

    @SerializedName("cust_message")
    private String cust_message;

    @SerializedName("receipt_img")
    private String receipt_img;

    @SerializedName("responce_date")
    private String responce_date;

    @SerializedName("td_status")
    private String td_status;

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

    public String getRedeem_id() {
        return redeem_id;
    }

    public String getUser_points() {
        return user_points;
    }

    public String getRedeem_price() {
        return redeem_price;
    }

    public String getPayment_mode() {
        return payment_mode;
    }

    public String getBank_details() {
        return bank_details;
    }

    public String getRequest_date() {
        return request_date;
    }

    public String getCust_message() {
        return cust_message;
    }

    public String getReceipt_img() {
        return receipt_img;
    }

    public String getResponce_date() {
        return responce_date;
    }

    public String getTd_status() {
        return td_status;
    }
}
