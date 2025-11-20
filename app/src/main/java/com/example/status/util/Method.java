package com.example.status.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.example.status.R;
import com.example.status.activity.Login;
import com.example.status.database.DatabaseHandler;
import com.example.status.interfaces.FavouriteIF;
import com.example.status.interfaces.FullScreen;
import com.example.status.interfaces.OnClick;
import com.example.status.interfaces.VideoAd;
import com.example.status.item.SubCategoryList;
import com.example.status.response.FavouriteRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.service.DownloadIGService;
import com.example.status.service.DownloadVideoService;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.CacheFlag;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.RewardedVideoAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.wortise.ads.rewarded.models.Reward;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Method {

    private Activity activity;
    private OnClick onClick;
    private VideoAd videoAd;
    private FullScreen fullScreen;
    public static boolean loginBack = false;
    public static boolean isUpload = true, isDownload = true;

    public SharedPreferences pref;
    public SharedPreferences.Editor editor;
    private final String myPreference = "status";
    public String prefLogin = "pref_login";
    public String profileId = "profileId";
    public String userImage = "userImage";
    public String loginType = "loginType";
    public String showLogin = "show_login";
    public String notification = "notification";
    public String verificationCode = "verification_code";
    public String isVerification = "is_verification";
    public String themSetting = "them";
    public String IS_WELCOME = "is_welcome";
    public String IS_LANGUAGE = "is_language";
    public String IS_STARTAPP_GDPR = "is_startapp_gdpr";

    public String regName = "reg_name";
    public String regEmail = "reg_email";
    public String regPassword = "reg_password";
    public String regPhoneNo = "reg_phoneNo";
    public String regReference = "reg_reference";

    public String languageIds = "language_ids";

    private String filename;
    private DatabaseHandler db;

    @SuppressLint("CommitPrefEdits")
    public Method(Activity activity) {
        this.activity = activity;
        db = new DatabaseHandler(activity);
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
    }

    @SuppressLint("CommitPrefEdits")
    public Method(Activity activity, VideoAd videoAd) {
        this.activity = activity;
        db = new DatabaseHandler(activity);
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
        this.videoAd = videoAd;
    }

    @SuppressLint("CommitPrefEdits")
    public Method(Activity activity, OnClick onClick) {
        this.activity = activity;
        db = new DatabaseHandler(activity);
        this.onClick = onClick;
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
    }

    @SuppressLint("CommitPrefEdits")
    public Method(Activity activity, OnClick onClick, VideoAd videoAd, FullScreen fullScreen) {
        this.activity = activity;
        db = new DatabaseHandler(activity);
        this.onClick = onClick;
        this.videoAd = videoAd;
        this.fullScreen = fullScreen;
        pref = activity.getSharedPreferences(myPreference, 0); // 0 - for private mode
        editor = pref.edit();
    }

    public void login() {
        String firstTime = "firstTime";
        if (!pref.getBoolean(firstTime, false)) {
            editor.putBoolean(prefLogin, false);
            editor.putBoolean(firstTime, true);
            editor.commit();
        }
    }

    public void setFirstWelcome(boolean isFirstTime) {
        editor.putBoolean(IS_WELCOME, isFirstTime);
        editor.commit();
    }

    public void setFirstLanguage(boolean isFirstTime) {
        editor.putBoolean(IS_LANGUAGE, isFirstTime);
        editor.commit();
    }

    public boolean isWelcome() {
        return pref.getBoolean(IS_WELCOME, true);
    }

    public void setFirstStartAppGDPR(boolean isFirstTime) {
        editor.putBoolean(IS_STARTAPP_GDPR, isFirstTime);
        editor.commit();
    }

    public boolean isStartAppGDPR() {
        return pref.getBoolean(IS_STARTAPP_GDPR, true);
    }

    public boolean isLanguage() {
        return pref.getBoolean(IS_LANGUAGE, true);
    }

    //User login or not
    public boolean isLogin() {
        return pref.getBoolean(prefLogin, false);
    }

    public String userId() {
        return pref.getString(profileId, "");
    }

    //Get login type
    public String getLoginType() {
        return pref.getString(loginType, "");
    }

    //Get language id
    public String getLanguageIds() {
        return pref.getString(languageIds, "");
    }

    //Get theme
    public String getTheme() {
        return pref.getString(themSetting, "system");
    }

    /**
     * Main folder path
     * Recommended to use same name as your app
     * Please not use space in folder path name
     */
    public String storagePath() {
        return activity.getExternalFilesDir("Status").toString();
    }

    public String videoPath() {
        return storagePath() + "/Video/";
    }

    public String imagePath() {
        return storagePath() + "/StatusImage/";
    }

    public String downLoadStatusPath() {
        return storagePath() + "/StatusSaver/";
    }

    public String getStatus() {
        return Environment.getExternalStorageDirectory() + "/" + "WhatsApp/Media/.Statuses";
    }

    //get device id
    @SuppressLint("HardwareIds")
    public String getDeviceId() {
        String deviceId;
        try {
            deviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            deviceId = "NotFound";
        }
        return deviceId;
    }

    //rtl
    public void forceRTLIfSupported() {
        if (activity.getResources().getString(R.string.isRTL).equals("true")) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    //rtl or not
    public boolean isRtl() {
        return activity.getResources().getString(R.string.isRTL).equals("true");
    }

    /**
     * Making notification bar transparent
     */
    public void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    //Whatsapp application installation or not check
    public boolean isAppInstalledWhatsapp() {
        String packageName = "com.whatsapp";
        Intent mIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        return mIntent != null;
    }

    //Instagram application installation or not check
    public boolean isAppInstalledInstagram() {
        String packageName = "com.instagram.android";
        Intent mIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        return mIntent != null;
    }

    //Facebook application installation or not check
    public boolean isAppInstalledFacebook() {
        String packageName = "com.facebook.katana";
        Intent mIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        return mIntent != null;
    }

    //Facebook messenger application installation or not check
    public boolean isAppInstalledFbMessenger() {
        String packageName = "com.facebook.orca";
        Intent mIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        return mIntent != null;
    }

    //Twitter application installation or not check
    public boolean isAppInstalledTwitter() {
        String packageName = "com.twitter.android";
        Intent mIntent = activity.getPackageManager().getLaunchIntentForPackage(packageName);
        return mIntent != null;
    }

    //Network check
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //get screen width
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnWidth = point.x;
        return columnWidth;
    }

    //get screen height
    public int getScreenHeight() {
        int columnHeight;
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnHeight = point.y;
        return columnHeight;
    }

    //---------------Rewarded Ad show dialog---------------//

    public void VideoAdDialog(String type, String value) {

        if (Constant.appRP != null) {
            if (Constant.appRP.isRewarded_video_ads()) {
                if (value.equals("view_ad")) {
                    showAdDialog(type);
                } else {
                    Constant.REWARD_VIDEO_AD_COUNT = Constant.REWARD_VIDEO_AD_COUNT + 1;
                    if (Constant.REWARD_VIDEO_AD_COUNT == Constant.REWARD_VIDEO_AD_COUNT_SHOW) {
                        Constant.REWARD_VIDEO_AD_COUNT = 0;
                        showAdDialog(type);
                    } else {
                        callVideoAdData(type);
                    }
                }
            } else {
                callVideoAdData(type);
            }
        } else {
            callVideoAdData(type);
        }

    }

    private void showAdDialog(String type) {
        Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_view_ad);
        if (isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        MaterialButton buttonYes = dialog.findViewById(R.id.button_yes_viewAd);
        MaterialButton buttonNo = dialog.findViewById(R.id.button_no_viewAd);

        buttonYes.setOnClickListener(v -> {
            dialog.dismiss();
            showVideoAd(type);
        });

        buttonNo.setOnClickListener(v -> {
            dialog.dismiss();
            if (Constant.appRP.isInterstitial_ad()) {
                skipVideoAd(type);
            } else {
                callVideoAdData(type);
            }
        });

        dialog.show();
    }

    private void showVideoAd(String type) {

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        switch (Constant.appRP.getAd_network()) {
            case "admob":
                AdRequest.Builder builder = new AdRequest.Builder();
                RewardedAd.load(activity, Constant.appRP.getRewarded_video_ads_id(), builder.build(), new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        callVideoAdData(type);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {

                        progressDialog.dismiss();
                        rewardedAd.show(activity, rewardItem -> {
                            //user earn point
                        });

                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d("reward_ad", "Ad was shown.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.@NotNull AdError adError) {
                                // Called when ad fails to show.
                                Log.d("reward_ad", "Ad failed to show.");
                                callVideoAdData(type);
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Don't forget to set the ad reference to null so you
                                // don't show the ad a second time.
                                Log.d("reward_ad", "Ad was dismissed.");
                                callVideoAdData(type);
                            }
                        });

                    }
                });
                break;
            case "facebook":
                RewardedVideoAd rewardedVideoAd = new RewardedVideoAd(activity, Constant.appRP.getRewarded_video_ads_id());
                RewardedVideoAdListener rewardedVideoAdListener = new RewardedVideoAdListener() {
                    @Override
                    public void onError(Ad ad, AdError adError) {
                        Log.e("FbReward", "Rewarded video ad failed to load: " + adError.getErrorMessage());
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        progressDialog.dismiss();
                        rewardedVideoAd.show();
                        Log.d("FbReward", "Rewarded video ad is loaded and ready to be displayed!");
                    }

                    @Override
                    public void onAdClicked(Ad ad) {
                        Log.d("FbReward", "Rewarded video ad clicked!");
                    }

                    @Override
                    public void onLoggingImpression(Ad ad) {
                        Log.d("FbReward", "Rewarded video ad impression logged!");
                    }


                    @Override
                    public void onRewardedVideoCompleted() {
                        // Rewarded Video View Complete - the video has been played to the end.
                        // You can use this event to initialize your reward
                        Log.d("FbReward", "Rewarded video completed!");

                        // Call method to give reward
                        // giveReward();

                    }

                    @Override
                    public void onRewardedVideoClosed() {
                        // The Rewarded Video ad was closed - this can occur during the video
                        // by closing the app, or closing the end card.
                        Log.d("FbReward", "Rewarded video ad closed!");
                        //call activity
                        callVideoAdData(type);
                    }
                };
                rewardedVideoAd.loadAd(
                        rewardedVideoAd.buildLoadAdConfig()
                                .withAdListener(rewardedVideoAdListener)
                                .build());
                break;
            case "unityds":
                UnityAds.load(Constant.appRP.getRewarded_video_ads_id(), new IUnityAdsLoadListener() {
                    @Override
                    public void onUnityAdsAdLoaded(String placementId) {
                        UnityAds.show((Activity) activity, "Rewarded_Android", new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                            @Override
                            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                                Log.e("UnityAdsExample", "Unity Ads failed to show ad for " + placementId + " with error: [" + error + "] " + message);
                                progressDialog.dismiss();
                                callVideoAdData(type);
                            }

                            @Override
                            public void onUnityAdsShowStart(String placementId) {

                                Log.v("UnityAdsExample", "onUnityAdsShowStart: " + placementId);
                            }

                            @Override
                            public void onUnityAdsShowClick(String placementId) {
                                Log.v("UnityAdsExample", "onUnityAdsShowClick: " + placementId);
                            }

                            @Override
                            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                                Log.v("UnityAdsExample", "onUnityAdsShowComplete: " + placementId);
                                if (state.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED)) {
                                    // Reward the user for watching the ad to completion
                                } else {
                                    // Do not reward the user for skipping the ad
                                }
                                progressDialog.dismiss();
                                callVideoAdData(type);
                            }
                        });

                    }

                    @Override
                    public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                        Log.e("UnityAdsExample", "Unity Ads failed to load ad for " + placementId + " with error: [" + error + "] " + message);
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }
                });

                break;
            case "applovins":
                MaxRewardedAd rewardedAd = MaxRewardedAd.getInstance(Constant.appRP.getRewarded_video_ads_id(), activity);
                rewardedAd.setListener(new MaxRewardedAdListener() {
                    @Override
                    public void onRewardedVideoStarted(MaxAd ad) {
                        Log.e("ApplovinAdsExample", "Applovin start");
                    }

                    @Override
                    public void onRewardedVideoCompleted(MaxAd ad) {
                        Log.e("ApplovinAdsExample", "Applovin complete");
                        callVideoAdData(type);
                    }

                    @Override
                    public void onUserRewarded(MaxAd ad, MaxReward reward) {
                        Log.e("ApplovinAdsExample", "Applovin user get reward");
                    }

                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        Log.e("ApplovinAdsExample", "Applovin loaded");
                        progressDialog.dismiss();
                        rewardedAd.showAd();
                    }

                    @Override
                    public void onAdDisplayed(MaxAd ad) {
                        Log.e("ApplovinAdsExample", "Applovin display");
                    }

                    @Override
                    public void onAdHidden(MaxAd ad) {
                        Log.e("ApplovinAdsExample", "Applovin hidden");
                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {
                        Log.e("ApplovinAdsExample", "Applovin click");
                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                        Log.e("ApplovinAdsExample", "Applovin failed");
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                        Log.e("ApplovinAdsExample", "Applovin fail to display");
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }
                });
                rewardedAd.loadAd();
                break;
            case "wortise":
               com.wortise.ads.rewarded.RewardedAd mRewarded = new com.wortise.ads.rewarded.RewardedAd(activity, Constant.appRP.getRewarded_video_ads_id());
               mRewarded.setListener(new com.wortise.ads.rewarded.RewardedAd.Listener() {
                   @Override
                   public void onRewardedImpression(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd) {

                   }

                   @Override
                   public void onRewardedFailedToShow(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd, @NonNull com.wortise.ads.AdError adError) {
                       progressDialog.dismiss();
                       callVideoAdData(type);
                   }

                   @Override
                   public void onRewardedFailedToLoad(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd, @NonNull com.wortise.ads.AdError adError) {
                       progressDialog.dismiss();
                       callVideoAdData(type);
                   }

                   @Override
                   public void onRewardedClicked(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd) {

                   }

                   @Override
                   public void onRewardedCompleted(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd, @NonNull Reward reward) {
                       callVideoAdData(type);
                   }

                   @Override
                   public void onRewardedDismissed(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd) {
                       progressDialog.dismiss();
                       callVideoAdData(type);
                   }

                   @Override
                   public void onRewardedLoaded(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd) {
                       progressDialog.dismiss();
                       if (mRewarded.isAvailable()) {
                           mRewarded.showAd();
                       }
                   }

                   @Override
                   public void onRewardedShown(@NonNull com.wortise.ads.rewarded.RewardedAd rewardedAd) {

                   }
               });
                mRewarded.loadAd();
                break;
        }

    }

    private void skipVideoAd(String type) {

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        switch (Constant.appRP.getAd_network()) {
            case "admob":
                AdRequest.Builder builder = new AdRequest.Builder();
                InterstitialAd.load(activity, Constant.appRP.getInterstitial_ad_id(), builder.build(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        Log.i("admob_error", "onAdLoaded");
                        progressDialog.dismiss();
                        interstitialAd.show(activity);
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                Log.d("TAG", "The ad was dismissed.");
                                callVideoAdData(type);
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.@NotNull AdError adError) {
                                // Called when fullscreen content failed to show.
                                Log.d("TAG", "The ad failed to show.");
                                callVideoAdData(type);
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                Log.d("TAG", "The ad was shown.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("admob_error", loadAdError.getMessage());
                        callVideoAdData(type);
                        progressDialog.dismiss();
                    }
                });
                break;
            case "facebook":
                com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(activity, Constant.appRP.getInterstitial_ad_id());
                InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                    @Override
                    public void onInterstitialDisplayed(Ad ad) {
                        // Interstitial ad displayed callback
                        Log.e("fb_ad", "Interstitial ad displayed.");
                    }

                    @Override
                    public void onInterstitialDismissed(Ad ad) {
                        // Interstitial dismissed callback
                        progressDialog.dismiss();
                        callVideoAdData(type);
                        Log.e("fb_ad", "Interstitial ad dismissed.");
                    }

                    @Override
                    public void onError(Ad ad, AdError adError) {
                        // Ad error callback
                        callVideoAdData(type);
                        progressDialog.dismiss();
                        Log.e("fb_ad", "Interstitial ad failed to load: " + adError.getErrorMessage());
                    }

                    @Override
                    public void onAdLoaded(Ad ad) {
                        // Interstitial ad is loaded and ready to be displayed
                        Log.d("fb_ad", "Interstitial ad is loaded and ready to be displayed!");
                        progressDialog.dismiss();
                        // Show the ad
                        interstitialAd.show();
                    }

                    @Override
                    public void onAdClicked(Ad ad) {
                        // Ad clicked callback
                        Log.d("fb_ad", "Interstitial ad clicked!");
                    }

                    @Override
                    public void onLoggingImpression(Ad ad) {
                        // Ad impression logged callback
                        Log.d("fb_ad", "Interstitial ad impression logged!");
                    }
                };

                // For auto play video ads, it's recommended to load the ad
                // at least 30 seconds before it is shown
                com.facebook.ads.InterstitialAd.InterstitialLoadAdConfig loadAdConfig = interstitialAd.buildLoadAdConfig().
                        withAdListener(interstitialAdListener).withCacheFlags(CacheFlag.ALL).build();
                interstitialAd.loadAd(loadAdConfig);
                break;
            case "startapp":
                StartAppAd startAppAd = new StartAppAd(activity);
                startAppAd.loadAd(new AdEventListener() {
                    @Override
                    public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                        progressDialog.dismiss();
                        startAppAd.showAd(new AdDisplayListener() {
                            @Override
                            public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                                progressDialog.dismiss();
                                callVideoAdData(type);
                            }

                            @Override
                            public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {

                            }

                            @Override
                            public void adClicked(com.startapp.sdk.adsbase.Ad ad) {
                                progressDialog.dismiss();
                            }

                            @Override
                            public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                                progressDialog.dismiss();
                                callVideoAdData(type);
                            }
                        });
                    }

                    @Override
                    public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }
                });
                break;
            case "applovins":
                MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(Constant.appRP.getInterstitial_ad_id(), (Activity) activity);
                maxInterstitialAd.setListener(new MaxAdListener() {
                    @Override
                    public void onAdLoaded(MaxAd ad) {
                        progressDialog.dismiss();
                        maxInterstitialAd.showAd();
                    }

                    @Override
                    public void onAdDisplayed(MaxAd ad) {
                    }

                    @Override
                    public void onAdHidden(MaxAd ad) {
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }

                    @Override
                    public void onAdClicked(MaxAd ad) {
                    }

                    @Override
                    public void onAdLoadFailed(String adUnitId, MaxError error) {
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }

                    @Override
                    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }
                });
                // Load the first ad
                maxInterstitialAd.loadAd();

                break;
            case "unityds":
                UnityAds.show((Activity) activity, Constant.appRP.getInterstitial_ad_id(), new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }

                    @Override
                    public void onUnityAdsShowStart(String s) {
                    }

                    @Override
                    public void onUnityAdsShowClick(String s) {
                    }

                    @Override
                    public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }
                });
                break;
            case "wortise":
                com.wortise.ads.interstitial.InterstitialAd wInterstitial = new com.wortise.ads.interstitial.InterstitialAd(activity, Constant.appRP.getInterstitial_ad_id());
                wInterstitial.setListener(new com.wortise.ads.interstitial.InterstitialAd.Listener() {
                    @Override
                    public void onInterstitialImpression(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                    }

                    @Override
                    public void onInterstitialFailedToShow(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }

                    @Override
                    public void onInterstitialFailedToLoad(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }

                    @Override
                    public void onInterstitialClicked(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                    }

                    @Override
                    public void onInterstitialDismissed(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                        progressDialog.dismiss();
                        callVideoAdData(type);
                    }

                    @Override
                    public void onInterstitialLoaded(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                        if (wInterstitial.isAvailable()) {
                            wInterstitial.showAd();
                        }
                    }

                    @Override
                    public void onInterstitialShown(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                        progressDialog.dismiss();
                    }
                });
                wInterstitial.loadAd();
                break;
        }
    }

    //call interface
    private void callVideoAdData(String videoAdType) {
        videoAd.videoAdClick(videoAdType);
    }

    //---------------Rewarded Ad show dialog---------------//

    //---------------Interstitial Ad---------------//

    public void onClickData(int position, String title, String type, String statusType, String id, String tag) {

        ProgressDialog progressDialog = new ProgressDialog(activity);

        progressDialog.show();
        progressDialog.setMessage(activity.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        if (Constant.appRP != null) {

            if (Constant.appRP.isInterstitial_ad()) {

                Constant.AD_COUNT = Constant.AD_COUNT + 1;
                if (Constant.AD_COUNT == Constant.AD_COUNT_SHOW) {
                    Constant.AD_COUNT = 0;

                    switch (Constant.appRP.getAd_network()) {
                        case "admob":
                            AdRequest.Builder builder = new AdRequest.Builder();
                            InterstitialAd.load(activity, Constant.appRP.getInterstitial_ad_id(), builder.build(), new InterstitialAdLoadCallback() {
                                @Override
                                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                    // The mInterstitialAd reference will be null until
                                    // an ad is loaded.
                                    Log.i("admob_error", "onAdLoaded");
                                    progressDialog.dismiss();
                                    interstitialAd.show(activity);
                                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                        @Override
                                        public void onAdDismissedFullScreenContent() {
                                            // Called when fullscreen content is dismissed.
                                            Log.d("TAG", "The ad was dismissed.");
                                            onClick.position(position, title, type, statusType, id, tag);
                                        }

                                        @Override
                                        public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                            // Called when fullscreen content failed to show.
                                            Log.d("TAG", "The ad failed to show.");
                                            onClick.position(position, title, type, statusType, id, tag);
                                        }

                                        @Override
                                        public void onAdShowedFullScreenContent() {
                                            // Called when fullscreen content is shown.
                                            // Make sure to set your reference to null so you don't
                                            // show it a second time.
                                            Log.d("TAG", "The ad was shown.");
                                        }
                                    });
                                }

                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                    // Handle the error
                                    Log.i("admob_error", loadAdError.getMessage());
                                    onClick.position(position, title, type, statusType, id, tag);
                                    progressDialog.dismiss();
                                }
                            });

                            break;
                        case "facebook":
                            com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(activity, Constant.appRP.getInterstitial_ad_id());
                            InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                                @Override
                                public void onInterstitialDisplayed(Ad ad) {
                                    // Interstitial ad displayed callback
                                    Log.e("fb_ad", "Interstitial ad displayed.");
                                }

                                @Override
                                public void onInterstitialDismissed(Ad ad) {
                                    // Interstitial dismissed callback
                                    progressDialog.dismiss();
                                    onClick.position(position, title, type, statusType, id, tag);
                                    Log.e("fb_ad", "Interstitial ad dismissed.");
                                }

                                @Override
                                public void onError(Ad ad, AdError adError) {
                                    // Ad error callback
                                    onClick.position(position, title, type, statusType, id, tag);
                                    progressDialog.dismiss();
                                    Log.e("fb_ad", "Interstitial ad failed to load: " + adError.getErrorMessage());
                                }

                                @Override
                                public void onAdLoaded(Ad ad) {
                                    // Interstitial ad is loaded and ready to be displayed
                                    Log.d("fb_ad", "Interstitial ad is loaded and ready to be displayed!");
                                    progressDialog.dismiss();
                                    // Show the ad
                                    interstitialAd.show();
                                }

                                @Override
                                public void onAdClicked(Ad ad) {
                                    // Ad clicked callback
                                    Log.d("fb_ad", "Interstitial ad clicked!");
                                }

                                @Override
                                public void onLoggingImpression(Ad ad) {
                                    // Ad impression logged callback
                                    Log.d("fb_ad", "Interstitial ad impression logged!");
                                }
                            };

                            // For auto play video ads, it's recommended to load the ad
                            // at least 30 seconds before it is shown
                            com.facebook.ads.InterstitialAd.InterstitialLoadAdConfig loadAdConfig = interstitialAd.buildLoadAdConfig().
                                    withAdListener(interstitialAdListener).withCacheFlags(CacheFlag.ALL).build();
                            interstitialAd.loadAd(loadAdConfig);

                            break;
                        case "startapp":
                            StartAppAd startAppAd = new StartAppAd(activity);
                            startAppAd.loadAd(new AdEventListener() {
                                @Override
                                public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                    progressDialog.dismiss();
                                    startAppAd.showAd(new AdDisplayListener() {
                                        @Override
                                        public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                                            progressDialog.dismiss();
                                            onClick.position(position, title, type, statusType, id, tag);
                                        }

                                        @Override
                                        public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {

                                        }

                                        @Override
                                        public void adClicked(com.startapp.sdk.adsbase.Ad ad) {
                                            progressDialog.dismiss();
                                        }

                                        @Override
                                        public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                                            progressDialog.dismiss();
                                            onClick.position(position, title, type, statusType, id, tag);
                                        }
                                    });
                                }

                                @Override
                                public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                    progressDialog.dismiss();
                                    onClick.position(position, title, type, statusType, id, tag);
                                }
                            });
                            break;
                        case "applovins":
                            MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(Constant.appRP.getInterstitial_ad_id(), (Activity) activity);
                            maxInterstitialAd.setListener(new MaxAdListener() {
                                @Override
                                public void onAdLoaded(MaxAd ad) {
                                    progressDialog.dismiss();
                                    maxInterstitialAd.showAd();
                                }

                                @Override
                                public void onAdDisplayed(MaxAd ad) {
                                }

                                @Override
                                public void onAdHidden(MaxAd ad) {
                                    progressDialog.dismiss();
                                    onClick.position(position, title, type, statusType, id, tag);
                                }

                                @Override
                                public void onAdClicked(MaxAd ad) {
                                }

                                @Override
                                public void onAdLoadFailed(String adUnitId, MaxError error) {
                                    progressDialog.dismiss();
                                    onClick.position(position, title, type, statusType, id, tag);
                                }

                                @Override
                                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                    progressDialog.dismiss();
                                    onClick.position(position, title, type, statusType, id, tag);
                                }
                            });
                            // Load the first ad
                            maxInterstitialAd.loadAd();

                            break;
                        case "unityds":
                            UnityAds.show((Activity) activity, Constant.appRP.getInterstitial_ad_id(), new IUnityAdsShowListener() {
                                @Override
                                public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                                    progressDialog.dismiss();
                                    onClick.position(position, title, type, statusType, id, tag);
                                }

                                @Override
                                public void onUnityAdsShowStart(String s) {
                                }

                                @Override
                                public void onUnityAdsShowClick(String s) {
                                }

                                @Override
                                public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {
                                    progressDialog.dismiss();
                                    onClick.position(position, title, type, statusType, id, tag);
                                }
                            });
                            break;
                        case "wortise":
                            com.wortise.ads.interstitial.InterstitialAd wInterstitial = new com.wortise.ads.interstitial.InterstitialAd(activity, Constant.appRP.getInterstitial_ad_id());
                            wInterstitial.setListener(new com.wortise.ads.interstitial.InterstitialAd.Listener() {
                                @Override
                                public void onInterstitialImpression(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                                }

                                @Override
                                public void onInterstitialFailedToShow(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                    progressDialog.dismiss();
                                    callVideoAdData(type);
                                }

                                @Override
                                public void onInterstitialFailedToLoad(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                    progressDialog.dismiss();
                                    callVideoAdData(type);
                                }

                                @Override
                                public void onInterstitialClicked(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                                }

                                @Override
                                public void onInterstitialDismissed(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                                    progressDialog.dismiss();
                                    onClick.position(position, title, type, statusType, id, tag);
                                }

                                @Override
                                public void onInterstitialLoaded(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                                    if (wInterstitial.isAvailable()) {
                                        wInterstitial.showAd();
                                    }
                                }

                                @Override
                                public void onInterstitialShown(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                                    progressDialog.dismiss();
                                }
                            });
                            wInterstitial.loadAd();
                            break;
                    }

                } else {
                    progressDialog.dismiss();
                    onClick.position(position, title, type, statusType, id, tag);
                }
            } else {
                progressDialog.dismiss();
                onClick.position(position, title, type, statusType, id, tag);
            }

        } else {
            progressDialog.dismiss();
            onClick.position(position, title, type, statusType, id, tag);
        }

    }

    //---------------Interstitial Ad---------------//


    //---------------Full Screen---------------//

    //call interface full screen
    public void ShowFullScreen(boolean isFullScreen) {
        fullScreen.fullscreen(isFullScreen);
    }

    //---------------Full Screen---------------//

    //---------------Download status video---------------//

    public void download(String id, String statusName, String category, String statusImageS, String statusImageB,
                         String videoUri, String layoutType, String statusType, String watermarkImage, String watermarkOnOff) {

        String filePath = null;

        if (statusType.equals("video")) {

            filename = "filename-" + id + ".mp4";

            //video file save folder name
            File rootVideo = new File(videoPath());
            if (!rootVideo.exists()) {
                rootVideo.mkdirs();
            }

            File file = new File(videoPath(), filename);
            filePath = file.toString();

            //check file exist or not
            if (!file.exists()) {

                Method.isDownload = false;

                Intent serviceIntent = new Intent(activity, DownloadVideoService.class);
                serviceIntent.setAction(DownloadVideoService.ACTION_START);
                serviceIntent.putExtra("video_id", id);
                serviceIntent.putExtra("downloadUrl", videoUri);
                serviceIntent.putExtra("file_path", rootVideo.toString());
                serviceIntent.putExtra("file_name", filename);
                serviceIntent.putExtra("layout_type", layoutType);
                serviceIntent.putExtra("status_type", statusType);
                serviceIntent.putExtra("watermark_image", watermarkImage);
                serviceIntent.putExtra("watermark_on_off", watermarkOnOff);
                activity.startService(serviceIntent);


            } else {
                alertBox(activity.getResources().getString(R.string.you_have_already_download_video));
            }

        } else if (statusType.equals("image") || statusType.equals("gif")) {

            if (statusType.equals("image")) {
                filename = "filename-" + id + ".jpg";
            } else {
                filename = "filename-" + id + ".gif";
            }

            File rootFile = new File(imagePath());
            if (!rootFile.exists()) {
                rootFile.mkdirs();
            }

            File file = new File(imagePath(), filename);
            filePath = file.toString();

            Intent serviceIntent = new Intent(activity, DownloadIGService.class);
            serviceIntent.setAction(DownloadIGService.ACTION_START);
            serviceIntent.putExtra("id", id);
            serviceIntent.putExtra("downloadUrl", statusImageB);
            serviceIntent.putExtra("file_path", rootFile.toString());
            serviceIntent.putExtra("file_name", filename);
            serviceIntent.putExtra("status_type", statusType);
            activity.startService(serviceIntent);

        }

        new DownloadImage().execute(statusImageB, id, statusName, category, layoutType, statusType, filePath);

    }

    @SuppressLint("StaticFieldLeak")
    private class DownloadImage extends AsyncTask<String, String, String> {

        private String filePath = null;
        private String iconsStoragePath;
        private String id, statusName, category, layoutType, statusType, getFilePath;

        @Override
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);
                id = params[1];
                statusName = params[2];
                category = params[3];
                layoutType = params[4];
                statusType = params[5];
                getFilePath = params[6];

                if (statusType.equals("video")) {

                    iconsStoragePath = videoPath();
                    File sdIconStorageDir = new File(iconsStoragePath);
                    if (!sdIconStorageDir.exists()) {
                        sdIconStorageDir.mkdirs();
                    }

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap bitmapDownload = BitmapFactory.decodeStream(input);

                    filePath = iconsStoragePath + "Image-" + id + ".jpg";
                    File file = new File(iconsStoragePath, filePath);
                    if (file.exists()) {
                        Log.d("file_exists", "file_exists");
                    } else {
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
                            //choose another format if PNG doesn't suit you
                            bitmapDownload.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                            bos.flush();
                            bos.close();
                        } catch (IOException e) {
                            Log.w("TAG", "Error saving image file: " + e.getMessage());
                        }
                    }

                }

            } catch (IOException e) {
                // Log exception
                Log.w("error", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (statusType.equals("image")) {
                Toast.makeText(activity, activity.getResources().getString(R.string.download), Toast.LENGTH_SHORT).show();
            }

            if (db.checkIdStatusDownload(id, statusType)) {
                if (statusType.equals("video")) {
                    db.addStatusDownload(new SubCategoryList(id, statusName, iconsStoragePath + filename, "", filePath, filePath, category, layoutType, statusType));
                } else if (statusType.equals("image")) {
                    db.addStatusDownload(new SubCategoryList(id, statusName, "", "", getFilePath, getFilePath, category, layoutType, statusType));
                } else {
                    db.addStatusDownload(new SubCategoryList(id, statusName, "", getFilePath, getFilePath, getFilePath, category, layoutType, statusType));
                }
            }

            super.onPostExecute(s);
        }

    }

    //---------------Download status video---------------//

    //add to favourite
    public void addToFav(String id, String userId, String statusType, FavouriteIF favouriteIF) {

        ProgressDialog progressDialog = new ProgressDialog(activity);

        progressDialog.show();
        progressDialog.setMessage(activity.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("post_id", id);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("type", statusType);
        jsObj.addProperty("method_name", "status_favourite");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<FavouriteRP> call = apiService.isFavouriteStatus(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<FavouriteRP>() {
            @Override
            public void onResponse(@NotNull Call<FavouriteRP> call, @NotNull Response<FavouriteRP> response) {

                try {
                    FavouriteRP favouriteRP = response.body();
                    assert favouriteRP != null;

                    if (favouriteRP.getStatus().equals("1")) {
                        if (favouriteRP.getSuccess().equals("1")) {
                            favouriteIF.isFavourite(favouriteRP.getIs_favourite(), favouriteRP.getMsg());
                        } else {
                            favouriteIF.isFavourite("", favouriteRP.getMsg());
                        }
                        Toast.makeText(activity, favouriteRP.getMsg(), Toast.LENGTH_SHORT).show();
                    } else if (favouriteRP.getStatus().equals("2")) {
                        suspend(favouriteRP.getMessage());
                    } else {
                        alertBox(favouriteRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    alertBox(activity.getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<FavouriteRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                alertBox(activity.getResources().getString(R.string.failed_try_again));
            }
        });

    }

    //alert message box
    public void alertBox(String message) {

        try {
            if (activity != null) {
                if (!activity.isFinishing()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
                    builder.setMessage(Html.fromHtml(message));
                    builder.setCancelable(false);
                    builder.setPositiveButton(activity.getResources().getString(R.string.ok),
                            (arg0, arg1) -> {

                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        } catch (Exception e) {
            Log.d("error_message", e.toString());
        }

    }

    //alert message box
    public void alertBoxDeleteLogin(String message) {

        try {
            if (activity != null) {
                if (!activity.isFinishing()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
                    builder.setMessage(Html.fromHtml(message));
                    builder.setCancelable(false);
                    builder.setPositiveButton(activity.getResources().getString(R.string.ok),
                            (arg0, arg1) -> {
                                editor.putBoolean(prefLogin, false);
                                editor.commit();
                                activity.startActivity(new Intent(activity, Login.class));
                                activity.finishAffinity();

                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        } catch (Exception e) {
            Log.d("error_message", e.toString());
        }

    }

    //alert message box suspend user account
    public void suspend(String message) {

        if (isLogin()) {

            String typeLogin = pref.getString(loginType, "");
            if (typeLogin.equals("google")) {

                //Google login
                GoogleSignInClient mGoogleSignInClient;

                // Configure sign-in to request the user's ID, email address, and basic
                // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

                // Build a GoogleSignInClient with the options specified by gso.
                mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);

                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
            }

            editor.putBoolean(prefLogin, false);
            editor.commit();
            Events.Login loginNotify = new Events.Login("");
            GlobalBus.getBus().post(loginNotify);
        }

        try {
            if (activity != null) {
                if (!activity.isFinishing()) {
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
                    builder.setMessage(Html.fromHtml(message));
                    builder.setCancelable(false);
                    builder.setPositiveButton(activity.getResources().getString(R.string.ok),
                            (arg0, arg1) -> {
                                activity.startActivity(new Intent(activity, Login.class));
                                activity.finishAffinity();
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        } catch (Exception e) {
            Log.d("error_message", e.toString());
        }

    }

    //view count and user video like format
    public String format(Number number) {
        char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
        long numValue = number.longValue();
        int value = (int) Math.floor(Math.log10(numValue));
        int base = value / 3;
        if (value >= 3 && base < suffix.length) {
            return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
        } else {
            return new DecimalFormat("#,##0").format(numValue);
        }
    }

    //check dark mode or not
    public boolean isDarkMode() {
        int currentNightMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                return true;
            default:
                return false;
        }
    }

    public String webViewText() {
        String color;
        if (isDarkMode()) {
            color = Constant.webTextDark;
        } else {
            color = Constant.webTextLight;
        }
        return color;
    }

    public String webViewLink() {
        String color;
        if (isDarkMode()) {
            color = Constant.webLinkDark;
        } else {
            color = Constant.webLinkLight;
        }
        return color;
    }

    public String isWebViewTextRtl() {
        String isRtl;
        if (isRtl()) {
            isRtl = "rtl";
        } else {
            isRtl = "ltr";
        }
        return isRtl;
    }

}
