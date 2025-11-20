package com.example.status.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.manager.SupportRequestManagerFragment;
import com.example.status.BuildConfig;
import com.example.status.R;
import com.example.status.fragment.CategoryFragment;
import com.example.status.fragment.HomeMainFragment;
import com.example.status.fragment.ProfileFragment;
import com.example.status.fragment.RewardPointFragment;
import com.example.status.fragment.SCDetailFragment;
import com.example.status.fragment.SubCategoryFragment;
import com.example.status.interfaces.FullScreen;
import com.example.status.response.AppRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.BannerAds;
import com.example.status.util.Constant;
import com.example.status.util.Events;
import com.example.status.util.GDPRChecker;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.example.status.util.NotificationTiramisu;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startapp.sdk.adsbase.StartAppSDK;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("StaticFieldLeak")
public class MainActivity extends AppCompatActivity {

    private Method method;
    public static MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private LinearLayout linearLayout;
    private ConstraintLayout conBottomNav;
    private boolean doubleBackToExitPressedOnce = false;
    private String id, type = "", statusType, title;
    ImageView[] imageViews;
    TextView[] textViews;
    LinearLayout[] linearLayouts;
    LinearLayout frameHome, frameReward, frameUpload, frameCat, frameProfile;
    ImageView imageHome, imageReward, imageUpload, imageCat, imageProfile;
    TextView tvHome, tvReward, tvUpload, tvCat, tvProfile;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    "com.example.status", //Insert your own package name.
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {

        }

        GlobalBus.getBus().register(this);

        FullScreen fullScreen = isFull -> checkFullScreen(isFull);

        method = new Method(MainActivity.this, null, null, fullScreen);
        method.forceRTLIfSupported();

        if (getIntent().hasExtra("type")) {
            id = getIntent().getStringExtra("id");
            type = getIntent().getStringExtra("type");
            statusType = getIntent().getStringExtra("status_type");
            title = getIntent().getStringExtra("title");
        }

        toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(toolbar);

        linearLayout = findViewById(R.id.ll_adView_main);
        progressBar = findViewById(R.id.progressbar_main);
        conBottomNav = findViewById(R.id.con_bottomNav_main);

        frameHome = findViewById(R.id.frameHome);
        frameReward = findViewById(R.id.frameReward);
        frameUpload = findViewById(R.id.frameUpload);
        frameCat = findViewById(R.id.frameCat);
        frameProfile = findViewById(R.id.frameProfile);

        imageHome = findViewById(R.id.imageHome);
        imageReward = findViewById(R.id.imageReward);
        imageUpload = findViewById(R.id.imageUpload);
        imageCat = findViewById(R.id.imageCat);
        imageProfile = findViewById(R.id.imageProfile);

        tvHome = findViewById(R.id.tvHome);
        tvReward = findViewById(R.id.tvReward);
        tvUpload = findViewById(R.id.tvUpload);
        tvCat = findViewById(R.id.tvCat);
        tvProfile = findViewById(R.id.tvProfile);

        linearLayouts = new LinearLayout[]{frameHome, frameReward, frameUpload, frameCat, frameProfile};
        imageViews = new ImageView[]{imageHome, imageReward, imageUpload, imageCat, imageProfile};
        textViews = new TextView[]{tvHome, tvReward, tvUpload, tvCat, tvProfile};

        NotificationTiramisu.takePermission(this);

        if (type.equals("payment_withdraw")) {
            selectBottomNav(1);
        } else if (type.equals("category")) {
            selectBottomNav(3);
        } else {
            selectBottomNav(0);
        }

        if (method.isNetworkAvailable()) {
            appDetail();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
            progressBar.setVisibility(View.GONE);
        }

        selectBottomNav(0);
        stopPlaying();
        frameHome.setOnClickListener(v -> {
            selectBottomNav(0);
            stopPlaying();
            backStackRemove();
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new HomeMainFragment(), getResources().getString(R.string.home)).commitAllowingStateLoss();
        });

        frameReward.setOnClickListener(v -> {
            selectBottomNav(1);
            stopPlaying();
            backStackRemove();
            RewardPointFragment rewardPointFragment_nav = new RewardPointFragment();
            Bundle bundle = new Bundle();
            bundle.putString("type", type);
            rewardPointFragment_nav.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, rewardPointFragment_nav, getResources().getString(R.string.reward_point)).commit();
            type = "";
        });

        frameUpload.setOnClickListener(v -> {
            stopPlaying();
            startActivity(new Intent(MainActivity.this, UploadStatus.class));
        });

        frameCat.setOnClickListener(v -> {
            selectBottomNav(3);
            stopPlaying();
            backStackRemove();
            CategoryFragment categoryFragment = new CategoryFragment();
            Bundle bundle = new Bundle();
            bundle.putString("type", "drawer_category");
            categoryFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, categoryFragment, getResources().getString(R.string.category)).commit();

        });

        frameProfile.setOnClickListener(v -> {
            selectBottomNav(4);
            stopPlaying();
            backStackRemove();
            ProfileFragment profileFragment = new ProfileFragment();
            Bundle bundle_profile = new Bundle();
            bundle_profile.putString("type", "user");
            bundle_profile.putString("id", method.userId());
            profileFragment.setArguments(bundle_profile);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, profileFragment, getResources().getString(R.string.profile)).commit();
        });

    }

    public void backStackRemove() {
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onBackPressed() {


        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            String title;
            if (!(getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount()) instanceof SupportRequestManagerFragment)) {
                title = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount()).getTag();
            } else {
                title = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount() - 1).getTag();
            }
            if (title != null) {
                method.ShowFullScreen(false);
                toolbar.setTitle(title);

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                getWindow().clearFlags(1024);

                stopPlaying();

            }
            super.onBackPressed();
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.Please_click_BACK_again_to_exit), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    public void stopPlaying() {
        Events.StopPlay stopPlay = new Events.StopPlay("");
        GlobalBus.getBus().post(stopPlay);
    }

    private void selectBottomNav(int pos) {
        for (int i = 0; i < linearLayouts.length; i++) {
            if (i == pos) {
                textViews[i].setTextColor(ContextCompat.getColor(MainActivity.this, R.color.active_bottomBar));
                imageViews[i].setColorFilter(getResources().getColor(R.color.active_bottomBar), PorterDuff.Mode.SRC_IN);
            } else if (i==2){
               // textViews[i].setTextColor(ContextCompat.getColor(MainActivity.this, R.color.inactive_bottomBar));
               // imageViews[i].setColorFilter(getResources().getColor(R.color.inactive_bottomBar), PorterDuff.Mode.SRC_IN);
            }else {
                textViews[i].setTextColor(ContextCompat.getColor(MainActivity.this, R.color.inactive_bottomBar));
                imageViews[i].setColorFilter(getResources().getColor(R.color.inactive_bottomBar), PorterDuff.Mode.SRC_IN);

            }
        }
    }


    public void appDetail() {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(MainActivity.this));
        jsObj.addProperty("method_name", "app_settings");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<AppRP> call = apiService.getAppData(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<AppRP>() {
            @Override
            public void onResponse(@NotNull Call<AppRP> call, @NotNull Response<AppRP> response) {

                try {
                    Constant.appRP = response.body();
                    assert Constant.appRP != null;

                    if (Constant.appRP.getStatus().equals("1")) {

                        if (Constant.appRP.getSuccess().equals("1")) {

                            if (Constant.appRP.getApp_update_status().equals("true") && Constant.appRP.getApp_new_version() > BuildConfig.VERSION_CODE) {
                                showUpdateDialog(Constant.appRP.getApp_update_desc(),
                                        Constant.appRP.getApp_redirect_url(),
                                        Constant.appRP.getCancel_update_status());
                            }

                            if (!Constant.appRP.getInterstitial_ad_click().equals("")) {
                                Constant.AD_COUNT_SHOW = Integer.parseInt(Constant.appRP.getInterstitial_ad_click());
                            }

                            if (!Constant.appRP.getRewarded_video_click().equals("")) {
                                Constant.REWARD_VIDEO_AD_COUNT_SHOW = Integer.parseInt(Constant.appRP.getRewarded_video_click());
                            }

                            if (Constant.appRP.getAd_network().equals("admob")) {
                                checkForConsent();
                            } else if (Constant.appRP.getAd_network().equals("startapp") && method.isStartAppGDPR()) {
                                checkForConsentStartApp();
                            } else {
                                BannerAds.showBannerAds(MainActivity.this, linearLayout);
                            }

                            switch (type) {
                                case "payment_withdraw":
                                    try {
                                        stopPlaying();
                                        //backStackRemove();
                                        toolbar.setTitle(getResources().getString(R.string.reward_point));
                                        RewardPointFragment rewardPointFragment = new RewardPointFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("type", type);
                                        rewardPointFragment.setArguments(bundle);
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, rewardPointFragment, getResources().getString(R.string.reward_point)).commit();
                                        type = "";
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, getResources().getString(R.string.wrong), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case "category":
                                    try {
                                        SubCategoryFragment subCategoryFragment = new SubCategoryFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("id", id);
                                        bundle.putString("category_name", title);
                                        bundle.putString("type", "category");
                                        subCategoryFragment.setArguments(bundle);
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, subCategoryFragment, title).commitAllowingStateLoss();
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, getResources().getString(R.string.wrong), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                case "single_status":
                                    try {
                                        SCDetailFragment scDetailFragment = new SCDetailFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("id", id);
                                        bundle.putString("type", "notification");
                                        bundle.putInt("position", 0);//dummy value
                                        bundle.putString("status_type", statusType);//status type value
                                        scDetailFragment.setArguments(bundle);
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, scDetailFragment, title).commitAllowingStateLoss();
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, getResources().getString(R.string.wrong), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                                default:
                                    try {
                                        HomeMainFragment homeMainFragment = new HomeMainFragment();
                                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, homeMainFragment, getResources().getString(R.string.home)).commitAllowingStateLoss();
                                    } catch (Exception e) {
                                        Toast.makeText(MainActivity.this, getResources().getString(R.string.wrong), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
                            }

                        } else {
                            method.alertBox(Constant.appRP.getMsg());
                        }

                    } else {
                        method.alertBox(Constant.appRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<AppRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    public void checkForConsent() {
        new GDPRChecker()
                .withContext(MainActivity.this)
                .check();
        BannerAds.showBannerAds(MainActivity.this, linearLayout);
    }

    public void checkForConsentStartApp() {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_startapp_gdpr);
        dialog.setCancelable(false);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

        MaterialButton buttonYes = dialog.findViewById(R.id.button_update_dialog_update);
        MaterialButton buttonNo = dialog.findViewById(R.id.button_cancel_dialog_update);

        buttonYes.setOnClickListener(v -> {
            StartAppSDK.setUserConsent(MainActivity.this,
                    "pas",
                    System.currentTimeMillis(),
                    true);
            method.setFirstStartAppGDPR(false);
            dialog.dismiss();
        });

        buttonNo.setOnClickListener(v -> {
            StartAppSDK.setUserConsent(MainActivity.this,
                    "pas",
                    System.currentTimeMillis(),
                    false);
            method.setFirstStartAppGDPR(false);
            dialog.dismiss();
        });
        BannerAds.showBannerAds(MainActivity.this, linearLayout);
        dialog.show();

    }

    @Subscribe
    public void getFullscreen(Events.FullScreenNotify fullScreenNotify) {
        checkFullScreen(fullScreenNotify.isFullscreen());
    }

    @Subscribe
    public void onEvent(Events.CategoryHome categoryHome){
        selectBottomNav(3);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onPause() {
        stopPlaying();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().clearFlags(1024);
        checkFullScreen(false);
        super.onPause();
    }

    public void checkFullScreen(boolean isFull) {
        if (isFull) {
            toolbar.setVisibility(View.GONE);
            conBottomNav.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
        } else {
            toolbar.setVisibility(View.VISIBLE);
            conBottomNav.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
        }
    }

    public void status() {
        startActivity(new Intent(MainActivity.this, UploadStatus.class));
    }

    private void showUpdateDialog(String description, String link, String isCancel) {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_app);
        dialog.setCancelable(false);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

        MaterialTextView textViewDescription = dialog.findViewById(R.id.textView_description_dialog_update);
        MaterialButton buttonUpdate = dialog.findViewById(R.id.button_update_dialog_update);
        MaterialButton buttonCancel = dialog.findViewById(R.id.button_cancel_dialog_update);

        if (isCancel.equals("true")) {
            buttonCancel.setVisibility(View.VISIBLE);
        } else {
            buttonCancel.setVisibility(View.GONE);
        }
        textViewDescription.setText(description);

        buttonUpdate.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }

}
