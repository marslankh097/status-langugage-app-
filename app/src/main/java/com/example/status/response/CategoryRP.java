package com.example.status.response;

import com.example.status.item.CategoryList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CategoryRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("ANDROID_REWARDS_APP")
    private List<CategoryList> categoryLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<CategoryList> getCategoryLists() {
        return categoryLists;
    }
}
