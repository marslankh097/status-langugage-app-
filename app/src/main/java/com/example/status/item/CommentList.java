package com.example.status.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CommentList implements Serializable {

    @SerializedName("comment_id")
    private String comment_id;

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("user_image")
    private String user_image;

    @SerializedName("post_id")
    private String post_id;

    @SerializedName("status_type")
    private String status_type;

    @SerializedName("comment_text")
    private String comment_text;

    @SerializedName("comment_date")
    private String comment_date;

    public CommentList(String comment_id, String user_id, String user_name, String user_image, String post_id, String status_type, String comment_text, String comment_date) {
        this.comment_id = comment_id;
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_image = user_image;
        this.post_id = post_id;
        this.status_type = status_type;
        this.comment_text = comment_text;
        this.comment_date = comment_date;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getStatus_type() {
        return status_type;
    }

    public void setStatus_type(String status_type) {
        this.status_type = status_type;
    }

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public String getComment_date() {
        return comment_date;
    }

    public void setComment_date(String comment_date) {
        this.comment_date = comment_date;
    }
}
