package com.example.status.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RewardPointList implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("status_thumbnail")
    private String status_thumbnail;

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("activity_type")
    private String activity_type;

    @SerializedName("points")
    private String points;

    @SerializedName("date")
    private String date;

    @SerializedName("time")
    private String time;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus_thumbnail() {
        return status_thumbnail;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getActivity_type() {
        return activity_type;
    }

    public String getPoints() {
        return points;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}
