package com.example.status.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.adapter.EarnPointAdapter;
import com.example.status.response.PointDetailRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.BannerAds;
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

public class PointDetail extends AppCompatActivity {

    private Method method;
    MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private EarnPointAdapter earnPointAdapter;
    LinearLayout linearLayout;
    private ConstraintLayout conMain, conNoData;
    private LayoutAnimationController animation;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_detail);

        method = new Method(PointDetail.this);
        method.forceRTLIfSupported();

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(PointDetail.this, resId);

        toolbar = findViewById(R.id.toolbar_pd);
        toolbar.setTitle(getResources().getString(R.string.earn_point));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        conMain = findViewById(R.id.con_main_pd);
        conNoData = findViewById(R.id.con_noDataFound);
        progressBar = findViewById(R.id.progressbar_pd);
        recyclerView = findViewById(R.id.recyclerView_pd);
        linearLayout = findViewById(R.id.linearLayout_pd);
        BannerAds.showBannerAds(PointDetail.this, linearLayout);

        progressBar.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);
        conMain.setVisibility(View.GONE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PointDetail.this);
        recyclerView.setLayoutManager(layoutManager);

        if (method.isNetworkAvailable()) {
            Point();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    private void Point() {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(PointDetail.this));
        jsObj.addProperty("method_name", "points_details");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<PointDetailRP> call = apiService.getPointDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<PointDetailRP>() {
            @Override
            public void onResponse(@NotNull Call<PointDetailRP> call, @NotNull Response<PointDetailRP> response) {

                try {
                    PointDetailRP pointDetailRP = response.body();
                    assert pointDetailRP != null;

                    if (pointDetailRP.getStatus().equals("1")) {

                        if (pointDetailRP.getPointDetailLists().size() == 0) {
                            conNoData.setVisibility(View.VISIBLE);
                        } else {
                            earnPointAdapter = new EarnPointAdapter(PointDetail.this, pointDetailRP.getPointDetailLists());
                            recyclerView.setAdapter(earnPointAdapter);
                            recyclerView.setLayoutAnimation(animation);
                        }

                        conMain.setVisibility(View.VISIBLE);

                    } else {
                        method.alertBox(pointDetailRP.getMessage());
                        conNoData.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<PointDetailRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                conNoData.setVisibility(View.VISIBLE);
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
