package com.example.status.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.adapter.UploadStatusAdapter;
import com.example.status.interfaces.UploadStatusIF;
import com.example.status.interfaces.VideoAd;
import com.example.status.item.UploadStatusList;
import com.example.status.response.DataRP;
import com.example.status.response.UploadStatusOptRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.BannerAds;
import com.example.status.util.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadStatus extends AppCompatActivity {

    private Method method;
    VideoAd videoAd;
    private UploadStatusOptRP uploadStatusOptRP;
    private UploadStatusIF uploadStatusIF;
    private ProgressDialog progressDialog;
    private List<UploadStatusList> uploadStatusLists;
    ImageView imageView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ConstraintLayout conNoData, conMain;
    private UploadStatusAdapter uploadStatusAdapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_status);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        uploadStatusLists = new ArrayList<>();

        videoAd = type -> choseOption(type);
        method = new Method(UploadStatus.this, videoAd);
        method.forceRTLIfSupported();
        // making notification bar transparent
        method.changeStatusBarColor();
        progressDialog = new ProgressDialog(UploadStatus.this);

        progressBar = findViewById(R.id.progressBar_us);
        conNoData = findViewById(R.id.con_noDataFound);
        conMain = findViewById(R.id.con_main_us);
        imageView = findViewById(R.id.imageView_close_us);
        recyclerView = findViewById(R.id.recyclerView_us);
        LinearLayout linearLayout = findViewById(R.id.linearLayout_us);
        BannerAds.showBannerAds(UploadStatus.this, linearLayout);

        conMain.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        GridLayoutManager layoutManager = new GridLayoutManager(UploadStatus.this, 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (uploadStatusAdapter.getItemViewType(position) == 1) {
                    return 2;
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        uploadStatusIF = type -> {

            if (method.isLogin()) {
                checkUploadLimit(method.userId(), type);
            } else {
                Method.loginBack = true;
                startActivity(new Intent(UploadStatus.this, Login.class));
            }

        };

        imageView.setOnClickListener(v -> onBackPressed());

        if (method.isNetworkAvailable()) {
            upload();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    public void upload() {

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(UploadStatus.this));
        jsObj.addProperty("method_name", "upload_status_opt");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<UploadStatusOptRP> call = apiService.getUploadStatusOPT(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<UploadStatusOptRP>() {
            @Override
            public void onResponse(@NotNull Call<UploadStatusOptRP> call, @NotNull Response<UploadStatusOptRP> response) {

                try {

                    uploadStatusOptRP = response.body();

                    assert uploadStatusOptRP != null;
                    if (uploadStatusOptRP.getStatus().equals("1")) {

                        if (uploadStatusOptRP.getSuccess().equals("1")) {

                            if (uploadStatusOptRP.getVideo_upload_opt().equals("true")) {
                                uploadStatusLists.add(new UploadStatusList(getResources().getString(R.string.upload_video), "video",
                                        R.drawable.video_status_ic, R.drawable.line_video_us));
                            }
                            if (uploadStatusOptRP.getQuotes_upload_opt().equals("true")) {
                                uploadStatusLists.add(new UploadStatusList(getResources().getString(R.string.upload_quotes), "quote",
                                        R.drawable.quotes_status_ic, R.drawable.line_quotes_us));
                            }
                            if (uploadStatusOptRP.getImage_upload_opt().equals("true")) {
                                uploadStatusLists.add(new UploadStatusList(getResources().getString(R.string.upload_image), "image",
                                        R.drawable.photos_status_ic, R.drawable.line_image_us));
                            }
                            if (uploadStatusOptRP.getGif_upload_opt().equals("true")) {
                                uploadStatusLists.add(new UploadStatusList(getResources().getString(R.string.upload_gif), "gif",
                                        R.drawable.gif_status_ic, R.drawable.line_gif_us));
                            }

                            if (uploadStatusLists.size() != 0) {
                                conMain.setVisibility(View.VISIBLE);
                                uploadStatusAdapter = new UploadStatusAdapter(UploadStatus.this, uploadStatusLists, uploadStatusIF);
                                recyclerView.setAdapter(uploadStatusAdapter);
                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                            }

                        } else {
                            method.alertBox(uploadStatusOptRP.getMsg());
                            conNoData.setVisibility(View.VISIBLE);
                        }

                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(uploadStatusOptRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<UploadStatusOptRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("onFailure_data", t.toString());
                progressBar.setVisibility(View.GONE);
                conNoData.setVisibility(View.VISIBLE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    public void checkUploadLimit(String user_id, String type) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(UploadStatus.this));
        jsObj.addProperty("user_id", user_id);
        jsObj.addProperty("type", type);
        jsObj.addProperty("method_name", "daily_upload_limit");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.getDailyUploadLimit(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {
                    DataRP dataRP = response.body();
                    assert dataRP != null;

                    if (dataRP.getStatus().equals("1")) {

                        if (dataRP.getSuccess().equals("1")) {

                            switch (type) {
                                case "video":
                                    if (uploadStatusOptRP.getVideo_add().equals("true")) {
                                        method.VideoAdDialog(type, "");
                                    } else {
                                        choseOption(type);
                                    }
                                    break;
                                case "quote":
                                    if (uploadStatusOptRP.getQuotes_add().equals("true")) {
                                        method.VideoAdDialog(type, "");
                                    } else {
                                        choseOption(type);
                                    }
                                    break;
                                case "image":
                                    if (uploadStatusOptRP.getImage_add().equals("true")) {
                                        method.VideoAdDialog(type, "");
                                    } else {
                                        choseOption(type);
                                    }
                                    break;
                                default:
                                    if (uploadStatusOptRP.getGif_add().equals("true")) {
                                        method.VideoAdDialog(type, "");
                                    } else {
                                        choseOption(type);
                                    }
                                    break;
                            }

                        } else {
                            method.alertBox(dataRP.getMsg());
                        }

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

    public void choseOption(String type) {
        switch (type) {
            case "video":
                startActivity(new Intent(UploadStatus.this, VideoUpload.class)
                        .putExtra("size", Integer.parseInt(uploadStatusOptRP.getVideo_file_size()))
                        .putExtra("duration", Integer.parseInt(uploadStatusOptRP.getVideo_file_duration()))
                        .putExtra("video_msg", uploadStatusOptRP.getVideo_msg())
                        .putExtra("size_msg", uploadStatusOptRP.getVideo_size_msg())
                        .putExtra("duration_msg", uploadStatusOptRP.getVideo_duration_msg()));
                break;
            case "quote":
                startActivity(new Intent(UploadStatus.this, QuotesUpload.class));
                break;
            case "image":
                startActivity(new Intent(UploadStatus.this, ImageUpload.class)
                        .putExtra("size", Integer.parseInt(uploadStatusOptRP.getImage_file_size()))
                        .putExtra("size_msg", uploadStatusOptRP.getImage_file_size()));
                break;
            default:
                startActivity(new Intent(UploadStatus.this, GIFUpload.class)
                        .putExtra("size", Integer.parseInt(uploadStatusOptRP.getImage_file_size()))
                        .putExtra("size_msg", uploadStatusOptRP.getGif_size_msg()));
                break;
        }
    }

}
