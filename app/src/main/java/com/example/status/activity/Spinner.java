package com.example.status.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.status.R;
import com.example.status.interfaces.VideoAd;
import com.example.status.response.SpinnerRP;
import com.example.status.response.SubmitSpinnerRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.BannerAds;
import com.example.status.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rubikstudio.library.LuckyWheelView;
import rubikstudio.library.model.LuckyItem;

public class Spinner extends AppCompatActivity {

    private Method method;
    private VideoAd videoAd;
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private List<LuckyItem> spinnerLists;
    private LuckyWheelView luckyWheelView;
    private ImageView imageViewData;
    private ConstraintLayout conNoData, conMain;
    private MaterialButton buttonSpinner, buttonLogin;
    private MaterialTextView textViewNotLogin, textViewMsg;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spinner);

        videoAd = type -> {
            int indexPosition = getRandomIndex();
            luckyWheelView.startLuckyWheelWithTargetIndex(indexPosition);
        };
        method = new Method(Spinner.this, videoAd);
        method.forceRTLIfSupported();
        spinnerLists = new ArrayList<>();

        toolbar = findViewById(R.id.toolbar_spinner);
        toolbar.setTitle(getResources().getString(R.string.spinner));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(Spinner.this);

        progressBar = findViewById(R.id.progressbar_spinner);
        imageViewData = findViewById(R.id.imageView_not_login);
        buttonLogin = findViewById(R.id.button_not_login);
        textViewNotLogin = findViewById(R.id.textView_not_login);
        conNoData = findViewById(R.id.con_not_login);
        luckyWheelView = findViewById(R.id.luckyWheel_spinner);
        buttonSpinner = findViewById(R.id.button_spinner);
        conMain = findViewById(R.id.con_main_spinner);
        textViewMsg = findViewById(R.id.textView_msg_spinner);
        LinearLayout linearLayout = findViewById(R.id.linearLayout_spinner);
        BannerAds.showBannerAds(Spinner.this, linearLayout);

        progressBar.setVisibility(View.GONE);
        conMain.setVisibility(View.GONE);
        data(false, false);

        buttonLogin.setOnClickListener(v -> {
            startActivity(new Intent(Spinner.this, Login.class));
            finishAffinity();
        });

        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                SpinnerData(method.userId());
            } else {
                data(true, true);
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void data(boolean isShow, boolean isLogin) {
        if (isShow) {
            if (isLogin) {
                buttonLogin.setVisibility(View.VISIBLE);
                textViewNotLogin.setText(getResources().getString(R.string.you_have_not_login));
                imageViewData.setImageDrawable(getResources().getDrawable(R.drawable.no_login));
            } else {
                buttonLogin.setVisibility(View.GONE);
                textViewNotLogin.setText(getResources().getString(R.string.no_data_found));
                imageViewData.setImageDrawable(getResources().getDrawable(R.drawable.no_data));
            }
            conNoData.setVisibility(View.VISIBLE);
        } else {
            conNoData.setVisibility(View.GONE);
        }
    }

    private void SpinnerData(String userId) {

        spinnerLists.clear();
        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Spinner.this));
        jsObj.addProperty("method_name", "get_spinner");
        jsObj.addProperty("user_id", userId);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<SpinnerRP> call = apiService.getSpinnerData(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<SpinnerRP>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call<SpinnerRP> call, @NotNull Response<SpinnerRP> response) {

                try {
                    SpinnerRP spinnerRP = response.body();
                    assert spinnerRP != null;

                    if (spinnerRP.getStatus().equals("1")) {

                        String msgOne = getResources().getString(R.string.daily_total_spins);
                        String msgTwo = getResources().getString(R.string.remaining_spins_today);

                        textViewMsg.setText(msgOne + " " + spinnerRP.getDaily_spinner_limit() + " "
                                + msgTwo + " " + spinnerRP.getRemain_spin());

                        for (int i = 0; i < spinnerRP.getSpinnerLists().size(); i++) {

                            LuckyItem objItem = new LuckyItem();
                            objItem.text = spinnerRP.getSpinnerLists().get(i).getPoints();
                            objItem.icon = R.drawable.coins;
                            objItem.color = Color.parseColor(spinnerRP.getSpinnerLists().get(i).getBg_color());

                            spinnerLists.add(objItem);
                        }

                        if (spinnerLists.size() != 0) {
                            luckyWheelView.setData(spinnerLists);
                            luckyWheelView.setRound(2);

                            if (spinnerRP.getRemain_spin().equals("0")) {
                                buttonSpinner.setVisibility(View.GONE);
                            } else {
                                buttonSpinner.setVisibility(View.VISIBLE);
                            }

                            conMain.setVisibility(View.VISIBLE);

                            buttonSpinner.setOnClickListener(view -> {
                                if (spinnerRP.getAd_on_spin().equals("true")) {
                                    method.VideoAdDialog("spinner", "view_ad");
                                } else {
                                    method.VideoAdDialog("spinner", "");
                                }
                            });

                            luckyWheelView.setLuckyRoundItemSelectedListener(index -> {
                                if (index != 0) {
                                    index = index - 1;
                                }
                                int pointAdd = Integer.parseInt(spinnerLists.get(index).text);
                                if (pointAdd != 0) {
                                    sendSpinnerData(userId, pointAdd);
                                }
                            });
                        } else {
                            data(true, false);

                        }

                    } else if (spinnerRP.getStatus().equals("2")) {
                        method.suspend(spinnerRP.getMessage());
                    } else {
                        method.alertBox(spinnerRP.getMessage());
                        conNoData.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<SpinnerRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                conNoData.setVisibility(View.VISIBLE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    private void sendSpinnerData(String userId, int point) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Spinner.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("ponints", String.valueOf(point));
        jsObj.addProperty("method_name", "save_spinner_points");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<SubmitSpinnerRP> call = apiService.submitSpinnerData(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<SubmitSpinnerRP>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NotNull Call<SubmitSpinnerRP> call, @NotNull Response<SubmitSpinnerRP> response) {

                try {
                    SubmitSpinnerRP submitSpinnerRP = response.body();
                    assert submitSpinnerRP != null;

                    if (submitSpinnerRP.getStatus().equals("1")) {

                        String msgOne = getResources().getString(R.string.daily_total_spins);
                        String msgTwo = getResources().getString(R.string.remaining_spins_today);

                        textViewMsg.setText(msgOne + " " + submitSpinnerRP.getDaily_spinner_limit() + " "
                                + msgTwo + " " + submitSpinnerRP.getRemain_spin());

                        if (submitSpinnerRP.getSuccess().equals("1")) {
                            if (submitSpinnerRP.getRemain_spin().equals("0")) {
                                buttonSpinner.setVisibility(View.GONE);
                            } else {
                                buttonSpinner.setVisibility(View.VISIBLE);
                            }
                        } else {
                            buttonSpinner.setVisibility(View.GONE);
                        }

                        method.alertBox(submitSpinnerRP.getMsg());

                    } else if (submitSpinnerRP.getStatus().equals("2")) {
                        method.suspend(submitSpinnerRP.getMessage());
                    }else {
                        method.alertBox(submitSpinnerRP.getMessage());
                        conNoData.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<SubmitSpinnerRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                conNoData.setVisibility(View.VISIBLE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    private int getRandomIndex() {
        Random rand = new Random();
        return rand.nextInt(spinnerLists.size() - 1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
