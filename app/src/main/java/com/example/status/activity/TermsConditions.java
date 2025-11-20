package com.example.status.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.status.R;
import com.example.status.response.TermsConditionsRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TermsConditions extends AppCompatActivity {

    private Method method;
    public MaterialToolbar toolbar;
    private WebView webView;
    private ProgressBar progressBar;
    private ConstraintLayout conNoData;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        method = new Method(TermsConditions.this);
        method.forceRTLIfSupported();

        toolbar = findViewById(R.id.toolbar_terms);
        toolbar.setTitle(getResources().getString(R.string.terms_condition));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressBar = findViewById(R.id.progressBar_terms);
        conNoData = findViewById(R.id.con_noDataFound);
        webView = findViewById(R.id.webView_terms);

        conNoData.setVisibility(View.GONE);

        if (method.isNetworkAvailable()) {
            getTerms();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    public void getTerms() {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(TermsConditions.this));
        jsObj.addProperty("method_name", "app_term_condition");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<TermsConditionsRP> call = apiService.getTermsCondition(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<TermsConditionsRP>() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onResponse(@NotNull Call<TermsConditionsRP> call, @NotNull Response<TermsConditionsRP> response) {

                try {
                    TermsConditionsRP termsConditionsRP = response.body();
                    assert termsConditionsRP != null;

                    if (termsConditionsRP.getStatus().equals("1")) {

                        webView.setBackgroundColor(Color.TRANSPARENT);
                        webView.setFocusableInTouchMode(false);
                        webView.setFocusable(false);
                        webView.getSettings().setDefaultTextEncodingName("UTF-8");
                        webView.getSettings().setJavaScriptEnabled(true);
                        String mimeType = "text/html";
                        String encoding = "utf-8";

                        String text = "<html dir=" + method.isWebViewTextRtl() + "><head>"
                                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/opensans_semi_bold.otf\")}body{font-family: MyFont;color: " + method.webViewText() + "line-height:1.6}"
                                + "a {color:" + method.webViewLink() + "text-decoration:none}"
                                + "</style></head>"
                                + "<body>"
                                + termsConditionsRP.getApp_term_condition()
                                + "</body></html>";

                        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(termsConditionsRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<TermsConditionsRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                conNoData.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
