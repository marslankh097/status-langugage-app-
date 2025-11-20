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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.activity.MainActivity;
import com.example.status.adapter.CategoryAdapter;
import com.example.status.interfaces.OnClick;
import com.example.status.item.CategoryList;
import com.example.status.response.CategoryRP;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    String type;
    private ProgressBar progressBar;
    private ConstraintLayout conNoData;
    private RecyclerView recyclerView;
    private List<CategoryList> categoryLists;
    private CategoryAdapter categoryAdapter;
    private LayoutAnimationController animation;
    private Boolean isOver = false;
    private int paginationIndex = 1;
    int j = 1;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_fragment, container, false);

        categoryLists = new ArrayList<>();

        assert getArguments() != null;
        type = getArguments().getString("type");
        assert type != null;
        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.category));
        }

        if (type.equals("home_category")) {
            Events.Select select = new Events.Select("");
            GlobalBus.getBus().post(select);
        }

        onClick = (position, title, type, status_type, id, tag) -> {
            SubCategoryFragment subCategoryFragment = new SubCategoryFragment();
            Bundle bundle = new Bundle();
            bundle.putString("id", id);
            bundle.putString("category_name", title);
            bundle.putString("type", type);
            subCategoryFragment.setArguments(bundle);
            requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, subCategoryFragment, title).addToBackStack(title).commitAllowingStateLoss();
        };
        method = new Method(getActivity(), onClick);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        progressBar = view.findViewById(R.id.progressbar_category);
        conNoData = view.findViewById(R.id.con_noDataFound);
        recyclerView = view.findViewById(R.id.recyclerView_category);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (categoryAdapter.getItemViewType(position) == 1) {
                    return 1;
                } else {
                    return 3;
                }
            }
        });
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
                    categoryAdapter.hideHeader();
                }
            }
        });

        callData();

        setHasOptionsMenu(true);
        return view;
    }

    private void callData() {
        if (method.isNetworkAvailable()) {
            Category();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
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

    private void Category() {

        if (getActivity() != null) {

            if (categoryAdapter == null) {
                categoryLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("method_name", "cat_list");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<CategoryRP> call = apiService.getCategory(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<CategoryRP>() {
                @Override
                public void onResponse(@NotNull Call<CategoryRP> call, @NotNull Response<CategoryRP> response) {

                    if (getActivity() != null) {

                        try {
                            CategoryRP categoryRP = response.body();
                            assert categoryRP != null;

                            if (categoryRP.getStatus().equals("1")) {

                                if (categoryRP.getCategoryLists().size() == 0) {
                                    if (categoryAdapter != null) {
                                        categoryAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    if (categoryRP.getCategoryLists().size() != 0) {
                                        for (int i = 0; i < categoryRP.getCategoryLists().size(); i++) {
                                            if (Constant.appRP.isNativ_ad()) {
                                                if (j % Integer.parseInt(Constant.appRP.getNativ_ad_position()) == 0) {
                                                    categoryLists.add(null);
                                                    j++;
                                                }
                                            }
                                            categoryLists.add(categoryRP.getCategoryLists().get(i));
                                            j++;
                                        }
                                    }
                                }

                                if (categoryAdapter == null) {
                                    if (categoryLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        categoryAdapter = new CategoryAdapter(getActivity(), categoryLists, "category", onClick);
                                        recyclerView.setAdapter(categoryAdapter);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    categoryAdapter.notifyDataSetChanged();
                                }

                            } else {
                                method.alertBox(categoryRP.getMessage());
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
                public void onFailure(@NotNull Call<CategoryRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }
}
