package com.example.status.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.response.AccountDetailRP;
import com.example.status.response.DataRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.BannerAds;
import com.example.status.util.Method;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeleteAccount extends AppCompatActivity {

    private Method method;
    MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private WebView webView;
    private CircleImageView imageView;
    private MaterialButton button;
    private InputMethodManager imm;
    private ConstraintLayout conMain, conNoData;
    private MaterialTextView textViewUserName, textViewQuoteStatus, textViewImageStatus, textViewGifStatus, textViewVideoStatus,
            textViewFollowers, textViewFollowing, textViewEarnPoint, textViewPendingPoint, textViewReferenceCode;

    //Google login
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        method = new Method(DeleteAccount.this);
        method.forceRTLIfSupported();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        toolbar = findViewById(R.id.toolbar_delete);
        toolbar.setTitle(getResources().getString(R.string.delete_my_account));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        conMain = findViewById(R.id.con_main_delete);
        conNoData = findViewById(R.id.con_noDataFound);
        progressBar = findViewById(R.id.progressbar_delete);
        imageView = findViewById(R.id.imageView_delete);
        webView = findViewById(R.id.webView_delete);
        button = findViewById(R.id.button_delete);
        textViewUserName = findViewById(R.id.textView_userName_delete);
        textViewQuoteStatus = findViewById(R.id.textView_quoteStatus_delete);
        textViewImageStatus = findViewById(R.id.textView_imageStatus_delete);
        textViewGifStatus = findViewById(R.id.textView_gifStatus_delete);
        textViewVideoStatus = findViewById(R.id.textView_videoStatus_delete);
        textViewFollowers = findViewById(R.id.textView_followers_delete);
        textViewFollowing = findViewById(R.id.textView_following_delete);
        textViewEarnPoint = findViewById(R.id.textView_earnPoint_delete);
        textViewPendingPoint = findViewById(R.id.textView_pendingPoint_delete);
        textViewReferenceCode = findViewById(R.id.textView_reference_code_delete);

        conMain.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);

        LinearLayout linearLayout = findViewById(R.id.linearLayout_delete);
        BannerAds.showBannerAds(DeleteAccount.this,linearLayout);

        if (method.isNetworkAvailable()) {
            accountDetail(method.userId());
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    public void accountDetail(String userId) {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(DeleteAccount.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "get_user_data");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<AccountDetailRP> call = apiService.getAccountDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<AccountDetailRP>() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void onResponse(@NotNull Call<AccountDetailRP> call, @NotNull Response<AccountDetailRP> response) {

                try {

                    AccountDetailRP accountDetailRP = response.body();

                    assert accountDetailRP != null;
                    if (accountDetailRP.getStatus().equals("1")) {

                        if (accountDetailRP.getSuccess().equals("1")) {

                            Glide.with(DeleteAccount.this)
                                    .load(accountDetailRP.getUser_image()).placeholder(R.drawable.user_profile)
                                    .into(imageView);

                            textViewUserName.setText(accountDetailRP.getName());
                            if (accountDetailRP.getIs_verified().equals("true")) {
                                textViewUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verification, 0);
                            }

                            textViewQuoteStatus.setText(accountDetailRP.getTotal_quote());
                            textViewImageStatus.setText(accountDetailRP.getTotal_image());
                            textViewGifStatus.setText(accountDetailRP.getTotal_gif());
                            textViewVideoStatus.setText(accountDetailRP.getTotal_video());
                            textViewFollowers.setText(accountDetailRP.getTotal_followers());
                            textViewFollowing.setText(accountDetailRP.getTotal_following());
                            textViewEarnPoint.setText(accountDetailRP.getTotal_point());
                            textViewPendingPoint.setText(accountDetailRP.getPending_point());
                            textViewReferenceCode.setText(accountDetailRP.getUser_code());

                            webView.setBackgroundColor(Color.TRANSPARENT);
                            webView.setFocusableInTouchMode(false);
                            webView.setFocusable(false);
                            webView.getSettings().setDefaultTextEncodingName("UTF-8");
                            webView.getSettings().setJavaScriptEnabled(true);
                            String mimeType = "text/html";
                            String encoding = "utf-8";

                            String text = "<html dir=" + method.isWebViewTextRtl() + "><head>"
                                    + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/opensans_semi_bold.otf\")}body{font-family: MyFont;color: " + method.webViewText() + "line-height:1.6}"
                                    + "a {color:" + method.webViewLink() + "text-decoration:none}"
                                    + "</style></head>"
                                    + "<body>"
                                    + accountDetailRP.getDelete_note()
                                    + "</body></html>";

                            webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

                            conMain.setVisibility(View.VISIBLE);

                            button.setOnClickListener(v -> {

                                final Dialog dialog = new Dialog(DeleteAccount.this);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView(R.layout.dialog_delete);
                                if (method.isRtl()) {
                                    dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                                }
                                dialog.getWindow().setLayout(ViewPager.LayoutParams.FILL_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
                                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                                MaterialButton buttonDialog = dialog.findViewById(R.id.button_dialog_delete);
                                TextInputEditText editTextEmail = dialog.findViewById(R.id.editText_dialog_delete);

                                editTextEmail.setText(accountDetailRP.getEmail());

                                buttonDialog.setOnClickListener(v1 -> {

                                    String emailId = editTextEmail.getText().toString();

                                    editTextEmail.clearFocus();
                                    imm.hideSoftInputFromWindow(editTextEmail.getWindowToken(), 0);

                                    if (!isValidMail(emailId) || emailId.isEmpty()) {
                                        editTextEmail.requestFocus();
                                        editTextEmail.setError(getResources().getString(R.string.please_enter_email));
                                    } else {

                                        if (method.isNetworkAvailable()) {
                                            deleteAccount(emailId, userId);
                                        } else {
                                            method.alertBox(getResources().getString(R.string.internet_connection));
                                        }

                                    }

                                });

                                dialog.show();

                            });

                        } else {
                            conNoData.setVisibility(View.VISIBLE);
                            method.alertBox(accountDetailRP.getMsg());
                        }
                    } else if (accountDetailRP.getStatus().equals("2")) {
                        method.suspend(accountDetailRP.getMessage());
                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(accountDetailRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NotNull Call<AccountDetailRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("onFailure_data", t.toString());
                conNoData.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void deleteAccount(String email_id, String userId) {

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(DeleteAccount.this));
        jsObj.addProperty("email", email_id);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "delete_user_account");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.deleteAccount(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {

                    DataRP dataRP = response.body();

                    assert dataRP != null;
                    if (dataRP.getStatus().equals("1")) {

                        if (dataRP.getSuccess().equals("1")) {

                            OneSignal.sendTag("user_id", method.userId());

                            if (method.getLoginType().equals("google")) {
                                mGoogleSignInClient.signOut()
                                        .addOnCompleteListener(DeleteAccount.this, new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                            }
                                        });
                            }

                            Toast.makeText(DeleteAccount.this, dataRP.getMsg(), Toast.LENGTH_SHORT).show();

                            method.editor.putBoolean(method.prefLogin, false);
                            method.editor.commit();

                            startActivity(new Intent(DeleteAccount.this, Login.class));
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

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(@NotNull Call<DataRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("onFailure_data", t.toString());
                progressBar.setVisibility(View.GONE);
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
