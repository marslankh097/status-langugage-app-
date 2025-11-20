package com.example.status.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.util.BannerAds;
import com.example.status.util.Method;
import com.example.status.util.TouchImageView;
import com.google.android.material.appbar.MaterialToolbar;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ViewImage extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        Method method = new Method(ViewImage.this);
        method.forceRTLIfSupported();

        String string = getIntent().getStringExtra("path");

        MaterialToolbar toolbar = findViewById(R.id.toolbar_view_image);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        // making notification bar transparent
        method.changeStatusBarColor();

        LinearLayout linearLayout = findViewById(R.id.linearLayout_view_image);
        BannerAds.showBannerAds(ViewImage.this, linearLayout);

        TouchImageView imageView = findViewById(R.id.imageView_view_image);

        Glide.with(ViewImage.this).load(string)
                .placeholder(R.drawable.placeholder_landscape)
                .into(imageView);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
