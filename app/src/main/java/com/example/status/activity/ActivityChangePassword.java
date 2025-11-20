package com.example.status.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.response.DataRP;
import com.example.status.response.ProfileRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityChangePassword extends AppCompatActivity {

    private Method method;
    private ProgressBar progressBar;
    private InputMethodManager imm;
    private ProgressDialog progressDialog;
    private MaterialButton button;
    private CircleImageView imageView;
    private MaterialTextView textViewName;
    private ConstraintLayout conNoData, conMain;
    private TextInputEditText editTextOldPassword, editTextPassword, editTextConfirmPassword;
    MaterialToolbar toolbar;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        method = new Method(ActivityChangePassword.this);
        method.forceRTLIfSupported();

        toolbar = findViewById(R.id.toolbar_about_us);
        toolbar.setTitle(getResources().getString(R.string.change_password));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        progressDialog = new ProgressDialog(ActivityChangePassword.this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        conNoData = findViewById(R.id.con_noDataFound);
        conMain = findViewById(R.id.con_main_cp_fragment);
        progressBar = findViewById(R.id.progressBar_cp_fragment);
        imageView = findViewById(R.id.imageView_cp_fragment);
        textViewName = findViewById(R.id.textView_name_cp_fragment);
        editTextOldPassword = findViewById(R.id.editText_old_password_cp_fragment);
        editTextPassword = findViewById(R.id.editText_password_cp_fragment);
        editTextConfirmPassword = findViewById(R.id.editText_confirm_password_cp_fragment);
        button = findViewById(R.id.button_edit_cp_fragment);

        progressBar.setVisibility(View.GONE);
        conMain.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);

        button.setOnClickListener(v -> save());

        if (method.isNetworkAvailable()) {
            profile(method.userId());
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }



    }

    private void save() {

        String oldPassword = editTextOldPassword.getText().toString();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        editTextOldPassword.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);

        if (oldPassword.equals("") || oldPassword.isEmpty()) {
            editTextOldPassword.requestFocus();
            editTextOldPassword.setError(getResources().getString(R.string.please_enter_old_password));
        } else if (password.equals("") || password.isEmpty()) {
            editTextPassword.requestFocus();
            editTextPassword.setError(getResources().getString(R.string.please_enter_new_password));
        } else if (confirmPassword.equals("") || confirmPassword.isEmpty()) {
            editTextConfirmPassword.requestFocus();
            editTextConfirmPassword.setError(getResources().getString(R.string.please_enter_new_confirm_password));
        } else if (!password.equals(confirmPassword)) {
            method.alertBox(getResources().getString(R.string.new_password_not_match));
        } else {
            if (method.isNetworkAvailable()) {

                editTextOldPassword.clearFocus();
                editTextPassword.clearFocus();
                editTextConfirmPassword.clearFocus();
                imm.hideSoftInputFromWindow(editTextOldPassword.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextPassword.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextConfirmPassword.getWindowToken(), 0);

                passwordUpdate(method.userId(), oldPassword, password);

            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        }

    }

    public void profile(final String id) {

        if (ActivityChangePassword.this != null) {

            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(ActivityChangePassword.this));
            jsObj.addProperty("user_id", id);
            jsObj.addProperty("method_name", "user_profile");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<ProfileRP> call = apiService.getProfile(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<ProfileRP>() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onResponse(@NotNull Call<ProfileRP> call, @NotNull Response<ProfileRP> response) {

                    if (ActivityChangePassword.this != null) {

                        try {

                            ProfileRP profileRP = response.body();
                            assert profileRP != null;

                            if (profileRP.getStatus().equals("1")) {

                                if (profileRP.getSuccess().equals("1")) {
                                    textViewName.setText(profileRP.getName());

                                    Glide.with(ActivityChangePassword.this).load(profileRP.getUser_image())
                                            .placeholder(R.drawable.profile)
                                            .circleCrop()
                                            .into(imageView);

                                    conMain.setVisibility(View.VISIBLE);

                                } else {
                                    conNoData.setVisibility(View.VISIBLE);
                                    method.alertBox(profileRP.getMsg());
                                }

                            } else if (profileRP.getStatus().equals("2")) {
                                method.suspend(profileRP.getMessage());
                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(profileRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NotNull Call<ProfileRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    conNoData.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });


        }

    }

    private void passwordUpdate(String userId, String oldPassword, String password) {

        if (ActivityChangePassword.this != null) {

            progressDialog.show();
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(ActivityChangePassword.this));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("old_password", oldPassword);
            jsObj.addProperty("new_password", password);
            jsObj.addProperty("method_name", "change_password");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataRP> call = apiService.updatePassword(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<DataRP>() {
                @Override
                public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                    if (ActivityChangePassword.this != null) {

                        try {
                            DataRP dataRP = response.body();
                            assert dataRP != null;

                            if (dataRP.getStatus().equals("1")) {
                                if (dataRP.getSuccess().equals("1")) {
                                    editTextOldPassword.setText("");
                                    editTextPassword.setText("");
                                    editTextConfirmPassword.setText("");
                                }
                                method.alertBox(dataRP.getMsg());
                            } else if (dataRP.getStatus().equals("2")) {
                                method.suspend(dataRP.getMessage());
                            } else {
                                method.alertBox(dataRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }
                    }

                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(@NotNull Call<DataRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("onFailure_data", t.toString());
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
