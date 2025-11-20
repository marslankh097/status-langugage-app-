package com.example.status.util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.applovin.mediation.ads.MaxAdView;
import com.example.status.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.startapp.sdk.ads.banner.Banner;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.wortise.ads.banner.BannerAd;

public class BannerAds {
    public static void showBannerAds(Context context, LinearLayout mAdViewLayout) {
        if (Constant.appRP.isBanner_ad()) {
            switch (Constant.appRP.getAd_network()) {
                case "admob":
                    AdView mAdView = new AdView(context);
                    mAdView.setAdSize(AdSize.BANNER);
                    mAdView.setAdUnitId(Constant.appRP.getBanner_ad_id());
                    AdRequest.Builder builder = new AdRequest.Builder();
                    mAdView.loadAd(builder.build());
                    mAdViewLayout.addView(mAdView);
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case "facebook":
                    com.facebook.ads.AdView adView = new com.facebook.ads.AdView(context, Constant.appRP.getBanner_ad_id(), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
                    adView.loadAd();
                    mAdViewLayout.addView(adView);
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case "startapp":
                    Banner startAppBanner = new Banner(context);
                    mAdViewLayout.addView(startAppBanner);
                    startAppBanner.loadAd();
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case "applovins":
                    MaxAdView maxAdView = new MaxAdView(Constant.appRP.getBanner_ad_id(), context);
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int heightPx = context.getResources().getDimensionPixelSize(R.dimen.applovin_banner_height);
                    maxAdView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                    maxAdView.loadAd();
                    mAdViewLayout.addView(maxAdView);
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case "unityds":
                    BannerView bottomBanner = new BannerView((Activity) context, Constant.appRP.getBanner_ad_id(), new UnityBannerSize(320, 50));
                    bottomBanner.load();
                    mAdViewLayout.addView(bottomBanner);
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case "wortise":
                    BannerAd mBannerAd = new BannerAd(context);
                    mBannerAd.setAdSize(com.wortise.ads.AdSize.HEIGHT_50);
                    mBannerAd.setAdUnitId(Constant.appRP.getBanner_ad_id());
                    mAdViewLayout.addView(mBannerAd);
                    mBannerAd.loadAd();
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
            }
        } else {
            mAdViewLayout.setVisibility(View.GONE);
        }
    }
}
