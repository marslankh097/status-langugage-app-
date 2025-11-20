package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TermsConditionsRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("app_term_condition")
    private String app_term_condition;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getApp_term_condition() {
        return app_term_condition;
    }
}
