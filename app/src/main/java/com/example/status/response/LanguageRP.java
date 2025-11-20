package com.example.status.response;

import com.example.status.item.LanguageList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class LanguageRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("ANDROID_REWARDS_APP")
    private List<LanguageList> languageLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<LanguageList> getLanguageLists() {
        return languageLists;
    }
}
