package com.example.status.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.status.R;
import com.example.status.response.CheckOtpRP;
import com.example.status.response.DataRP;
import com.example.status.response.RegisterRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Method;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

import cn.refactor.library.SmoothCheckBox;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Register extends AppCompatActivity {

    private Method method;
    private String reference = "";
    private InputMethodManager imm;
    private ProgressDialog progressDialog;
    private MaterialButton buttonSubmit;
    private SmoothCheckBox checkBox;
    private String name, email, password, phoneNo;
    private TextInputEditText editTextName, editTextEmail, editTextPassword, editTextPhoneNo, editTextReference;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_register);

        method = new Method(Register.this);
        method.forceRTLIfSupported();

        progressDialog = new ProgressDialog(Register.this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        editTextName = findViewById(R.id.editText_name_register);
        editTextEmail = findViewById(R.id.editText_email_register);
        editTextPassword = findViewById(R.id.editText_password_register);
        editTextPhoneNo = findViewById(R.id.editText_phoneNo_register);
        editTextReference = findViewById(R.id.editText_reference_code_register);

        buttonSubmit = findViewById(R.id.button_login);
        MaterialTextView textViewLogin = findViewById(R.id.textView_login_register);
        TextView textViewTerms = findViewById(R.id.tvPrivacyTerms);
        checkBox = findViewById(R.id.cbPrivacyTerms);

        textViewTerms.setOnClickListener(v -> startActivity(new Intent(Register.this, TermsConditions.class)));

        textViewLogin.setOnClickListener(v -> {
            method.editor.putBoolean(method.isVerification, false);
            method.editor.commit();
            startActivity(new Intent(Register.this, Login.class));
            finishAffinity();
        });

        checkOtp();

    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void checkOtp() {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Register.this));
        jsObj.addProperty("method_name", "otp_status");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<CheckOtpRP> call = apiService.getOtpStatus(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<CheckOtpRP>() {
            @Override
            public void onResponse(@NotNull Call<CheckOtpRP> call, @NotNull Response<CheckOtpRP> response) {

                try {
                    CheckOtpRP checkOtpRP = response.body();
                    assert checkOtpRP != null;

                    if (checkOtpRP.getStatus().equals("1")) {
                        if (checkOtpRP.getStatus().equals("1")) {
                            buttonSubmit.setOnClickListener(v -> form(checkOtpRP.getOtp_status()));
                        } else {
                            method.alertBox(checkOtpRP.getMessage());
                        }
                    } else {
                        method.alertBox(checkOtpRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<CheckOtpRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    public void form(String status) {

        name = editTextName.getText().toString();
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        phoneNo = editTextPhoneNo.getText().toString();
        reference = editTextReference.getText().toString();

        editTextName.setError(null);
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        editTextPhoneNo.setError(null);

        if (name.equals("") || name.isEmpty()) {
            editTextName.requestFocus();
            editTextName.setError(getResources().getString(R.string.please_enter_name));
        } else if (!isValidMail(email) || email.isEmpty()) {
            editTextEmail.requestFocus();
            editTextEmail.setError(getResources().getString(R.string.please_enter_email));
        } else if (password.equals("") || password.isEmpty()) {
            editTextPassword.requestFocus();
            editTextPassword.setError(getResources().getString(R.string.please_enter_password));
        } else if (phoneNo.equals("") || phoneNo.isEmpty()) {
            editTextPhoneNo.requestFocus();
            editTextPhoneNo.setError(getResources().getString(R.string.please_enter_phone));
        } else if (!checkBox.isChecked()) {
            method.alertBox(getResources().getString(R.string.please_select_terms));
        } else {

            editTextName.clearFocus();
            editTextEmail.clearFocus();
            editTextPassword.clearFocus();
            editTextPhoneNo.clearFocus();
            imm.hideSoftInputFromWindow(editTextName.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editTextEmail.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editTextPassword.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editTextPhoneNo.getWindowToken(), 0);

            if (method.isNetworkAvailable()) {

                if (status.equals("true")) {

                    Random generator = new Random();
                    int n = generator.nextInt(9999 - 1000) + 1000;

                    verificationCall(email, String.valueOf(n));

                } else {
                    register(name, email, password, phoneNo, reference);
                }

            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }

        }
    }

    public void verificationCall(String sendEmail, String otp) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Register.this));
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

                            method.editor.putBoolean(method.isVerification, true);
                            method.editor.putString(method.regName, name);
                            method.editor.putString(method.regEmail, email);
                            method.editor.putString(method.regPassword, password);
                            method.editor.putString(method.regPhoneNo, phoneNo);
                            method.editor.putString(method.regReference, reference);
                            method.editor.putString(method.verificationCode, otp);
                            method.editor.commit();

                            editTextName.setText("");
                            editTextEmail.setText("");
                            editTextPassword.setText("");
                            editTextPhoneNo.setText("");
                            editTextReference.setText("");

                            Toast.makeText(Register.this, dataRP.getMsg(), Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(Register.this, Verification.class)
                                    .putExtra("name", name)
                                    .putExtra("email", email)
                                    .putExtra("password", password)
                                    .putExtra("phoneNo", phoneNo)
                                    .putExtra("reference", reference));
                            finishAffinity();

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

    @SuppressLint("HardwareIds")
    public void register(String sendName, String sendEmail, String sendPassword, String sendPhone, String reference) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Register.this));
        jsObj.addProperty("method_name", "user_register");
        jsObj.addProperty("type", "normal");
        jsObj.addProperty("name", sendName);
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("password", sendPassword);
        jsObj.addProperty("phone", sendPhone);
        jsObj.addProperty("device_id", method.getDeviceId());
        jsObj.addProperty("user_refrence_code", reference);
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<RegisterRP> call = apiService.getRegisterDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<RegisterRP>() {
            @Override
            public void onResponse(@NotNull Call<RegisterRP> call, @NotNull Response<RegisterRP> response) {

                try {

                    RegisterRP registerRP = response.body();

                    assert registerRP != null;
                    if (registerRP.getStatus().equals("1")) {
                        if (registerRP.getSuccess().equals("1")) {
                            startActivity(new Intent(Register.this, Login.class));
                            finishAffinity();
                            Toast.makeText(Register.this, registerRP.getMsg(), Toast.LENGTH_SHORT).show();
                        } else {
                            method.alertBox(registerRP.getMsg());
                        }
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

}
