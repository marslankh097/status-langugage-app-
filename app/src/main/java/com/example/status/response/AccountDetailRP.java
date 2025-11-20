package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AccountDetailRP implements Serializable {

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

    @SerializedName("name")
    private String name;

    @SerializedName("is_verified")
    private String is_verified;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("user_image")
    private String user_image;

    @SerializedName("user_code")
    private String user_code;

    @SerializedName("total_point")
    private String total_point;

    @SerializedName("pending_point")
    private String pending_point;

    @SerializedName("total_video")
    private String total_video;

    @SerializedName("total_image")
    private String total_image;

    @SerializedName("total_gif")
    private String total_gif;

    @SerializedName("total_quote")
    private String total_quote;

    @SerializedName("total_followers")
    private String total_followers;

    @SerializedName("total_following")
    private String total_following;

    @SerializedName("delete_note")
    private String delete_note;

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

    public String getName() {
        return name;
    }

    public String getIs_verified() {
        return is_verified;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getUser_image() {
        return user_image;
    }

    public String getUser_code() {
        return user_code;
    }

    public String getTotal_point() {
        return total_point;
    }

    public String getPending_point() {
        return pending_point;
    }

    public String getTotal_video() {
        return total_video;
    }

    public String getTotal_image() {
        return total_image;
    }

    public String getTotal_gif() {
        return total_gif;
    }

    public String getTotal_quote() {
        return total_quote;
    }

    public String getTotal_followers() {
        return total_followers;
    }

    public String getTotal_following() {
        return total_following;
    }

    public String getDelete_note() {
        return delete_note;
    }
}
