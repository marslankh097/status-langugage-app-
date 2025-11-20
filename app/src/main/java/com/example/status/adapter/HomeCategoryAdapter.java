package com.example.status.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.interfaces.OnClick;
import com.example.status.item.CategoryList;
import com.example.status.R;
import com.example.status.util.Method;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;


public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder> {

    Activity activity;
    Method method;
    String type;
    private int rowIndex = -1;
    List<CategoryList> categoryLists;

    public HomeCategoryAdapter(Activity activity, List<CategoryList> categoryLists, String type, OnClick onClick) {
        this.activity = activity;
        this.type = type;
        this.categoryLists = categoryLists;
        method = new Method(activity, onClick);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.home_category_adapter, parent, false);

        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if (categoryLists.get(position).getCategory_name().equals(activity.getResources().getString(R.string.view_all))) {
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{activity.getResources().getColor(R.color.textView_app_color),
                            activity.getResources().getColor(R.color.textView_app_color)});
            gd.setCornerRadius(90);
            holder.conImage.setBackground(gd);
            Glide.with(activity).load(activity.getResources().getDrawable(R.drawable.view_all_ic))
                    .placeholder(R.drawable.placeholder_landscape).into(holder.imageView);
        } else {

            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{Color.parseColor(categoryLists.get(position).getStart_color()), Color.parseColor(categoryLists.get(position).getEnd_color())});
            gd.setCornerRadius(90);
            holder.conImage.setBackground(gd);

            Glide.with(activity).load(categoryLists.get(position).getCategory_image())
                    .placeholder(R.drawable.placeholder_landscape).into(holder.imageView);
        }

        holder.textView.setText(categoryLists.get(position).getCategory_name());

        holder.con.setOnClickListener(v -> {
            rowIndex = position;
            notifyDataSetChanged();
            method.onClickData(position, categoryLists.get(position).getCategory_name(), type, "", categoryLists.get(position).getCid(), "");
        });


        if (rowIndex == position) {
            holder.textView.setTextColor(activity.getResources().getColor(R.color.textView_homeCat_select_adapter));
        } else {
            holder.textView.setTextColor(activity.getResources().getColor(R.color.textView_homeCat_adapter));
        }

    }

    @Override
    public int getItemCount() {
        return categoryLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        MaterialTextView textView;
        ConstraintLayout con, conImage;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_homeCat_adapter);
            textView = itemView.findViewById(R.id.textView_homeCat_adapter);
            conImage = itemView.findViewById(R.id.con_image_homeCat_adapter);
            con = itemView.findViewById(R.id.con_homeCat_adapter);

        }
    }
}
