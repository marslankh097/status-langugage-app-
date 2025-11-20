package com.example.status.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.interfaces.OnClick;
import com.example.status.item.UserFollowList;
import com.example.status.R;
import com.example.status.util.Method;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserFollowAdapter extends RecyclerView.Adapter {

    Method method;
    Activity activity;
    String type;
    List<UserFollowList> userFollowLists;

    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;

    public UserFollowAdapter(Activity activity, List<UserFollowList> userFollowLists, String type, OnClick onClick) {
        this.activity = activity;
        this.type = type;
        method = new Method(activity, onClick);
        this.userFollowLists = userFollowLists;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.user_follow_adapter, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View v = LayoutInflater.from(activity).inflate(R.layout.layout_loading_item, parent, false);
            return new ProgressViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ViewHolder viewHolder = (ViewHolder) holder;

            if (!userFollowLists.get(position).getUser_image().equals("")) {
                Glide.with(activity).load(userFollowLists.get(position).getUser_image())
                        .placeholder(R.drawable.user_profile).into(viewHolder.circleImageView);
            } else {
                // make sure Glide doesn't load anything into this view until told otherwise
                Glide.with(activity).clear(viewHolder.circleImageView);
            }

            if (userFollowLists.get(position).getIs_verified().equals("true")) {
                viewHolder.textViewUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verification, 0);
            } else {
                viewHolder.textViewUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            viewHolder.textViewUserName.setText(userFollowLists.get(position).getUser_name());

            viewHolder.constraintLayout.setOnClickListener(v -> method.onClickData(position, "", type, "", userFollowLists.get(position).getUser_id(), ""));

        }

    }

    @Override
    public int getItemCount() {
        return userFollowLists.size() + 1;
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    private boolean isHeader(int position) {
        return position == userFollowLists.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout constraintLayout;
        CircleImageView circleImageView;
        MaterialTextView textViewUserName;

        public ViewHolder(View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.con_userFollow_adapter);
            circleImageView = itemView.findViewById(R.id.imageView_userFollow_adapter);
            textViewUserName = itemView.findViewById(R.id.textView_userFollow_adapter);

        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public static ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }
}
