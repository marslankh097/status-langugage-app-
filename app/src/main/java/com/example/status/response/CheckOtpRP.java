package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CheckOtpRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("otp_status")
    private String otp_status;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getOtp_status() {
        return otp_status;
    }
}
