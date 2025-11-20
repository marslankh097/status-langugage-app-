package com.example.status.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.interfaces.OnClick;
import com.example.status.item.SubCategoryList;
import com.example.status.response.DataRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Method;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyStatusAdapter extends RecyclerView.Adapter {

    Activity activity;
    Method method;
    int columnWidth;
    String userId, type;
    List<SubCategoryList> myVideoLists;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private final int VIEW_TYPE_QUOTES = 2;

    public MyStatusAdapter(Activity activity, List<SubCategoryList> myVideoLists, String userId, String type, OnClick onClick) {
        this.activity = activity;
        this.userId = userId;
        this.type = type;
        this.myVideoLists = myVideoLists;
        method = new Method(activity, onClick);
        columnWidth = (method.getScreenWidth());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.my_status_adapter, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_QUOTES) {
            View v = LayoutInflater.from(activity).inflate(R.layout.my_quotes_adapter, parent, false);
            return new Quotes(v);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View v = LayoutInflater.from(activity).inflate(R.layout.layout_loading_item, parent, false);
            return new ProgressViewHolder(v);
        }
        return null;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if (holder instanceof ViewHolder) {

            final ViewHolder viewHolder = (ViewHolder) holder;

            String status_type = myVideoLists.get(position).getStatus_type();
            if (status_type.equals("video")) {
                viewHolder.imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.video_ic));
            } else if (status_type.equals("image")) {
                viewHolder.imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.img_ic));
            } else {
                viewHolder.imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.gif_ic));
            }

            if (myVideoLists.get(position).getAlready_like().equals("true")) {
                viewHolder.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_hov));
            } else {
                if (method.isDarkMode()) {
                    viewHolder.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_white));
                } else {
                    viewHolder.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_ic));
                }
            }

            if (userId.equals(method.userId())) {
                viewHolder.imageViewDelete.setVisibility(View.VISIBLE);
                viewHolder.cardViewReview.setVisibility(View.VISIBLE);
                if (myVideoLists.get(position).getIs_reviewed().equals("true")) {
                    viewHolder.textViewReview.setText(activity.getResources().getString(R.string.approved));
                } else {
                    viewHolder.textViewReview.setText(activity.getResources().getString(R.string.on_review));
                }
            } else {
                viewHolder.imageViewDelete.setVisibility(View.GONE);
                viewHolder.cardViewReview.setVisibility(View.GONE);
            }

            String typeLayout = myVideoLists.get(position).getStatus_layout();
            if (typeLayout.equals("Portrait")) {
                viewHolder.viewThumb.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(columnWidth / 2 - 60, columnWidth / 2);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                viewHolder.imageView.setLayoutParams(layoutParams);
            } else {
                viewHolder.viewThumb.setVisibility(View.GONE);
                viewHolder.imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
            }

            if (status_type.equals("gif")) {
                Glide.with(activity)
                        .asBitmap()
                        .load(myVideoLists.get(position).getStatus_thumbnail_s())
                        .placeholder(R.drawable.placeholder_landscape)
                        .into(viewHolder.imageView);
            } else {
                Glide.with(activity).load(myVideoLists.get(position).getStatus_thumbnail_s())
                        .placeholder(R.drawable.placeholder_landscape)
                        .into(viewHolder.imageView);
            }

            viewHolder.textViewTitle.setText(myVideoLists.get(position).getStatus_title());
            viewHolder.textViewSubTitle.setText(myVideoLists.get(position).getCategory_name());
            viewHolder.textViewLike.setText(method.format(Double.parseDouble(myVideoLists.get(position).getTotal_likes())));
            viewHolder.textViewView.setText(method.format(Double.parseDouble(myVideoLists.get(position).getTotal_viewer())));

            viewHolder.rel_myStatus_adapter.setOnClickListener(v -> method.onClickData(position, myVideoLists.get(position).getStatus_title(), type, myVideoLists.get(position).getStatus_type(), myVideoLists.get(position).getId(), ""));

            viewHolder.imageViewDelete.setOnClickListener(v -> delete(position));

        } else if (holder.getItemViewType() == VIEW_TYPE_QUOTES) {

            final Quotes quotes = (Quotes) holder;

            if (myVideoLists.get(position).getAlready_like().equals("true")) {
                quotes.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_hov));
            } else {
                if (method.isDarkMode()) {
                    quotes.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_white));
                } else {
                    quotes.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_ic));
                }
            }

            if (userId.equals(method.userId())) {
                quotes.imageViewDelete.setVisibility(View.VISIBLE);
                quotes.cardViewReview.setVisibility(View.VISIBLE);
                if (myVideoLists.get(position).getIs_reviewed().equals("true")) {
                    quotes.textViewReview.setText(activity.getResources().getString(R.string.approved));
                } else {
                    quotes.textViewReview.setText(activity.getResources().getString(R.string.on_review));
                }
            } else {
                quotes.imageViewDelete.setVisibility(View.GONE);
                quotes.cardViewReview.setVisibility(View.GONE);
            }

            quotes.con.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, columnWidth / 2));

            Typeface typeface = Typeface.createFromAsset(activity.getAssets(), "text_font/" + myVideoLists.get(position).getQuote_font());
            quotes.textView.setTypeface(typeface);

            quotes.textView.setText(myVideoLists.get(position).getStatus_title());
            quotes.textView.post(() -> {
                ViewGroup.LayoutParams params = quotes.textView.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                final int widthSpec = View.MeasureSpec.makeMeasureSpec(quotes.textView.getWidth(), View.MeasureSpec.UNSPECIFIED);
                final int heightSpec = View.MeasureSpec.makeMeasureSpec(quotes.textView.getHeight(), View.MeasureSpec.UNSPECIFIED);
                quotes.textView.measure(widthSpec, heightSpec);
                quotes.textView.setMaxLines(heightSpec / quotes.textView.getLineHeight());
                quotes.textView.setEllipsize(TextUtils.TruncateAt.END);
            });

            quotes.textViewCategory.setText(myVideoLists.get(position).getCategory_name());

            quotes.con.setBackgroundColor(Color.parseColor(myVideoLists.get(position).getQuote_bg()));

            quotes.textViewView.setText(method.format(Double.parseDouble(myVideoLists.get(position).getTotal_viewer())));
            quotes.textViewLike.setText(method.format(Double.parseDouble(myVideoLists.get(position).getTotal_likes())));

            quotes.cardView.setOnClickListener(v -> method.onClickData(position, myVideoLists.get(position).getStatus_title(), type, myVideoLists.get(position).getStatus_type(), myVideoLists.get(position).getId(), ""));

            quotes.imageViewDelete.setOnClickListener(v -> delete(position));

        }

    }

    @Override
    public int getItemCount() {
        if (myVideoLists.size() != 0) {
            return myVideoLists.size() + 1;
        } else {
            return myVideoLists.size();
        }
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemViewType(int position) {

        if (position != myVideoLists.size()) {
            if (myVideoLists.get(position).getStatus_type().equals("quote")) {
                return VIEW_TYPE_QUOTES;
            } else {
                return VIEW_TYPE_ITEM;
            }
        } else {
            return VIEW_TYPE_LOADING;
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View viewThumb;
        MaterialCardView cardViewReview;
        ImageView imageView, imageViewType, imageViewLike, imageViewDelete;
        RelativeLayout rel_myStatus_adapter;
        MaterialTextView textViewTitle, textViewLike, textViewSubTitle, textViewView, textViewReview;

        public ViewHolder(View itemView) {
            super(itemView);

            viewThumb = itemView.findViewById(R.id.view_myStatus_adapter);
            imageView = itemView.findViewById(R.id.imageView_myStatus_adapter);
            imageViewType = itemView.findViewById(R.id.imageView_type_myStatus_adapter);
            imageViewLike = itemView.findViewById(R.id.imageView_like_myStatus_adapter);
            imageViewDelete = itemView.findViewById(R.id.imageView_delete_myStatus_adapter);
            textViewTitle = itemView.findViewById(R.id.textView_title_myStatus_adapter);
            textViewSubTitle = itemView.findViewById(R.id.textView_cat_myStatus_adapter);
            textViewView = itemView.findViewById(R.id.textView_view_myStatus_adapter);
            textViewLike = itemView.findViewById(R.id.textView_like_myStatus_adapter);
            textViewReview = itemView.findViewById(R.id.textView_review_myStatus_adapter);
            cardViewReview = itemView.findViewById(R.id.cardView_review_myStatus_adapter);
            rel_myStatus_adapter = itemView.findViewById(R.id.rel_myStatus_adapter);

        }
    }

    public static class Quotes extends RecyclerView.ViewHolder {

        ConstraintLayout con;
        MaterialCardView cardView, cardViewReview;
        ImageView imageViewDelete, imageViewLike;
        MaterialTextView textView, textViewCategory, textViewView, textViewLike, textViewReview;

        public Quotes(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.textView_my_quotes_adapter);
            textViewCategory = itemView.findViewById(R.id.textView_cat_my_quotes_adapter);
            con = itemView.findViewById(R.id.con_text_my_quotes_adapter);
            textViewView = itemView.findViewById(R.id.textView_view_my_quotes_adapter);
            textViewLike = itemView.findViewById(R.id.textView_like_my_quotes_adapter);
            imageViewLike = itemView.findViewById(R.id.imageView_like_my_quotes_adapter);
            imageViewDelete = itemView.findViewById(R.id.imageView_delete_my_quotes_adapter);
            textViewReview = itemView.findViewById(R.id.textView_review_my_quotes_adapter);
            cardView = itemView.findViewById(R.id.cardView_my_quotes_adapter);
            cardViewReview = itemView.findViewById(R.id.cardView_review_my_quotes_adapter);

        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public static ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    private void delete(int position) {
        if (method.isNetworkAvailable()) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
            builder.setMessage(activity.getResources().getString(R.string.delete_msg));
            builder.setCancelable(false);
            builder.setPositiveButton(activity.getResources().getString(R.string.delete),
                    (arg0, arg1) -> deleteStatus(position, userId));
            builder.setNegativeButton(activity.getResources().getString(R.string.cancel_dialog),
                    (dialog, which) -> dialog.dismiss());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {
            method.alertBox(activity.getResources().getString(R.string.internet_connection));
        }
    }

    private void deleteStatus(final int position, String userId) {

        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getResources().getString(R.string.delete));
        progressDialog.setCancelable(false);
        progressDialog.show();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("post_id", myVideoLists.get(position).getId());
        jsObj.addProperty("type", myVideoLists.get(position).getStatus_type());
        jsObj.addProperty("method_name", "user_status_delete");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.deleteStatus(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {

                    DataRP dataRP = response.body();

                    assert dataRP != null;
                    if (dataRP.getStatus().equals("1")) {

                        if (dataRP.getSuccess().equals("1")) {

                            myVideoLists.remove(position);
                            notifyDataSetChanged();

                            Toast.makeText(activity, dataRP.getMsg(), Toast.LENGTH_SHORT).show();

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
                    method.alertBox(activity.getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<DataRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("onFailure_data", t.toString());
                progressDialog.dismiss();
                method.alertBox(activity.getResources().getString(R.string.failed_try_again));
            }
        });
    }

}
