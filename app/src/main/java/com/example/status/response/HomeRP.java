package com.example.status.response;

import com.example.status.item.SubCategoryList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class HomeRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("slider_status")
    private List<SubCategoryList> sliderLists;

    @SerializedName("statuses")
    private List<SubCategoryList> subCategoryLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<SubCategoryList> getSliderLists() {
        return sliderLists;
    }

    public List<SubCategoryList> getSubCategoryLists() {
        return subCategoryLists;
    }
}
