package com.example.status.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.activity.MainActivity;
import com.example.status.adapter.UserFollowAdapter;
import com.example.status.interfaces.OnClick;
import com.example.status.item.UserFollowList;
import com.example.status.response.UserFollowRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.EndlessRecyclerViewScrollListener;
import com.example.status.util.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserFollowFragment extends Fragment {

    private OnClick onClick;
    private Method method;
    private String type, userId, search;
    private List<UserFollowList> userFollowLists;
    private ProgressBar progressBar;
    private ConstraintLayout conNoData;
    private RecyclerView recyclerView;
    private UserFollowAdapter userFollowAdapter;
    private LayoutAnimationController animation;
    private Boolean isOver = false;
    private int paginationIndex = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.sub_cat_fragment, container, false);

        assert getArguments() != null;
        type = getArguments().getString("type");
        userId = getArguments().getString("user_id");
        search = getArguments().getString("search");

        if (MainActivity.toolbar != null) {
            if (type.equals("follower")) {
                MainActivity.toolbar.setTitle(getResources().getString(R.string.followers));
            } else if (type.equals("following")) {
                MainActivity.toolbar.setTitle(getResources().getString(R.string.following));
            } else {
                MainActivity.toolbar.setTitle(search);
            }
        }

        userFollowLists = new ArrayList<>();

        onClick = (position, title, type, status_type, id, tag) -> {
            if (getActivity() != null) {
                ProfileFragment profileFragment = new ProfileFragment();
                Bundle bundleProfile = new Bundle();
                bundleProfile.putString("type", "other_user");
                bundleProfile.putString("id", id);
                profileFragment.setArguments(bundleProfile);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, profileFragment, getResources().getString(R.string.profile)).addToBackStack(getResources().getString(R.string.profile)).commitAllowingStateLoss();
            } else {
                method.alertBox(getResources().getString(R.string.wrong));
            }
        };
        method = new Method(getActivity(), onClick);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        conNoData = view.findViewById(R.id.con_noDataFound);
        progressBar = view.findViewById(R.id.progressbar_sub_category);
        recyclerView = view.findViewById(R.id.recyclerView_sub_category);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
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
                    userFollowAdapter.hideHeader();
                }
            }
        });

        callData();

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.ic_searchView);
        searchItem.setVisible(method.isLogin());
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener((new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (method.isNetworkAvailable()) {
                    if (method.isLogin()) {
                        UserFollowFragment userFollowFragment = new UserFollowFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "search_user");
                        bundle.putString("user_id", method.userId());
                        bundle.putString("search", query);
                        userFollowFragment.setArguments(bundle);
                        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, userFollowFragment, query).addToBackStack(query).commitAllowingStateLoss();
                    } else {
                        method.alertBox(getResources().getString(R.string.you_have_not_login));
                    }
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

    public void callData() {
        if (method.isNetworkAvailable()) {
            userFollow(userId);
        } else {
            progressBar.setVisibility(View.GONE);
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    private void userFollow(String userId) {

        if (getActivity() != null) {

            if (userFollowAdapter == null) {
                userFollowLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            if (type.equals("follower")) {
                jsObj.addProperty("method_name", "user_followers");
            } else if (type.equals("following")) {
                jsObj.addProperty("method_name", "user_following");
            } else {
                jsObj.addProperty("search_keyword", search);
                jsObj.addProperty("method_name", "user_search");
            }
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("page", paginationIndex);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<UserFollowRP> call = apiService.getUserFollow(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<UserFollowRP>() {
                @Override
                public void onResponse(@NotNull Call<UserFollowRP> call, @NotNull Response<UserFollowRP> response) {

                    if (getActivity() != null) {

                        try {
                            UserFollowRP userFollowRP = response.body();
                            assert userFollowRP != null;

                            if (userFollowRP.getStatus().equals("1")) {

                                if (userFollowRP.getUserFollowLists().size() == 0) {
                                    if (userFollowAdapter != null) {
                                        userFollowAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    userFollowLists.addAll(userFollowRP.getUserFollowLists());
                                }

                                if (userFollowAdapter == null) {
                                    if (userFollowLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        userFollowAdapter = new UserFollowAdapter(getActivity(), userFollowLists, "follow_following", onClick);
                                        recyclerView.setAdapter(userFollowAdapter);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    userFollowAdapter.notifyDataSetChanged();
                                }

                            } else if (userFollowRP.getStatus().equals("2")) {
                                method.suspend(userFollowRP.getMessage());
                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(userFollowRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NotNull Call<UserFollowRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    conNoData.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

}
