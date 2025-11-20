package com.example.status.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.activity.MainActivity;
import com.example.status.activity.SettingActivity;
import com.example.status.adapter.HomeCategoryAdapter;
import com.example.status.interfaces.OnClick;
import com.example.status.item.CategoryList;
import com.example.status.response.CategoryRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
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

public class HomeMainFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private List<CategoryList> categoryLists;
    private HomeCategoryAdapter homeCategoryAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.home_main_fragment, container, false);

        GlobalBus.getBus().register(this);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.home));
        }

        categoryLists = new ArrayList<>();

        onClick = (position, title, type, status_type, id, tag) -> {
            if (position == categoryLists.size() - 1) {
                CategoryFragment categoryFragment = new CategoryFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "home_category");
                categoryFragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                assert fragmentManager != null;
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frameLayout_main, categoryFragment, getResources().getString(R.string.category)).commitAllowingStateLoss();
                GlobalBus.getBus().post(new Events.CategoryHome());
            } else {
                SubCategoryFragment subCategoryFragment = new SubCategoryFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", id);
                bundle.putString("category_name", title);
                bundle.putString("type", "home_category");
                subCategoryFragment.setArguments(bundle);
                getChildFragmentManager().beginTransaction().replace(R.id.frameLayout_home_main, subCategoryFragment, title).commitAllowingStateLoss();
            }
        };
        method = new Method(getActivity(), onClick);

        progressBar = view.findViewById(R.id.progressBar_home_main_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_home_main_fragment);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        if (method.isNetworkAvailable()) {
            homeCategory();
            getChildFragmentManager().beginTransaction().replace(R.id.frameLayout_home_main, new HomeFragment(), getResources().getString(R.string.home)).commitAllowingStateLoss();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
            progressBar.setVisibility(View.GONE);
        }

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_lg_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.ic_search_lg);
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

        MenuItem searchLanguage = menu.findItem(R.id.search_language);
        searchLanguage.setOnMenuItemClickListener(item -> {
            startActivity(new Intent(requireActivity(), SettingActivity.class));
            return false;
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void backStackRemove() {
        for (int i = 0; i < getActivity().getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Subscribe
    public void getNotify(Events.Language language) {
        getChildFragmentManager().beginTransaction().replace(R.id.frameLayout_home_main, new HomeFragment(), getResources().getString(R.string.home)).commitAllowingStateLoss();
    }

    private void homeCategory() {

        if (getActivity() != null) {

            categoryLists.clear();
            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "home_cat_list");
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

                                categoryLists.addAll(categoryRP.getCategoryLists());
                                categoryLists.add(categoryLists.size(), new CategoryList("", getResources().getString(R.string.view_all), "no", "no", "", ""));

                                homeCategoryAdapter = new HomeCategoryAdapter(getActivity(), categoryLists, "", onClick);
                                recyclerView.setAdapter(homeCategoryAdapter);

                            } else {
                                method.alertBox(categoryRP.getMessage());
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister the registered event.
        GlobalBus.getBus().unregister(this);
    }

}
