package com.example.status.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.bumptech.glide.Glide;
import com.example.status.BuildConfig;
import com.example.status.R;
import com.example.status.activity.Login;
import com.example.status.interfaces.FavouriteIF;
import com.example.status.interfaces.OnClick;
import com.example.status.item.SubCategoryList;
import com.example.status.util.Constant;
import com.example.status.util.Method;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.wortise.ads.natives.GoogleNativeAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;


public class SubCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Activity activity;
    Method method;
    int columnWidth;
    String type;
    Animation myAnim;
    List<SubCategoryList> subCategoryLists;

    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_QUOTES = 2;
    private final int VIEW_TYPE_Ad = 3;
    private Boolean isAdLoaded = false;
    AdLoader adLoader = null;
    List<com.google.android.gms.ads.nativead.NativeAd> mNativeAdsAdmob = new ArrayList<>();

    public SubCategoryAdapter(Activity activity, List<SubCategoryList> subCategoryLists, OnClick onClick, String type) {
        this.activity = activity;
        this.type = type;
        this.subCategoryLists = subCategoryLists;
        method = new Method(activity, onClick);
        columnWidth = (method.getScreenWidth());
        myAnim = AnimationUtils.loadAnimation(activity, R.anim.bounce);
        loadNativeAds();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.sub_category_adapter, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_QUOTES) {
            View v = LayoutInflater.from(activity).inflate(R.layout.quotes_adapter, parent, false);
            return new Quotes(v);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View v = LayoutInflater.from(activity).inflate(R.layout.layout_loading_item, parent, false);
            return new ProgressViewHolder(v);
        } else if (viewType == VIEW_TYPE_Ad) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admob_adapter, parent, false);
            return new AdOption(view);
        }
        return null;
    }

    @Override
    @SuppressLint("UseCompatLoadingForDrawables")
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ViewHolder viewHolder = (ViewHolder) holder;

            String statusType = subCategoryLists.get(position).getStatus_type();
            if (statusType.equals("video")) {
                viewHolder.imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.video_ic));
            } else if (statusType.equals("image")) {
                viewHolder.imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.img_ic));
            } else {
                viewHolder.imageViewType.setImageDrawable(activity.getResources().getDrawable(R.drawable.gif_ic));
            }

            if (subCategoryLists.get(position).getIs_favourite().equals("true")) {
                viewHolder.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav_hov));
            } else {
                if (method.isDarkMode()) {
                    viewHolder.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.fav_white_ic));
                } else {
                    viewHolder.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav));
                }
            }

            if (subCategoryLists.get(position).getAlready_like().equals("true")) {
                viewHolder.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_hov));
            } else {
                if (method.isDarkMode()) {
                    viewHolder.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_white));
                } else {
                    viewHolder.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_ic));
                }
            }

            String typeLayout = subCategoryLists.get(position).getStatus_layout();
            if (typeLayout.equals("Portrait")) {
                viewHolder.viewThumb.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(columnWidth / 2 - 60, columnWidth / 2);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                viewHolder.imageView.setLayoutParams(layoutParams);
            } else {
                viewHolder.viewThumb.setVisibility(View.GONE);
                viewHolder.imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
            }

            if (statusType.equals("gif")) {
                Glide.with(activity)
                        .asBitmap()
                        .load(subCategoryLists.get(position).getStatus_thumbnail_s())
                        .placeholder(R.drawable.placeholder_landscape).into(viewHolder.imageView);

            } else {
                Glide.with(activity)
                        .load(subCategoryLists.get(position).getStatus_thumbnail_s())
                        .placeholder(R.drawable.placeholder_landscape).into(viewHolder.imageView);
            }

            viewHolder.textViewTitle.setText(subCategoryLists.get(position).getStatus_title());
            viewHolder.textViewSubTitle.setText(subCategoryLists.get(position).getCategory_name());
            viewHolder.textViewView.setText(method.format(Double.parseDouble(subCategoryLists.get(position).getTotal_viewer())));
            viewHolder.textViewLike.setText(method.format(Double.parseDouble(subCategoryLists.get(position).getTotal_likes())));

            viewHolder.constraintLayout.setOnClickListener(v -> method.onClickData(position, subCategoryLists.get(position).getStatus_title(), type, subCategoryLists.get(position).getStatus_type(), subCategoryLists.get(position).getId(), ""));

            viewHolder.imageViewFavourite.setOnClickListener(v -> {
                viewHolder.imageViewFavourite.startAnimation(myAnim);
                if (method.isLogin()) {
                    FavouriteIF favouriteIF = (isFavourite, message) -> {
                        if (!isFavourite.equals("")) {
                            if (isFavourite.equals("true")) {
                                viewHolder.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav_hov));
                            } else {
                                if (method.isDarkMode()) {
                                    viewHolder.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.fav_white_ic));
                                } else {
                                    viewHolder.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav));
                                }
                            }
                        }
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    };
                    method.addToFav(subCategoryLists.get(position).getId(), method.userId(), subCategoryLists.get(position).getStatus_type(), favouriteIF);
                } else {
                    Method.loginBack = true;
                    activity.startActivity(new Intent(activity, Login.class));
                }
            });

        } else if (holder.getItemViewType() == VIEW_TYPE_QUOTES) {

            final Quotes quotes = (Quotes) holder;

            if (subCategoryLists.get(position).getIs_favourite().equals("true")) {
                quotes.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav_hov));
            } else {
                if (method.isDarkMode()) {
                    quotes.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.fav_white_ic));
                } else {
                    quotes.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav));
                }
            }

            if (subCategoryLists.get(position).getAlready_like().equals("true")) {
                quotes.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_hov));
            } else {
                if (method.isDarkMode()) {
                    quotes.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_white));
                } else {
                    quotes.imageViewLike.setImageDrawable(activity.getResources().getDrawable(R.drawable.like_ic));
                }
            }

            quotes.con.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, columnWidth / 2));

            Typeface typeface = Typeface.createFromAsset(activity.getAssets(), "text_font/" + subCategoryLists.get(position).getQuote_font());
            quotes.textView.setTypeface(typeface);

            quotes.textView.setText(subCategoryLists.get(position).getStatus_title());
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

            quotes.textViewCategory.setText(subCategoryLists.get(position).getCategory_name());

            quotes.con.setBackgroundColor(Color.parseColor(subCategoryLists.get(position).getQuote_bg()));

            quotes.textViewView.setText(method.format(Double.parseDouble(subCategoryLists.get(position).getTotal_viewer())));
            quotes.textViewLike.setText(method.format(Double.parseDouble(subCategoryLists.get(position).getTotal_likes())));

            quotes.cardView.setOnClickListener(v -> method.onClickData(position, subCategoryLists.get(position).getStatus_title(), type, subCategoryLists.get(position).getStatus_type(), subCategoryLists.get(position).getId(), ""));

            quotes.imageViewFavourite.setOnClickListener(v -> {
                quotes.imageViewFavourite.startAnimation(myAnim);
                if (method.isLogin()) {
                    FavouriteIF favouriteIF = (isFavourite, message) -> {
                        if (!isFavourite.equals("")) {
                            if (isFavourite.equals("true")) {
                                quotes.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav_hov));
                            } else {
                                if (method.isDarkMode()) {
                                    quotes.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.fav_white_ic));
                                } else {
                                    quotes.imageViewFavourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav));
                                }
                            }
                        }
                        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                    };
                    method.addToFav(subCategoryLists.get(position).getId(), method.userId(), subCategoryLists.get(position).getStatus_type(), favouriteIF);
                } else {
                    Method.loginBack = true;
                    activity.startActivity(new Intent(activity, Login.class));
                }
            });

        } else if (holder.getItemViewType() == VIEW_TYPE_Ad) {
            final AdOption adOption = (AdOption) holder;
            if (Constant.appRP.isNativ_ad()) {
                switch (Constant.appRP.getAd_network()) {
                    case "admob":
                        if (isAdLoaded) {
                            if (adOption.linearLayout.getChildCount() == 0) {
                                if (mNativeAdsAdmob.size() >= 5) {
                                    int i = new Random().nextInt(mNativeAdsAdmob.size() - 1);
                                    @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.layout_native_ad_admob, null);
                                    populateUnifiedNativeAdView(mNativeAdsAdmob.get(i), adView);
                                    adOption.linearLayout.removeAllViews();
                                    adOption.linearLayout.addView(adView);
                                    adOption.linearLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        break;
                    case "applovins":
                        if (adOption.linearLayout.getChildCount() == 0) {
                            LayoutInflater inflater = LayoutInflater.from(activity);
                            FrameLayout nativeAdLayout = (FrameLayout) inflater.inflate(R.layout.activity_native_max_template, adOption.linearLayout, false);
                            MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(Constant.appRP.getNativ_ad_id(), activity);
                            nativeAdLoader.loadAd();
                            nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                                @Override
                                public void onNativeAdLoaded(@Nullable MaxNativeAdView maxNativeAdView, MaxAd maxAd) {
                                    super.onNativeAdLoaded(maxNativeAdView, maxAd);
                                    // Add ad view to view.
                                    nativeAdLayout.removeAllViews();
                                    nativeAdLayout.addView(maxNativeAdView);
                                    adOption.linearLayout.addView(nativeAdLayout);
                                }

                                @Override
                                public void onNativeAdLoadFailed(String s, MaxError maxError) {
                                    super.onNativeAdLoadFailed(s, maxError);
                                }

                                @Override
                                public void onNativeAdClicked(MaxAd maxAd) {
                                    super.onNativeAdClicked(maxAd);
                                }
                            });
                        }
                        break;
                    case "startapp":
                        if (adOption.linearLayout.getChildCount() == 0) {
                            LayoutInflater inflater = LayoutInflater.from(activity);
                            CardView adView = (CardView) inflater.inflate(R.layout.native_start_item, adOption.linearLayout, false);
                            adOption.linearLayout.addView(adView);
                            adOption.linearLayout.setBackgroundColor(activity.getResources().getColor(R.color.transparent));
                            ImageView icon = adView.findViewById(R.id.icon);
                            TextView title = adView.findViewById(R.id.title);
                            TextView description = adView.findViewById(R.id.description);
                            Button button = adView.findViewById(R.id.button);
                            final StartAppNativeAd nativeAd = new StartAppNativeAd(activity);
                            nativeAd.setPreferences(new NativeAdPreferences().setAdsNumber(5).setAutoBitmapDownload(true).setPrimaryImageSize(4));

                            nativeAd.loadAd(new AdEventListener() {
                                @Override
                                public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                    ArrayList<NativeAdDetails> ads = nativeAd.getNativeAds();    // get NativeAds list
                                    int i = new Random().nextInt(ads.size() - 1);
                                    NativeAdDetails nativeAdDetails = ads.get(i);
                                    if (nativeAdDetails != null) {
                                        icon.setImageBitmap(nativeAdDetails.getImageBitmap());
                                        title.setText(nativeAdDetails.getTitle());
                                        description.setText(nativeAdDetails.getDescription());
                                        button.setText(nativeAdDetails.isApp() ? "Install" : "Open");
                                        nativeAdDetails.registerViewForInteraction(adView);
                                        nativeAdDetails.registerViewForInteraction(button);
                                    }
                                }

                                @Override
                                public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                    if (BuildConfig.DEBUG) {
                                         assert ad != null;
                                         Log.e("onFailedToReceiveAd: ", "" + ad.getErrorMessage());
                                     }
                                }
                            });
                        }
                        break;
                    case "facebook":
                        if (adOption.linearLayout.getChildCount() == 0) {
                            LayoutInflater inflater = LayoutInflater.from(activity);
                            LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout, adOption.linearLayout, false);
                            adOption.linearLayout.addView(adView);
                            final LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
                            final TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
                            final MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
                            final TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
                            final TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
                            final TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
                            final Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);
                            final NativeAd nativeAd = new NativeAd(activity, Constant.appRP.getNativ_ad_id());
                            NativeAdListener nativeAdListener = new NativeAdListener() {
                                @Override
                                public void onMediaDownloaded(Ad ad) {
                                    Log.d("status_data", "MediaDownloaded" + " " + ad.toString());
                                }

                                @Override
                                public void onError(Ad ad, AdError adError) {
                                    Toast.makeText(activity, adError.toString(), Toast.LENGTH_SHORT).show();
                                    Log.d("status_data", "error" + " " + adError);
                                }

                                @Override
                                public void onAdLoaded(Ad ad) {
                                    // Race condition, load() called again before last ad was displayed
                                    if (nativeAd != ad) {
                                        return;
                                    }
                                    Log.d("status_data", "on load" + " " + ad);
                                    NativeAdLayout nativeAdLayout = new NativeAdLayout(activity);
                                    AdOptionsView adOptionsView = new AdOptionsView(activity, nativeAd, nativeAdLayout);
                                    adChoicesContainer.removeAllViews();
                                    adChoicesContainer.addView(adOptionsView, 0);
                                    nativeAdTitle.setText(nativeAd.getAdvertiserName());
                                    nativeAdBody.setText(nativeAd.getAdBodyText());
                                    nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                                    nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                                    nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                                    sponsoredLabel.setText(nativeAd.getSponsoredTranslation());
                                    List<View> clickableViews = new ArrayList<>();
                                    clickableViews.add(nativeAdTitle);
                                    clickableViews.add(nativeAdCallToAction);
                                    nativeAd.registerViewForInteraction(
                                            adOption.linearLayout,
                                            nativeAdMedia,
                                            clickableViews);

                                }

                                @Override
                                public void onAdClicked(Ad ad) {
                                    Log.d("status_data", "AdClicked" + " " + ad.toString());
                                }

                                @Override
                                public void onLoggingImpression(Ad ad) {
                                    Log.d("status_data", "Impression" + " " + ad.toString());
                                }
                            };
                            nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
                        }
                        break;
                    case "wortise":
                        if (adOption.linearLayout.getChildCount() == 0) {
                            GoogleNativeAd googleNativeAd = new GoogleNativeAd(
                                    activity, Constant.appRP.getNativ_ad_id(), new GoogleNativeAd.Listener() {
                                @Override
                                public void onNativeFailedToLoad(@NonNull GoogleNativeAd googleNativeAd, @NonNull com.wortise.ads.AdError adError) {

                                }

                                @Override
                                public void onNativeLoaded(@NonNull GoogleNativeAd googleNativeAd, @NonNull com.google.android.gms.ads.nativead.NativeAd nativeAd) {
                                    @SuppressLint("InflateParams") NativeAdView adView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.layout_native_ad_wortise, null);
                                    populateUnifiedNativeAdView(nativeAd, adView);
                                    adOption.linearLayout.removeAllViews();
                                    adOption.linearLayout.addView(adView);
                                    adOption.linearLayout.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onNativeClicked(@NonNull GoogleNativeAd googleNativeAd) {
                                }

                                @Override
                                public void onNativeImpression(@NonNull GoogleNativeAd googleNativeAd) {

                                }
                            });
                            googleNativeAd.load();
                        }
                        break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != subCategoryLists ? subCategoryLists.size() + 1 : 0);
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemViewType(int position) {

        if (position != subCategoryLists.size()) {
            if (subCategoryLists.get(position) == null) {
                return VIEW_TYPE_Ad;
            } else {
                if (subCategoryLists.get(position).getStatus_type().equals("quote")) {
                    return VIEW_TYPE_QUOTES;
                } else {
                    return VIEW_TYPE_ITEM;
                }
            }

        } else {
            return VIEW_TYPE_LOADING;
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View viewThumb;
        ConstraintLayout constraintLayout;
        ImageView imageView, imageViewType, imageViewFavourite, imageViewLike;
        MaterialTextView textViewTitle, textViewSubTitle, textViewView, textViewLike;

        public ViewHolder(View itemView) {
            super(itemView);

            viewThumb = itemView.findViewById(R.id.view_subCat_adapter);
            constraintLayout = itemView.findViewById(R.id.con_subCat_adapter);
            imageView = itemView.findViewById(R.id.imageView_subCat_adapter);
            imageViewType = itemView.findViewById(R.id.imageView_type_subCat_adapter);
            imageViewFavourite = itemView.findViewById(R.id.imageView_fav_subCat_adapter);
            imageViewLike = itemView.findViewById(R.id.imageView_like_subCat_adapter);
            textViewTitle = itemView.findViewById(R.id.textView_title_subCat_adapter);
            textViewSubTitle = itemView.findViewById(R.id.textView_cat_subCat_adapter);
            textViewView = itemView.findViewById(R.id.textView_view_subCat_adapter);
            textViewLike = itemView.findViewById(R.id.textView_like_subCategory_adapter);

        }
    }

    public static class Quotes extends RecyclerView.ViewHolder {

        ConstraintLayout con;
        MaterialCardView cardView;
        ImageView imageViewFavourite, imageViewLike;
        MaterialTextView textView, textViewCategory, textViewView, textViewLike;

        public Quotes(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView_quotes_adapter);
            con = itemView.findViewById(R.id.con_quotes_adapter);
            textView = itemView.findViewById(R.id.textView_quotes_adapter);
            textViewCategory = itemView.findViewById(R.id.textView_cat_quotes_adapter);
            textViewView = itemView.findViewById(R.id.textView_view_quotes_adapter);
            textViewLike = itemView.findViewById(R.id.textView_like_quotes_adapter);
            imageViewLike = itemView.findViewById(R.id.imageView_like_quotes_adapter);
            imageViewFavourite = itemView.findViewById(R.id.imageView_fav_quotes_adapter);

        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        @SuppressLint("StaticFieldLeak")
        public static ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

    public static class AdOption extends RecyclerView.ViewHolder {

        RelativeLayout linearLayout;

        public AdOption(View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.rl_native_ad);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadNativeAds() {
        if (Constant.appRP.isNativ_ad()) {
            if (Constant.appRP.getAd_network().equals("admob")) {

                AdLoader.Builder builder = new AdLoader.Builder(activity, Constant.appRP.getNativ_ad_id());
                adLoader = builder.forNativeAd(nativeAd -> {
                    mNativeAdsAdmob.add(nativeAd);
                    isAdLoaded = true;
                    notifyDataSetChanged();
                }).build();

                // Load the Native Express ad.
                adLoader.loadAds(new AdRequest.Builder().build(), 5);

            }
        }
    }

    private void populateUnifiedNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());
        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) Objects.requireNonNull(adView.getStarRatingView()))
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            Objects.requireNonNull(adView.getAdvertiserView()).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) Objects.requireNonNull(adView.getAdvertiserView())).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }
}
