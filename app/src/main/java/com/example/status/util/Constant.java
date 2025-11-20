package com.example.status.util;

import android.net.Uri;

import com.example.status.BuildConfig;
import com.example.status.response.AppRP;

import java.util.List;

public class Constant {

    //server api url
    public static String url = BuildConfig.My_api + "api.php";

    //video upload api url
    public static String videoUploadUrl = BuildConfig.My_api + "api_video_upload.php";

    //Change WebView text color light and dark mode
    public static String webTextLight = "#8b8b8b;";
    public static String webTextDark = "#FFFFFF;";

    //Change WebView link color light and dark mode
    public static String webLinkLight = "#0782C1;";
    public static String webLinkDark = "#0782C1;";

    public static int AD_COUNT = 0;
    public static int AD_COUNT_SHOW = 0;

    public static int REWARD_VIDEO_AD_COUNT = 0;
    public static int REWARD_VIDEO_AD_COUNT_SHOW = 0;

    public static AppRP appRP;

    public static List<Uri> imageFilesList;
    public static List<Uri> videoFilesList;

    public static List<Uri> downloadImageFilesList;
    public static List<Uri> downloadVideoFilesList;
}
