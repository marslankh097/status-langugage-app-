package com.example.status.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.status.R;
import com.example.status.response.ProfileStatusRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Method;
import com.example.status.util.NotificationTiramisu;
import com.example.status.util.YouApplication;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SettingActivity extends AppCompatActivity {

    private Method method;
    MaterialToolbar toolbar;
    private String them_mode;
    private ProgressDialog progressDialog;
    SwitchMaterial switchMaterial;
    RelativeLayout rlClearCache, rlTheme, rlContact, rlFaq, rlVerify, rlEarn,rlLan, rlShare, rlRate, rlMore, rlPrivacy, rlAbout;
    TextView tvSettingClearCacheSize, tvSettingThemeName;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        method = new Method(SettingActivity.this);
        method.forceRTLIfSupported();

        toolbar = findViewById(R.id.toolbar_about_us);
        toolbar.setTitle(getResources().getString(R.string.setting));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        method = new Method(this);

        progressDialog = new ProgressDialog(SettingActivity.this);

        switchMaterial = findViewById(R.id.switch_setting);
        rlClearCache = findViewById(R.id.rlClearCache);
        rlTheme = findViewById(R.id.rlTheme);
        rlContact = findViewById(R.id.rlContact);
        rlFaq = findViewById(R.id.rlFaq);
        rlVerify = findViewById(R.id.rlVerify);
        rlEarn = findViewById(R.id.rlEarn);
        rlShare = findViewById(R.id.rlShare);
        rlRate = findViewById(R.id.rlRate);
        rlMore = findViewById(R.id.rlMore);
        rlPrivacy = findViewById(R.id.rlPrivacy);
        rlAbout = findViewById(R.id.rlAbout);
        tvSettingClearCacheSize = findViewById(R.id.tvSettingClearCacheSize);
        tvSettingThemeName = findViewById(R.id.tvSettingThemeName);
        rlLan=findViewById(R.id.rlLan);

        switch (method.getTheme()) {
            case "system":
                tvSettingThemeName.setText(getResources().getString(R.string.system_default));
                break;
            case "light":
                tvSettingThemeName.setText(getResources().getString(R.string.light));
                break;
            case "dark":
                tvSettingThemeName.setText(getResources().getString(R.string.dark));
                break;
            default:
                break;
        }

        initializeCache();
        rlClearCache.setOnClickListener(v -> {
            FileUtils.deleteQuietly(getCacheDir());
            FileUtils.deleteQuietly(getExternalCacheDir());
            tvSettingClearCacheSize.setText("0 MB");
        });

        switchMaterial.setChecked(NotificationTiramisu.isNotificationChecked(SettingActivity.this));//myApplication.getNotification()
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                NotificationTiramisu.takePermissionSettings(SettingActivity.this, switchMaterial, activityResultLauncher);
            } else {
                OneSignal.disablePush(false);
                YouApplication.getInstance().saveIsNotification(false);
                OneSignal.unsubscribeWhenNotificationsAreDisabled(false);
            }
        });

        rlLan.setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, Language.class)
                .putExtra("type", "menu")));

        rlShare.setOnClickListener(v -> shareApp());

        rlRate.setOnClickListener(v -> rateApp());

        rlMore.setOnClickListener(v -> moreApp());

        rlAbout.setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, AboutUs.class)));

        rlPrivacy.setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, PrivacyPolicy.class)));

        rlContact.setOnClickListener(v -> {
                    startActivity(new Intent(SettingActivity.this, ContactUs.class));
        });

        rlFaq.setOnClickListener(v -> startActivity(new Intent(SettingActivity.this, Faq.class)));

        rlEarn.setOnClickListener(v -> {
            if (method.isNetworkAvailable()) {
                if (method.isLogin()) {
                    startActivity(new Intent(SettingActivity.this, PointDetail.class));
                } else {
                    method.alertBox(getResources().getString(R.string.you_have_not_login));
                }
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        });

        rlVerify.setOnClickListener(v -> {
            if (method.isNetworkAvailable()) {
                if (method.isLogin()) {
                    request(method.userId());
                } else {
                    method.alertBox(getResources().getString(R.string.you_have_not_login));
                }
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        });

        rlTheme.setOnClickListener(v -> {

            Dialog dialog = new Dialog(SettingActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialogbox_them);
            if (method.isRtl()) {
                dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
            dialog.getWindow().setLayout(ViewPager.LayoutParams.FILL_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup_them);
            MaterialTextView textViewOk = dialog.findViewById(R.id.textView_ok_them);
            MaterialTextView textViewCancel = dialog.findViewById(R.id.textView_cancel_them);

            switch (method.getTheme()) {
                case "system":
                    radioGroup.check(radioGroup.getChildAt(0).getId());
                    break;
                case "light":
                    radioGroup.check(radioGroup.getChildAt(1).getId());
                    break;
                case "dark":
                    radioGroup.check(radioGroup.getChildAt(2).getId());
                    break;
                default:
                    break;
            }

            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                MaterialRadioButton rb = group.findViewById(checkedId);
                if (null != rb && checkedId > -1) {
                    switch (checkedId) {
                        case R.id.radioButton_system_them:
                            them_mode = "system";
                            break;
                        case R.id.radioButton_light_them:
                            them_mode = "light";
                            break;
                        case R.id.radioButton_dark_them:
                            them_mode = "dark";
                            break;
                        default:
                            break;
                    }
                }
            });

            textViewOk.setOnClickListener(vTextViewOk -> {
                method.editor.putString(method.themSetting, them_mode);
                method.editor.commit();
                dialog.dismiss();

                startActivity(new Intent(SettingActivity.this, SplashScreen.class));
                finishAffinity();

            });

            textViewCancel.setOnClickListener(vTextViewCancel -> dialog.dismiss());

            dialog.show();

        });
    }

    private void request(String userId) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(SettingActivity.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "profile_status");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<ProfileStatusRP> call = apiService.getProfileStatus(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<ProfileStatusRP>() {
            @Override
            public void onResponse(@NotNull Call<ProfileStatusRP> call, @NotNull Response<ProfileStatusRP> response) {

                try {
                    ProfileStatusRP profileStatusRP = response.body();
                    assert profileStatusRP != null;

                    if (profileStatusRP.getStatus().equals("1")) {

                        if (profileStatusRP.getSuccess().equals("1")) {

                            switch (profileStatusRP.getProfile_status()) {
                                case "0":
                                case "1":
                                case "2":
                                    startActivity(new Intent(SettingActivity.this, AVStatus.class));
                                    break;
                                case "3":
                                    startActivity(new Intent(SettingActivity.this, AccountVerification.class)
                                            .putExtra("name", profileStatusRP.getName()));
                                    break;
                            }

                        } else {
                            method.alertBox(profileStatusRP.getMsg());
                        }

                    } else if (profileStatusRP.getStatus().equals("2")) {
                        method.suspend(profileStatusRP.getMessage());
                    } else {
                        method.alertBox(profileStatusRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<ProfileStatusRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    private void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getApplication().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplication().getPackageName())));
        }
    }

    private void moreApp() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.play_more_app))));
    }

    private void shareApp() {

        try {

            String string = "\n" + getResources().getString(R.string.Let_me_recommend_you_this_application) + "\n\n" + "https://play.google.com/store/apps/details?id=" + getApplication().getPackageName();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, string);
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.choose_one)));

        } catch (Exception e) {
            //e.toString();
        }

    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        NotificationTiramisu.setCheckedFromSetting(SettingActivity.this, switchMaterial);
    });

    private void initializeCache() {
        long size = 0;
        size += getDirSize(this.getCacheDir());
        size += getDirSize(this.getExternalCacheDir());
        tvSettingClearCacheSize.setText(readableFileSize(size));
    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) return "0 Bytes";
        final String[] units = new String[]{"Bytes", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
