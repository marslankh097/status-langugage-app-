package com.example.status.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SubCategoryList implements Serializable {

    @SerializedName("adView")
    private String adView;

    @SerializedName("id")
    private String id;

    @SerializedName("cid")
    private String cid;

    @SerializedName("status_type")
    private String status_type;

    @SerializedName("status_title")
    private String status_title;

    @SerializedName("video_url")
    private String video_url;

    @SerializedName("gif_url")
    private String gif_url;

    @SerializedName("status_layout")
    private String status_layout;

    @SerializedName("status_thumbnail_b")
    private String status_thumbnail_b;

    @SerializedName("status_thumbnail_s")
    private String status_thumbnail_s;

    @SerializedName("total_viewer")
    private String total_viewer;

    @SerializedName("total_likes")
    private String total_likes;

    @SerializedName("total_download")
    private String total_download;

    @SerializedName("already_like")
    private String already_like;

    @SerializedName("category_name")
    private String category_name;

    @SerializedName("quote_bg")
    private String quote_bg;

    @SerializedName("quote_font")
    private String quote_font;

    @SerializedName("is_favourite")
    private String is_favourite;

    @SerializedName("is_reviewed")
    private String is_reviewed;

    @SerializedName("external_link")
    private String external_link;

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("user_image")
    private String user_image;

    @SerializedName("already_follow")
    private String already_follow;

    @SerializedName("is_verified")
    private String is_verified;

    @SerializedName("total_comment")
    private String total_comment;

    @SerializedName("watermark_image")
    private String watermark_image;

    @SerializedName("watermark_on_off")
    private String watermark_on_off;

    @SerializedName("user_comments")
    private List<CommentList> commentLists;

    public SubCategoryList() {
    }

    //download
    public SubCategoryList(String id, String video_title, String video_url, String gif_url, String video_thumbnail_b, String video_thumbnail_s, String category_name, String video_layout, String status_type) {
        this.id = id;
        this.status_title = video_title;
        this.video_url = video_url;
        this.gif_url = gif_url;
        this.status_thumbnail_b = video_thumbnail_b;
        this.status_thumbnail_s = video_thumbnail_s;
        this.category_name = category_name;
        this.status_layout = video_layout;
        this.status_type = status_type;
    }

    public String getAdView() {
        return adView;
    }

    public void setAdView(String adView) {
        this.adView = adView;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getStatus_type() {
        return status_type;
    }

    public void setStatus_type(String status_type) {
        this.status_type = status_type;
    }

    public String getStatus_title() {
        return status_title;
    }

    public void setStatus_title(String status_title) {
        this.status_title = status_title;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getGif_url() {
        return gif_url;
    }

    public void setGif_url(String gif_url) {
        this.gif_url = gif_url;
    }

    public String getStatus_layout() {
        return status_layout;
    }

    public void setStatus_layout(String status_layout) {
        this.status_layout = status_layout;
    }

    public String getStatus_thumbnail_b() {
        return status_thumbnail_b;
    }

    public void setStatus_thumbnail_b(String status_thumbnail_b) {
        this.status_thumbnail_b = status_thumbnail_b;
    }

    public String getStatus_thumbnail_s() {
        return status_thumbnail_s;
    }

    public void setStatus_thumbnail_s(String status_thumbnail_s) {
        this.status_thumbnail_s = status_thumbnail_s;
    }

    public String getTotal_viewer() {
        return total_viewer;
    }

    public void setTotal_viewer(String total_viewer) {
        this.total_viewer = total_viewer;
    }

    public String getTotal_likes() {
        return total_likes;
    }

    public void setTotal_likes(String total_likes) {
        this.total_likes = total_likes;
    }

    public String getTotal_download() {
        return total_download;
    }

    public void setTotal_download(String total_download) {
        this.total_download = total_download;
    }

    public String getAlready_like() {
        return already_like;
    }

    public void setAlready_like(String already_like) {
        this.already_like = already_like;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getQuote_bg() {
        return quote_bg;
    }

    public void setQuote_bg(String quote_bg) {
        this.quote_bg = quote_bg;
    }

    public String getQuote_font() {
        return quote_font;
    }

    public void setQuote_font(String quote_font) {
        this.quote_font = quote_font;
    }

    public String getIs_favourite() {
        return is_favourite;
    }

    public void setIs_favourite(String is_favourite) {
        this.is_favourite = is_favourite;
    }

    public String getIs_reviewed() {
        return is_reviewed;
    }

    public void setIs_reviewed(String is_reviewed) {
        this.is_reviewed = is_reviewed;
    }

    public String getExternal_link() {
        return external_link;
    }

    public void setExternal_link(String external_link) {
        this.external_link = external_link;
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

    public String getAlready_follow() {
        return already_follow;
    }

    public void setAlready_follow(String already_follow) {
        this.already_follow = already_follow;
    }

    public String getIs_verified() {
        return is_verified;
    }

    public void setIs_verified(String is_verified) {
        this.is_verified = is_verified;
    }

    public String getTotal_comment() {
        return total_comment;
    }

    public void setTotal_comment(String total_comment) {
        this.total_comment = total_comment;
    }

    public String getWatermark_image() {
        return watermark_image;
    }

    public void setWatermark_image(String watermark_image) {
        this.watermark_image = watermark_image;
    }

    public String getWatermark_on_off() {
        return watermark_on_off;
    }

    public void setWatermark_on_off(String watermark_on_off) {
        this.watermark_on_off = watermark_on_off;
    }

    public List<CommentList> getCommentLists() {
        return commentLists;
    }

    public void setCommentLists(List<CommentList> commentLists) {
        this.commentLists = commentLists;
    }
}
