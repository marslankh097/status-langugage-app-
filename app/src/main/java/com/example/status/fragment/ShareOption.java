package com.example.status.fragment;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.status.BuildConfig;
import com.example.status.R;
import com.example.status.response.DataRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Method;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShareOption extends BottomSheetDialogFragment {

    private Method method;
    private Dialog dialog;
    private String reportType, message,title,url,statusId,statusType,statusVideoUrl,statusImage;
    private TextInputEditText editText;
    private RadioGroup radioGroup;
    private InputMethodManager imm;
    private ProgressDialog progressDialog;
    ConstraintLayout layoutFb,layoutTwi,layoutInst,layoutWp;
    ImageView imageViewFbMsg;

    public ShareOption() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bottom_sheet_option, container, false);

        method = new Method(getActivity());
        if (method.isRtl()) {
            view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        Bundle bundle = getArguments();
        statusId = bundle.getString("id");
        url = bundle.getString("url");
        statusType = bundle.getString("status_type");
        title=bundle.getString("status_title");
        statusVideoUrl=bundle.getString("status_v_url");
        statusImage=bundle.getString("status_v_image");

        progressDialog = new ProgressDialog(getActivity());

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        ConstraintLayout conShare = view.findViewById(R.id.con_share_bottomSheet);
        ConstraintLayout conReport = view.findViewById(R.id.con_report_bottomSheet);
        ConstraintLayout conCopy = view.findViewById(R.id.con_copy_bottomSheet);
        layoutFb= view.findViewById(R.id.con_share_bottomSheet_fb);
        layoutTwi= view.findViewById(R.id.con_share_bottomSheet_twi);
        layoutInst= view.findViewById(R.id.con_share_bottomSheet_insta);
        layoutWp= view.findViewById(R.id.con_share_bottomSheet_wp);
        imageViewFbMsg=view.findViewById(R.id.imageView_share_bottomSheet_fb);

        if (statusType.equals("quote")) {
            imageViewFbMsg.setImageDrawable(getResources().getDrawable(R.drawable.messanger_ic));
        } else {
            imageViewFbMsg.setImageDrawable(getResources().getDrawable(R.drawable.facebook));
        }

        assert statusType != null;
        if (!statusType.equals("quote")) {
            conCopy.setVisibility(View.GONE);
        } else {
            conCopy.setVisibility(View.VISIBLE);
        }

        layoutInst.setOnClickListener(v -> shareType("instagram"));

        layoutTwi.setOnClickListener(v -> shareType("twitter"));

        layoutWp.setOnClickListener(v -> shareType("whatsapp"));

        layoutFb.setOnClickListener(v -> shareType("facebook"));

        conShare.setOnClickListener(v -> {
            if (statusType.equals("quote")) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, url);
                startActivity(intent);
            } else {
                if (method.isNetworkAvailable()) {
                    new ShareVideo().execute(url, statusId, statusType);
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
            }
        });

        conCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", url);
            assert clipboard != null;
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), getResources().getString(R.string.copy_quote), Toast.LENGTH_SHORT).show();
        });

        conReport.setOnClickListener(v -> {
            if (method.isLogin()) {

                dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.bottom_sheet_report);
                if (method.isRtl()) {
                    dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                }
                dialog.getWindow().setLayout(ViewPager.LayoutParams.FILL_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);

                radioGroup = dialog.findViewById(R.id.radioGroup_report_bottomSheet);
                editText = dialog.findViewById(R.id.editText_report_bottomSheet);
                MaterialButton button = dialog.findViewById(R.id.button_send_report_bottomSheet);

                radioGroup.clearCheck();

                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    MaterialRadioButton rb = group.findViewById(checkedId);
                    if (null != rb && checkedId > -1) {
                        reportType = rb.getText().toString();
                    }
                });

                button.setOnClickListener(vButton -> report(statusId, statusType));

                dialog.show();
            } else {
                method.alertBox(getResources().getString(R.string.you_have_not_login));
            }
        });

        return view;
    }

    @SuppressLint("StaticFieldLeak")
    private class ShareVideo extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String statusType;
        private String iconsStoragePath;
        private File sdIconStorageDir;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_dialog), (dialog, which) -> {
                if (sdIconStorageDir != null) {
                    sdIconStorageDir.delete();
                }
                dialog.dismiss();
                cancel(true);
            });
            progressDialog.show();
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {

            int count;
            try {
                URL url = new URL(params[0]);
                String id = params[1];
                statusType = params[2];
                iconsStoragePath = getActivity().getExternalCacheDir().getAbsolutePath();
                String filePath;
                if (statusType.equals("image")) {
                    filePath = "file" + id + ".jpg";
                } else if (statusType.equals("gif")) {
                    filePath = "file" + id + ".gif";
                } else {
                    filePath = "file" + id + ".mp4";
                }

                sdIconStorageDir = new File(iconsStoragePath, filePath);

                //create storage directories, if they don't exist
                if (sdIconStorageDir.exists()) {
                    Log.d("File_name", "File_name");
                } else {
                    URLConnection connection = url.openConnection();
                    connection.setRequestProperty("Accept-Encoding", "identity");
                    connection.connect();
                    int lenghtOfFile = connection.getContentLength();// getting file length
                    InputStream input = new BufferedInputStream(url.openStream(), 8192); // input stream to read file - with 8k buffer
                    OutputStream output = new FileOutputStream(sdIconStorageDir);    // Output stream to write file
                    byte data[] = new byte[1024];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        // publishing the progress....
                        progressDialog.setProgress((int) (total * 100 / lenghtOfFile));
                        output.write(data, 0, count);
                    }
                    output.flush(); // flushing output
                    output.close();// closing streams
                    input.close();
                }

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            progressDialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_SEND);// Create the new Intent using the 'Send' action.
            if (statusType.equals("video")) {
                intent.setType("video/mp4");
            } else if (statusType.equals("gif")) {
                intent.setType("image/gif");
            } else {
                intent.setType("image/*");
            }
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", sdIconStorageDir);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri); // Broadcast the Intent.
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_to)));

        }

    }

    //---------------report-------------//

    private void report(String postId, String statusType) {

        editText.setError(null);

        message = editText.getText().toString();
        editText.clearFocus();
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

        if (message == null || message.equals("") || message.isEmpty()) {
            editText.requestFocus();
            editText.setError(getResources().getString(R.string.please_enter_message));
        } else if (reportType == null || reportType.equals("") || reportType.isEmpty()) {
            method.alertBox(getResources().getString(R.string.please_select_option));
        } else {
            String id = method.userId();
            submit(id, postId, reportType, statusType, message);
        }

    }

    private void submit(String userId, String postId, String reportType, String statusType, String reportMessage) {

        if (getActivity() != null) {

            progressDialog.show();
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("post_id", postId);
            jsObj.addProperty("type", statusType);
            jsObj.addProperty("report_type", reportType);
            jsObj.addProperty("report_text", reportMessage);
            jsObj.addProperty("comment_id", "");
            jsObj.addProperty("method_name", "status_report");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataRP> call = apiService.submitReview(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<DataRP>() {
                @Override
                public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                    if (getActivity() != null) {

                        try {
                            DataRP dataRP = response.body();
                            assert dataRP != null;

                            if (dataRP.getStatus().equals("1")) {

                                if (dataRP.getSuccess().equals("1")) {
                                    editText.setText("");
                                    radioGroup.clearCheck();
                                    dialog.dismiss();
                                    dismiss();
                                    Toast.makeText(getActivity(), dataRP.getMsg(), Toast.LENGTH_SHORT).show();
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
    }

    //---------------report-------------//

    private void shareType(String shareType) {

        if (method.isNetworkAvailable()) {
            switch (shareType) {
                case "whatsapp":
                    if (method.isAppInstalledWhatsapp()) {
                        if (statusType.equals("quote")) {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.setPackage("com.whatsapp");
                            intent.putExtra(Intent.EXTRA_TEXT, title);
                            startActivity(intent);
                        } else if (statusType.equals("image") || statusType.equals("gif")) {
                            new ShareImage().execute(statusType, "whatsapp");
                        } else {
                            new ShareVideoSocial().execute("whatsapp");
                        }
                    } else {
                        method.alertBox(getResources().getString(R.string.please_install_whatsapp));
                    }
                    break;
                case "facebook":
                    if (statusType.equals("quote")) {
                        if (method.isAppInstalledFbMessenger()) {
                            Intent shareFb = new Intent(Intent.ACTION_SEND);// Create the new Intent using the 'Send' action.
                            shareFb.setType("text/plain"); // Set the MIME type
                            shareFb.setPackage("com.facebook.orca");
                            shareFb.putExtra(Intent.EXTRA_TEXT, title); // Broadcast the Intent.
                            startActivity(shareFb);
                        } else {
                            method.alertBox(getResources().getString(R.string.please_install_fb_messenger));
                        }
                    } else {
                        if (method.isAppInstalledFacebook()) {
                            if (statusType.equals("image") || statusType.equals("gif")) {
                                new ShareImage().execute(statusType, "facebook");
                            } else {
                                new ShareVideoSocial().execute("facebook");
                            }

                        } else {
                            method.alertBox(getResources().getString(R.string.please_install_facebook));
                        }
                    }
                    break;
                case "twitter":
                    if (method.isAppInstalledTwitter()) {
                        if (statusType.equals("quote")) {
                            Intent share_tw = new Intent(Intent.ACTION_SEND);
                            share_tw.setType("text/plain");
                            share_tw.setPackage("com.twitter.android");
                            share_tw.putExtra(Intent.EXTRA_TEXT, title); // Broadcast the Intent.
                            startActivity(Intent.createChooser(share_tw, "Share to"));
                        } else if (statusType.equals("image") || statusType.equals("gif")) {
                            new ShareImage().execute(statusType, "twitter");
                        } else {
                            new ShareVideoSocial().execute("twitter");
                        }
                    } else {
                        method.alertBox(getResources().getString(R.string.please_install_twitter));
                    }
                    break;
                case "instagram":
                    if (method.isAppInstalledInstagram()) {
                        if (statusType.equals("quote")) {
                            try {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("text/plain");
                                intent.setPackage("com.instagram.android");
                                intent.putExtra(Intent.EXTRA_TEXT, title);
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        } else if (statusType.equals("image") || statusType.equals("gif")) {
                            new ShareImage().execute(statusType, "instagram");
                        } else {
                            new ShareVideoSocial().execute("instagram");
                        }
                    } else {
                        method.alertBox(getResources().getString(R.string.please_install_instagram));
                    }
                    break;
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    @SuppressLint("StaticFieldLeak")
    public class ShareVideoSocial extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        String iconsStoragePath;
        private File sdIconStorageDir;
        private String type;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_dialog), (dialog, which) -> {
                if (sdIconStorageDir != null) {
                    sdIconStorageDir.delete();
                }
                dialog.dismiss();
                cancel(true);
            });
            progressDialog.show();
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {

            int count;
            try {
                URL url = new URL(statusVideoUrl);
                String id = statusId;
                type = params[0];
                iconsStoragePath = requireActivity().getExternalCacheDir().getAbsolutePath();
                String filePath = "file" + id + ".mp4";

                sdIconStorageDir = new File(iconsStoragePath, filePath);

                //create storage directories, if they don't exist
                if (sdIconStorageDir.exists()) {
                    Log.d("File_name", "File_name");
                } else {
                    URLConnection conection = url.openConnection();
                    conection.setRequestProperty("Accept-Encoding", "identity");
                    conection.connect();
                    int lenghtOfFile = conection.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);
                    OutputStream output = new FileOutputStream(sdIconStorageDir);
                    byte data[] = new byte[1024];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        progressDialog.setProgress((int) (total * 100 / lenghtOfFile));
                        output.write(data, 0, count);
                    }
                    output.flush(); // flushing output
                    output.close();// closing streams
                    input.close();
                }

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            progressDialog.dismiss();

            switch (type) {
                case "whatsapp":
                    videoWhatsApp(sdIconStorageDir);
                    break;
                case "facebook":
                    videoFb(sdIconStorageDir);
                    break;
                case "instagram":
                    videoInstagram(sdIconStorageDir);
                    break;
                case "twitter":
                    videoTwitter(sdIconStorageDir);
                    break;
                default:
                    break;
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ShareImage extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private File sdIconStorageDir;
        private String type, statusType;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.cancel_dialog), (dialog, which) -> {
                if (sdIconStorageDir != null) {
                    sdIconStorageDir.delete();
                }
                dialog.dismiss();
                cancel(true);
            });
            progressDialog.show();
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {

            int count;
            try {
                URL url = new URL(statusImage);
                String id = statusId;
                statusType = params[0];
                type = params[1];
                String iconsStoragePath = getActivity().getExternalCacheDir().getAbsolutePath();

                String filePath;

                if (statusType.equals("image")) {
                    filePath = "file" + id + ".jpg";
                } else {
                    filePath = "file" + id + ".gif";
                }

                sdIconStorageDir = new File(iconsStoragePath, filePath);

                //create storage directories, if they don't exist
                if (sdIconStorageDir.exists()) {
                    Log.d("File_name", "File_name");
                } else {
                    URLConnection connection = url.openConnection();
                    connection.setRequestProperty("Accept-Encoding", "identity");
                    connection.connect();
                    int lengthOfFile = connection.getContentLength();// getting file length
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);    // input stream to read file - with 8k buffer
                    OutputStream output = new FileOutputStream(sdIconStorageDir);// Output stream to write file
                    byte data[] = new byte[1024];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        progressDialog.setProgress((int) (total * 100 / lengthOfFile));
                        output.write(data, 0, count);
                    }
                    output.flush(); // flushing output
                    output.close();// closing streams
                    input.close();
                }

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            progressDialog.dismiss();

            switch (type) {
                case "whatsapp":
                    imageWhatsApp(sdIconStorageDir);
                    break;
                case "facebook":
                    imageFb(sdIconStorageDir);
                    break;
                case "instagram":
                    imageInstagram(sdIconStorageDir);
                    break;
                case "twitter":
                    imageTwitter(sdIconStorageDir);
                    break;
                default:
                    break;
            }
        }

    }
    private void videoWhatsApp(File path) {

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.setPackage("com.whatsapp");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", path);
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=" + getActivity().getApplication().getPackageName());
        startActivity(intent);

    }

    private void imageWhatsApp(File path) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (statusType.equals("gif")) {
            intent.setType("image/gif");
        } else {
            intent.setType("image/*");
        }
        intent.setPackage("com.whatsapp");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", path);
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        intent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=" + getActivity().getApplication().getPackageName());
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_to)));
    }

    private void videoFb(File path) {
        Intent shareFb = new Intent(Intent.ACTION_SEND);
        shareFb.setType("video/*");
        shareFb.setPackage("com.facebook.katana");
        Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", path);
        shareFb.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(shareFb, getResources().getString(R.string.share_to)));
    }

    private void imageFb(File path) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (statusType.equals("gif")) {
            intent.setType("image/gif");
        } else {
            intent.setType("image/*");
        }
        intent.setPackage("com.facebook.katana");
        Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", path);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_to)));
    }

    private void videoInstagram(File file) {

        try {
            Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
            Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
            intent.setDataAndType(contentUri, "video/*");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra("content_url", "");

            // Instantiate activity and verify it will resolve implicit intent
            Activity activity = getActivity();
            if (activity.getPackageManager().resolveActivity(intent, 0) != null) {
                activity.startActivityForResult(intent, 0);
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    private void imageInstagram(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            if (statusType.equals("gif")) {
                intent.setType("image/gif");
            } else {
                intent.setType("image/*");
            }
            intent.setPackage("com.instagram.android");
            Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_to)));
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void imageTwitter(File path) {
        Intent intent = new Intent(Intent.ACTION_SEND);// Create the new Intent using the 'Send' action.
        intent.setType("image/*");   // Set the MIME type
        intent.setPackage("com.twitter.android");
        Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", path);
        intent.putExtra(Intent.EXTRA_STREAM, contentUri); // Broadcast the Intent.
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_to)));
    }

    private void videoTwitter(File path) {
        Intent shareTw = new Intent(Intent.ACTION_SEND);// Create the new Intent using the 'Send' action.
        shareTw.setType("video/*");   // Set the MIME type
        shareTw.setPackage("com.twitter.android");
        Uri contentUri = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileprovider", path);
        shareTw.putExtra(Intent.EXTRA_STREAM, contentUri); // Broadcast the Intent.
        startActivity(Intent.createChooser(shareTw, getResources().getString(R.string.share_to)));
    }
}
