package com.example.status.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.status.R;
import com.example.status.response.DataRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnterReferenceCode extends AppCompatActivity {

    private Method method;
    MaterialToolbar toolbar;
    private String userId;
    private InputMethodManager imm;
    private ProgressDialog progressDialog;
    private TextInputEditText editText;
    MaterialButton buttonContinue, buttonSkip;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_reference_code);

        method = new Method(EnterReferenceCode.this);
        method.forceRTLIfSupported();

        userId = getIntent().getStringExtra("user_id");

        toolbar = findViewById(R.id.toolbar_erc);
        toolbar.setTitle(getResources().getString(R.string.reference_code));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(EnterReferenceCode.this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        editText = findViewById(R.id.editText_erc);
        buttonContinue = findViewById(R.id.button_continue_erc);
        buttonSkip = findViewById(R.id.button_skip_erc);

        buttonContinue.setOnClickListener(v -> {

            editText.setError(null);

            String refCode = editText.getText().toString();

            if (refCode.equals("") || refCode.isEmpty()) {
                editText.requestFocus();
                editText.setError(getResources().getString(R.string.please_enter_reference_code));
            } else {

                editText.clearFocus();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                if (method.isNetworkAvailable()) {
                    referenceCode(userId, refCode);
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }

            }

        });

        buttonSkip.setOnClickListener(v -> {
            startActivity(new Intent(EnterReferenceCode.this, MainActivity.class));
            finishAffinity();
        });

    }

    public void referenceCode(String userId, String code) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(EnterReferenceCode.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("user_refrence_code", code);
        jsObj.addProperty("method_name", "apply_user_refrence_code");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.submitReferenceCode(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {
                    DataRP dataRP = response.body();
                    assert dataRP != null;

                    if (dataRP.getStatus().equals("1")) {

                        if (dataRP.getSuccess().equals("1")) {
                            Toast.makeText(EnterReferenceCode.this, dataRP.getMsg(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EnterReferenceCode.this, MainActivity.class));
                            finishAffinity();
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

}
