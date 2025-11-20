package com.example.status.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CategoryList implements Serializable {

    @SerializedName("cid")
    private String cid;

    @SerializedName("category_name")
    private String category_name;

    @SerializedName("category_image")
    private String category_image;

    @SerializedName("category_image_thumb")
    private String category_image_thumb;

    @SerializedName("start_color")
    private String start_color;

    @SerializedName("end_color")
    private String end_color;

    public CategoryList(String cid, String category_name, String category_image, String category_image_thumb, String start_color, String end_color) {
        this.cid = cid;
        this.category_name = category_name;
        this.category_image = category_image;
        this.category_image_thumb = category_image_thumb;
        this.start_color = start_color;
        this.end_color = end_color;
    }

    public String getCid() {
        return cid;
    }

    public String getCategory_name() {
        return category_name;
    }

    public String getCategory_image() {
        return category_image;
    }

    public String getCategory_image_thumb() {
        return category_image_thumb;
    }

    public String getStart_color() {
        return start_color;
    }

    public String getEnd_color() {
        return end_color;
    }
}
