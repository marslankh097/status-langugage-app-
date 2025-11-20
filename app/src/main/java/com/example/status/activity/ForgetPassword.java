package com.example.status.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

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

public class ForgetPassword extends AppCompatActivity {

    private Method method;
    public MaterialToolbar toolbar;
    private TextInputEditText editTextFp;
    MaterialButton button;
    private ProgressDialog progressDialog;
    private InputMethodManager imm;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        method = new Method(ForgetPassword.this);
        method.forceRTLIfSupported();

        progressDialog = new ProgressDialog(ForgetPassword.this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        toolbar = findViewById(R.id.toolbar_fp);
        toolbar.setTitle(getResources().getString(R.string.forget_password));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        editTextFp = findViewById(R.id.editText_fp);
        button = findViewById(R.id.button_fp);

        button.setOnClickListener(v -> {

            String string_fp = editTextFp.getText().toString();
            editTextFp.setError(null);

            if (!isValidMail(string_fp) || string_fp.isEmpty()) {
                editTextFp.requestFocus();
                editTextFp.setError(getResources().getString(R.string.please_enter_email));
            } else {

                editTextFp.clearFocus();
                imm.hideSoftInputFromWindow(editTextFp.getWindowToken(), 0);

                if (method.isNetworkAvailable()) {
                    forgetPassword(string_fp);
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
            }

        });

    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void forgetPassword(String sendEmail_forget_password) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(ForgetPassword.this));
        jsObj.addProperty("method_name", "forgot_pass");
        jsObj.addProperty("email", sendEmail_forget_password);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.getForgetPassword(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {

                    DataRP dataRP = response.body();

                    assert dataRP != null;
                    if (dataRP.getStatus().equals("1")) {
                        if (dataRP.getSuccess().equals("1")) {
                            editTextFp.setText("");
                        }
                        method.alertBox(dataRP.getMsg());
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
                Log.e("onFailure_data", t.toString());
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
