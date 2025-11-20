package com.example.status.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.adapter.LanguageAdapter;
import com.example.status.interfaces.LanguageIF;
import com.example.status.response.LanguageRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Language extends AppCompatActivity {

    private Method method;
    private LanguageIF languageIF;
    private String type, languageId = "";
    private ProgressBar progressBar;
    ImageView imageViewClose;
    private MaterialButton buttonContinue;
    private RecyclerView recyclerView;
    private ArrayList<String> languageIdsList;
    private LanguageAdapter languageAdapter;
    private MaterialTextView textViewSkip;
    private ConstraintLayout conNoData, conMain;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        method = new Method(Language.this);
        method.forceRTLIfSupported();

        type = getIntent().getStringExtra("type");

        // Checking for first time launch - before calling setContentView()
        if (!method.isLanguage()) {
            if (type.equals("welcome")) {
                launchHomeScreen();
            }
        }

        setContentView(R.layout.activity_language);

        languageIdsList = new ArrayList<>();

        languageIF = (id, getType, position, isValue) -> {
            if (isValue) {
                languageIdsList.add(id);
                if (type.equals("welcome")) {
                    if (languageIdsList.size() == 0) {
                        buttonContinue.setVisibility(View.GONE);
                        textViewSkip.setVisibility(View.VISIBLE);
                    } else {
                        buttonContinue.setVisibility(View.VISIBLE);
                        textViewSkip.setVisibility(View.GONE);
                    }
                }
            } else {
                new LanguageSelect().execute(id);
            }
        };

        conMain = findViewById(R.id.con_main_language);
        conNoData = findViewById(R.id.con_noDataFound);
        progressBar = findViewById(R.id.progressBar_language);
        imageViewClose = findViewById(R.id.imageView_close_language);
        textViewSkip = findViewById(R.id.textView_skip_language);
        buttonContinue = findViewById(R.id.button_language);
        recyclerView = findViewById(R.id.recyclerView_language);

        conNoData.setVisibility(View.GONE);
        conMain.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        buttonContinue.setVisibility(View.GONE);

        LinearLayout linearLayout = findViewById(R.id.linearLayout_language);

        if (type.equals("setting") || type.equals("menu")) {
            textViewSkip.setVisibility(View.GONE);
            imageViewClose.setVisibility(View.VISIBLE);
        } else {
            textViewSkip.setVisibility(View.VISIBLE);
            imageViewClose.setVisibility(View.GONE);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(Language.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);

        textViewSkip.setOnClickListener(v -> launchHomeScreen());

        imageViewClose.setOnClickListener(v -> onBackPressed());

        if (method.isNetworkAvailable()) {
            language();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    @SuppressLint("StaticFieldLeak")
    class LanguageSelect extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            for (int i = 0; i < languageIdsList.size(); i++) {
                if (languageIdsList.get(i).equals(strings[0])) {
                    languageIdsList.remove(i);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (type.equals("welcome")) {
                if (languageIdsList.size() == 0) {
                    buttonContinue.setVisibility(View.GONE);
                    textViewSkip.setVisibility(View.VISIBLE);
                } else {
                    buttonContinue.setVisibility(View.VISIBLE);
                    textViewSkip.setVisibility(View.GONE);
                }
            }
        }
    }

    private void launchHomeScreen() {
        method.setFirstLanguage(false);
        startActivity(new Intent(Language.this, SplashScreen.class)
                .putExtra("type", "welcome"));
        finish();
    }

    private void language() {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Language.this));
        jsObj.addProperty("lang_ids", method.getLanguageIds());
        jsObj.addProperty("method_name", "get_language");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<LanguageRP> call = apiService.getLanguage(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<LanguageRP>() {
            @Override
            public void onResponse(@NotNull Call<LanguageRP> call, @NotNull Response<LanguageRP> response) {

                try {
                    LanguageRP languageRP = response.body();
                    assert languageRP != null;

                    if (languageRP.getStatus().equals("1")) {

                        for (int i = 0; i < languageRP.getLanguageLists().size(); i++) {
                            if (languageRP.getLanguageLists().get(i).getIs_selected().equals("true")) {
                                languageIdsList.add(languageRP.getLanguageLists().get(i).getLanguage_id());
                            }
                        }

                        if (languageRP.getLanguageLists().size() != 0) {

                            if (type.equals("welcome")) {
                                if (languageIdsList.size() == 0) {
                                    buttonContinue.setVisibility(View.GONE);
                                    textViewSkip.setVisibility(View.VISIBLE);
                                } else {
                                    buttonContinue.setVisibility(View.VISIBLE);
                                    textViewSkip.setVisibility(View.GONE);
                                }
                            } else {
                                buttonContinue.setVisibility(View.VISIBLE);
                            }

                            buttonContinue.setOnClickListener(v -> {

                                method.editor.putString(method.languageIds, "");

                                for (int i = 0; i < languageIdsList.size(); i++) {
                                    if (i != 0) {
                                        languageId = languageId.concat(",");
                                    }
                                    languageId = languageId.concat(languageIdsList.get(i));
                                }

                                method.editor.putString(method.languageIds, languageId);
                                method.editor.commit();

                                if (type.equals("setting")) {
                                    onBackPressed();
                                } else if (type.equals("menu")) {
                                    onBackPressed();
                                    Events.Language language = new Events.Language(type);
                                    GlobalBus.getBus().post(language);
                                } else {
                                    launchHomeScreen();
                                }

                            });

                            languageAdapter = new LanguageAdapter(Language.this, languageRP.getLanguageLists(), languageIF);
                            recyclerView.setAdapter(languageAdapter);

                            conMain.setVisibility(View.VISIBLE);

                        } else {
                            conNoData.setVisibility(View.VISIBLE);
                        }

                    } else {
                        method.alertBox(languageRP.getMessage());
                        conNoData.setVisibility(View.VISIBLE);
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<LanguageRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                conNoData.setVisibility(View.VISIBLE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

}
