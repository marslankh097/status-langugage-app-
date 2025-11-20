package com.example.status.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.example.status.R;
import com.example.status.response.DataRP;
import com.example.status.response.RegisterRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Method;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Verification extends AppCompatActivity {

    private Method method;
    private PinView pinView;
    private InputMethodManager imm;
    private ProgressDialog progressDialog;
    private String verification, name, email, password, phoneNo, reference;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        method = new Method(Verification.this);
        method.forceRTLIfSupported();

        progressDialog = new ProgressDialog(Verification.this);

        Intent intent = getIntent();
        if (intent.hasExtra("name")) {
            name = intent.getStringExtra("name");
            email = intent.getStringExtra("email");
            password = intent.getStringExtra("password");
            phoneNo = intent.getStringExtra("phoneNo");
            reference = intent.getStringExtra("reference");
        } else {
            name = method.pref.getString(method.regName, null);
            email = method.pref.getString(method.regEmail, null);
            password = method.pref.getString(method.regPassword, null);
            phoneNo = method.pref.getString(method.regPhoneNo, null);
            reference = method.pref.getString(method.regReference, null);
        }

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        pinView = findViewById(R.id.firstPinView);
        MaterialButton button_verification = findViewById(R.id.button_verification);
        MaterialButton button_register = findViewById(R.id.button_register_verification);
        MaterialTextView textView = findViewById(R.id.resend_verification);

        textView.setOnClickListener(v -> {

            Random generator = new Random();
            int n = generator.nextInt(9999 - 1000) + 1000;

            String stringEmail = method.pref.getString(method.regEmail, null);
            resendVerification(stringEmail, String.valueOf(n));

        });

        button_verification.setOnClickListener(v -> {
            verification = pinView.getText().toString();
            verification();
        });

        button_register.setOnClickListener(v -> {
            method.editor.putBoolean(method.isVerification, false);
            method.editor.commit();
            startActivity(new Intent(Verification.this, Register.class));
            finishAffinity();
        });

    }

    public void verification() {

        pinView.clearFocus();
        imm.hideSoftInputFromWindow(pinView.getWindowToken(), 0);

        if (verification == null || verification.equals("") || verification.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_enter_verification_code));
        } else {
            if (method.isNetworkAvailable()) {
                pinView.setText("");
                if (verification.equals(method.pref.getString(method.verificationCode, null))) {
                    register(name, email, password, phoneNo, reference);
                } else {
                    method.alertBox(getResources().getString(R.string.verification_message));
                }
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }

        }
    }

    @SuppressLint("HardwareIds")
    public void register(String sendName, String sendEmail, String sendPassword, String sendPhone, String reference) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Verification.this));
        jsObj.addProperty("type", "normal");
        jsObj.addProperty("name", sendName);
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("password", sendPassword);
        jsObj.addProperty("phone", sendPhone);
        jsObj.addProperty("device_id", method.getDeviceId());
        jsObj.addProperty("user_refrence_code", reference);
        jsObj.addProperty("method_name", "user_register");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<RegisterRP> call = apiService.getRegisterDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<RegisterRP>() {
            @Override
            public void onResponse(@NotNull Call<RegisterRP> call, @NotNull Response<RegisterRP> response) {

                try {

                    RegisterRP registerRP = response.body();

                    assert registerRP != null;
                    if (registerRP.getStatus().equals("1")) {

                        method.editor.putBoolean(method.isVerification, false);
                        method.editor.commit();

                        if (registerRP.getSuccess().equals("1")) {
                            startActivity(new Intent(Verification.this, Login.class));
                        } else {
                            startActivity(new Intent(Verification.this, Register.class));
                        }
                        finishAffinity();

                        Toast.makeText(Verification.this, registerRP.getMsg(), Toast.LENGTH_SHORT).show();

                    } else {
                        method.alertBox(registerRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<RegisterRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("onFailure_data", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }


    public void resendVerification(String sendEmail, String otp) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Verification.this));
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("otp_code", otp);
        jsObj.addProperty("method_name", "user_register_verify_email");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.getVerification(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {

                    DataRP dataRP = response.body();

                    assert dataRP != null;
                    if (dataRP.getStatus().equals("1")) {
                        if (dataRP.getSuccess().equals("1")) {
                            method.editor.putString(method.verificationCode, otp);
                            method.editor.commit();
                        } else {
                            method.alertBox(dataRP.getMsg());
                        }
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
}
