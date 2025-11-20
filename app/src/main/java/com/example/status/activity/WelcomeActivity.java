package com.example.status.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.status.R;
import com.example.status.util.Method;
import com.google.android.material.textview.MaterialTextView;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class WelcomeActivity extends AppCompatActivity {

    private Method method;
    private int[] layouts;
    private ViewPager viewPager;
    private ImageView imageViewNext;
    private MyViewPagerAdapter myViewPagerAdapter;
    private MaterialTextView textViewSkip, textViewNext;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        method = new Method(WelcomeActivity.this);
        method.forceRTLIfSupported();

        // Checking for first time launch - before calling setContentView()
        if (!method.isWelcome()) {
            if (!method.isLanguage()) {
                startActivity(new Intent(WelcomeActivity.this, SplashScreen.class));
                finish();
            } else {
                launchHomeScreen();
            }
        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        // making notification bar transparent
        method.changeStatusBarColor();

        setContentView(R.layout.activity_welcome);

        viewPager = findViewById(R.id.view_pager);
        textViewSkip = findViewById(R.id.textView_skip);
        textViewNext = findViewById(R.id.textView_next);
        imageViewNext = findViewById(R.id.imageView_next);

        textViewNext.setVisibility(View.GONE);

        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{R.layout.welcome_slide_one, R.layout.welcome_slide_two, R.layout.welcome_slide_three};

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        textViewSkip.setOnClickListener(v -> launchHomeScreen());

        textViewNext.setOnClickListener(v -> {
            int current = getItem(+1);
            if (current < layouts.length) {
                viewPager.setCurrentItem(current);
            } else {
                launchHomeScreen();
            }
        });

        imageViewNext.setOnClickListener(v -> {
            int current = getItem(+1);
            if (current < layouts.length) {
                viewPager.setCurrentItem(current);
            } else {
                launchHomeScreen();
            }
        });

    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        method.setFirstWelcome(false);
        startActivity(new Intent(WelcomeActivity.this, Language.class)
                .putExtra("type", "welcome"));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                textViewSkip.setVisibility(View.GONE);
                imageViewNext.setVisibility(View.GONE);
                textViewNext.setVisibility(View.VISIBLE);
            } else {
                // still pages are left
                textViewSkip.setVisibility(View.VISIBLE);
                imageViewNext.setVisibility(View.VISIBLE);
                textViewNext.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            assert layoutInflater != null;
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}