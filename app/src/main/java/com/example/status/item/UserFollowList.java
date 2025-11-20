package com.example.status.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserFollowList implements Serializable {

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("user_image")
    private String user_image;

    @SerializedName("is_verified")
    private String is_verified;

    public String getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_image() {
        return user_image;
    }

    public String getIs_verified() {
        return is_verified;
    }
}
