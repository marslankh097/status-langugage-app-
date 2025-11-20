package com.example.status.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.response.SuspendRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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

public class Suspend extends AppCompatActivity {

    private Method method;
    MaterialToolbar toolbar;
    private String accountId;
    private ProgressBar progressBar;
    private View viewData;
    MaterialButton button;
    private MaterialCardView cardView;
    private CircleImageView imageView;
    private ConstraintLayout conNoData;
    private MaterialTextView textViewUserName, textViewStatusMsg, textViewStatus, textViewDate, textViewTitleAdminMsg, textViewAdminMsg;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suspend);

        method = new Method(Suspend.this);
        method.forceRTLIfSupported();

        toolbar = findViewById(R.id.toolbar_suspend);
        toolbar.setTitle(getResources().getString(R.string.account_status));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        accountId = getIntent().getStringExtra("id");

        progressBar = findViewById(R.id.progressbar_suspend);
        cardView = findViewById(R.id.cardView_suspend);
        conNoData = findViewById(R.id.con_noDataFound);
        button = findViewById(R.id.button_suspend);
        imageView = findViewById(R.id.imageView_suspend);
        viewData = findViewById(R.id.view_date_suspend);
        textViewUserName = findViewById(R.id.textView_userName_suspend);
        textViewStatusMsg = findViewById(R.id.textView_statusMsg_suspend);
        textViewStatus = findViewById(R.id.textView_status_suspend);
        textViewDate = findViewById(R.id.textView_date_suspend);
        textViewTitleAdminMsg = findViewById(R.id.textView_titleAdmin_msg_suspend);
        textViewAdminMsg = findViewById(R.id.textView_admin_msg_suspend);

        cardView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);

        if (method.isNetworkAvailable()) {
            userAccount();
        } else {
            progressBar.setVisibility(View.GONE);
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

        button.setOnClickListener(view -> finishAffinity());

    }

    public void userAccount() {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Suspend.this));
        jsObj.addProperty("account_id", accountId);
        jsObj.addProperty("method_name", "user_suspend");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<SuspendRP> call = apiService.getSuspend(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<SuspendRP>() {
            @Override
            public void onResponse(@NotNull Call<SuspendRP> call, @NotNull Response<SuspendRP> response) {

                try {
                    SuspendRP suspendrp = response.body();
                    assert suspendrp != null;

                    if (suspendrp.getStatus().equals("1")) {

                        if (!suspendrp.getUser_image().equals("")) {
                            Glide.with(Suspend.this).load(suspendrp.getUser_image())
                                    .placeholder(R.drawable.user_profile).into(imageView);
                        }

                        textViewUserName.setText(suspendrp.getUser_name());
                        if (suspendrp.getIs_verified().equals("true")) {
                            textViewUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verification, 0);
                        }
                        textViewDate.setText(suspendrp.getDate());


                        if (suspendrp.getSuccess().equals("1")) {
                            textViewTitleAdminMsg.setVisibility(View.GONE);
                            textViewAdminMsg.setVisibility(View.GONE);
                            viewData.setVisibility(View.GONE);
                            textViewStatus.setText(getResources().getString(R.string.active));
                            textViewStatus.setTextColor(getResources().getColor(R.color.green));
                            textViewStatusMsg.setText(getResources().getString(R.string.msg_approved));
                            textViewStatusMsg.setTextColor(getResources().getColor(R.color.green));
                        } else {

                            textViewTitleAdminMsg.setVisibility(View.VISIBLE);
                            textViewAdminMsg.setVisibility(View.VISIBLE);
                            viewData.setVisibility(View.VISIBLE);

                            textViewAdminMsg.setText(suspendrp.getMsg());

                            textViewStatus.setText(getResources().getString(R.string.suspend));
                            textViewStatus.setTextColor(getResources().getColor(R.color.red));
                            textViewStatusMsg.setText(getResources().getString(R.string.msg_suspend));
                            textViewStatusMsg.setTextColor(getResources().getColor(R.color.red));

                            if (method.isLogin()) {

                                if (method.getLoginType().equals("google")) {

                                    //Google login
                                    GoogleSignInClient mGoogleSignInClient;

                                    // Configure sign-in to request the user's ID, email address, and basic
                                    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestEmail()
                                            .build();

                                    // Build a GoogleSignInClient with the options specified by gso.
                                    mGoogleSignInClient = GoogleSignIn.getClient(Suspend.this, gso);

                                    mGoogleSignInClient.signOut()
                                            .addOnCompleteListener(Suspend.this, new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                }
                                            });
                                }

                                method.editor.putBoolean(method.prefLogin, false);
                                method.editor.commit();
                                Events.Login loginNotify = new Events.Login("");
                                GlobalBus.getBus().post(loginNotify);
                            }

                        }

                        cardView.setVisibility(View.VISIBLE);

                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(suspendrp.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<SuspendRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
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
