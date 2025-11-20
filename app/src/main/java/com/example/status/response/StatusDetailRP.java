package com.example.status.response;

import com.example.status.item.CommentList;
import com.example.status.item.SubCategoryList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class StatusDetailRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("id")
    private String id;

    @SerializedName("status_type")
    private String status_type;

    @SerializedName("status_layout")
    private String status_layout;

    @SerializedName("category_name")
    private String category_name;

    @SerializedName("cat_id")
    private String cat_id;

    @SerializedName("status_title")
    private String status_title;

    @SerializedName("video_url")
    private String video_url;

    @SerializedName("status_thumbnail_b")
    private String status_thumbnail_b;

    @SerializedName("status_thumbnail_s")
    private String status_thumbnail_s;

    @SerializedName("quote_bg")
    private String quote_bg;

    @SerializedName("quote_font")
    private String quote_font;

    @SerializedName("total_likes")
    private String total_likes;

    @SerializedName("total_download")
    private String total_download;

    @SerializedName("total_viewer")
    private String total_viewer;

    @SerializedName("already_like")
    private String already_like;

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

    @SerializedName("created_at")
    private String created_at;

    @SerializedName("total_comment")
    private String total_comment;

    @SerializedName("watermark_image")
    private String watermark_image;

    @SerializedName("watermark_on_off")
    private String watermark_on_off;

    @SerializedName("is_favourite")
    private String is_favourite;

    @SerializedName("video_views_status_ad")
    private boolean video_views_status_ad;

    @SerializedName("like_video_status_ad")
    private boolean like_video_status_ad;

    @SerializedName("download_video_status_ad")
    private boolean download_video_status_ad;

    @SerializedName("like_image_status_ad")
    private boolean like_image_status_ad;

    @SerializedName("download_image_status_ad")
    private boolean download_image_status_ad;

    @SerializedName("like_gif_points_status_ad")
    private boolean like_gif_points_status_ad;

    @SerializedName("download_gif_status_ad")
    private boolean download_gif_status_ad;

    @SerializedName("like_quotes_status_ad")
    private boolean like_quotes_status_ad;

    @SerializedName("user_comments")
    private UserCommentRP userCommentRP;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus_type() {
        return status_type;
    }

    public void setStatus_type(String status_type) {
        this.status_type = status_type;
    }

    public String getStatus_layout() {
        return status_layout;
    }

    public void setStatus_layout(String status_layout) {
        this.status_layout = status_layout;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getCat_id() {
        return cat_id;
    }

    public void setCat_id(String cat_id) {
        this.cat_id = cat_id;
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

    public String getTotal_viewer() {
        return total_viewer;
    }

    public void setTotal_viewer(String total_viewer) {
        this.total_viewer = total_viewer;
    }

    public String getAlready_like() {
        return already_like;
    }

    public void setAlready_like(String already_like) {
        this.already_like = already_like;
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

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
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

    public String getIs_favourite() {
        return is_favourite;
    }

    public void setIs_favourite(String is_favourite) {
        this.is_favourite = is_favourite;
    }

    public boolean isVideo_views_status_ad() {
        return video_views_status_ad;
    }

    public void setVideo_views_status_ad(boolean video_views_status_ad) {
        this.video_views_status_ad = video_views_status_ad;
    }

    public boolean isLike_video_status_ad() {
        return like_video_status_ad;
    }

    public void setLike_video_status_ad(boolean like_video_status_ad) {
        this.like_video_status_ad = like_video_status_ad;
    }

    public boolean isDownload_video_status_ad() {
        return download_video_status_ad;
    }

    public void setDownload_video_status_ad(boolean download_video_status_ad) {
        this.download_video_status_ad = download_video_status_ad;
    }

    public boolean isLike_image_status_ad() {
        return like_image_status_ad;
    }

    public void setLike_image_status_ad(boolean like_image_status_ad) {
        this.like_image_status_ad = like_image_status_ad;
    }

    public boolean isDownload_image_status_ad() {
        return download_image_status_ad;
    }

    public void setDownload_image_status_ad(boolean download_image_status_ad) {
        this.download_image_status_ad = download_image_status_ad;
    }

    public boolean isLike_gif_points_status_ad() {
        return like_gif_points_status_ad;
    }

    public void setLike_gif_points_status_ad(boolean like_gif_points_status_ad) {
        this.like_gif_points_status_ad = like_gif_points_status_ad;
    }

    public boolean isDownload_gif_status_ad() {
        return download_gif_status_ad;
    }

    public void setDownload_gif_status_ad(boolean download_gif_status_ad) {
        this.download_gif_status_ad = download_gif_status_ad;
    }

    public boolean isLike_quotes_status_ad() {
        return like_quotes_status_ad;
    }

    public void setLike_quotes_status_ad(boolean like_quotes_status_ad) {
        this.like_quotes_status_ad = like_quotes_status_ad;
    }

    public UserCommentRP getUserCommentRP() {
        return userCommentRP;
    }

    public void setUserCommentRP(UserCommentRP userCommentRP) {
        this.userCommentRP = userCommentRP;
    }
}
