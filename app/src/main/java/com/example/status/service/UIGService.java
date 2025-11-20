package com.example.status.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.status.R;
import com.example.status.util.API;
import com.example.status.util.Constant;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UIGService extends Service {

    private RemoteViews rv;
    private OkHttpClient client;
    private Thread thread;
    private int NOTIFICATION_ID = 115;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private static final String CANCEL_TAG = "c_uig";
    private String NOTIFICATION_CHANNEL_ID = "upload_ig";
    public static final String ACTION_START = "com.uig.action.START";
    public static final String ACTION_STOP = "com.uig.action.STOP";
    private String statusType, imageFile, userId, catId, langIds, imageTags, imageLayout, imageTitle;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_onesignal_large_icon_default)
                .setTicker(getResources().getString(R.string.ready_to_upload))
                .setWhen(System.currentTimeMillis())
                .setOnlyAlertOnce(true);

        rv = new RemoteViews(getPackageName(), R.layout.my_custom_notification);
        rv.setTextViewText(R.id.nf_title, getString(R.string.app_name));
        rv.setProgressBar(R.id.progress, 100, 0, false);
        rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.upload_image) + " " + "(0%)");

        Intent closeIntent = new Intent(this, UIGService.class);
        closeIntent.setAction(ACTION_STOP);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, PendingIntent.FLAG_IMMUTABLE);
        rv.setOnClickPendingIntent(R.id.relativeLayout_nf, pcloseIntent);

        builder.setCustomContentView(rv);
        NotificationChannel mChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getResources().getString(R.string.app_name);// The user-visible name of the channel.
            mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, builder.build());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent.getAction() != null && intent.getAction().equals(ACTION_START)) {

            userId = intent.getStringExtra("user_id");
            catId = intent.getStringExtra("cat_id");
            langIds = intent.getStringExtra("lang_ids");
            imageTags = intent.getStringExtra("image_tags");
            imageTitle = intent.getStringExtra("image_title");
            imageLayout = intent.getStringExtra("image_layout");
            statusType = intent.getStringExtra("status_type");
            imageFile = intent.getStringExtra("image_file");

            init();
        }
        if (intent.getAction() != null && intent.getAction().equals(ACTION_STOP)) {
            try {
                if (client != null) {
                    for (Call call : client.dispatcher().queuedCalls()) {
                        if (call.request().tag().equals(CANCEL_TAG))
                            call.cancel();
                    }
                    for (Call call : client.dispatcher().runningCalls()) {
                        if (call.request().tag().equals(CANCEL_TAG))
                            call.cancel();
                    }
                }
                if (thread != null) {
                    thread.interrupt();
                    thread = null;
                }
                Events.UploadFinish uploadFinish = new Events.UploadFinish("");
                GlobalBus.getBus().post(uploadFinish);
                stopForeground(false);
                stopSelf();
                Method.isUpload = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(false);
        stopSelf();
    }

    public void init() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                client = new OkHttpClient();
                Request.Builder builder = new Request.Builder()
                        .url(Constant.url)
                        .tag(CANCEL_TAG);

                File imageFile = new File(UIGService.this.imageFile);

                MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
                bodyBuilder.setType(MultipartBody.FORM);
                JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getApplicationContext()));
                jsObj.addProperty("user_id", userId);
                jsObj.addProperty("cat_id", catId);
                jsObj.addProperty("lang_ids", langIds);
                jsObj.addProperty("image_tags", imageTags);
                jsObj.addProperty("image_title", imageTitle);
                jsObj.addProperty("image_layout", imageLayout);
                jsObj.addProperty("status_type", statusType);
                jsObj.addProperty("method_name", "upload_img_gif_status");
                bodyBuilder.addFormDataPart("data", API.toBase64(jsObj.toString()));
                bodyBuilder.addFormDataPart("image_file", imageFile.getName(), RequestBody.create(null, imageFile));
                MultipartBody build = bodyBuilder.build();

                RequestBody requestBody = ProgressHelper.withProgress(build, new ProgressUIListener() {

                    //if you don't need this method, don't override this method. It isn't an abstract method, just an empty method.
                    @Override
                    public void onUIProgressStart(long totalBytes) {
                        super.onUIProgressStart(totalBytes);
                        Log.e("TAG", "onUIProgressStart:" + totalBytes);
                    }

                    @Override
                    public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                        nfDataUpdate("1", (int) (100 * percent));
                    }

                    //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                    @Override
                    public void onUIProgressFinish() {
                        super.onUIProgressFinish();
                        nfDataUpdate("2",0);
                        Method.isUpload = true;
                        Events.UploadFinish uploadFinish = new Events.UploadFinish("");
                        GlobalBus.getBus().post(uploadFinish);
                    }
                });
                builder.post(requestBody);

                Call call = client.newCall(builder.build());

                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.e("TAG", "=============onFailure===============");
                        e.printStackTrace();
                        Method.isUpload = true;
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        Log.e("TAG", "=============onResponse===============");
                        Log.e("TAG", "request headers:" + response.request().headers());
                        Log.e("TAG", "response headers:" + response.headers());
                    }
                });
            }
        });
        thread.start();
    }

    private void nfDataUpdate(String message, int progress) {
        switch (message) {
            case "1":
                try {
                    rv.setProgressBar(R.id.progress, 100, progress, false);
                    rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.upload_image) + " " + "(" + progress + " %)");
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                } catch (Exception e) {
                    Log.d("progress_tag", e.toString());
                }
                break;
            case "2":
                stopForeground(false);
                stopSelf();
                break;
        }
    }

}
