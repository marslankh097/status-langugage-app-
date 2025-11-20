package com.example.status.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.item.PointDetailList;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class EarnPointAdapter extends RecyclerView.Adapter<EarnPointAdapter.ViewHolder> {

    Activity activity;
    List<PointDetailList> pointDetailLists;

    public EarnPointAdapter(Activity activity, List<PointDetailList> pointDetailLists) {
        this.activity = activity;
        this.pointDetailLists = pointDetailLists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.earn_point_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (position == pointDetailLists.size() - 1) {
            holder.view.setVisibility(View.GONE);
        } else {
            holder.view.setVisibility(View.VISIBLE);
        }

        holder.textViewTitle.setText(pointDetailLists.get(position).getTitle());
        holder.textViewPoint.setText(pointDetailLists.get(position).getPoint());

    }

    @Override
    public int getItemCount() {
        return pointDetailLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        MaterialTextView textViewTitle, textViewPoint;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.view_ep_adapter);
            textViewTitle = itemView.findViewById(R.id.textView_title_ep_adapter);
            textViewPoint = itemView.findViewById(R.id.textView_ep_adapter);

        }
    }
}
