package com.example.status.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.adapter.SelectLanguageAdapter;
import com.example.status.adapter.SpinnerCatAdapter;
import com.example.status.interfaces.LanguageIF;
import com.example.status.item.CategoryList;
import com.example.status.response.CatLanguageRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.service.UIGService;
import com.example.status.util.API;
import com.example.status.util.BannerAds;
import com.example.status.util.Events;
import com.example.status.util.GetPath;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
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

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GIFUpload extends AppCompatActivity {

    private Method method;
    private LanguageIF languageIF;
    MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private TextInputEditText editText;
    private ImageView imageView;
    private MaterialTextView textViewMsg;
    private RecyclerView recyclerView;
    private NachoTextView nachoTextView;
    private MaterialButton button;
    LinearLayout linearLayout;
    private Spinner spinnerCat;
    private InputMethodManager imm;
    private ConstraintLayout conNoData, conMain;
    private ArrayList<String> languageIdsList;
    private SelectLanguageAdapter selectLanguageAdapter;
    private String sizeMsg, imagePath, categoryId;
    int gifSize, REQUEST_CODE_CHOOSE = 109, positionImageType = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_upload);

        GlobalBus.getBus().register(this);

        Intent intent = getIntent();
        gifSize = intent.getIntExtra("size", 0);
        sizeMsg = intent.getStringExtra("size_msg");

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        method = new Method(GIFUpload.this);
        method.forceRTLIfSupported();

        languageIdsList = new ArrayList<>();

        languageIF = (id, type, position, isValue) -> {
            if (isValue) {
                languageIdsList.add(id);
            } else {
                new SelectLanguage().execute(id);
            }
        };

        toolbar = findViewById(R.id.toolbar_gif_upload);
        toolbar.setTitle(getResources().getString(R.string.upload_gif));

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        linearLayout = findViewById(R.id.ll_ad_gif_upload);
        BannerAds.showBannerAds(GIFUpload.this,linearLayout);

        conMain = findViewById(R.id.con_main_gif_upload);
        progressBar = findViewById(R.id.progressBar_gif_upload);
        conNoData = findViewById(R.id.con_noDataFound);
        editText = findViewById(R.id.editText_gif_upload);
        imageView = findViewById(R.id.imageView_gif_upload);
        spinnerCat = findViewById(R.id.spinner_cat_gif_upload);
        textViewMsg = findViewById(R.id.textView_msg_gif_upload);
        recyclerView = findViewById(R.id.recyclerView_gif_upload);
        nachoTextView = findViewById(R.id.nacho_gif_upload);
        button = findViewById(R.id.button_gif_upload);

        conNoData.setVisibility(View.GONE);
        conMain.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        textViewMsg.setVisibility(View.GONE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(GIFUpload.this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        nachoTextView.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN);

        imageView.setOnClickListener(v -> {
            Dexter.withContext(GIFUpload.this)
                    .withPermission(permission())
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            selectImage();
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


        button.setOnClickListener(v -> submitGif());

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

    @Subscribe
    public void getData(Events.UploadFinish uploadFinish) {
        if (editText != null) {
            finishUpload();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //gif get code
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            File file = null;
            String filePath = GetPath.getPath(GIFUpload.this, data.getData());
            assert filePath != null;
            try {
                textViewMsg.setVisibility(View.VISIBLE);
                file = new File(filePath);
                if (file.getName().contains(".gif")) {
                    int fileSize = (int) file.length() / (1024 * 1024);
                    if (fileSize <= gifSize) {
                        imagePath = filePath;
                        textViewMsg.setTextColor(getResources().getColor(R.color.textView_hint_upload));
                        textViewMsg.setText(imagePath);
                        Glide.with(GIFUpload.this).load(imagePath)
                                .placeholder(R.drawable.placeholder_landscape)
                                .into(imageView);
                    } else {
                        imagePath = "";
                        textViewMsg.setTextColor(getResources().getColor(R.color.green));
                        textViewMsg.setText(sizeMsg);
                    }
                } else {
                    imagePath = "";
                    textViewMsg.setTextColor(getResources().getColor(R.color.green));
                    textViewMsg.setText(getResources().getString(R.string.file_type_gif));
                }
            } catch (Exception e) {
                method.alertBox(getResources().getString(R.string.upload_folder_error));
            }

        }

    }

    private void selectImage() {
        Intent intent_upload = new Intent();
        intent_upload.setType("image/gif");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, REQUEST_CODE_CHOOSE);
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

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(GIFUpload.this));
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
                            selectLanguageAdapter = new SelectLanguageAdapter(GIFUpload.this, catLanguageRP.getLanguageLists(), languageIF);
                            recyclerView.setAdapter(selectLanguageAdapter);
                        }

                        catLanguageRP.getCategoryLists().add(0, new CategoryList("", getResources().getString(R.string.selected_category), "", "", "", ""));
                        SpinnerCatAdapter spinnerCatAdapter = new SpinnerCatAdapter(GIFUpload.this, catLanguageRP.getCategoryLists());
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

    public void submitGif() {

        String title = editText.getText().toString();
        editText.setError(null);

        //get all language id
        String langIds = "";
        for (int i = 0; i < languageIdsList.size(); i++) {
            if (i != 0) {
                langIds = langIds.concat(",");
            }
            langIds = langIds.concat(languageIdsList.get(i));
        }

        if (title.equals("") || title.isEmpty()) {
            editText.requestFocus();
            editText.setError(getResources().getString(R.string.please_enter_title));
        } else if (imagePath == null || imagePath.equals("") || imagePath.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_image));
        } else if (categoryId.equals("") || categoryId.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_category));
        } else if (langIds.equals("") || langIds.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_language));
        } else {

            editText.clearFocus();
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

            if (method.isNetworkAvailable()) {

                //get all tag
                String imageTags = "";
                for (int i = 0; i < nachoTextView.getAllChips().size(); i++) {
                    if (i != 0) {
                        imageTags = imageTags.concat(",");
                    }
                    imageTags = imageTags.concat(nachoTextView.getAllChips().get(i).toString());
                }

                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(imagePath, options);
                    int imageHeight = options.outHeight;
                    int imageWidth = options.outWidth;
                    if (imageHeight > imageWidth) {
                        positionImageType = 0;
                    } else {
                        positionImageType = 1;
                    }
                } catch (Exception e) {
                    Log.d("error_show", e.toString());
                }

                gifUpload(method.userId(), categoryId, langIds, imageTags, title, imagePath);

            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        }

    }

    private void gifUpload(String userId, String catId, String langIds, String imageTags, String imageTitle, String imageFile) {

        Method.isUpload = false;
        button.setVisibility(View.GONE);

        String image_type = null;

        if (positionImageType == 1) {
            image_type = "Landscape";
        } else {
            image_type = "Portrait";
        }

        Intent serviceIntent = new Intent(GIFUpload.this, UIGService.class);
        serviceIntent.setAction(UIGService.ACTION_START);
        serviceIntent.putExtra("user_id", userId);
        serviceIntent.putExtra("cat_id", catId);
        serviceIntent.putExtra("lang_ids", langIds);
        serviceIntent.putExtra("image_tags", imageTags);
        serviceIntent.putExtra("image_title", imageTitle);
        serviceIntent.putExtra("image_layout", image_type);
        serviceIntent.putExtra("image_file", imageFile);
        serviceIntent.putExtra("status_type", "gif");
        startService(serviceIntent);

    }

    public void finishUpload() {
        button.setVisibility(View.VISIBLE);
        editText.setText("");
        categoryId = "";
        nachoTextView.setText("");
        imagePath = "";
        languageIdsList.clear();
        spinnerCat.setSelection(0);
        if (selectLanguageAdapter != null) {
            selectLanguageAdapter.clearCheckBox();
        }
        Glide.with(GIFUpload.this).load(R.drawable.placeholder_landscape).into(imageView);
        textViewMsg.setTextColor(getResources().getColor(R.color.textView_hint_upload));
        textViewMsg.setText("");
        textViewMsg.setVisibility(View.GONE);
        Toast.makeText(this, getResources().getString(R.string.upload_gif), Toast.LENGTH_SHORT).show();
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
