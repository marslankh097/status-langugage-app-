package com.example.status.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.interfaces.LanguageIF;
import com.example.status.item.LanguageList;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class SelectLanguageAdapter extends RecyclerView.Adapter<SelectLanguageAdapter.ViewHolder> {

    Activity activity;
    LanguageIF languageIF;
    private boolean isSelectedAll = false;
    List<LanguageList> languageLists;

    public SelectLanguageAdapter(Activity activity, List<LanguageList> languageLists, LanguageIF languageIF) {
        this.activity = activity;
        this.languageLists = languageLists;
        this.languageIF = languageIF;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.select_language_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (!isSelectedAll) {
            holder.checkBox.setChecked(false);
        }

        holder.textView.setText(languageLists.get(position).getLanguage_name());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> languageIF.selectLanguage(languageLists.get(position).getLanguage_id(), "", position, isChecked));

        holder.cardView.setOnClickListener(v -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
        });

    }

    public void clearCheckBox() {
        isSelectedAll = false;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return languageLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout cardView;
        MaterialTextView textView;
        MaterialCheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView_sl_adapter);
            checkBox = itemView.findViewById(R.id.checkbox_sl_adapter);
            textView = itemView.findViewById(R.id.textView_sl_adapter);

        }
    }
}
