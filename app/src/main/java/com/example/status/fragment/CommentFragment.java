package com.example.status.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.activity.Login;
import com.example.status.adapter.CommentAdapter;
import com.example.status.item.CommentList;
import com.example.status.response.CommentRP;
import com.example.status.response.UserCommentRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.EndlessRecyclerViewScrollListener;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CommentFragment extends Fragment {

    private Method method;
    private String postId, type;
    private ProgressBar progressBar;
    ImageView imageViewClose;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private TextInputEditText editTextComment;
    MaterialTextView textViewPost;
    private List<CommentList> commentLists;
    private InputMethodManager inputMethodManager;
    ConstraintLayout conNoData, conMain;
    private Boolean isOver = false;
    private int paginationIndex = 1;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.comment_fragment, container, false);
        GlobalBus.getBus().register(this);
        method = new Method(getActivity());

        commentLists = new ArrayList<>();

        postId = getArguments().getString("postId");
        type = getArguments().getString("type");

        inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        conNoData = view.findViewById(R.id.con_noDataFound);
        conMain = view.findViewById(R.id.con_main_comment);
        progressBar = view.findViewById(R.id.progressbar_comment);
        imageViewClose = view.findViewById(R.id.imageView_close_comment);
        editTextComment = view.findViewById(R.id.editText_comment);
        textViewPost = view.findViewById(R.id.textView_post_comment);
        ImageView imageView = view.findViewById(R.id.imageView_comment);
        recyclerView = view.findViewById(R.id.recyclerView_comment);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        conMain.setOnClickListener(v ->{});

        if (method.isLogin()) {
            String image = method.pref.getString(method.userImage, null);
            if (image != null && !image.equals("")) {
                Glide.with(getActivity()).load(image)
                        .placeholder(R.drawable.user_profile)
                        .into(imageView);
            }
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        paginationIndex++;
                        callData();
                    }, 1000);
                } else {
                    commentAdapter.hideHeader();
                }
            }
        });

        imageViewClose.setOnClickListener(v ->
                getActivity().getSupportFragmentManager().popBackStack());

        textViewPost.setOnClickListener(v -> {

            if (method.isLogin()) {

                editTextComment.setError(null);
                String comment = Objects.requireNonNull(editTextComment.getText()).toString();

                if (comment.equals("") || comment.isEmpty()) {
                    editTextComment.requestFocus();
                    editTextComment.setError(getResources().getString(R.string.please_enter_comment));
                } else {
                    if (method.isNetworkAvailable()) {
                        editTextComment.clearFocus();
                        inputMethodManager.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);
                        submitComment(method.userId(), comment, postId, type);
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.internet_connection), Toast.LENGTH_SHORT).show();
                    }
                }

            } else {
                Method.loginBack = true;
                startActivity(new Intent(getActivity(), Login.class));
            }
        });

        callData();

        return view;

    }

    private void callData() {
        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                getComment(postId, method.userId(), type);
            } else {
                getComment(postId, "0", type);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    public void getComment(String postId, String userId, String type) {

        if (getActivity() != null) {

            if (commentAdapter == null) {
                commentLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("post_id", postId);
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("type", type);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("method_name", "get_all_comments");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<CommentRP> call = apiService.getAllComment(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<CommentRP>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NotNull Call<CommentRP> call, @NotNull Response<CommentRP> response) {

                    if (getActivity() != null) {

                        try {
                            CommentRP commentRP = response.body();
                            assert commentRP != null;

                            if (commentRP.getStatus().equals("1")) {

                                if (commentRP.getCommentLists().size() == 0) {
                                    if (commentAdapter != null) {
                                        commentAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    commentLists.addAll(commentRP.getCommentLists());
                                }

                                if (commentAdapter == null) {
                                    if (commentLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        commentAdapter = new CommentAdapter(getActivity(), commentLists);
                                        recyclerView.setAdapter(commentAdapter);
                                    }
                                } else {
                                    commentAdapter.notifyDataSetChanged();
                                }

                            } else if (commentRP.getStatus().equals("2")) {
                                method.suspend(commentRP.getMessage());
                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(commentRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NotNull Call<CommentRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });
        }

    }

    public void submitComment(final String userId, final String comment, String postId, String type) {

        if (getActivity() != null) {

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("post_id", postId);
            jsObj.addProperty("type", type);
            jsObj.addProperty("comment_text", comment);
            jsObj.addProperty("method_name", "add_status_comment");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<UserCommentRP> call = apiService.submitComment(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<UserCommentRP>() {
                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onResponse(@NotNull Call<UserCommentRP> call, @NotNull Response<UserCommentRP> response) {

                    if (getActivity() != null) {

                        try {
                            UserCommentRP userCommentRP = response.body();
                            assert userCommentRP != null;

                            if (userCommentRP.getStatus().equals("1")) {

                                if (userCommentRP.getSuccess().equals("1")) {

                                    conNoData.setVisibility(View.GONE);
                                    editTextComment.setText("");

                                    commentLists.add(0, new CommentList(userCommentRP.getComment_id(), userCommentRP.getUser_id(), userCommentRP.getUser_name(), userCommentRP.getUser_image(), postId, userCommentRP.getStatus_type(), userCommentRP.getComment_text(), userCommentRP.getComment_date()));

                                    Events.Comment comment = new Events.Comment(userCommentRP.getComment_status(), "", userCommentRP.getComment_id(),
                                            userCommentRP.getUser_id(), userCommentRP.getUser_name(), userCommentRP.getUser_image(), userCommentRP.getPost_id(),
                                            userCommentRP.getStatus_type(), userCommentRP.getComment_text(), userCommentRP.getComment_date(), userCommentRP.getTotal_comment(), "add");
                                    GlobalBus.getBus().post(comment);

                                    if (commentAdapter == null) {
                                        commentAdapter = new CommentAdapter(getActivity(), commentLists);
                                        recyclerView.setAdapter(commentAdapter);
                                    } else {
                                        commentAdapter.notifyDataSetChanged();
                                    }

                                    Toast.makeText(getActivity(), userCommentRP.getMsg(), Toast.LENGTH_SHORT).show();

                                }
                            } else if (userCommentRP.getStatus().equals("2")) {
                                method.suspend(userCommentRP.getMessage());
                            } else {
                                method.alertBox(userCommentRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                }

                @Override
                public void onFailure(@NotNull Call<UserCommentRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
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

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    public void getData(Events.Login login) {
            commentAdapter.notifyDataSetChanged();
    }
}
