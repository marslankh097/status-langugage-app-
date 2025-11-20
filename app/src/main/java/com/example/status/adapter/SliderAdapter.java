package com.example.status.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.interfaces.OnClick;
import com.example.status.item.SubCategoryList;
import com.example.status.util.Method;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class SliderAdapter extends PagerAdapter {

    Method method;
    Activity activity;
    String type;
    int columnWidth;
    List<SubCategoryList> subCategoryLists;

    public SliderAdapter(Activity activity, String type, List<SubCategoryList> subCategoryLists, OnClick onClick) {
        this.activity = activity;
        this.subCategoryLists = subCategoryLists;
        this.type = type;
        method = new Method(activity, onClick);
        columnWidth = (method.getScreenWidth());
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        View view;
        if (subCategoryLists.get(position).getStatus_type().equals("quote")) {

            view = activity.getLayoutInflater().inflate(R.layout.slider_quotes_adapter, container, false);

            MaterialTextView textView = view.findViewById(R.id.textView_slider_quotes);
            ConstraintLayout con = view.findViewById(R.id.con_slider_quotes);
            MaterialCardView cardView = view.findViewById(R.id.cardView_main_slider_quotes);

            con.setBackgroundColor(Color.parseColor(subCategoryLists.get(position).getQuote_bg()));

            Typeface typeface = Typeface.createFromAsset(activity.getAssets(), "text_font/" + subCategoryLists.get(position).getQuote_font());
            textView.setTypeface(typeface);

            textView.setText(subCategoryLists.get(position).getStatus_title());
            textView.post(() -> {
                ViewGroup.LayoutParams params = textView.getLayoutParams();
                if (params == null) {
                    params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                final int widthSpec = View.MeasureSpec.makeMeasureSpec(textView.getWidth(), View.MeasureSpec.UNSPECIFIED);
                final int heightSpec = View.MeasureSpec.makeMeasureSpec(textView.getHeight(), View.MeasureSpec.UNSPECIFIED);
                textView.measure(widthSpec, heightSpec);
                textView.setMaxLines(heightSpec / textView.getLineHeight());
                textView.setEllipsize(TextUtils.TruncateAt.END);
            });

            cardView.setOnClickListener(v -> method.onClickData(position, subCategoryLists.get(position).getStatus_title(), type, subCategoryLists.get(position).getStatus_type(), subCategoryLists.get(position).getId(), ""));

        } else {

            view = activity.getLayoutInflater().inflate(R.layout.slider_adapter, container, false);

            assert view != null;
            MaterialTextView textViewTitle = view.findViewById(R.id.textView_title_slider);
            ImageView imageView = view.findViewById(R.id.imageView_slider_adapter);
            ImageView imageViewType = view.findViewById(R.id.imageView_type_slider);
            MaterialCardView cardView = view.findViewById(R.id.cardView_slider_adapter);

            String statusType = subCategoryLists.get(position).getStatus_type();

            if (statusType.equals("gif")) {
                Glide.with(activity)
                        .asBitmap()
                        .load(subCategoryLists.get(position).getStatus_thumbnail_s())
                        .placeholder(R.drawable.placeholder_landscape).into(imageView);
            } else {
                Glide.with(activity).load(subCategoryLists.get(position).getStatus_thumbnail_s())
                        .placeholder(R.drawable.placeholder_landscape).into(imageView);
            }

            String typeLayout = subCategoryLists.get(position).getStatus_layout();
            if (typeLayout.equals("Portrait")) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(columnWidth / 2 - 60, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                imageView.setLayoutParams(layoutParams);
            }

            switch (statusType) {
                case "video":
                    imageViewType.setVisibility(View.VISIBLE);
                    imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.video_ic));
                    break;
                case "image":
                    imageViewType.setVisibility(View.VISIBLE);
                    imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.img_ic));
                    break;
                case "gif":
                    imageViewType.setVisibility(View.VISIBLE);
                    imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.gif_ic));
                    break;
                case "external":
                    imageViewType.setVisibility(View.GONE);
                    break;
            }

            textViewTitle.setText(subCategoryLists.get(position).getStatus_title());

            cardView.setOnClickListener(v -> {
                if (statusType.equals("external")) {
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(subCategoryLists.get(position).getExternal_link())));
                    } catch (Exception e) {
                        method.alertBox(activity.getResources().getString(R.string.wrong));
                    }
                } else {
                    method.onClickData(position, subCategoryLists.get(position).getStatus_title(), type, subCategoryLists.get(position).getStatus_type(), subCategoryLists.get(position).getId(), "");
                }
            });

        }
        container.addView(view, 0);
        return view;

    }

    @Override
    public int getCount() {
        return subCategoryLists.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        (container).removeView((View) object);
    }
}

