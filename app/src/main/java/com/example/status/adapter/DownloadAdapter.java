package com.example.status.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.BuildConfig;
import com.example.status.R;
import com.example.status.activity.VideoPlayer;
import com.example.status.activity.ViewImage;
import com.example.status.database.DatabaseHandler;
import com.example.status.item.SubCategoryList;
import com.example.status.util.Method;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.util.List;


public class
DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

    Activity activity;
    Method method;
    DatabaseHandler db;
    Animation myAnim;
    int columnWidth;
    List<SubCategoryList> downloadLists;

    public DownloadAdapter(Activity activity, List<SubCategoryList> subCategoryLists) {
        this.activity = activity;
        this.downloadLists = subCategoryLists;
        db = new DatabaseHandler(activity);
        method = new Method(activity);
        columnWidth = (method.getScreenWidth());
        myAnim = AnimationUtils.loadAnimation(activity, R.anim.bounce);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.download_adapter, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        String status_type = downloadLists.get(position).getStatus_type();
        if (status_type.equals("video")) {
            holder.imageViewPlay.setVisibility(View.VISIBLE);
            holder.imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.video_ic));
        } else if (status_type.equals("image")) {
            holder.imageViewPlay.setVisibility(View.GONE);
            holder.imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.img_ic));
        } else {
            holder.imageViewPlay.setVisibility(View.GONE);
            holder.imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.gif_ic));
        }

        String typeLayout = downloadLists.get(position).getStatus_layout();
        if (typeLayout.equals("Portrait")) {
            holder.viewThumb.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(columnWidth / 2 - 60, columnWidth / 2);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            holder.imageView.setLayoutParams(layoutParams);
        } else {
            holder.viewThumb.setVisibility(View.VISIBLE);
            holder.imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
        }

        if (status_type.equals("gif")) {
            Glide.with(activity)
                    .asBitmap()
                    .load("file://" + downloadLists.get(position).getStatus_thumbnail_s())
                    .placeholder(R.drawable.placeholder_landscape)
                    .into(holder.imageView);
        } else {
            Glide.with(activity).load("file://" + downloadLists.get(position).getStatus_thumbnail_s())
                    .placeholder(R.drawable.placeholder_landscape)
                    .into(holder.imageView);
        }

        holder.textViewName.setText(downloadLists.get(position).getStatus_title());
        holder.textViewSubName.setText(downloadLists.get(position).getCategory_name());

        holder.cardView.setOnClickListener(v -> {
            if (downloadLists.get(position).getStatus_type().equals("video")) {
                Intent intent = new Intent(activity, VideoPlayer.class);
                intent.putExtra("Video_url", downloadLists.get(position).getVideo_url());
                intent.putExtra("video_type", downloadLists.get(position).getStatus_layout());
                activity.startActivity(intent);
            } else if (downloadLists.get(position).getStatus_type().equals("gif")) {
                activity.startActivity(new Intent(activity, ViewImage.class)
                        .putExtra("path", downloadLists.get(position).getGif_url()));
            } else {
                activity.startActivity(new Intent(activity, ViewImage.class)
                        .putExtra("path", downloadLists.get(position).getStatus_thumbnail_b()));
            }
        });

        holder.imageViewShare.setOnClickListener(v -> {
            String sharePath;
            Intent intent = new Intent(Intent.ACTION_SEND);// Create the new Intent using the 'Send' action.
            if (downloadLists.get(position).getStatus_type().equals("video")) {
                intent.setType("video/mp4");
                sharePath = downloadLists.get(position).getVideo_url();
            } else if (downloadLists.get(position).getStatus_type().equals("gif")) {
                intent.setType("image/gif");
                sharePath = downloadLists.get(position).getGif_url();
            } else {
                intent.setType("image/*");
                sharePath = downloadLists.get(position).getStatus_thumbnail_b();
            }
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".fileprovider", new File(sharePath));
            intent.putExtra(Intent.EXTRA_STREAM, contentUri); // Broadcast the Intent.
            activity.startActivity(Intent.createChooser(intent, activity.getResources().getString(R.string.share_to)));
        });

        holder.imageViewDelete.setOnClickListener(v -> {
            holder.imageViewDelete.startAnimation(myAnim);
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
            builder.setMessage(activity.getResources().getString(R.string.delete_msg));
            builder.setCancelable(false);
            builder.setPositiveButton(activity.getResources().getString(R.string.delete),
                    (arg0, arg1) -> {
                        db.deleteStatusDownload(downloadLists.get(position).getId(), downloadLists.get(position).getStatus_type());
                        if (downloadLists.get(position).getStatus_type().equals("video")) {
                            File file = new File(downloadLists.get(position).getVideo_url());
                            file.delete();
                        }
                        File fileImage;
                        if (downloadLists.get(position).getStatus_type().equals("gif")) {
                            fileImage = new File(downloadLists.get(position).getGif_url());
                        } else {
                            fileImage = new File(downloadLists.get(position).getStatus_thumbnail_b());
                        }
                        fileImage.delete();
                        downloadLists.remove(position);
                        notifyDataSetChanged();
                    });
            builder.setNegativeButton(activity.getResources().getString(R.string.cancel_dialog),
                    (dialog, which) -> dialog.dismiss());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return downloadLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View viewThumb;
        MaterialCardView cardView;
        MaterialTextView textViewName, textViewSubName;
        ImageView imageView, imageViewType, imageViewPlay, imageViewShare, imageViewDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView_download_adapter);
            viewThumb = itemView.findViewById(R.id.view_download_adapter);
            textViewName = itemView.findViewById(R.id.textView_title_download_adapter);
            textViewSubName = itemView.findViewById(R.id.textView_sub_title_download_adapter);
            imageView = itemView.findViewById(R.id.imageView_download_adapter);
            imageViewType = itemView.findViewById(R.id.imageView_type_download_adapter);
            imageViewPlay = itemView.findViewById(R.id.imageView_play_download_adapter);
            imageViewShare = itemView.findViewById(R.id.imageView_share_download_adapter);
            imageViewDelete = itemView.findViewById(R.id.imageView_delete_download_adapter);

        }
    }
}
