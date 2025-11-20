package com.example.status.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.interfaces.LanguageIF;
import com.example.status.item.LanguageList;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import cn.refactor.library.SmoothCheckBox;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {

    Activity activity;
    LanguageIF languageIF;
    List<LanguageList> languageLists;

    public LanguageAdapter(Activity activity, List<LanguageList> languageLists, LanguageIF languageIF) {
        this.activity = activity;
        this.languageLists = languageLists;
        this.languageIF = languageIF;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.language_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.textView.setText(languageLists.get(position).getLanguage_name());

        Glide.with(activity).load(languageLists.get(position).getLanguage_image_thumb())
                .placeholder(R.drawable.placeholder_portable)
                .into(holder.imageView);

        holder.checkBox.setChecked(languageLists.get(position).getIs_selected().equals("true"));



        holder.checkBox.setOnCheckedChangeListener((checkBox, isChecked) -> {
            if (isChecked){
                holder.con.setBackgroundResource(R.drawable.language_bg_select);
            }else {
                holder.con.setBackgroundResource(R.drawable.language_bg);
            }
            languageIF.selectLanguage(languageLists.get(position).getLanguage_id(), "", position, isChecked);
        });

        holder.con.setOnClickListener(v -> holder.checkBox.setChecked(!holder.checkBox.isChecked()));
    }

    @Override
    public int getItemCount() {
        return languageLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout con;
        ImageView imageView;
        MaterialTextView textView;
        SmoothCheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_language_adapter);
            checkBox = itemView.findViewById(R.id.checkBox_language_adapter);
            textView = itemView.findViewById(R.id.textView_language_adapter);
            con = itemView.findViewById(R.id.con_language_adapter);

        }
    }
}
