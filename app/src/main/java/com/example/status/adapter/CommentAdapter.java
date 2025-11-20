package com.example.status.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.activity.Login;
import com.example.status.item.CommentList;
import com.example.status.response.UserCommentRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentAdapter extends RecyclerView.Adapter {

    Method method;
    Animation myAnim;
    Activity activity;
    List<CommentList> commentLists;

    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private Dialog dialog;
    InputMethodManager inputMethodManager;
    String reportType;

    public CommentAdapter(Activity activity, List<CommentList> commentLists) {
        this.activity = activity;
        this.commentLists = commentLists;
        method = new Method(activity);
        myAnim = AnimationUtils.loadAnimation(activity, R.anim.bounce);
        inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.comment_adapter, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View v = LayoutInflater.from(activity).inflate(R.layout.layout_loading_item, parent, false);
            return new ProgressViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ViewHolder viewHolder = (ViewHolder) holder;

            if (!commentLists.get(position).getUser_image().equals("")) {
                Glide.with(activity).load(commentLists.get(position).getUser_image())
                        .placeholder(R.drawable.profile)
                        .into(viewHolder.circleImageView);
            } else {
                // make sure Glide doesn't load anything into this view until told otherwise
                Glide.with(activity).clear(viewHolder.circleImageView);
            }

            if (method.isLogin()) {
                if (method.userId().equals(commentLists.get(position).getUser_id())) {
                    viewHolder.textViewDelete.setText(activity.getString(R.string.delete));
                } else {
                    viewHolder.textViewDelete.setText(activity.getString(R.string.report_tag));
                }
             } else {
                viewHolder.textViewDelete.setText(activity.getString(R.string.report_tag));
           }

            viewHolder.textViewName.setText(commentLists.get(position).getUser_name());
            viewHolder.textViewDate.setText(commentLists.get(position).getComment_date());
            viewHolder.textViewComment.setText(commentLists.get(position).getComment_text());

            viewHolder.textViewDelete.setOnClickListener(view -> {

                viewHolder.textViewDelete.startAnimation(myAnim);
                if (method.isLogin()) {
                    if (method.userId().equals(commentLists.get(position).getUser_id())) {
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
                        builder.setMessage(activity.getResources().getString(R.string.delete_comment));
                        builder.setCancelable(false);
                        builder.setPositiveButton(activity.getResources().getString(R.string.delete),
                                (arg0, arg1) -> delete(commentLists.get(position).getPost_id(), commentLists.get(position).getStatus_type(),
                                        commentLists.get(position).getComment_id(), position));
                        builder.setNegativeButton(activity.getResources().getString(R.string.cancel_dialog),
                                (dialog, which) -> dialog.dismiss());

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                    } else {
                        dialog = new Dialog(activity);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.bottom_sheet_comment_report);
                        if (method.isRtl()) {
                            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                        }
                        dialog.getWindow().setLayout(ViewPager.LayoutParams.FILL_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);

                        TextInputEditText editText = dialog.findViewById(R.id.editText_report_bottomSheet);
                        MaterialButton button = dialog.findViewById(R.id.button_send_report_bottomSheet);
                        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup_report_bottomSheet);
                        radioGroup.clearCheck();

                        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                            MaterialRadioButton rb = group.findViewById(checkedId);
                            if (null != rb && checkedId > -1) {
                                reportType = rb.getText().toString();
                            }
                        });
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (editText.getText().toString().isEmpty()) {
                                    Toast.makeText(activity, activity.getString(R.string.report_reason), Toast.LENGTH_SHORT).show();
                                } else if (reportType == null || reportType.equals("") || reportType.isEmpty()) {
                                    method.alertBox(activity.getResources().getString(R.string.please_select_option));
                                } else {
                                    editText.clearFocus();
                                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                                    reportComment(method.userId(), "", commentLists.get(position).getPost_id(), reportType, editText.getText().toString(), commentLists.get(position).getStatus_type(), commentLists.get(position).getComment_id(), position);
                                    dialog.dismiss();
                                }

                            }
                        });

                        dialog.show();
                    }
                } else {
                    //viewHolder.textViewDelete.setVisibility(View.GONE);
                    Method.loginBack = true;
                    activity.startActivity(new Intent(activity, Login.class));
                }
            });

        }

    }

    @Override
    public int getItemCount() {
        if (commentLists.size() != 0) {
            return commentLists.size() + 1;
        } else {
            return 0;
        }
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    private boolean isHeader(int position) {
        return position == commentLists.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circleImageView;
        MaterialTextView textViewName, textViewDate, textViewComment, textViewDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.imageView_comment_adapter);
            textViewName = itemView.findViewById(R.id.textView_userName_comment_adapter);
            textViewDate = itemView.findViewById(R.id.textView_date_comment_adapter);
            textViewComment = itemView.findViewById(R.id.textView_comment_adapter);
            textViewDelete = itemView.findViewById(R.id.textView_delete_adapter);

        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public static ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    public void delete(String postId, String statusType, String commentId, int position) {

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.show();
        progressDialog.setMessage(activity.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("post_id", postId);
        jsObj.addProperty("type", statusType);
        jsObj.addProperty("comment_id", commentId);
        jsObj.addProperty("method_name", "delete_comment");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<UserCommentRP> call = apiService.deleteComment(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<UserCommentRP>() {
            @Override
            public void onResponse(@NotNull Call<UserCommentRP> call, @NotNull Response<UserCommentRP> response) {

                try {
                    UserCommentRP userCommentRP = response.body();
                    assert userCommentRP != null;

                    if (userCommentRP.getStatus().equals("1")) {

                        if (userCommentRP.getSuccess().equals("1")) {

                            commentLists.remove(position);
                            notifyDataSetChanged();

                            Events.Comment comment;
                            if (userCommentRP.getComment_status().equals("1")) {
                                comment = new Events.Comment(userCommentRP.getComment_status(), commentId, userCommentRP.getComment_id(),
                                        userCommentRP.getUser_id(), userCommentRP.getUser_name(), userCommentRP.getUser_image(), userCommentRP.getPost_id(),
                                        userCommentRP.getStatus_type(), userCommentRP.getComment_text(), userCommentRP.getComment_date(), userCommentRP.getTotal_comment(), "delete");
                            } else {
                                comment = new Events.Comment(userCommentRP.getComment_status(), commentId, "",
                                        "", "", "", postId,
                                        "", "", "", userCommentRP.getTotal_comment(), "delete");
                            }
                            GlobalBus.getBus().post(comment);

                        } else {
                            method.alertBox(userCommentRP.getMsg());
                        }

                    } else {
                        method.alertBox(userCommentRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(activity.getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<UserCommentRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(activity.getResources().getString(R.string.failed_try_again));
            }
        });

    }

    public void reportComment(String userId, String email, String postId, String reportType, String reportText, String type, String commentId, int position) {

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.show();
        progressDialog.setMessage(activity.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("email", email);
        jsObj.addProperty("post_id", postId);
        jsObj.addProperty("report_type", reportType);
        jsObj.addProperty("report_text", reportText);
        jsObj.addProperty("type", type);
        jsObj.addProperty("comment_id", commentId);
        jsObj.addProperty("method_name", "status_report");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<UserCommentRP> call = apiService.deleteComment(API.toBase64(jsObj.toString()));
        Log.e("eeee", "" + API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<UserCommentRP>() {
            @Override
            public void onResponse(@NotNull Call<UserCommentRP> call, @NotNull Response<UserCommentRP> response) {

                try {
                    UserCommentRP userCommentRP = response.body();
                    assert userCommentRP != null;

                    if (userCommentRP.getStatus().equals("1")) {

                        if (userCommentRP.getSuccess().equals("1")) {
                            //
                            Toast.makeText(activity, userCommentRP.getMsg(), Toast.LENGTH_SHORT).show();
                        } else {
                            method.alertBox(userCommentRP.getMsg());
                        }

                    } else {
                        method.alertBox(userCommentRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(activity.getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<UserCommentRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(activity.getResources().getString(R.string.failed_try_again));
            }
        });
    }
}
