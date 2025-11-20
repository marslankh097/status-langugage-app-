package com.example.status.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.response.DataRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.BannerAds;
import com.example.status.util.GetPath;
import com.example.status.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION.SDK_INT;

public class AccountVerification extends AppCompatActivity {

    private Method method;
    public MaterialToolbar toolbar;
    private ProgressDialog progressDialog;
    private String name, documentImage;
    private MaterialButton button;
    private ImageView imageView;
    private ConstraintLayout conImage;
    private InputMethodManager imm;
    private final int REQUEST_CODE_CHOOSE = 100;
    private final int REQUEST_CODE_PERMISSION = 101;
    MaterialTextView textViewTitle, textViewImage;
    private TextInputEditText editTextUserName, editTextFullName, editTextMsg;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_verification);

        method = new Method(AccountVerification.this);
        method.forceRTLIfSupported();

        name = getIntent().getStringExtra("name");

        toolbar = findViewById(R.id.toolbar_av);
        toolbar.setTitle(getResources().getString(R.string.request_verification));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(AccountVerification.this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        textViewTitle = findViewById(R.id.textView_title_av);
        textViewImage = findViewById(R.id.textView_image_av);
        editTextUserName = findViewById(R.id.editText_userName_av);
        editTextFullName = findViewById(R.id.editText_full_name_av);
        editTextMsg = findViewById(R.id.editText_msg_av);
        imageView = findViewById(R.id.imageView_av);
        conImage = findViewById(R.id.con_image_av);
        button = findViewById(R.id.button_av);

        editTextUserName.clearFocus();
        editTextUserName.setCursorVisible(false);
        editTextUserName.setFocusable(false);

        textViewTitle.setText(getResources().getString(R.string.apply_for)
                + " " + getResources().getString(R.string.app_name)
                + " " + getResources().getString(R.string.verification));

        LinearLayout linearLayout = findViewById(R.id.linearLayout_av);

        BannerAds.showBannerAds(AccountVerification.this,linearLayout);

        if (method.isNetworkAvailable()) {
            getData();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
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

        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            try {
                documentImage = GetPath.getPath(AccountVerification.this, data.getData());
                if (documentImage != null) {
                    Glide.with(AccountVerification.this).load(data.getData())
                            .placeholder(R.drawable.placeholder_landscape).into(imageView);
                    textViewImage.setText(documentImage);
                } else {
                    method.alertBox(getResources().getString(R.string.upload_folder_error));
                }
            } catch (Exception e) {
                method.alertBox(getResources().getString(R.string.upload_folder_error));
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
    }

    public void chooseGalleryImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE);
    }

    private void getData() {

        editTextUserName.setText(name);

        button.setOnClickListener(view -> {

            String name = editTextUserName.getText().toString();
            String fullName = editTextFullName.getText().toString();
            String msg = editTextMsg.getText().toString();

            form(name, fullName, msg, documentImage);

        });

        conImage.setOnClickListener(view -> {
            if (SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            } else {
                chooseGalleryImage();
            }
        });

    }

    private void form(String name, String full_name, String msg, String document) {

        editTextUserName.setError(null);
        editTextFullName.setError(null);
        editTextMsg.setError(null);

        if (name.equals("") || name.isEmpty()) {
            editTextUserName.requestFocus();
            editTextUserName.setError(getResources().getString(R.string.please_enter_name));
        } else if (full_name.equals("") || full_name.isEmpty()) {
            editTextFullName.requestFocus();
            editTextFullName.setError(getResources().getString(R.string.please_enter_full_name));
        } else if (msg.equals("") || msg.isEmpty()) {
            editTextMsg.requestFocus();
            editTextMsg.setError(getResources().getString(R.string.please_enter_message));
        } else if (document == null || document.equals("") || document.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_image));
        } else {

            editTextFullName.clearFocus();
            editTextMsg.clearFocus();
            imm.hideSoftInputFromWindow(editTextFullName.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editTextMsg.getWindowToken(), 0);

            if (method.isNetworkAvailable()) {
                submit(method.userId(), full_name, msg, document);
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        }

    }

    public void submit(String userId, String sendFullName, String sendMessage, String document) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(AccountVerification.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("full_name", sendFullName);
        jsObj.addProperty("message", sendMessage);
        jsObj.addProperty("method_name", "profile_verify");
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), new File(document));
        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("document", new File(document).getName(), requestFile);
        RequestBody requestBodyData = RequestBody.create(MediaType.parse("multipart/form-data"), API.toBase64(jsObj.toString()));
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.submitAccountVerification(requestBodyData, body);
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {
                    DataRP dataRP = response.body();
                    assert dataRP != null;

                    if (dataRP.getStatus().equals("1")) {
                        if (dataRP.getSuccess().equals("1")) {

                            editTextFullName.setText("");
                            editTextMsg.setText("");
                            documentImage = "";
                            textViewImage.setText(getResources().getString(R.string.add_thumbnail_file));
                            Glide.with(AccountVerification.this)
                                    .load(R.drawable.placeholder_landscape)
                                    .placeholder(R.drawable.placeholder_landscape).into(imageView);

                            onBackPressed();

                            Toast.makeText(AccountVerification.this, dataRP.getMsg(), Toast.LENGTH_SHORT).show();

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
