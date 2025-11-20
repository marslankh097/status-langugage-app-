package com.example.status.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PointDetailList implements Serializable {

    @SerializedName("title")
    private String title;

    @SerializedName("point")
    private String point;

    public String getTitle() {
        return title;
    }

    public String getPoint() {
        return point;
    }
}
