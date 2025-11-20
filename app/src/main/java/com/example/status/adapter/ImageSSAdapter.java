package com.example.status.adapter;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.interfaces.OnClick;
import com.example.status.util.Method;

import java.io.File;
import java.util.List;

public class ImageSSAdapter extends RecyclerView.Adapter<ImageSSAdapter.ViewHolder> {

    Method method;
    Activity activity;
    String type;
    int columnWidth;
    List<Uri> imageList;

    public ImageSSAdapter(Activity activity, List<Uri> imageList, OnClick onClick, String type) {
        this.activity = activity;
        this.imageList = imageList;
        this.type = type;
        method = new Method(activity, onClick);
        Resources r = activity.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, r.getDisplayMetrics());
        columnWidth = (int) ((method.getScreenWidth() - ((5 + 2) * padding)));
    }

    @NonNull
    @Override
    public ImageSSAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.image_ss_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSSAdapter.ViewHolder holder, final int position) {

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.imageView.getLayoutParams();
        params.width = columnWidth / 2;
        params.height = columnWidth / 2;
        holder.imageView.setLayoutParams(params);

        Glide.with(activity).load(imageList.get(position))
                .placeholder(R.drawable.placeholder_portable).into(holder.imageView);

        holder.imageView.setOnClickListener(v -> method.onClickData(position, "", type, "", "", ""));

    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_ss_adapter);

        }
    }
}
