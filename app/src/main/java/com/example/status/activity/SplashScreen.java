package com.example.status.activity;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.example.status.R;
import com.example.status.response.AppRP;
import com.example.status.response.LoginRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Constant;
import com.example.status.util.Method;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.wortise.ads.WortiseSdk;
import com.wortise.ads.consent.ConsentManager;

import org.jetbrains.annotations.NotNull;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {

    static int SPLASH_TIME_OUT = 1000;
    private Boolean isCancelled = false;
    private Method method;
    private ProgressBar progressBar;
    private String id = "", type = "", statusType = "", title = "";
    //Google login
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        method = new Method(SplashScreen.this);
        method.login();
        method.forceRTLIfSupported();
        switch (method.getTheme()) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                break;
        }

        setContentView(R.layout.activity_splace_screen);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        // making notification bar transparent
        method.changeStatusBarColor();

        progressBar = findViewById(R.id.progressBar_splash_screen);
        progressBar.setVisibility(View.GONE);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (getIntent().hasExtra("type")) {
            type = getIntent().getStringExtra("type");
            assert type != null;
            if (type.equals("single_status")) {
                statusType = getIntent().getStringExtra("status_type");
            }
            if (type.equals("category") || type.equals("single_status")) {
                title = getIntent().getStringExtra("title");
            }
        }

        if (getIntent().hasExtra("id")) {
            id = getIntent().getStringExtra("id");
        }

    appIniDetail();

    }

    public void appIniDetail() {

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(SplashScreen.this));
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
                        initializeAds();
                        splashScreen();
                    } else {
                        method.alertBox(Constant.appRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

            }

            @Override
            public void onFailure(@NotNull Call<AppRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    private void initializeAds() {
        switch (Constant.appRP.getAd_network()) {
            case "unityds":
                UnityAds.initialize(SplashScreen.this, Constant.appRP.getUnity_game_id(), false, new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        Log.d(TAG, "Unity Ads Initialization Complete");
                    }

                    @Override
                    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                        Log.d(TAG, "Unity Ads Initialization Failed: [" + error + "] " + message);
                    }
                });
                break;
            case "applovins":
                AppLovinSdk.getInstance(SplashScreen.this).setMediationProvider(AppLovinMediationProvider.MAX);
                AppLovinSdk.getInstance(SplashScreen.this).initializeSdk(config -> {

                });
                break;
            case "startapp":
                StartAppSDK.init(this, Constant.appRP.getStartapp_app_id(), false);
                StartAppAd.disableSplash();
               // StartAppSDK.setTestAdsEnabled(true);
                 break;
            case "wortise":
                WortiseSdk.initialize(this, Constant.appRP.getWortise_app_id(), () -> {
                    ConsentManager.request(SplashScreen.this);
                    return Unit.INSTANCE;
                });
                break;
        }

    }

    public void splashScreen() {

        if (method.isNetworkAvailable()) {
            new Handler().postDelayed(() -> {
                if (!isCancelled) {
                    switch (type) {
                        case "payment_withdraw":
                            callActivity();
                            break;
                        case "account_verification":
                            startActivity(new Intent(SplashScreen.this, AVStatus.class));
                            finishAffinity();
                            break;
                        case "account_status":
                            startActivity(new Intent(SplashScreen.this, Suspend.class)
                                    .putExtra("id", id));
                            finishAffinity();
                            break;
                        default:
                            if (method.isLogin()) {
                                login();
                            } else {
                                if (method.pref.getBoolean(method.isVerification, false)) {
                                    startActivity(new Intent(SplashScreen.this, Verification.class));
                                    finishAffinity();
                                } else {
                                    if (method.pref.getBoolean(method.showLogin, true)) {
                                        method.editor.putBoolean(method.showLogin, false);
                                        method.editor.commit();
                                        Intent i = new Intent(SplashScreen.this, Login.class);
                                        startActivity(i);
                                        finishAffinity();
                                    } else {
                                        callActivity();
                                    }
                                }
                            }
                            break;
                    }
                }

            }, SPLASH_TIME_OUT);

        } else {
            callActivity();
        }

    }


    public void login() {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(SplashScreen.this));
        jsObj.addProperty("method_name", "user_login");
        jsObj.addProperty("user_id", method.userId());
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<LoginRP> call = apiService.getLoginDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<LoginRP>() {
            @Override
            public void onResponse(@NotNull Call<LoginRP> call, @NotNull Response<LoginRP> response) {

                try {
                    LoginRP loginRP = response.body();
                    assert loginRP != null;

                    if (loginRP.getStatus().equals("1")) {

                        String loginType = method.getLoginType();

                        if (loginRP.getSuccess().equals("1")) {

                            OneSignal.sendTag("user_id", method.userId());
                            OSDeviceState device = OneSignal.getDeviceState();
                            assert device != null;
                            OneSignal.sendTag("player_id", device.getUserId());

                            if (loginType.equals("google")) {
                                if (GoogleSignIn.getLastSignedInAccount(SplashScreen.this) != null) {
                                    callActivity();
                                } else {
                                    method.editor.putBoolean(method.prefLogin, false);
                                    method.editor.commit();
                                    startActivity(new Intent(SplashScreen.this, Login.class));
                                    finishAffinity();
                                }
                            } else {
                                callActivity();
                            }
                        } else {
                            OneSignal.sendTag("user_id", method.userId());

                            if (loginType.equals("google")) {

                                mGoogleSignInClient.signOut()
                                        .addOnCompleteListener(SplashScreen.this, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });


                            }

                            method.editor.putBoolean(method.prefLogin, false);
                            method.editor.commit();
                            startActivity(new Intent(SplashScreen.this, Login.class));
                            finishAffinity();
                        }
                    } else {
                        method.alertBoxDeleteLogin(loginRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<LoginRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    public void callActivity() {
        startActivity(new Intent(SplashScreen.this, MainActivity.class)
                .putExtra("type", type)
                .putExtra("id", id)
                .putExtra("status_type", statusType)
                .putExtra("title", title));
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        isCancelled = true;
        super.onDestroy();
    }

}


