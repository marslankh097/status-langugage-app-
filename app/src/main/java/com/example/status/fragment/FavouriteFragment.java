package com.example.status.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.activity.Login;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FavouriteFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private ProgressBar progressBar;
    private ImageView imageViewData;
    private MaterialButton buttonLogin;
    private MaterialTextView textViewData;
    private ConstraintLayout conNoData;
    private RecyclerView recyclerView;
    private List<SubCategoryList> subCategoryLists;
    private SubCategoryAdapter subCategoryAdapter;
    private LayoutAnimationController animation;
    private Boolean isOver = false;
    private int paginationIndex = 1;
    int j = 1;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.favourite_fragment, container, false);

        GlobalBus.getBus().register(this);

        subCategoryLists = new ArrayList<>();

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        onClick = (position, title, type, status_type, id, tag) -> {
            SCDetailFragment scDetailFragment = new SCDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("id", id);
            bundle.putString("type", type);
            bundle.putString("status_type", status_type);
            bundle.putInt("position", position);
            scDetailFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, scDetailFragment, title).addToBackStack(null).commitAllowingStateLoss();
        };
        method = new Method(getActivity(), onClick);

        conNoData = view.findViewById(R.id.con_not_login);
        progressBar = view.findViewById(R.id.progressbar_fav);
        imageViewData = view.findViewById(R.id.imageView_not_login);
        textViewData = view.findViewById(R.id.textView_not_login);
        buttonLogin = view.findViewById(R.id.button_not_login);
        recyclerView = view.findViewById(R.id.recyclerView_fav);

        progressBar.setVisibility(View.GONE);
        data(false, false);

        buttonLogin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), Login.class));
            getActivity().finishAffinity();
        });

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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void data(boolean isShow, boolean isLogin) {
        if (isShow) {
            if (isLogin) {
                buttonLogin.setVisibility(View.VISIBLE);
                textViewData.setText(getResources().getString(R.string.you_have_not_login));
                imageViewData.setImageDrawable(getResources().getDrawable(R.drawable.no_login));
            } else {
                buttonLogin.setVisibility(View.GONE);
                textViewData.setText(getResources().getString(R.string.no_data_found));
                imageViewData.setImageDrawable(getResources().getDrawable(R.drawable.no_data));
            }
            conNoData.setVisibility(View.VISIBLE);
        } else {
            conNoData.setVisibility(View.GONE);
        }
    }



    private void callData() {
        if (getActivity() != null) {
            if (method.isNetworkAvailable()) {
                if (method.isLogin()) {
                    favourite(method.userId());
                } else {
                    data(true, true);
                }
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        }
    }

    private void favourite(String userId) {

        if (getActivity() != null) {

            if (subCategoryAdapter == null) {
                subCategoryLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("method_name", "get_favourite_status");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<StatusRP> call = apiService.getFavStatusList(API.toBase64(jsObj.toString()));
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
                                        data(true, false);
                                    } else {
                                        subCategoryAdapter = new SubCategoryAdapter(getActivity(), subCategoryLists, onClick, "favorites");
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
                                data(true, false);
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

    @Subscribe
    public void getMessage(Events.InfoUpdate infoUpdate) {
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

    @Subscribe
    public void getNotify(Events.FavouriteNotify favouriteNotify) {
        if (subCategoryAdapter != null) {
            for (int i = 0; i < subCategoryLists.size(); i++) {
                if (subCategoryLists.get(i).getId().equals(favouriteNotify.getId())) {
                    if (subCategoryLists.get(i).getStatus_type().equals(favouriteNotify.getStatus_type())) {
                        subCategoryLists.get(i).setIs_favourite(favouriteNotify.getIs_favourite());
                        subCategoryAdapter.notifyItemChanged(i);
                    }
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister the registered event.
        GlobalBus.getBus().unregister(this);
    }

}
