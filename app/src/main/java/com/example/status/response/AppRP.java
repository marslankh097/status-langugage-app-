package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("app_name")
    private String app_name;

    @SerializedName("privacy_policy_url")
    private String privacy_policy_url;

    @SerializedName("publisher_id")
    private String publisher_id;

    @SerializedName("spinner_opt")
    private String spinner_opt;

    @SerializedName("banner_ad_type")
    private String banner_ad_type;

    @SerializedName("banner_ad_id")
    private String banner_ad_id;

    @SerializedName("interstitial_ad_type")
    private String interstitial_ad_type;

    @SerializedName("interstitial_ad_id")
    private String interstitial_ad_id;

    @SerializedName("interstitial_ad_click")
    private String interstitial_ad_click;

    @SerializedName("rewarded_video_ads_id")
    private String rewarded_video_ads_id;

    @SerializedName("rewarded_video_click")
    private String rewarded_video_click;

    @SerializedName("app_update_status")
    private String app_update_status;

    @SerializedName("app_new_version")
    private int app_new_version;

    @SerializedName("app_update_desc")
    private String app_update_desc;

    @SerializedName("app_redirect_url")
    private String app_redirect_url;

    @SerializedName("cancel_update_status")
    private String cancel_update_status;

     @SerializedName("banner_ad")
    private boolean banner_ad = false;

    @SerializedName("interstitial_ad")
    private boolean interstitial_ad = false;

    @SerializedName("rewarded_video_ads")
    private boolean rewarded_video_ads = false;

    @SerializedName("nativ_ad")
    private boolean nativ_ad = false;

    @SerializedName("nativ_ad_id")
    private String nativ_ad_id;

    @SerializedName("nativ_ad_position")
    private String nativ_ad_position;

    @SerializedName("unity_game_id")
    private String unity_game_id;

    @SerializedName("ad_network")
    private String ad_network;

    @SerializedName("startapp_app_id")
    private String startapp_app_id;

    @SerializedName("wortise_app_id")
    private String wortise_app_id;

    public AppRP() {
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public String getApp_name() {
        return app_name;
    }

    public String getPrivacy_policy_url() {
        return privacy_policy_url;
    }

    public String getPublisher_id() {
        return publisher_id;
    }

    public String getSpinner_opt() {
        return spinner_opt;
    }

    public String getBanner_ad_type() {
        return banner_ad_type;
    }

    public String getBanner_ad_id() {
        return banner_ad_id;
    }

    public String getInterstitial_ad_type() {
        return interstitial_ad_type;
    }

    public String getInterstitial_ad_id() {
        return interstitial_ad_id;
    }

    public String getInterstitial_ad_click() {
        return interstitial_ad_click;
    }

    public String getRewarded_video_ads_id() {
        return rewarded_video_ads_id;
    }

    public String getRewarded_video_click() {
        return rewarded_video_click;
    }

    public String getApp_update_status() {
        return app_update_status;
    }

    public int getApp_new_version() {
        return app_new_version;
    }

    public String getApp_update_desc() {
        return app_update_desc;
    }

    public String getApp_redirect_url() {
        return app_redirect_url;
    }

    public String getCancel_update_status() {
        return cancel_update_status;
    }

    public boolean isBanner_ad() {
        return banner_ad;
    }

    public boolean isInterstitial_ad() {
        return interstitial_ad;
    }

    public boolean isRewarded_video_ads() {
        return rewarded_video_ads;
    }

    public String getUnity_game_id() {
        return unity_game_id;
    }

    public void setUnity_game_id(String unity_game_id) {
        this.unity_game_id = unity_game_id;
    }

    public String getStartapp_app_id() {
        return startapp_app_id;
    }

    public void setStartapp_app_id(String startapp_app_id) {
        this.startapp_app_id = startapp_app_id;
    }

    public String getAd_network() {
        return ad_network;
    }

    public void setAd_network(String ad_network) {
        this.ad_network = ad_network;
    }

    public String getWortise_app_id() {
        return wortise_app_id;
    }

    public void setWortise_app_id(String wortise_app_id) {
        this.wortise_app_id = wortise_app_id;
    }

    public boolean isNativ_ad() {
        return nativ_ad;
    }

    public void setNativ_ad(boolean nativ_ad) {
        this.nativ_ad = nativ_ad;
    }

    public String getNativ_ad_id() {
        return nativ_ad_id;
    }

    public void setNativ_ad_id(String nativ_ad_id) {
        this.nativ_ad_id = nativ_ad_id;
    }

    public String getNativ_ad_position() {
        return nativ_ad_position;
    }

    public void setNativ_ad_position(String nativ_ad_position) {
        this.nativ_ad_position = nativ_ad_position;
    }
}
