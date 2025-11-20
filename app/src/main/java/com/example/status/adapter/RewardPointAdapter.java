package com.example.status.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.item.RewardPointList;
import com.example.status.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class RewardPointAdapter extends RecyclerView.Adapter<RewardPointAdapter.ViewHolder> {

    Activity activity;
    List<RewardPointList> rewardPointLists;

    public RewardPointAdapter(Activity activity, List<RewardPointList> rewardPointLists) {
        this.activity = activity;
        this.rewardPointLists = rewardPointLists;
    }

    @NonNull
    @Override
    public RewardPointAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.reward_point_adapter, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RewardPointAdapter.ViewHolder holder, final int position) {

        if (rewardPointLists.get(position).getTitle().equals("")) {

            Glide.with(activity).load(rewardPointLists.get(position).getStatus_thumbnail())
                    .placeholder(R.drawable.app_icon).into(holder.imageView);
            holder.textViewTitle.setText(rewardPointLists.get(position).getActivity_type());
            holder.textViewType.setText(rewardPointLists.get(position).getActivity_type());

        } else {

            Glide.with(activity).load(rewardPointLists.get(position).getStatus_thumbnail())
                    .placeholder(R.drawable.app_icon).into(holder.imageView);
            holder.textViewTitle.setText(rewardPointLists.get(position).getTitle());
            holder.textViewType.setText(rewardPointLists.get(position).getActivity_type());
        }

        holder.textViewDate.setText(rewardPointLists.get(position).getDate() + "  " + rewardPointLists.get(position).getTime());
        holder.textViewPoint.setText(rewardPointLists.get(position).getPoints());

    }

    @Override
    public int getItemCount() {
        return rewardPointLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageView;
        MaterialTextView textViewTitle, textViewDate, textViewType, textViewPoint;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_rewardPoint_adapter);
            textViewTitle = itemView.findViewById(R.id.textView_title_rewardPoint_adapter);
            textViewDate = itemView.findViewById(R.id.textView_dateTime_rewardPoint_adapter);
            textViewPoint = itemView.findViewById(R.id.textView_point_rewardPoint_adapter);
            textViewType = itemView.findViewById(R.id.textView_type_rewardPoint_adapter);

        }
    }
}
