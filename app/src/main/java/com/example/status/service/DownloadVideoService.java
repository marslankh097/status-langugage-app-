package com.example.status.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.daasuu.mp4compose.composer.Mp4Composer;
import com.daasuu.mp4compose.filter.GlWatermarkFilter;
import com.example.status.R;
import com.example.status.database.DatabaseHandler;
import com.example.status.util.Method;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class DownloadVideoService extends Service {

    private DatabaseHandler db;
    private RemoteViews rv;
    private OkHttpClient client;
    private Thread thread;
    private WaterMark waterMark;
    private int NOTIFICATION_ID = 107;
    private boolean isResize = false;
    private boolean isWaterMark = false;
    private Mp4Composer mp4Composer;
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private static final String CANCEL_TAG = "c_dv";
    private String NOTIFICATION_CHANNEL_ID = "download_video";
    public static final String ACTION_STOP = "com.dv.action.STOP";
    public static final String ACTION_START = "com.dv.action.START";
    private String videoId, downloadUrl, filePathLocal, filePath, filePathDelete, fileName, layoutType, statusType, watermarkImage, watermarkOnOff;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        db = new DatabaseHandler(getApplicationContext());
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.drawable.ic_onesignal_large_icon_default)
                .setTicker(getResources().getString(R.string.downloading))
                .setWhen(System.currentTimeMillis())
                .setOnlyAlertOnce(true);

        rv = new RemoteViews(getPackageName(), R.layout.my_custom_notification);
        rv.setTextViewText(R.id.nf_title, getString(R.string.app_name));
        rv.setProgressBar(R.id.progress, 100, 0, false);
        rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.downloading) + " " + "(0%)");

        Intent closeIntent = new Intent(this, DownloadVideoService.class);
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

            videoId = intent.getStringExtra("video_id");
            downloadUrl = intent.getStringExtra("downloadUrl");
            filePath = intent.getStringExtra("file_path");
            fileName = intent.getStringExtra("file_name");
            layoutType = intent.getStringExtra("layout_type");
            statusType = intent.getStringExtra("status_type");
            watermarkImage = intent.getStringExtra("watermark_image");
            watermarkOnOff = intent.getStringExtra("watermark_on_off");

            assert watermarkOnOff != null;
            if (watermarkOnOff.equals("true")) {
                filePathLocal = getExternalCacheDir().getAbsolutePath();
            } else {
                filePathLocal = filePath;
            }
            filePathDelete = filePathLocal;

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
                if (waterMark != null) {
                    waterMark.cancel(true);
                }
                if (mp4Composer != null) {
                    mp4Composer.cancel();
                }
                if (db != null) {
                    if (!db.checkIdStatusDownload(videoId, statusType)) {
                        db.deleteStatusDownload(videoId, statusType);
                    }
                }
                if (filePathDelete != null || fileName != null) {
                    File file = new File(filePathDelete, fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                }
                stopForeground(false);
                stopSelf();
                Method.isDownload = true;
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
                        .url(downloadUrl)
                        .addHeader("Accept-Encoding", "identity")
                        .get()
                        .tag(CANCEL_TAG);

                Call call = client.newCall(builder.build());

                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("TAG", "=============onFailure===============");
                        e.printStackTrace();
                        Log.d("error_downloading", e.toString());
                        Method.isDownload = true;
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        Log.e("TAG", "=============onResponse===============");
                        assert response.body() != null;
                        final ResponseBody responseBody = ProgressHelper.withProgress(response.body(), new ProgressUIListener() {

                            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressStart(long totalBytes) {
                                super.onUIProgressStart(totalBytes);
                                Log.e("TAG", "onUIProgressStart:" + totalBytes);
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.downloading), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                                Log.e("TAG", "=============start===============");
                                nfDataUpdate("1", (int) (100 * percent));
                            }

                            //if you don't need this method, don't override this method. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressFinish() {
                                super.onUIProgressFinish();
                                Log.e("TAG", "onUIProgressFinish:");

                                if (watermarkOnOff.equals("true")) {
                                    //call data watermark class add to watermark
                                    MediaPlayer mp = new MediaPlayer();
                                    try {
                                        mp.setDataSource(filePathLocal + "/" + fileName);
                                        mp.prepareAsync();
                                        mp.setOnVideoSizeChangedListener((mp1, width, height) -> {
                                            if (layoutType.equals("Portrait")) {
                                                if (height <= 700) {
                                                    isResize = true;
                                                }
                                            } else {
                                                if (height <= 400 || width <= 400) {
                                                    isResize = true;
                                                }
                                            }
                                            waterMark = new WaterMark();
                                            waterMark.execute();
                                        });
                                    } catch (Exception e) {
                                        waterMark = new WaterMark();
                                        waterMark.execute();
                                        e.printStackTrace();
                                    }
                                } else {
                                    nfDataUpdate("2", 0);
                                    Method.isDownload = true;
                                    showMedia(filePathLocal, fileName);
                                }

                            }
                        });

                        try {

                            BufferedSource source = responseBody.source();
                            File outFile = new File(filePathLocal + "/" + fileName);
                            BufferedSink sink = Okio.buffer(Okio.sink(outFile));
                            source.readAll(sink);
                            sink.flush();
                            source.close();

                        } catch (Exception e) {
                            Log.d("show_data", e.toString());
                        }

                    }
                });
            }
        });
        thread.start();
    }

    @SuppressLint("StaticFieldLeak")
    class WaterMark extends AsyncTask<String, String, String> {

        Bitmap image = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(watermarkImage);
                try {
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (Exception e) {
                    Log.d("error_data", e.toString());
                }
            } catch (IOException e) {
                image = BitmapFactory.decodeResource(getResources(), R.drawable.watermark);
            }
            if (isResize) {
                image = getResizedBitmap(image, 40, 40);
                isResize = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            filePathDelete = filePath;
            mp4Composer = null;
            mp4Composer = new Mp4Composer(filePathLocal + "/" + fileName, filePath + "/" + fileName)
                    .filter(new GlWatermarkFilter(image, GlWatermarkFilter.Position.RIGHT_BOTTOM))
                    .listener(new Mp4Composer.Listener() {
                        @Override
                        public void onProgress(double progress) {
                            isWaterMark = true;
                            double value = progress * 100;
                            int i = (int) value;
                            nfDataUpdate("1", i);
                            Log.d("progress_tag", String.valueOf(progress));
                        }

                        @Override
                        public void onCurrentWrittenVideoTime(long timeUs) {

                        }

                        @Override
                        public void onCompleted() {
                            isWaterMark = false;
                            new File(getExternalCacheDir().getAbsolutePath() + "/" + fileName).delete();//delete file to save in cash folder
                            nfDataUpdate("2", 0);
                            Method.isDownload = true;
                            showMedia(filePath, fileName);
                            Log.d("progress_tag", "onCompleted");
                        }

                        @Override
                        public void onCanceled() {
                            isWaterMark = false;
                            nfDataUpdate("2", 0);
                            Method.isDownload = true;
                            Log.d("progress_tag", "onCanceled");
                        }

                        @Override
                        public void onFailed(Exception exception) {
                            isWaterMark = false;
                            nfDataUpdate("2", 0);
                            Method.isDownload = true;
                            Log.d("progress_tag", "onFailed");
                        }
                    })
                    .start();
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public void showMedia(String filePath, String fileName) {
        try {
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{filePath + "/" + fileName},
                    null,
                    (path, uri) -> {

                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nfDataUpdate(String message, int progress) {
        switch (message) {
            case "1":
                try {
                    rv.setProgressBar(R.id.progress, 100, progress, false);
                    if (isWaterMark) {
                        rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.watermark) + " " + "(" + progress + " %)");
                    } else {
                        rv.setTextViewText(R.id.nf_percentage, getResources().getString(R.string.downloading) + " " + "(" + progress + " %)");
                    }
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
