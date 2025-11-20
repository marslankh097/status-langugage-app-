package com.example.status.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SpinnerList implements Serializable {

    @SerializedName("spinner_id")
    private String spinner_id;

    @SerializedName("points")
    private String points;

    @SerializedName("bg_color")
    private String bg_color;

    public String getSpinner_id() {
        return spinner_id;
    }

    public String getPoints() {
        return points;
    }

    public String getBg_color() {
        return bg_color;
    }
}
