package com.example.status.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;

import androidx.multidex.MultiDex;

import com.example.status.R;
import com.example.status.activity.SplashScreen;
import com.google.android.gms.ads.MobileAds;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

import org.json.JSONException;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class YouApplication extends Application {

    private static YouApplication mInstance;
    public SharedPreferences preferences;
    public String prefName = "app";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    public YouApplication() {
        mInstance = this;
    }

    public static synchronized YouApplication getInstance() {
        return mInstance;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(YouApplication.this, initializationStatus -> {

        });

        //OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId("8ae91bea-8990-4b45-83e8-cc4e46478fcb");
        OneSignal.setNotificationOpenedHandler(new NotificationExtenderExample());

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/opensans_semi_bold.otf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

    }

    public void saveIsNotification(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        Editor editor = preferences.edit();
        editor.putBoolean("IsNotification", flag);
        editor.apply();
    }

    public boolean getNotification() {
        preferences = this.getSharedPreferences(prefName, 0);
        return preferences.getBoolean("IsNotification", true);
    }

    class NotificationExtenderExample implements OneSignal.OSNotificationOpenedHandler {

        private String id;
        private String statusType;
        private String titleName;

        @Override
        public void notificationOpened(OSNotificationOpenedResult result) {

            try {
                String url = result.getNotification().getAdditionalData().getString("external_link");
                String type = result.getNotification().getAdditionalData().getString("type");
                switch (type) {
                    case "single_status":
                        id = result.getNotification().getAdditionalData().getString("id");
                        statusType = result.getNotification().getAdditionalData().getString("status_type");
                        titleName = result.getNotification().getAdditionalData().getString("title");
                        break;
                    case "category":
                        id = result.getNotification().getAdditionalData().getString("id");
                        titleName = result.getNotification().getAdditionalData().getString("title");
                        break;
                    case "account_status":
                        id = result.getNotification().getAdditionalData().getString("id");
                        break;
                }

                Intent intent;
                if (!url.equals("false") && !url.trim().isEmpty()) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(url));
                } else {
                    intent = new Intent(YouApplication.this, SplashScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("type", type);
                    if (type.equals("account_status") || type.equals("single_status") || type.equals("category")) {
                        intent.putExtra("id", id);
                    }
                    if (type.equals("single_status")) {
                        intent.putExtra("status_type", statusType);
                        intent.putExtra("title", titleName);
                    }
                    if (type.equals("category")) {
                        intent.putExtra("title", titleName);
                    }
                }
                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
