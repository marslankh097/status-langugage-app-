package com.example.status.fragment;

import android.annotation.SuppressLint;
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
import com.example.status.adapter.SubCategoryAdapter;
import com.example.status.interfaces.OnClick;
import com.example.status.item.SubCategoryList;
import com.example.status.response.StatusRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Constant;
import com.example.status.util.EndlessRecyclerViewScrollListener;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubCategoryFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private String id;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ConstraintLayout conNoData;
    private List<SubCategoryList> subCategoryLists;
    private SubCategoryAdapter subCategoryAdapter;
    private LayoutAnimationController animation;
    private Boolean isOver = false;
    private int paginationIndex = 1;
    int j = 1;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.sub_cat_fragment, container, false);

        GlobalBus.getBus().register(this);

        assert getArguments() != null;
        String type1 = getArguments().getString("type");
        id = getArguments().getString("id");
        String categoryName = getArguments().getString("category_name");

        assert type1 != null;
        if (type1.equals("category")) {
            if (MainActivity.toolbar != null) {
                MainActivity.toolbar.setTitle(categoryName);
            }
        }

        subCategoryLists = new ArrayList<>();

        onClick = (position, title, type, status_type, id, tag) -> {
            SCDetailFragment scDetailFragment = new SCDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("id", id);
            bundle.putString("type", type);
            bundle.putString("status_type", status_type);
            bundle.putInt("position", position);
            scDetailFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, scDetailFragment, title).addToBackStack(title).commitAllowingStateLoss();
        };
        method = new Method(getActivity(), onClick);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        conNoData = view.findViewById(R.id.con_noDataFound);
        progressBar = view.findViewById(R.id.progressbar_sub_category);
        recyclerView = view.findViewById(R.id.recyclerView_sub_category);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
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
                    subCategoryAdapter.hideHeader();
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
                    getActivity().getSupportFragmentManager()
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
        for (int i = 0; i < getActivity().getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Subscribe
    public void getNotify(Events.FavouriteNotify favouriteNotify) {
        for (int i = 0; i < subCategoryLists.size(); i++) {
            if (subCategoryLists.get(i).getId().equals(favouriteNotify.getId())) {
                if (subCategoryLists.get(i).getStatus_type().equals(favouriteNotify.getStatus_type())) {
                    subCategoryLists.get(i).setIs_favourite(favouriteNotify.getIs_favourite());
                    subCategoryAdapter.notifyItemChanged(i);
                }
            }
        }
    }

    @Subscribe
    public void getNotify(Events.InfoUpdate infoUpdate) {
        if (subCategoryAdapter != null) {
            for (int i = 0; i < subCategoryLists.size(); i++) {
                if (subCategoryLists.get(i).getId().equals(infoUpdate.getId())) {
                    if (subCategoryLists.get(i).getStatus_type().equals(infoUpdate.getStatus_type())) {
                        switch (infoUpdate.getType()) {
                            case "all":
                                subCategoryLists.get(i).setTotal_viewer(infoUpdate.getView());
                                subCategoryLists.get(i).setTotal_likes(infoUpdate.getTotal_like());
                                subCategoryLists.get(i).setAlready_like(infoUpdate.getAlready_like());
                                break;
                            case "view":
                                subCategoryLists.get(i).setTotal_viewer(infoUpdate.getView());
                                break;
                            case "like":
                                subCategoryLists.get(i).setTotal_likes(infoUpdate.getTotal_like());
                                subCategoryLists.get(i).setAlready_like(infoUpdate.getAlready_like());
                                break;
                        }
                        subCategoryAdapter.notifyItemChanged(i);
                    }
                }
            }
        }
    }

    private void callData() {
        if (getActivity() != null) {
            if (method.isNetworkAvailable()) {
                if (method.isLogin()) {
                    subCategory(method.userId());
                } else {
                    subCategory("0");
                }
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        }

    }

    private void subCategory(String userId) {

        if (getActivity() != null) {

            if (subCategoryAdapter == null) {
                subCategoryLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("cat_id", id);
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("lang_ids", method.getLanguageIds());
            jsObj.addProperty("method_name", "status_by_cat_id");
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
                                                    subCategoryLists.add(null);
                                                    j++;
                                                }
                                            }
                                            subCategoryLists.add(statusRP.getSubCategoryLists().get(i));
                                            j++;
                                        }
                                    }
                                }

                                if (subCategoryAdapter == null) {
                                    if (subCategoryLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        subCategoryAdapter = new SubCategoryAdapter(getActivity(), subCategoryLists, onClick, "sub_category");
                                        recyclerView.setAdapter(subCategoryAdapter);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    subCategoryAdapter.notifyDataSetChanged();
                                }

                            } else if (statusRP.getStatus().equals("2")) {
                                method.suspend(statusRP.getMessage());
                            } else {
                                method.alertBox(statusRP.getMessage());
                                conNoData.setVisibility(View.VISIBLE);
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
