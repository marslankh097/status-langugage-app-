package com.example.status.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.activity.Login;
import com.example.status.activity.MainActivity;
import com.example.status.activity.ViewImage;
import com.example.status.adapter.SubCategoryAdapter;
import com.example.status.interfaces.FavouriteIF;
import com.example.status.interfaces.FullScreen;
import com.example.status.interfaces.OnClick;
import com.example.status.interfaces.VideoAd;
import com.example.status.item.SubCategoryList;
import com.example.status.response.DataRP;
import com.example.status.response.StatusDetailRP;
import com.example.status.response.StatusDownloadRP;
import com.example.status.response.StatusLikeRP;
import com.example.status.response.StatusRP;
import com.example.status.response.UserFollowStatusRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Constant;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SCDetailFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private Animation myAnim;
    //Video player
    private SimpleExoPlayer player;
    private PlayerView playerView;
    //other variable
    String passId, type, statusType;
    private boolean isFullScreen = false, isView = true, isOver = false;
    private int position, columnWidth, columnHeight, paginationIndex = 1, oldPosition = 0;

    private ProgressDialog progressDialog;
    private CircleImageView imageViewUser, imageViewUserComment;
    private ProgressBar progressBar, progressBarPlayer;

    private StatusDetailRP statusDetailRP;
    private List<SubCategoryList> relatedLists;
    private SubCategoryAdapter subCategoryAdapter;
    private RecyclerView recyclerView;

    NestedScrollView nestedScrollView;
    private MaterialCardView cardViewOption, cardViewUserComment;
    private ConstraintLayout conMain, conNoData, conDownload, conUser, conComment, conRelated;
    private ImageView  imageViewMore,
            imageView, imageViewPlay, imageViewDownload, imageViewFullscreen, imageViewLike, imageViewFav;
    private MaterialTextView textViewQuotes, textViewTitle, textViewDate, textViewView, textViewLike, textViewDownLoad, textViewUserName,
            textViewFollow, textViewCommentCount, textViewComment;
    int j = 1;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.scdetail_fragment, container, false);

        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        GlobalBus.getBus().register(this);

        assert getArguments() != null;
        passId = getArguments().getString("id");//id
        type = getArguments().getString("type");//get which type of single detail
        statusType = getArguments().getString("status_type");// status type (image, gif, quotes, video)
        position = getArguments().getInt("position");//use in subcategory array

        progressDialog = new ProgressDialog(requireActivity());

        myAnim = AnimationUtils.loadAnimation(requireActivity(), R.anim.bounce);

        onClick = (position, title, type, status_type, id, tag) -> {
            new Handler().post(() -> {

                viewHide();
                playerStop();

                requireActivity().getSupportFragmentManager().popBackStack();

                SCDetailFragment scDetailFragment = new SCDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", id);
                bundle.putString("type", type);
                bundle.putString("status_type", status_type);
                bundle.putInt("position", position);
                scDetailFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, scDetailFragment, relatedLists.get(position).getStatus_title()).addToBackStack(relatedLists.get(position).getStatus_title()).commitAllowingStateLoss();
            });
        };
        VideoAd videoAd = type -> {
            switch (type) {
                case "download":
                    downloadStatus();
                    break;
                case "like":
                    likeStatus();
                    break;
                default:
                    playVideo();
                    break;
            }
        };

        FullScreen fullScreen = isFull -> {
            Events.FullScreenNotify fullScreenNotify = new Events.FullScreenNotify(isFull);
            GlobalBus.getBus().post(fullScreenNotify);
        };
        method = new Method(getActivity(), onClick, videoAd, fullScreen);

        columnWidth = (method.getScreenWidth());
        columnHeight = (method.getScreenHeight());
        relatedLists = new ArrayList<>();

        conMain = view.findViewById(R.id.con_main_scd);
        conNoData = view.findViewById(R.id.con_noDataFound);
        progressBar = view.findViewById(R.id.progressbar_scd_fragment);
        progressBarPlayer = view.findViewById(R.id.progressbar_player_scd_fragment);

        imageView = view.findViewById(R.id.imageView_scd);
        imageViewPlay = view.findViewById(R.id.imageView_play_scd);
        imageViewDownload = view.findViewById(R.id.imageView_download_scd);
        imageViewFullscreen = view.findViewById(R.id.imageView_fullscreen_scd);
        imageViewFav = view.findViewById(R.id.imageView_fav_scd);
        imageViewLike = view.findViewById(R.id.imageView_like_scd);
        imageViewMore = view.findViewById(R.id.imageView_more);
        imageViewUser = view.findViewById(R.id.imageView_profile_scd);
        imageViewUserComment = view.findViewById(R.id.imageView_userComment_scd);

        textViewQuotes = view.findViewById(R.id.textView_quotes_scd);
        textViewTitle = view.findViewById(R.id.textView_title_scd);
        textViewDate = view.findViewById(R.id.textView_date_scd);
        textViewView = view.findViewById(R.id.textView_view_scd);
        textViewLike = view.findViewById(R.id.textView_like_scd);
        textViewDownLoad = view.findViewById(R.id.textView_download_scd);
        textViewUserName = view.findViewById(R.id.textView_userName_scd);
        textViewFollow = view.findViewById(R.id.textView_follow_scd);
        textViewCommentCount = view.findViewById(R.id.textView_userComment_scd);
        textViewComment = view.findViewById(R.id.textView_comment_scd);

        playerView = view.findViewById(R.id.player_view);
        cardViewOption = view.findViewById(R.id.cardView_option_scd);
        conRelated = view.findViewById(R.id.con_related_scd);
        conDownload = view.findViewById(R.id.con_download_scd);
        conUser = view.findViewById(R.id.con_user_scd);
        conComment = view.findViewById(R.id.con_comment_scd);
        cardViewUserComment = view.findViewById(R.id.cardView_userComment_scd);
        recyclerView = view.findViewById(R.id.recyclerView_scd_fragment);
        nestedScrollView = view.findViewById(R.id.nestedScrollView_scd);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);
        recyclerView.setNestedScrollingEnabled(false);

        nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (v.getChildAt(v.getChildCount() - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                        scrollY > oldScrollY) {

                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        if (!isOver) {
                            oldPosition = relatedLists.size();
                            new Handler().postDelayed(() -> {
                                paginationIndex++;
                                related(statusDetailRP.getId(), statusDetailRP.getCat_id());
                            }, 1000);
                        } else {
                            subCategoryAdapter.hideHeader();
                        }
                    }
                }
            }
        });

        conNoData.setVisibility(View.GONE);
        conMain.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        progressBarPlayer.setVisibility(View.GONE);
        playerView.setVisibility(View.GONE);

        imageViewFullscreen.setOnClickListener(v -> {

            imageViewFullscreen.startAnimation(myAnim);

            if (isFullScreen) {
                isFullScreen = false;

                cardViewOption.setVisibility(View.VISIBLE);
                cardViewUserComment.setVisibility(View.VISIBLE);
                conRelated.setVisibility(View.VISIBLE);

                requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                requireActivity().getWindow().clearFlags(1024);

                imageViewFullscreen.setImageDrawable(getResources().getDrawable(R.drawable.full_screen));
                if (statusDetailRP.getStatus_layout().equals("Portrait")) {
                    playerView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight / 2 + 140));
                } else {
                    playerView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
                }

            } else {
                isFullScreen = true;

                cardViewOption.setVisibility(View.GONE);
                cardViewUserComment.setVisibility(View.GONE);
                conRelated.setVisibility(View.GONE);

                imageViewFullscreen.setImageDrawable(getResources().getDrawable(R.drawable.exitfull_screen));
                playerView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

                if (!statusDetailRP.getStatus_layout().equals("Portrait")) {
                    requireActivity().getWindow().setFlags(1024, 1024);
                    requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }

            }
            method.ShowFullScreen(isFullScreen);
        });

        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                detail(passId, method.userId());
            } else {
                detail(passId, "0");
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

        setHasOptionsMenu(true);
        return view;

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.ic_searchView);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener((new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (method.isNetworkAvailable()) {
                    backStackRemove();
                    SearchFragment searchFragment = new SearchFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("search_menu", query);
                    searchFragment.setArguments(bundle);
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction().replace(R.id.frameLayout_main, searchFragment, query).commitAllowingStateLoss();
                    return false;
                } else {
                    method.alertBox(getResources().getString(R.string.internet_connection));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        }));

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void backStackRemove() {
        for (int i = 0; i < requireActivity().getSupportFragmentManager().getBackStackEntryCount(); i++) {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    public void getPlay(Events.StopPlay stopPlay) {
        if (imageViewFullscreen != null) {
            isFullScreen = false;
            if (statusDetailRP != null) {
                if (statusDetailRP.getStatus_type().equals("video")) {
                    imageViewPlay.setVisibility(View.VISIBLE);
                }
                imageViewFullscreen.setImageDrawable(getResources().getDrawable(R.drawable.full_screen));
                if (statusDetailRP.getStatus_layout().equals("Portrait")) {
                    imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight / 2 + 140));
                    playerView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight / 2 + 140));
                } else {
                    imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
                    playerView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
                }
            }
        }
        viewHide();
        playerStop();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    public void getNotify(Events.InfoUpdate infoUpdate) {
        if (statusDetailRP != null) {
            if (statusDetailRP.getId().equals(infoUpdate.getId())) {
                if (statusDetailRP.getStatus_type().equals(infoUpdate.getStatus_type())) {
                    switch (infoUpdate.getType()) {
                        case "all":
                            textViewView.setText(infoUpdate.getView());
                            textViewLike.setText(infoUpdate.getTotal_like());
                            if (infoUpdate.getAlready_like().equals("true")) {
                                imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_hov));
                            } else {
                                if (method.isDarkMode()) {
                                    imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_white));
                                } else {
                                    imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_ic));
                                }
                            }
                            break;
                        case "view":
                            textViewView.setText(infoUpdate.getView());
                            break;
                        case "like":
                            textViewLike.setText(infoUpdate.getTotal_like());
                            if (infoUpdate.getAlready_like().equals("true")) {
                                imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_hov));
                            } else {
                                if (method.isDarkMode()) {
                                    imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_white));
                                } else {
                                    imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_ic));
                                }
                            }
                            break;
                    }
                }
            }
        }
    }

    @Subscribe
    public void getNotify(Events.DownloadUpdate downloadUpdate) {
        if (statusDetailRP.getId().equals(downloadUpdate.getId())) {
            if (statusDetailRP.getStatus_type().equals(downloadUpdate.getStatus_type())) {
                if (textViewDownLoad != null) {
                    textViewDownLoad.setText(method.format(Double.parseDouble(downloadUpdate.getDownload_count())));
                }
            }
        }
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    public void getNotify(Events.FavouriteNotify favouriteNotify) {
        for (int i = 0; i < relatedLists.size(); i++) {
            if (relatedLists.get(i).getId().equals(favouriteNotify.getId())) {
                if (relatedLists.get(i).getStatus_type().equals(favouriteNotify.getStatus_type())) {
                    relatedLists.get(i).setIs_favourite(favouriteNotify.getIs_favourite());
                    if (subCategoryAdapter != null) {
                        subCategoryAdapter.notifyItemChanged(i);
                    }
                }
            }
        }
        if (statusDetailRP.getId().equals(favouriteNotify.getId())) {
            statusDetailRP.setIs_favourite(favouriteNotify.getIs_favourite());
            if (statusDetailRP.getIs_favourite().equals("true")) {
                imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_hov));
            } else {
                if (method.isDarkMode()) {
                    imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.fav_white_ic));
                } else {
                    imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav));
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Subscribe
    public void getNotify(Events.Comment comment) {
        if (comment.getStatus().equals("1")) {
            if (statusDetailRP.getId().equals(comment.getPostId())) {
                textViewCommentCount.setText(method.format(Double.parseDouble(comment.getTotalComment())));
                if (comment.getType().equals("delete")) {
                    if (comment.getDeleteCommentId().equals(statusDetailRP.getUserCommentRP().getComment_id())) {
                        statusDetailRP.getUserCommentRP().setComment_id(comment.getCommentId());
                        statusDetailRP.getUserCommentRP().setUser_id(comment.getUserId());
                        statusDetailRP.getUserCommentRP().setUser_name(comment.getUserName());
                        statusDetailRP.getUserCommentRP().setUser_image(comment.getUserImage());
                        statusDetailRP.getUserCommentRP().setPost_id(comment.getPostId());
                        statusDetailRP.getUserCommentRP().setStatus_type(comment.getPostId());
                        statusDetailRP.getUserCommentRP().setComment_text(comment.getCommentText());
                        statusDetailRP.getUserCommentRP().setComment_date(comment.getCommentDate());
                        textViewComment.setText(statusDetailRP.getUserCommentRP().getComment_text());
                        textViewComment.setBackground(getResources().getDrawable(R.drawable.comment_tra_bg));
                        Glide.with(requireActivity()).load(statusDetailRP.getUserCommentRP().getUser_image())
                                .placeholder(R.drawable.user_profile).into(imageViewUserComment);
                    }
                } else {
                    if (statusDetailRP.getUserCommentRP().getComment_id().equals("")) {
                        statusDetailRP.getUserCommentRP().setComment_id(comment.getCommentId());
                        statusDetailRP.getUserCommentRP().setUser_id(comment.getUserId());
                        statusDetailRP.getUserCommentRP().setUser_name(comment.getUserName());
                        statusDetailRP.getUserCommentRP().setUser_image(comment.getUserImage());
                        statusDetailRP.getUserCommentRP().setPost_id(comment.getPostId());
                        statusDetailRP.getUserCommentRP().setStatus_type(comment.getPostId());
                        statusDetailRP.getUserCommentRP().setComment_text(comment.getCommentText());
                        statusDetailRP.getUserCommentRP().setComment_date(comment.getCommentDate());
                        textViewComment.setText(statusDetailRP.getUserCommentRP().getComment_text());
                        textViewComment.setBackground(getResources().getDrawable(R.drawable.comment_tra_bg));
                        Glide.with(requireActivity()).load(statusDetailRP.getUserCommentRP().getUser_image())
                                .placeholder(R.drawable.user_profile).into(imageViewUserComment);
                    }
                }
            }
        } else {
            if (statusDetailRP.getId().equals(comment.getPostId())) {
                Glide.with(requireActivity()).load(R.drawable.user_profile)
                        .placeholder(R.drawable.user_profile).into(imageViewUserComment);
                textViewCommentCount.setText(method.format(Double.parseDouble(comment.getTotalComment())));
                textViewComment.setText(getResources().getString(R.string.leave_your_comment));
                textViewComment.setBackground(getResources().getDrawable(R.drawable.comment_bg));
            }
        }
    }

    private void viewHide() {
        if (imageViewFullscreen != null) {
            playerView.setVisibility(View.GONE);
        }
    }

    private void playerStop() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
        }
    }

    private void detail(final String id, final String userId) {

        if (getActivity() != null) {

            relatedLists.clear();
            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("status_id", id);
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("type", statusType);
            jsObj.addProperty("lang_ids", method.getLanguageIds());
            jsObj.addProperty("method_name", "single_status");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<StatusDetailRP> call = apiService.getStatusDetail(API.toBase64(jsObj.toString()));
            Log.e("dataa", "" + API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<StatusDetailRP>() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onResponse(@NotNull Call<StatusDetailRP> call, @NotNull Response<StatusDetailRP> response) {

                    if (getActivity() != null) {

                        try {
                            statusDetailRP = response.body();
                            assert statusDetailRP != null;

                            if (statusDetailRP.getStatus().equals("1")) {

                                if (statusDetailRP.getSuccess().equals("1")) {

                                    String statusType = statusDetailRP.getStatus_type();

                                    //toolbar name set
                                    if (MainActivity.toolbar != null) {
                                        MainActivity.toolbar.setTitle(statusDetailRP.getStatus_title());
                                    }

                                    //check status type
                                    if (statusType.equals("image") || statusType.equals("gif") || statusType.equals("quote")) {
                                        imageViewPlay.setVisibility(View.GONE);
                                        if (statusType.equals("quote")) {

                                            textViewTitle.setVisibility(View.GONE);
                                            imageView.setVisibility(View.GONE);
                                            conDownload.setVisibility(View.GONE);
                                            textViewQuotes.setMinHeight(columnWidth);
                                            textViewQuotes.setVisibility(View.VISIBLE);
                                            textViewQuotes.setText(statusDetailRP.getStatus_title());

                                            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "text_font/" + statusDetailRP.getQuote_font());
                                            textViewQuotes.setTypeface(typeface);

                                            textViewQuotes.setBackgroundColor(Color.parseColor(statusDetailRP.getQuote_bg()));

                                        } else {
                                            textViewQuotes.setVisibility(View.GONE);
                                            if (statusDetailRP.getStatus_layout().equals("Portrait")) {
                                                imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight / 2 + 140));
                                            } else {
                                                imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
                                            }
                                        }
                                    } else {
                                        textViewQuotes.setVisibility(View.GONE);
                                        if (statusDetailRP.getStatus_layout().equals("Portrait")) {
                                            imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight / 2 + 140));
                                            playerView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnHeight / 2 + 140));
                                        } else {
                                            imageView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
                                            playerView.setLayoutParams(new RelativeLayout.LayoutParams(columnWidth, columnWidth / 2));
                                        }
                                    }

                                    //status view count
                                    if (statusType.equals("quote") || statusType.equals("image") || statusType.equals("gif")) {
                                        if (method.isLogin()) {
                                            statusView(method.userId());
                                        }
                                    }

                                    if (statusDetailRP.getIs_favourite().equals("true")) {
                                        imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_hov));
                                    } else {
                                        if (method.isDarkMode()) {
                                            imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.fav_white_ic));
                                        } else {
                                            imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav));
                                        }
                                    }

                                    if (statusDetailRP.getAlready_like().equals("true")) {
                                        imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_hov));
                                    } else {
                                        if (method.isDarkMode()) {
                                            imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_white));
                                        } else {
                                            imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_ic));
                                        }
                                    }

                                    textViewTitle.setText(statusDetailRP.getStatus_title());
                                    if (statusDetailRP.getCreated_at().equals("")) {
                                        textViewDate.setVisibility(View.GONE);
                                    } else {
                                        textViewDate.setVisibility(View.VISIBLE);
                                        textViewDate.setText(statusDetailRP.getCreated_at());
                                    }
                                    textViewView.setText(method.format(Double.parseDouble(statusDetailRP.getTotal_viewer())));
                                    textViewLike.setText(method.format(Double.parseDouble(statusDetailRP.getTotal_likes())));
                                    if (!statusType.equals("quote")) {
                                        textViewDownLoad.setText(method.format(Double.parseDouble(statusDetailRP.getTotal_download())));
                                    }

                                    Glide.with(getActivity()).load(statusDetailRP.getStatus_thumbnail_b())
                                            .placeholder(R.drawable.placeholder_landscape).into(imageView);

                                    textViewCommentCount.setText(method.format(Double.parseDouble(statusDetailRP.getTotal_comment())));
                                    if (!statusDetailRP.getUserCommentRP().getComment_id().equals("")) {
                                        Glide.with(getActivity()).load(statusDetailRP.getUserCommentRP().getUser_image())
                                                .placeholder(R.drawable.user_profile).into(imageViewUserComment);
                                        textViewComment.setBackground(getResources().getDrawable(R.drawable.comment_tra_bg));
                                        textViewComment.setText(statusDetailRP.getUserCommentRP().getComment_text());
                                    }

                                    //check user following or not
                                    checkLoginFollowing();

                                    //all data update
                                    updateData();

                                    related(statusDetailRP.getId(), statusDetailRP.getCat_id());

                                    imageView.setOnClickListener(v -> {
                                        if (statusType.equals("image") || statusType.equals("gif")) {
                                            startActivity(new Intent(getActivity(), ViewImage.class)
                                                    .putExtra("path", statusDetailRP.getStatus_thumbnail_b()));
                                        }
                                    });

                                    imageViewPlay.setOnClickListener(v -> {
                                        imageViewPlay.startAnimation(myAnim);
                                        if (statusDetailRP.isVideo_views_status_ad()) {
                                            method.VideoAdDialog("video_play", "");
                                        } else {
                                            playVideo();
                                        }
                                    });

                                    conDownload.setOnClickListener(v -> {
                                        imageViewDownload.startAnimation(myAnim);
                                        switch (statusType) {
                                            case "image":
                                                if (statusDetailRP.isDownload_image_status_ad()) {
                                                    method.VideoAdDialog("download", "");
                                                } else {
                                                    downloadStatus();
                                                }
                                                break;
                                            case "gif":
                                                if (statusDetailRP.isDownload_gif_status_ad()) {
                                                    method.VideoAdDialog("download", "");
                                                } else {
                                                    downloadStatus();
                                                }
                                                break;
                                            case "video":
                                                if (statusDetailRP.isDownload_video_status_ad()) {
                                                    if (Constant.REWARD_VIDEO_AD_COUNT + 1 == Constant.REWARD_VIDEO_AD_COUNT_SHOW) {
                                                        playerStop();
                                                        viewHide();
                                                    }
                                                    method.VideoAdDialog("download", "");
                                                } else {
                                                    downloadStatus();
                                                }
                                                break;
                                        }
                                    });

                                    imageViewLike.setOnClickListener(v -> {
                                        imageViewLike.startAnimation(myAnim);
                                        if (method.isLogin()) {

                                            if (!statusDetailRP.getUser_id().equals(method.userId())) {
                                                switch (statusType) {
                                                    case "image":
                                                        if (statusDetailRP.isLike_image_status_ad()) {
                                                            method.VideoAdDialog("like", "");
                                                        } else {
                                                            likeStatus();
                                                        }
                                                        break;
                                                    case "gif":
                                                        if (statusDetailRP.isLike_gif_points_status_ad()) {
                                                            method.VideoAdDialog("like", "");
                                                        } else {
                                                            likeStatus();
                                                        }
                                                        break;
                                                    case "quote":
                                                        if (statusDetailRP.isLike_quotes_status_ad()) {
                                                            method.VideoAdDialog("like", "");
                                                        } else {
                                                            likeStatus();
                                                        }
                                                        break;
                                                    default:
                                                        if (statusDetailRP.isLike_video_status_ad()) {
                                                            if (Constant.REWARD_VIDEO_AD_COUNT + 1 == Constant.REWARD_VIDEO_AD_COUNT_SHOW) {
                                                                playerStop();
                                                                viewHide();
                                                            }
                                                            method.VideoAdDialog("like", "");
                                                        } else {
                                                            likeStatus();
                                                        }
                                                        break;
                                                }
                                            } else {
                                                method.alertBox(getResources().getString(R.string.you_have_not_like_video));
                                            }
                                        } else {
                                            viewHide();
                                            playerStop();
                                            Method.loginBack = true;
                                            startActivity(new Intent(getActivity(), Login.class));
                                        }
                                    });

                                    imageViewFav.setOnClickListener(v -> {
                                        imageViewFav.startAnimation(myAnim);
                                        if (method.isLogin()) {
                                            FavouriteIF favouriteIF = (isFavourite, message) -> {
                                                if (!isFavourite.equals("")) {
                                                    if (isFavourite.equals("true")) {
                                                        imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_hov));
                                                    } else {
                                                        if (method.isDarkMode()) {
                                                            imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.fav_white_ic));
                                                        } else {
                                                            imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav));
                                                        }
                                                    }
                                                }
                                                Events.FavouriteNotify homeNotify = new Events.FavouriteNotify(statusDetailRP.getId(), statusDetailRP.getStatus_layout(), isFavourite, statusDetailRP.getStatus_type());
                                                GlobalBus.getBus().post(homeNotify);
                                            };
                                            method.addToFav(statusDetailRP.getId(), method.userId(), statusDetailRP.getStatus_type(), favouriteIF);
                                        } else {
                                            Method.loginBack = true;
                                            startActivity(new Intent(getActivity(), Login.class));
                                        }
                                    });

                                    imageViewMore.setOnClickListener(v -> {
                                        viewHide();
                                        playerStop();
                                        imageViewMore.startAnimation(myAnim);

                                        String url = "";

                                        if (statusType.equals("image") || statusType.equals("gif")) {
                                            url = statusDetailRP.getStatus_thumbnail_b();
                                        } else if (statusType.equals("video")) {
                                            url = statusDetailRP.getVideo_url();
                                        } else {
                                            url = statusDetailRP.getStatus_title();
                                        }

                                        BottomSheetDialogFragment bottomSheetDialogFragment = new ShareOption();
                                        Bundle args = new Bundle();
                                        args.putString("id", statusDetailRP.getId());
                                        args.putString("url", url);//status type is quote. then send title in url.
                                        args.putString("status_type", statusType);
                                        args.putString("status_title", statusDetailRP.getStatus_title());
                                        args.putString("status_v_url", statusDetailRP.getVideo_url());
                                        args.putString("status_v_image", statusDetailRP.getStatus_thumbnail_b());
                                        bottomSheetDialogFragment.setArguments(args);
                                        bottomSheetDialogFragment.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");

                                    });

                                    conUser.setOnClickListener(v -> {
                                        viewHide();
                                        playerStop();
                                        ProfileFragment profileFragment = new ProfileFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("type", "other_user");
                                        bundle.putString("id", statusDetailRP.getUser_id());
                                        profileFragment.setArguments(bundle);
                                        getActivity().getSupportFragmentManager().beginTransaction()
                                                .add(R.id.frameLayout_main, profileFragment, getResources().getString(R.string.profile))
                                                .addToBackStack(getResources().getString(R.string.profile)).commitAllowingStateLoss();
                                    });

                                    conComment.setOnClickListener(v -> {
                                        playerStop();
                                        viewHide();
                                        CommentFragment commentFragment = new CommentFragment();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("postId", statusDetailRP.getId());
                                        bundle.putString("type", statusDetailRP.getStatus_type());
                                        commentFragment.setArguments(bundle);
                                        getActivity().getSupportFragmentManager().beginTransaction()
                                                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down)
                                                .add(R.id.frameLayout_main, commentFragment, getResources().getString(R.string.comment))
                                                .addToBackStack(getResources().getString(R.string.comment)).commitAllowingStateLoss();
                                    });

                                    conMain.setVisibility(View.VISIBLE);

                                } else {
                                    conNoData.setVisibility(View.VISIBLE);
                                    method.alertBox(statusDetailRP.getMsg());
                                }

                            } else if (statusDetailRP.getStatus().equals("2")) {
                                method.suspend(statusDetailRP.getMessage());
                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(statusDetailRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                        progressBar.setVisibility(View.GONE);

                    }

                }

                @Override
                public void onFailure(@NotNull Call<StatusDetailRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressBar.setVisibility(View.GONE);
                    conNoData.setVisibility(View.VISIBLE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }

    private void checkLoginFollowing() {

        if (!statusDetailRP.getUser_image().equals("")) {
            Glide.with(requireActivity()).load(statusDetailRP.getUser_image())
                    .placeholder(R.drawable.user_profile).into(imageViewUser);
        }
        textViewUserName.setText(statusDetailRP.getUser_name());
        if (statusDetailRP.getIs_verified().equals("true")) {
            textViewUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verification, 0);
        }
        if (method.isLogin()) {
            if (statusDetailRP.getAlready_follow().equals("true")) {
                textViewFollow.setText(getResources().getString(R.string.unfollow));
            } else {
                textViewFollow.setText(getResources().getString(R.string.follow));
            }
        } else {
            textViewFollow.setText(getResources().getString(R.string.follow));
        }

        textViewFollow.setOnClickListener(v -> {
            if (method.isLogin()) {
                String userId = method.userId();
                assert userId != null;
                if (userId.equals(statusDetailRP.getUser_id())) {
                    method.alertBox(getResources().getString(R.string.you_have_not_onFollow));
                } else {
                    follow(userId, statusDetailRP.getUser_id());
                }
            } else {
                viewHide();
                playerStop();
                Method.loginBack = true;
                startActivity(new Intent(getActivity(), Login.class));
            }
        });
    }

    //------------------ play video ---------------------//

    private void playVideo() {

        playerView.setVisibility(View.VISIBLE);
        imageViewPlay.setVisibility(View.GONE);
        progressBarPlayer.setVisibility(View.VISIBLE);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(getActivity());
        player = new SimpleExoPlayer.Builder(requireActivity())
                .setTrackSelector(trackSelector)
                .build();
        playerView.setPlayer(player);

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(requireActivity(),
                Util.getUserAgent(requireActivity(), getResources().getString(R.string.app_name)));
        // This is the MediaSource representing the media to be played.
        MediaItem mediaItem = MediaItem.fromUri(statusDetailRP.getVideo_url());
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem);
        player.setMediaSource(mediaSource);
        // Prepare the player with the source.
        player.prepare();
        player.setPlayWhenReady(true);
        player.addListener(new Player.EventListener() {

            @Override
            public void onIsPlayingChanged(boolean playWhenReady) {
                if (playWhenReady) {
                    progressBarPlayer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED) {
                    if (isView) {
                        isView = false;
                        if (method.isLogin()) {
                            if (!method.userId().equals(statusDetailRP.getUser_id())) {
                                statusView(method.userId());
                            }
                        }
                    }
                }
            }
        });
    }

    //------------------ play video ---------------------//

    private void updateData() {
        if (passId.equals(statusDetailRP.getId())) {

            Events.InfoUpdate infoUpdate = new Events.InfoUpdate(statusDetailRP.getId(),
                    "all", statusDetailRP.getStatus_layout(), statusDetailRP.getStatus_type(),
                    statusDetailRP.getTotal_viewer(), statusDetailRP.getTotal_likes(),
                    statusDetailRP.getAlready_like(), position);
            GlobalBus.getBus().post(infoUpdate);

        }
    }

    private void downloadStatus() {

        if (method.isNetworkAvailable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Dexter.withContext(requireActivity())
                        .withPermission(Manifest.permission.POST_NOTIFICATIONS)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                if (Method.isDownload) {
                                    method.download(statusDetailRP.getId(),
                                            statusDetailRP.getStatus_title(),
                                            statusDetailRP.getCategory_name(),
                                            statusDetailRP.getStatus_thumbnail_s(),
                                            statusDetailRP.getStatus_thumbnail_b(),
                                            statusDetailRP.getVideo_url(),
                                            statusDetailRP.getStatus_layout(),
                                            statusDetailRP.getStatus_type(),
                                            statusDetailRP.getWatermark_image(),
                                            statusDetailRP.getWatermark_on_off());
                                    if (method.isLogin()) {
                                        if (!method.userId().equals(statusDetailRP.getUser_id())) {
                                            downloadCount(method.userId());
                                        }
                                    }
                                } else {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.download_later), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                // check for permanent denial of permission
                                Toast.makeText(requireActivity(), getString(R.string.download_permission), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();

            } else {
                if (Method.isDownload) {
                    method.download(statusDetailRP.getId(),
                            statusDetailRP.getStatus_title(),
                            statusDetailRP.getCategory_name(),
                            statusDetailRP.getStatus_thumbnail_s(),
                            statusDetailRP.getStatus_thumbnail_b(),
                            statusDetailRP.getVideo_url(),
                            statusDetailRP.getStatus_layout(),
                            statusDetailRP.getStatus_type(),
                            statusDetailRP.getWatermark_image(),
                            statusDetailRP.getWatermark_on_off());
                    if (method.isLogin()) {
                        if (!method.userId().equals(statusDetailRP.getUser_id())) {
                            downloadCount(method.userId());
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.download_later), Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    private void likeStatus() {

        if (getActivity() != null) {

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "user_status_like");
            jsObj.addProperty("user_id", method.userId());
            jsObj.addProperty("post_id", statusDetailRP.getId());
            jsObj.addProperty("type", statusDetailRP.getStatus_type());
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<StatusLikeRP> call = apiService.statusLike(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<StatusLikeRP>() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onResponse(@NotNull Call<StatusLikeRP> call, @NotNull Response<StatusLikeRP> response) {

                    if (getActivity() != null) {

                        try {
                            StatusLikeRP statusLikeRP = response.body();
                            assert statusLikeRP != null;

                            if (statusLikeRP.getStatus().equals("1")) {

                                if (statusLikeRP.getSuccess().equals("1")) {

                                    if (statusLikeRP.getActivity_status().equals("1")) {
                                        statusDetailRP.setTotal_likes(statusLikeRP.getTotal_likes());
                                        statusDetailRP.setAlready_like("true");
                                        textViewLike.setText(method.format(Double.parseDouble(statusDetailRP.getTotal_likes())));
                                    } else {
                                        statusDetailRP.setTotal_likes(statusLikeRP.getTotal_likes());
                                        statusDetailRP.setAlready_like("false");
                                        textViewLike.setText(method.format(Double.parseDouble(statusDetailRP.getTotal_likes())));
                                    }

                                    Toast.makeText(getActivity(), statusLikeRP.getMsg(), Toast.LENGTH_SHORT).show();

                                    if (passId.equals(statusDetailRP.getId())) {
                                        Events.InfoUpdate infoUpdate = new Events.InfoUpdate(statusDetailRP.getId(),
                                                "like", statusDetailRP.getStatus_layout(), statusDetailRP.getStatus_type(),
                                                "", statusDetailRP.getTotal_likes(), statusDetailRP.getAlready_like(), position);
                                        GlobalBus.getBus().post(infoUpdate);
                                    }

                                    if (statusLikeRP.getActivity_status().equals("1")) {
                                        imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_hov));
                                    } else {
                                        if (method.isDarkMode()) {
                                            imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_white));
                                        } else {
                                            imageViewLike.setImageDrawable(getResources().getDrawable(R.drawable.like_ic));
                                        }
                                    }

                                } else {
                                    method.alertBox(statusLikeRP.getMsg());
                                }

                            } else if (statusLikeRP.getStatus().equals("2")) {
                                method.suspend(statusLikeRP.getMessage());
                            } else {
                                method.alertBox(statusLikeRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                        progressDialog.dismiss();

                    }

                }

                @Override
                public void onFailure(@NotNull Call<StatusLikeRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

    private void statusView(String profileId) {

        if (getActivity() != null) {

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("post_id", statusDetailRP.getId());
            jsObj.addProperty("user_id", profileId);
            jsObj.addProperty("owner_id", statusDetailRP.getUser_id());
            jsObj.addProperty("type", statusDetailRP.getStatus_type());
            jsObj.addProperty("method_name", "single_status_view_count");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataRP> call = apiService.statusView(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<DataRP>() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                    if (getActivity() != null) {

                        try {
                            DataRP dataRP = response.body();
                            assert dataRP != null;

                            if (dataRP.getStatus().equals("1")) {

                                if (dataRP.getSuccess().equals("1")) {
                                    int totalView = Integer.parseInt(statusDetailRP.getTotal_viewer());
                                    totalView++;
                                    statusDetailRP.setTotal_viewer(String.valueOf(totalView));
                                    textViewView.setText(method.format(Double.parseDouble(String.valueOf(totalView))));

                                    if (passId.equals(statusDetailRP.getId())) {

                                        Events.InfoUpdate infoUpdate = new Events.InfoUpdate(statusDetailRP.getId(),
                                                "view", statusDetailRP.getStatus_layout(), statusDetailRP.getStatus_type(),
                                                statusDetailRP.getTotal_viewer(), "", "", position);
                                        GlobalBus.getBus().post(infoUpdate);
                                    }
                                } else {
                                    method.alertBox(dataRP.getMsg());
                                }

                            } else if (dataRP.getStatus().equals("2")) {
                                method.suspend(dataRP.getMessage());
                            } else {
                                method.alertBox(dataRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                        progressDialog.dismiss();

                    }

                }

                @Override
                public void onFailure(@NotNull Call<DataRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

    private void downloadCount(String profileId) {

        if (getActivity() != null) {

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "single_status_download");
            jsObj.addProperty("user_id", profileId);
            jsObj.addProperty("post_id", statusDetailRP.getId());
            jsObj.addProperty("type", statusDetailRP.getStatus_type());
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<StatusDownloadRP> call = apiService.statusDownloadCount(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<StatusDownloadRP>() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onResponse(@NotNull Call<StatusDownloadRP> call, @NotNull Response<StatusDownloadRP> response) {

                    if (getActivity() != null) {

                        try {
                            StatusDownloadRP statusDownloadRP = response.body();
                            assert statusDownloadRP != null;

                            if (statusDownloadRP.getStatus().equals("1")) {

                                if (statusDownloadRP.getSuccess().equals("1")) {

                                    Events.DownloadUpdate downloadUpdate = new Events.DownloadUpdate(statusDetailRP.getId(),
                                            statusDetailRP.getStatus_type(), statusDownloadRP.getTotal_download());
                                    GlobalBus.getBus().post(downloadUpdate);

                                } else {
                                    method.alertBox(statusDownloadRP.getMsg());
                                }

                            } else if (statusDownloadRP.getStatus().equals("2")) {
                                method.suspend(statusDownloadRP.getMessage());
                            } else {
                                method.alertBox(statusDownloadRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                        progressDialog.dismiss();

                    }

                }

                @Override
                public void onFailure(@NotNull Call<StatusDownloadRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

    private void follow(String userId, String otherUser) {

        if (getActivity() != null) {

            progressDialog.show();
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", otherUser);
            jsObj.addProperty("follower_id", userId);
            jsObj.addProperty("method_name", "user_follow");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<UserFollowStatusRP> call = apiService.getUserFollowStatus(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<UserFollowStatusRP>() {
                @Override
                public void onResponse(@NotNull Call<UserFollowStatusRP> call, @NotNull Response<UserFollowStatusRP> response) {

                    if (getActivity() != null) {

                        try {
                            UserFollowStatusRP userFollowStatusRP = response.body();
                            assert userFollowStatusRP != null;

                            if (userFollowStatusRP.getStatus().equals("1")) {

                                if (userFollowStatusRP.getSuccess().equals("1")) {

                                    if (userFollowStatusRP.getActivity_status().equals("1")) {
                                        textViewFollow.setText(getResources().getString(R.string.unfollow));
                                    } else {
                                        textViewFollow.setText(getResources().getString(R.string.follow));
                                    }
                                }
                                method.alertBox(userFollowStatusRP.getMsg());

                            } else if (userFollowStatusRP.getStatus().equals("2")) {
                                method.suspend(userFollowStatusRP.getMessage());
                            } else {
                                method.alertBox(userFollowStatusRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressDialog.dismiss();

                }


                @Override
                public void onFailure(@NotNull Call<UserFollowStatusRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

    private void related(String id, String catId) {

        if (getActivity() != null) {

            String userId = "0";
            if (method.isLogin()) {
                userId = method.userId();
            }

            if (subCategoryAdapter == null) {
                relatedLists.clear();
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("post_id", id);
            jsObj.addProperty("cat_id", catId);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("lang_ids", method.getLanguageIds());
            jsObj.addProperty("method_name", "related_status");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<StatusRP> call = apiService.getStatusList(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<StatusRP>() {
                @Override
                public void onResponse(@NotNull Call<StatusRP> call, @NotNull Response<StatusRP> response) {

                    if (getActivity() != null) {

                        try {
                            StatusRP statusRP = response.body();
                            assert statusRP != null;

                            if (statusRP.getStatus().equals("1")) {

                                if (statusRP.getSubCategoryLists().size() == 0) {
                                    if (subCategoryAdapter != null) {
                                        subCategoryAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    if (statusRP.getSubCategoryLists().size() != 0) {
                                        for (int i = 0; i < statusRP.getSubCategoryLists().size(); i++) {
                                            if (Constant.appRP.isNativ_ad()) {
                                                if (j % Integer.parseInt(Constant.appRP.getNativ_ad_position()) == 0) {
                                                    relatedLists.add(null);
                                                    j++;
                                                }
                                            }
                                            relatedLists.add(statusRP.getSubCategoryLists().get(i));
                                            j++;
                                        }
                                    }
                                }

                                if (subCategoryAdapter == null) {
                                    if (relatedLists.size() == 0) {
                                        conRelated.setVisibility(View.GONE);
                                    } else {
                                        subCategoryAdapter = new SubCategoryAdapter(getActivity(), relatedLists, onClick, "related");
                                        recyclerView.setAdapter(subCategoryAdapter);
                                    }
                                } else {
                                    subCategoryAdapter.notifyItemMoved(oldPosition, relatedLists.size());
                                }

                            } else if (statusRP.getStatus().equals("2")) {
                                method.suspend(statusRP.getMessage());
                            } else {
                                method.alertBox(statusRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NotNull Call<StatusRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister the registered event.
        GlobalBus.getBus().unregister(this);
    }

}
