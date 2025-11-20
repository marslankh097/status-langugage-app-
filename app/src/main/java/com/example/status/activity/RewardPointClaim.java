package com.example.status.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.status.R;
import com.example.status.item.PaymentList;
import com.example.status.response.DataRP;
import com.example.status.response.PaymentModeRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.BannerAds;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class RewardPointClaim extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Method method;
    MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private Spinner spinner;
    MaterialButton buttonSubmit;
    private InputMethodManager imm;
    LinearLayout linearLayout;
    private List<PaymentList> paymentLists;
    private TextInputEditText editTextDetail;
    private MaterialCardView cardView;
    private ConstraintLayout conNoData;
    private String paymentType, userId, userPoints;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_point_claim);

        method = new Method(RewardPointClaim.this);
        method.forceRTLIfSupported();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        paymentLists = new ArrayList<>();

        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        userPoints = intent.getStringExtra("user_points");

        progressDialog = new ProgressDialog(RewardPointClaim.this);

        toolbar = findViewById(R.id.toolbar_reward_point_claim);
        toolbar.setTitle(getResources().getString(R.string.payment_detail));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        linearLayout = findViewById(R.id.linearLayout_reward_point_claim);
        BannerAds.showBannerAds(RewardPointClaim.this, linearLayout);

        conNoData = findViewById(R.id.con_noDataFound);
        cardView = findViewById(R.id.cardView_reward_point_claim);
        progressBar = findViewById(R.id.progressBar_reward_point_claim);
        spinner = findViewById(R.id.spinner_reward_point_claim);
        editTextDetail = findViewById(R.id.editText_detail_reward_point_claim);
        buttonSubmit = findViewById(R.id.button_reward_point_claim);

        cardView.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);

        buttonSubmit.setOnClickListener(v -> detail());

        if (method.isNetworkAvailable()) {
            paymentMethod();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //first list item selected by default and sets the preset accordingly
        if (position == 0) {
            ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_upload));
        } else {
            ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_app_color));
        }
        paymentType = paymentLists.get(position).getMode_title();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void detail() {

        String detail = editTextDetail.getText().toString();

        editTextDetail.setError(null);

        if (paymentType.equals(getResources().getString(R.string.select_payment_type)) || paymentType.equals("") || paymentType.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_payment));
        } else if (detail.equals("") || detail.isEmpty()) {
            editTextDetail.requestFocus();
            editTextDetail.setError(getResources().getString(R.string.please_enter_detail));
        } else {

            editTextDetail.clearFocus();
            imm.hideSoftInputFromWindow(editTextDetail.getWindowToken(), 0);

            if (method.isNetworkAvailable()) {
                detailSubmit(userId, userPoints, paymentType, detail);
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }

        }

    }

    public void paymentMethod() {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(RewardPointClaim.this));
        jsObj.addProperty("method_name", "get_payment_mode");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<PaymentModeRP> call = apiService.getPaymentMode(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<PaymentModeRP>() {
            @Override
            public void onResponse(@NotNull Call<PaymentModeRP> call, @NotNull Response<PaymentModeRP> response) {

                try {
                    PaymentModeRP paymentModeRP = response.body();
                    assert paymentModeRP != null;

                    if (paymentModeRP.getStatus().equals("1")) {

                        paymentLists.addAll(paymentModeRP.getPaymentLists());

                        if (paymentLists.size() != 0) {

                            // Spinner Drop down elements
                            List<String> arrayList = new ArrayList<String>();
                            for (int i = 0; i < paymentLists.size(); i++) {
                                arrayList.add(paymentLists.get(i).getMode_title());
                            }

                            spinner.setOnItemSelectedListener(RewardPointClaim.this);

                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(RewardPointClaim.this, android.R.layout.simple_spinner_item, arrayList);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setAdapter(dataAdapter);

                            cardView.setVisibility(View.VISIBLE);

                        }

                    } else {
                        method.alertBox(paymentModeRP.getMessage());
                        conNoData.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<PaymentModeRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                conNoData.setVisibility(View.VISIBLE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }


    public void detailSubmit(final String userId, final String userPoints, String paymentMode, String detail) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(RewardPointClaim.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("user_points", userPoints);
        jsObj.addProperty("payment_mode", paymentMode);
        jsObj.addProperty("bank_details", detail);
        jsObj.addProperty("method_name", "user_redeem_request");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.submitPaymentDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {
                    DataRP dataRP = response.body();
                    assert dataRP != null;

                    if (dataRP.getStatus().equals("1")) {
                        Events.RewardNotify rewardNotify = new Events.RewardNotify("");
                        GlobalBus.getBus().post(rewardNotify);
                        onBackPressed();
                    } else if (dataRP.getStatus().equals("2")) {
                        method.suspend(dataRP.getMessage());
                    } else {
                        method.alertBox(dataRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<DataRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        super.onBackPressed();
    }
}
