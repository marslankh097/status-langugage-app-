package com.example.status.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UploadStatusOptRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("video_upload_opt")
    private String video_upload_opt;

    @SerializedName("image_upload_opt")
    private String image_upload_opt;

    @SerializedName("gif_upload_opt")
    private String gif_upload_opt;

    @SerializedName("quotes_upload_opt")
    private String quotes_upload_opt;

    @SerializedName("video_add")
    private String video_add;

    @SerializedName("image_add")
    private String image_add;

    @SerializedName("gif_add")
    private String gif_add;

    @SerializedName("quotes_add")
    private String quotes_add;

    @SerializedName("video_file_size")
    private String video_file_size;

    @SerializedName("video_file_duration")
    private String video_file_duration;

    @SerializedName("image_file_size")
    private String image_file_size;

    @SerializedName("gif_file_size")
    private String gif_file_size;

    @SerializedName("video_msg")
    private String video_msg;

    @SerializedName("video_size_msg")
    private String video_size_msg;

    @SerializedName("video_duration_msg")
    private String video_duration_msg;

    @SerializedName("img_size_msg")
    private String img_size_msg;

    @SerializedName("gif_size_msg")
    private String gif_size_msg;

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

    public String getVideo_upload_opt() {
        return video_upload_opt;
    }

    public String getImage_upload_opt() {
        return image_upload_opt;
    }

    public String getGif_upload_opt() {
        return gif_upload_opt;
    }

    public String getQuotes_upload_opt() {
        return quotes_upload_opt;
    }

    public String getVideo_add() {
        return video_add;
    }

    public String getImage_add() {
        return image_add;
    }

    public String getGif_add() {
        return gif_add;
    }

    public String getQuotes_add() {
        return quotes_add;
    }

    public String getVideo_file_size() {
        return video_file_size;
    }

    public String getVideo_file_duration() {
        return video_file_duration;
    }

    public String getImage_file_size() {
        return image_file_size;
    }

    public String getGif_file_size() {
        return gif_file_size;
    }

    public String getVideo_msg() {
        return video_msg;
    }

    public String getVideo_size_msg() {
        return video_size_msg;
    }

    public String getVideo_duration_msg() {
        return video_duration_msg;
    }

    public String getImg_size_msg() {
        return img_size_msg;
    }

    public String getGif_size_msg() {
        return gif_size_msg;
    }
}
