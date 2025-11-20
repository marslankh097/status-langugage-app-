package com.example.status.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.BuildConfig;
import com.example.status.R;
import com.example.status.adapter.SelectLanguageAdapter;
import com.example.status.adapter.SpinnerCatAdapter;
import com.example.status.interfaces.LanguageIF;
import com.example.status.item.CategoryList;
import com.example.status.response.CatLanguageRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.service.VideoUploadService;
import com.example.status.util.API;
import com.example.status.util.BannerAds;
import com.example.status.util.Constant;
import com.example.status.util.Events;
import com.example.status.util.GetPath;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.theartofdev.edmodo.cropper.CropImage;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION.SDK_INT;


public class VideoUpload extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private Method method;
    private LanguageIF languageIF;
    private InputMethodManager imm;
    private MaterialButton button;
    private ProgressBar progressBar;
    private ArrayList<String> languageIdsList;
    private RecyclerView recyclerView;
    private SelectLanguageAdapter selectLanguageAdapter;
    private ImageView imageView;
    private TextInputEditText editText;
    private NachoTextView nachoTextView;
    private Spinner spinnerCat;
    private LinearLayout linearLayout;
    private LinearLayout cardViewImageUpload;
    private MaterialTextView textViewImage, textViewVideo;
    private ConstraintLayout conMain, conNoData, conImage, conVideo;
    private String videoMsg, sizeMsg, durationMsg, videoPath, categoryId, videoImage;
    private final int REQUEST_CODE_CHOOSE = 100;
    private final int REQUEST_CODE_CHOOSE_VIDEO = 0;
    private final int REQUEST_CODE_PERMISSION = 101;
    private final int REQUEST_CODE_PERMISSION_VIDEO = 102;
    private int videoSize, videoDuration;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);

        GlobalBus.getBus().register(this);

        Intent intent = getIntent();
        videoSize = intent.getIntExtra("size", 0);
        videoDuration = intent.getIntExtra("duration", 0);
        videoMsg = intent.getStringExtra("video_msg");
        sizeMsg = intent.getStringExtra("size_msg");
        durationMsg = intent.getStringExtra("duration_msg");

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        method = new Method(VideoUpload.this);
        method.forceRTLIfSupported();

        languageIdsList = new ArrayList<>();

        languageIF = (id, type, position, isValue) -> {
            if (isValue) {
                languageIdsList.add(id);
            } else {
                new SelectLanguage().execute(id);
            }
        };

        toolbar = findViewById(R.id.toolbar_upload);
        toolbar.setTitle(getResources().getString(R.string.upload_video));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        conMain = findViewById(R.id.con_main_videoUpload);
        progressBar = findViewById(R.id.progressbar_upload);
        conNoData = findViewById(R.id.con_noDataFound);
        editText = findViewById(R.id.editText_video_upload);
        button = findViewById(R.id.button_video_upload);
        imageView = findViewById(R.id.imageView_video_upload);
        spinnerCat = findViewById(R.id.spinner_cat_video_upload);
        cardViewImageUpload = findViewById(R.id.cardView_imageUpload);
        conImage = findViewById(R.id.con_image_select_upload);
        conVideo = findViewById(R.id.con_videoSelect_upload);
        textViewImage = findViewById(R.id.textView_image_vu);
        textViewVideo = findViewById(R.id.textView_video_vu);
        recyclerView = findViewById(R.id.recyclerView_video_upload);
        nachoTextView = findViewById(R.id.nacho_video_upload);

        conMain.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);

        textViewVideo.setText(videoMsg);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(VideoUpload.this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        nachoTextView.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN);

        linearLayout = findViewById(R.id.linearLayout_upload);

        cardViewImageUpload.setVisibility(View.GONE);

        BannerAds.showBannerAds(VideoUpload.this, linearLayout);

        if (Method.isUpload) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
        }

        if (method.isNetworkAvailable()) {
            categoryLanguage();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    private String permission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        }else {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }

    private String permissionVideo(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_VIDEO;
        }else {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }

    @Subscribe
    public void getData(Events.UploadFinish uploadFinish) {
        if (editText != null) {
            finishUpload();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    chooseGalleryImage(); // perform action when allow permission success
                } else {
                    method.alertBox(getResources().getString(R.string.user_permission));
                }
            }
        }

        if (requestCode == REQUEST_CODE_PERMISSION_VIDEO) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    chooseVideo(); // perform action when allow permission success
                } else {
                    method.alertBox(getResources().getString(R.string.user_permission));
                }
            }
        }

        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                videoImage = GetPath.getPath(VideoUpload.this, data.getData());
                if (videoImage != null) {
                    Glide.with(VideoUpload.this).load(data.getData()).into(imageView);
                    textViewImage.setText(videoImage);
                    CropImage.activity(data.getData()).start(VideoUpload.this);
                } else {
                    method.alertBox(getResources().getString(R.string.upload_folder_error));
                }
            } catch (Exception e) {
                method.alertBox(getResources().getString(R.string.upload_folder_error));
            }
        }

        if (requestCode == REQUEST_CODE_CHOOSE_VIDEO && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            File file = null;
            String filePath = GetPath.getPath(VideoUpload.this, data.getData());
            assert filePath != null;
            try {
                file = new File(filePath);
                int fileSize = (int) file.length() / (1024 * 1024);
                String fileName = file.getName();

                if (fileName.contains(".mp4")) {
                    if (fileSize <= videoSize) {
                        videoPath = filePath;
                        if (!(getDurationInt(videoPath) <= videoDuration)) {
                            videoPath = "";
                            textViewVideo.setTextColor(getResources().getColor(R.color.green));
                            textViewVideo.setText(durationMsg);
                        } else {
                            textViewVideo.setTextColor(getResources().getColor(R.color.textView_hint_upload));
                            textViewVideo.setText(videoPath);
                            try {
                                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
                                if (thumb != null) {
                                    downloadImage(thumb);
                                } else {
                                    cardViewImageUpload.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                cardViewImageUpload.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        videoPath = "";
                        textViewVideo.setTextColor(getResources().getColor(R.color.green));
                        textViewVideo.setText(sizeMsg);
                    }
                } else {
                    videoPath = "";
                    textViewVideo.setTextColor(getResources().getColor(R.color.green));
                    textViewVideo.setText(getResources().getString(R.string.file_type_video));
                }
            } catch (Exception e) {
                method.alertBox(getResources().getString(R.string.upload_folder_error));
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                videoImage = resultUri.getPath();
                Glide.with(VideoUpload.this).load(resultUri).into(imageView);
                textViewImage.setText(videoImage);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String @NotNull [] permissions, int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseGalleryImage(); // perform action when allow permission success
            } else {
                method.alertBox(getResources().getString(R.string.user_permission));
            }
        }

        if (requestCode == REQUEST_CODE_PERMISSION_VIDEO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseVideo(); // perform action when allow permission success
            } else {
                method.alertBox(getResources().getString(R.string.user_permission));
            }
        }
    }

    public void chooseGalleryImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE);
    }

    public void chooseVideo() {
        Intent intent_upload = new Intent();
        intent_upload.setType("video/mp4");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, REQUEST_CODE_CHOOSE_VIDEO);
    }

    @SuppressLint("StaticFieldLeak")
    class SelectLanguage extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            for (int i = 0; i < languageIdsList.size(); i++) {
                if (languageIdsList.get(i).equals(strings[0])) {
                    languageIdsList.remove(i);
                }
            }

            return languageIdsList.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public void categoryLanguage() {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(VideoUpload.this));
        jsObj.addProperty("method_name", "get_cat_lang_list");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<CatLanguageRP> call = apiService.getCatLanguageRP(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<CatLanguageRP>() {
            @Override
            public void onResponse(@NotNull Call<CatLanguageRP> call, @NotNull Response<CatLanguageRP> response) {

                try {
                    CatLanguageRP catLanguageRP = response.body();
                    assert catLanguageRP != null;

                    if (catLanguageRP.getStatus().equals("1")) {

                        if (catLanguageRP.getLanguageLists().size() != 0) {
                            selectLanguageAdapter = new SelectLanguageAdapter(VideoUpload.this, catLanguageRP.getLanguageLists(), languageIF);
                            recyclerView.setAdapter(selectLanguageAdapter);
                        }

                        catLanguageRP.getCategoryLists().add(0, new CategoryList("", getResources().getString(R.string.selected_category), "", "", "", ""));
                        SpinnerCatAdapter spinnerCatAdapter = new SpinnerCatAdapter(VideoUpload.this, catLanguageRP.getCategoryLists());
                        spinnerCat.setAdapter(spinnerCatAdapter);

                        conMain.setVisibility(View.VISIBLE);

                        // Spinner click listener
                        spinnerCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position == 0) {
                                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_upload));
                                    ((TextView) parent.getChildAt(0)).setTextSize(16);
                                } else {
                                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_app_color));
                                    ((TextView) parent.getChildAt(0)).setTextSize(16);
                                }
                                categoryId = catLanguageRP.getCategoryLists().get(position).getCid();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        conImage.setOnClickListener(v -> {
//                            if (SDK_INT >= Build.VERSION_CODES.M) {
//                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
//                            } else {
//                                chooseGalleryImage();
//                            }
                            Dexter.withContext(VideoUpload.this)
                                    .withPermission(permission())
                                    .withListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted(PermissionGrantedResponse response) {
                                            chooseGalleryImage();
                                        }

                                        @Override
                                        public void onPermissionDenied(PermissionDeniedResponse response) {
                                            // check for permanent denial of permission
                                            method.alertBox(getResources().getString(R.string.allow_storage));

                                        }

                                        @Override
                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                            token.continuePermissionRequest();
                                        }
                                    }).check();
                        });

                        conVideo.setOnClickListener(v -> {
                            if (method.isNetworkAvailable()) {
//                                if (SDK_INT >= Build.VERSION_CODES.M) {
//                                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_VIDEO);
//                                } else {
//                                    chooseVideo();
//                                }
                                Dexter.withContext(VideoUpload.this)
                                        .withPermission(permission())
                                        .withListener(new PermissionListener() {
                                            @Override
                                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                                chooseVideo();
                                            }

                                            @Override
                                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                                // check for permanent denial of permission
                                                method.alertBox(getResources().getString(R.string.allow_storage));

                                            }

                                            @Override
                                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                                token.continuePermissionRequest();
                                            }
                                        }).check();
                            } else {
                                method.alertBox(getResources().getString(R.string.internet_connection));
                            }
                        });

                        button.setOnClickListener(v -> submitVideo());

                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(catLanguageRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<CatLanguageRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                conNoData.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    private long getDurationInt(String filePath) throws IOException {

        MediaMetadataRetriever metaRetriever_int = new MediaMetadataRetriever();
        metaRetriever_int.setDataSource(filePath);
        String songDuration = metaRetriever_int.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long duration = Long.parseLong(songDuration);
        //int time = (int) (duration % 60000) / 1000;
        // close object
        long time = (int) duration / 1000;
        metaRetriever_int.release();

        return time;

    }

    public void submitVideo() {

        String title = editText.getText().toString();
        editText.setError(null);

        String lang_ids = "";

        for (int i = 0; i < languageIdsList.size(); i++) {
            if (i != 0) {
                lang_ids = lang_ids.concat(",");
            }
            lang_ids = lang_ids.concat(languageIdsList.get(i));
        }

        if (title.equals("") || title.isEmpty()) {
            editText.requestFocus();
            editText.setError(getResources().getString(R.string.please_enter_title));
        } else if (categoryId.equals("") || categoryId.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_category));
        } else if (videoImage == null || videoImage.equals("") || videoImage.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_image));
        } else if (videoPath == null || videoPath.equals("") || videoPath.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_video));
        } else if (lang_ids.equals("") || lang_ids.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_language));
        } else {

            editText.clearFocus();
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

            if (method.isNetworkAvailable()) {

                String videoTags = "";
                for (int i = 0; i < nachoTextView.getAllChips().size(); i++) {
                    if (i != 0) {
                        videoTags = videoTags.concat(",");
                    }
                    videoTags = videoTags.concat(nachoTextView.getAllChips().get(i).toString());
                }
                upload(method.userId(), categoryId, title, videoPath, videoImage, lang_ids, videoTags);

            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        }

    }

    public void downloadImage(Bitmap bitmap) {

        String iconsStoragePath = getExternalCacheDir().getAbsolutePath();
        String fname = "image_upload" + new Random().nextInt(10000) + ".jpg";
        File file = new File(iconsStoragePath, fname);

        //create storage directories, if they don't exist
        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            //choose another format if PNG doesn't suit you
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            videoImage = file.toString();
            Glide.with(VideoUpload.this).load(file).into(imageView);
            cardViewImageUpload.setVisibility(View.VISIBLE);
        } catch (FileNotFoundException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            cardViewImageUpload.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            Log.w("TAG", "Error saving image file: " + e.getMessage());
            cardViewImageUpload.setVisibility(View.VISIBLE);
        }
    }


    public void upload(String userId, final String catId, final String videoTitle, String
            videoLocal, String videoThumbnail, String langIds, String videoTags) {

        Method.isUpload = false;
        button.setVisibility(View.GONE);

        Intent serviceIntent = new Intent(VideoUpload.this, VideoUploadService.class);
        serviceIntent.setAction(VideoUploadService.ACTION_START);
        serviceIntent.putExtra("uploadUrl", Constant.videoUploadUrl);
        serviceIntent.putExtra("user_id", userId);
        serviceIntent.putExtra("cat_id", catId);
        serviceIntent.putExtra("video_title", videoTitle);
        serviceIntent.putExtra("video_local", videoLocal);
        serviceIntent.putExtra("video_thumbnail", videoThumbnail);
        serviceIntent.putExtra("lang_ids", langIds);
        serviceIntent.putExtra("video_tags", videoTags);
        startService(serviceIntent);

        Toast.makeText(this, getResources().getString(R.string.upload), Toast.LENGTH_SHORT).show();

    }

    public void finishUpload() {
        if (editText != null) {
            button.setVisibility(View.VISIBLE);
            editText.setText("");
            categoryId = "";
            videoPath = "";
            nachoTextView.setText("");
            languageIdsList.clear();
            spinnerCat.setSelection(0);
            if (selectLanguageAdapter != null) {
                selectLanguageAdapter.clearCheckBox();
            }
            Glide.with(VideoUpload.this).load(R.drawable.placeholder_landscape).into(imageView);
            cardViewImageUpload.setVisibility(View.GONE);
            textViewImage.setText(getResources().getString(R.string.no_file_selected));
            textViewVideo.setTextColor(getResources().getColor(R.color.textView_hint_upload));
            textViewVideo.setText(videoMsg);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }


}
