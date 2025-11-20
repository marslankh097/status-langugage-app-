package com.example.status.response;

import com.example.status.item.SubCategoryList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class StatusRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("ANDROID_REWARDS_APP")
    private List<SubCategoryList> subCategoryLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<SubCategoryList> getSubCategoryLists() {
        return subCategoryLists;
    }
}
