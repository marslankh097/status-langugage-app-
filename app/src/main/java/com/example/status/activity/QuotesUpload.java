package com.example.status.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.status.R;
import com.example.status.adapter.SelectLanguageAdapter;
import com.example.status.adapter.SpinnerCatAdapter;
import com.example.status.interfaces.LanguageIF;
import com.example.status.item.CategoryList;
import com.example.status.response.CatLanguageRP;
import com.example.status.response.DataRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.BannerAds;
import com.example.status.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import top.defaults.colorpicker.ColorPickerView;

public class QuotesUpload extends AppCompatActivity {

    private Method method;
    private LanguageIF languageIF;
    private String categoryId, stringFont;
    MaterialToolbar toolbar;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private Dialog dialog;
    private Spinner spinnerCat;
    private RecyclerView recyclerView;
    private NachoTextView nachoTextView;
    private TextInputEditText editText;
    MaterialButton buttonUpload;
    LinearLayout linearLayout;
    private InputMethodManager imm;
    private ArrayList<String> languageIdsList;
    private SelectLanguageAdapter selectLanguageAdapter;
    ImageView imageViewColor;
    MaterialTextView textViewTextStyle;
    private int position, quotesColorBg = 0x7F313C93;
    ConstraintLayout conNoData, conMain, conQuotesBg;

    private final String[] font = {"Anton.ttf", "Cinzel.ttf", "Lemonada.ttf", "Pacifico.ttf", "Poppins.ttf", "Roboto.ttf"};

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quotes_upload);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        progressDialog = new ProgressDialog(QuotesUpload.this);

        method = new Method(QuotesUpload.this);
        method.forceRTLIfSupported();

        languageIdsList = new ArrayList<>();

        languageIF = (id, type, position, isValue) -> {
            if (isValue) {
                languageIdsList.add(id);
            } else {
                new SelectLanguage().execute(id);
            }
        };

        toolbar = findViewById(R.id.toolbar_qu);
        toolbar.setTitle(getResources().getString(R.string.upload_quotes));
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        conMain = findViewById(R.id.con_main_qu);
        progressBar = findViewById(R.id.progressBar_qu);
        conNoData = findViewById(R.id.con_noDataFound);
        spinnerCat = findViewById(R.id.spinner_qu);
        imageViewColor = findViewById(R.id.imageView_colorSelect_qu);
        textViewTextStyle = findViewById(R.id.textView_style_qu);
        buttonUpload = findViewById(R.id.button_qu);
        editText = findViewById(R.id.editText_qu);
        recyclerView = findViewById(R.id.recyclerView_qu);
        nachoTextView = findViewById(R.id.nacho_qu);
        conQuotesBg = findViewById(R.id.constrainLayout_bg_qu);

        conMain.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        dialog = new Dialog(QuotesUpload.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_color);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setLayout(ViewPager.LayoutParams.FILL_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
        ColorPickerView colorPickerView = dialog.findViewById(R.id.colorPicker_dialog_color);
        MaterialButton button_dialog = dialog.findViewById(R.id.button_dialog_color);
        colorPickerView.setInitialColor(0x7F313C93);
        colorPickerView.subscribe((color, fromUser, shouldPropagate) -> {
            conQuotesBg.setBackgroundColor(color);
            quotesColorBg = color;
        });

        button_dialog.setOnClickListener(v -> dialog.dismiss());

        conQuotesBg.setBackgroundColor(quotesColorBg);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(QuotesUpload.this, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        nachoTextView.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN);

        linearLayout = findViewById(R.id.linearLayout_qu);
        BannerAds.showBannerAds(QuotesUpload.this, linearLayout);

        imageViewColor.setOnClickListener(v -> dialog.show());

        Typeface typeface = Typeface.createFromAsset(getAssets(), "text_font/" + font[position]);
        editText.setTypeface(typeface);
        stringFont = font[position];

        textViewTextStyle.setOnClickListener(v -> {
            position++;
            if (position > font.length - 1) {
                position = 0;
            }
            stringFont = font[position];
            Typeface typeface1 = Typeface.createFromAsset(getAssets(), "text_font/" + font[position]);
            editText.setTypeface(typeface1);
        });

        buttonUpload.setOnClickListener(v -> submit());


        if (method.isNetworkAvailable()) {
            categoryLanguage();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    @SuppressLint("StaticFieldLeak")
    class SelectLanguage extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            for (int i = 0; i < languageIdsList.size(); i++) {
                if (languageIdsList.get(i).equals(strings[0])) {
                    languageIdsList.remove(i);
                }
            }

            return languageIdsList.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public void categoryLanguage() {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(QuotesUpload.this));
        jsObj.addProperty("method_name", "get_cat_lang_list");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<CatLanguageRP> call = apiService.getCatLanguageRP(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<CatLanguageRP>() {
            @Override
            public void onResponse(@NotNull Call<CatLanguageRP> call, @NotNull Response<CatLanguageRP> response) {

                try {
                    CatLanguageRP catLanguageRP = response.body();
                    assert catLanguageRP != null;

                    if (catLanguageRP.getStatus().equals("1")) {

                        if (catLanguageRP.getLanguageLists().size() != 0) {
                            selectLanguageAdapter = new SelectLanguageAdapter(QuotesUpload.this, catLanguageRP.getLanguageLists(), languageIF);
                            recyclerView.setAdapter(selectLanguageAdapter);
                        }

                        catLanguageRP.getCategoryLists().add(0, new CategoryList("", getResources().getString(R.string.selected_category), "", "", "", ""));
                        SpinnerCatAdapter spinnerCatAdapter = new SpinnerCatAdapter(QuotesUpload.this, catLanguageRP.getCategoryLists());
                        spinnerCat.setAdapter(spinnerCatAdapter);

                        conMain.setVisibility(View.VISIBLE);

                        // Spinner click listener
                        spinnerCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position == 0) {
                                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_upload));
                                    ((TextView) parent.getChildAt(0)).setTextSize(16);
                                } else {
                                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_app_color));
                                    ((TextView) parent.getChildAt(0)).setTextSize(16);
                                }
                                categoryId = catLanguageRP.getCategoryLists().get(position).getCid();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                    } else {
                        method.alertBox(catLanguageRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<CatLanguageRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    private void submit() {

        String quotes = editText.getText().toString();

        String langIds = "";
        for (int i = 0; i < languageIdsList.size(); i++) {
            if (i != 0) {
                langIds = langIds.concat(",");
            }
            langIds = langIds.concat(languageIdsList.get(i));
        }

        if (quotes.equals("") || quotes.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_enter_quotes));
        } else if (categoryId.equals("") || categoryId.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_category));
        } else if (langIds.equals("") || langIds.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_language));
        } else {

            if (method.isNetworkAvailable()) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                uploadQuotes(method.userId(), categoryId, langIds, quotes, stringFont, quotesColorBg);
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }

        }

    }

    public void uploadQuotes(String userId, String catId, String langIds, String quote, String quoteFont, int quotesColorBg) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        String quotesTags = "";
        for (int i = 0; i < nachoTextView.getAllChips().size(); i++) {
            if (i != 0) {
                quotesTags = quotesTags.concat(",");
            }
            quotesTags = quotesTags.concat(nachoTextView.getAllChips().get(i).toString());
        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(QuotesUpload.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("cat_id", catId);
        jsObj.addProperty("lang_ids", langIds);
        jsObj.addProperty("quote_tags", quotesTags);
        jsObj.addProperty("quote", quote);
        jsObj.addProperty("quote_font", quoteFont);
        jsObj.addProperty("bg_color", quotesColorBg);
        jsObj.addProperty("method_name", "upload_quote_status");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.uploadQuotes(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {
                    DataRP dataRP = response.body();
                    assert dataRP != null;

                    if (dataRP.getStatus().equals("1")) {

                        if (dataRP.getSuccess().equals("1")) {

                            editText.setText("");
                            nachoTextView.setText("");
                            categoryId = "";
                            spinnerCat.setSelection(0);
                            languageIdsList.clear();
                            if (selectLanguageAdapter != null) {
                                selectLanguageAdapter.clearCheckBox();
                            }

                            Toast.makeText(QuotesUpload.this, dataRP.getMsg(), Toast.LENGTH_SHORT).show();

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
