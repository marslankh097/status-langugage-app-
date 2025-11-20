package com.example.status.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.status.R;
import com.example.status.response.LoginRP;
import com.example.status.response.RegisterRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.onesignal.OSDeviceState;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.NotNull;

import cn.refactor.library.SmoothCheckBox;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private Method method;
    private SmoothCheckBox checkBox;
    private SmoothCheckBox checkBoxTerms;
    private TextInputEditText editTextEmail, editTextPassword;

    public static final String mypreference = "statusLogin";
    public static final String pref_email = "pref_email";
    public static final String pref_password = "pref_password";
    public static final String pref_check = "pref_check";
    static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;

    //Google login
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 007;
    private InputMethodManager imm;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_login);

        method = new Method(Login.this);
        method.forceRTLIfSupported();

        pref = getSharedPreferences(mypreference, 0); // 0 - for private mode
        editor = pref.edit();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        progressDialog = new ProgressDialog(Login.this);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        editTextEmail = findViewById(R.id.editText_email_login);
        editTextPassword = findViewById(R.id.editText_password_login);

        MaterialButton buttonLogin = findViewById(R.id.button_login);
        final RelativeLayout llGoogleSign = findViewById(R.id.rel_google_login);
        MaterialButton buttonSkip = findViewById(R.id.btnSkip);
        TextView textViewRegister = findViewById(R.id.tvSignUp);
        TextView textViewFP = findViewById(R.id.textView_fp_login);
        TextView textViewTerms = findViewById(R.id.tvPrivacyTerms);
        checkBoxTerms = findViewById(R.id.cbPrivacyTerms);
        checkBox = findViewById(R.id.checkbox_login);
        checkBox.setChecked(false);

        if (pref.getBoolean(pref_check, false)) {
            editTextEmail.setText(pref.getString(pref_email, null));
            editTextPassword.setText(pref.getString(pref_password, null));
            checkBox.setChecked(true);
        } else {
            editTextEmail.setText("");
            editTextPassword.setText("");
            checkBox.setChecked(false);
        }

        checkBox.setOnCheckedChangeListener((checkBox1, isChecked) -> {
            if (isChecked) {
                editor.putString(pref_email, editTextEmail.getText().toString());
                editor.putString(pref_password, editTextPassword.getText().toString());
                editor.putBoolean(pref_check, true);
            } else {
                editor.putBoolean(pref_check, false);
            }
            editor.commit();
        });

        buttonLogin.setOnClickListener(v -> login());

        llGoogleSign.setOnClickListener(view -> {
            if (checkBoxTerms.isChecked()) {
                signIn();
            } else {
                method.alertBox(getResources().getString(R.string.please_select_terms));
            }
        });

        textViewRegister.setOnClickListener(v -> {
            Method.loginBack = false;
            startActivity(new Intent(Login.this, Register.class));
        });

        textViewTerms.setOnClickListener(v -> {
            Method.loginBack = false;
            startActivity(new Intent(Login.this, TermsConditions.class));
        });

        buttonSkip.setOnClickListener(v -> {
            if (Method.loginBack) {
                Method.loginBack = false;
                onBackPressed();
            } else {
                startActivity(new Intent(Login.this, MainActivity.class));
                finish();
            }
        });

        textViewFP.setOnClickListener(v -> {
            Method.loginBack = false;
            startActivity(new Intent(Login.this, ForgetPassword.class));
        });

    }

    //Google login
    private void signIn() {
        if (method.isNetworkAvailable()) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    //Google login get callback
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    //Google login
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.

            assert account != null;
            String id = account.getId();
            String name = account.getDisplayName();
            String email = account.getEmail();

            registerSocialNetwork(id, name, email, "google");

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
        }
    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void login() {

        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        editTextEmail.setError(null);
        editTextPassword.setError(null);

        if (!isValidMail(email) || email.isEmpty()) {
            editTextEmail.requestFocus();
            editTextEmail.setError(getResources().getString(R.string.please_enter_email));
        } else if (password.isEmpty()) {
            editTextPassword.requestFocus();
            editTextPassword.setError(getResources().getString(R.string.please_enter_password));
        } else {

            editTextEmail.clearFocus();
            editTextPassword.clearFocus();
            imm.hideSoftInputFromWindow(editTextEmail.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editTextPassword.getWindowToken(), 0);

            if (method.isNetworkAvailable()) {
                login(email, password);
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        }
    }

    public void login(final String sendEmail, final String sendPassword) {

        OSDeviceState device = OneSignal.getDeviceState();

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Login.this));
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("password", sendPassword);
        assert device != null;
        jsObj.addProperty("player_id", device.getUserId());
        jsObj.addProperty("method_name", "user_login");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<LoginRP> call = apiService.getLogin(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<LoginRP>() {
            @Override
            public void onResponse(@NotNull Call<LoginRP> call, @NotNull Response<LoginRP> response) {

                try {
                    LoginRP loginRP = response.body();
                    assert loginRP != null;

                    if (loginRP.getStatus().equals("1")) {

                        if (loginRP.getSuccess().equals("1")) {

                            OneSignal.sendTag("user_id", loginRP.getUser_id());
                            OneSignal.sendTag("player_id", device.getUserId());

                            method.editor.putBoolean(method.prefLogin, true);
                            method.editor.putString(method.profileId, loginRP.getUser_id());
                            method.editor.putString(method.loginType, "normal");
                            method.editor.commit();

                            if (checkBox.isChecked()) {
                                editor.putString(pref_email, editTextEmail.getText().toString());
                                editor.putString(pref_password, editTextPassword.getText().toString());
                                editor.putBoolean(pref_check, true);
                                editor.commit();
                            }

                            editTextEmail.setText("");
                            editTextPassword.setText("");

                            if (Method.loginBack) {
                                Events.Login loginNotify = new Events.Login("");
                                GlobalBus.getBus().post(loginNotify);
                                Method.loginBack = false;
                                onBackPressed();
                            } else {
                                startActivity(new Intent(Login.this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                finishAffinity();
                            }

                        } else {
                            method.alertBox(loginRP.getMsg());
                        }

                    } else {
                        method.alertBox(loginRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<LoginRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }


    @SuppressLint("HardwareIds")
    public void registerSocialNetwork(String id, String sendName, String sendEmail, String type) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        OSDeviceState device = OneSignal.getDeviceState();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Login.this));
        jsObj.addProperty("type", type);
        jsObj.addProperty("auth_id", id);
        jsObj.addProperty("name", sendName);
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("device_id", method.getDeviceId());
        assert device != null;
        jsObj.addProperty("player_id", device.getUserId());
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

                        if (registerRP.getSuccess().equals("1")) {

                            method.editor.putBoolean(method.isVerification, false);
                            method.editor.commit();

                            OneSignal.sendTag("user_id", registerRP.getUser_id());
                            OneSignal.sendTag("player_id", device.getUserId());

                            method.editor.putBoolean(method.prefLogin, true);
                            method.editor.putString(method.profileId, registerRP.getUser_id());
                            method.editor.putString(method.loginType, type);
                            method.editor.commit();

                            if (Method.loginBack) {
                                Events.Login loginNotify = new Events.Login("");
                                GlobalBus.getBus().post(loginNotify);
                                Method.loginBack = false;
                                onBackPressed();
                            } else {
                                if (registerRP.getReferral_code().equals("true")) {
                                    startActivity(new Intent(Login.this, EnterReferenceCode.class)
                                            .putExtra("user_id", registerRP.getUser_id())
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                } else {
                                    startActivity(new Intent(Login.this, MainActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                                }
                                finishAffinity();
                            }

                        } else {
                            if (type.equals("google")) {
                                mGoogleSignInClient.signOut()
                                        .addOnCompleteListener(Login.this, task -> {
                                            method.editor.putBoolean(method.prefLogin, false);
                                            method.editor.commit();
                                        });
                            }
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
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    @Override
    public void onBackPressed() {
        Method.loginBack = false;
        super.onBackPressed();
    }
}
