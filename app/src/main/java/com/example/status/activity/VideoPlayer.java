package com.example.status.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.status.R;
import com.example.status.util.Method;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class VideoPlayer extends AppCompatActivity {

    private Method method;
    private SimpleExoPlayer player;
    private ImageView imageView;
    private PlayerView playerView;
    private String videoUrl, videoType;
    private ProgressBar progressBar;
    private boolean isFullScreen = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        method = new Method(VideoPlayer.this);
        method.forceRTLIfSupported();
        method.changeStatusBarColor();

        Intent in = getIntent();
        videoUrl = in.getStringExtra("Video_url");
        videoType = in.getStringExtra("video_type");

        imageView = findViewById(R.id.imageView_full_video_play);
        playerView = findViewById(R.id.player_view_video_play);
        progressBar = findViewById(R.id.progressBar_video_play);
        progressBar.setVisibility(View.VISIBLE);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(VideoPlayer.this);
        player = new SimpleExoPlayer.Builder(VideoPlayer.this)
                .setTrackSelector(trackSelector)
                .build();
        playerView.setPlayer(player);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(VideoPlayer.this,
                Util.getUserAgent(VideoPlayer.this, getResources().getString(R.string.app_name)));
        // This is the MediaSource representing the media to be played.
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem);
        player.setMediaSource(mediaSource);
        // Prepare the player with the source.
        player.prepare();
        player.setPlayWhenReady(true);
        player.addListener(new Player.EventListener() {
            @Override
            public void onIsPlayingChanged(boolean playWhenReady) {
                if (playWhenReady) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        if (!videoType.equals("Landscape")) {
            imageView.setVisibility(View.GONE);
        }

        imageView.setOnClickListener(v -> {
            if (isFullScreen) {
                isFullScreen = false;
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.full_screen));
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                getWindow().clearFlags(1024);
            } else {
                isFullScreen = true;
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.exitfull_screen));
                getWindow().setFlags(1024, 1024);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
        }
        super.onDestroy();
    }

}
