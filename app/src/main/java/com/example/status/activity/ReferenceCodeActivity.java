package com.example.status.activity;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.example.status.R;
import com.example.status.response.ProfileRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ReferenceCodeActivity extends AppCompatActivity {

    private Method method;
    private ProfileRP profileRP;
    private ImageView imageViewData;
    private MaterialButton buttonLogin;
    private ProgressBar progressBar;
    private MaterialTextView textView, textViewData;
    private ConstraintLayout conMain, conCopy, conNoData;

    private MaterialToolbar toolbar;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reference_code);

        toolbar = findViewById(R.id.toolbar_spinner);
        toolbar.setTitle(getResources().getString(R.string.reference_code));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        method = new Method(ReferenceCodeActivity.this);

        conNoData = findViewById(R.id.con_not_login);
        progressBar = findViewById(R.id.progressbar_spinner);
        imageViewData = findViewById(R.id.imageView_not_login);
        buttonLogin = findViewById(R.id.button_not_login);
        textViewData = findViewById(R.id.textView_not_login);
        conMain = findViewById(R.id.con_referenceCode);
        conCopy = findViewById(R.id.con_copyReference_code);
        textView = findViewById(R.id.textView_referenceCode);

        conMain.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        data(false, false);

        buttonLogin.setOnClickListener(v -> {
            startActivity(new Intent(ReferenceCodeActivity.this, Login.class));
            finishAffinity();
        });

        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                profile(method.userId());
            } else {
                data(true, true);
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share_menu, menu);
        MenuItem share = menu.findItem(R.id.action_share);
        share.setVisible(method.isLogin());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // action with ID action_refresh was selected
        if (item.getItemId() == R.id.action_share) {
            if (profileRP != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.your_reference_code) + ":-" + profileRP.getUser_code()
                        + "\n" + "\n" + "https://play.google.com/store/apps/details?id=" + ReferenceCodeActivity.this.getApplication().getPackageName());
                startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_to)));
            } else {
                method.alertBox(getResources().getString(R.string.wrong));
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void data(boolean isShow, boolean isLogin) {
        if (isShow) {
            if (isLogin) {
                buttonLogin.setVisibility(View.VISIBLE);
                textViewData.setText(getResources().getString(R.string.you_have_not_login));
                imageViewData.setImageDrawable(getResources().getDrawable(R.drawable.no_login));
            } else {
                buttonLogin.setVisibility(View.GONE);
                textViewData.setText(getResources().getString(R.string.no_data_found));
                imageViewData.setImageDrawable(getResources().getDrawable(R.drawable.no_data));
            }
            conNoData.setVisibility(View.VISIBLE);
        } else {
            conNoData.setVisibility(View.GONE);
        }
    }


    public void profile(String id) {

        if (ReferenceCodeActivity.this != null) {

            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(ReferenceCodeActivity.this));
            jsObj.addProperty("user_id", id);
            jsObj.addProperty("method_name", "user_profile");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<ProfileRP> call = apiService.getUserReferenceCode(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<ProfileRP>() {
                @Override
                public void onResponse(@NotNull Call<ProfileRP> call, @NotNull Response<ProfileRP> response) {

                    if (ReferenceCodeActivity.this != null) {

                        try {

                            profileRP = response.body();
                            assert profileRP != null;

                            if (profileRP.getStatus().equals("1")) {

                                if (profileRP.getSuccess().equals("1")) {

                                    textView.setText(profileRP.getUser_code());

                                    conMain.setVisibility(View.VISIBLE);

                                    conCopy.setOnClickListener(v -> {
                                        ClipboardManager clipboard = (ClipboardManager) ReferenceCodeActivity.this.getSystemService(CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("label", profileRP.getUser_code());
                                        assert clipboard != null;
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(ReferenceCodeActivity.this, getResources().getString(R.string.copy_text), Toast.LENGTH_SHORT).show();
                                    });


                                } else {
                                    data(true, false);
                                    method.alertBox(profileRP.getMsg());
                                }

                            } else if (profileRP.getStatus().equals("2")) {
                                method.suspend(profileRP.getMessage());
                            } else {
                                data(true, false);
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
                    data(true, false);
                    progressBar.setVisibility(View.GONE);
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
