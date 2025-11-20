package com.example.status.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.status.R;
import com.example.status.adapter.SliderAdapter;
import com.example.status.adapter.SubCategoryAdapter;
import com.example.status.interfaces.OnClick;
import com.example.status.item.SubCategoryList;
import com.example.status.response.HomeRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Constant;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private ViewPager viewPager;
    private ProgressBar progressBar;
    private NestedScrollView nestedScrollView;
    private RecyclerView recyclerView;
    private SliderAdapter sliderAdapter;
    private List<SubCategoryList> subCategoryLists;
    private SubCategoryAdapter subCategoryAdapter;
    private Boolean isOver = false;
    private int oldPosition = 0, paginationIndex = 1;
    private LayoutAnimationController animation;
    private FloatingActionButton floatingActionButton;
    private ConstraintLayout conNoData, conMain, conSlider, conStatus;

    private Timer timer;
    private Runnable Update;
    private final long DELAY_MS = 600;//delay in milliseconds before task is to be executed
    private final long PERIOD_MS = 3000;
    private final Handler handler = new Handler();
    int j = 1;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.home_fragment, container, false);

        GlobalBus.getBus().register(this);

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

        int columnWidth = method.getScreenWidth();
        subCategoryLists = new ArrayList<>();

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        progressBar = view.findViewById(R.id.progressbar_home);
        conMain = view.findViewById(R.id.con_main_home);
        conNoData = view.findViewById(R.id.con_noDataFound);
        viewPager = view.findViewById(R.id.slider_home);
        recyclerView = view.findViewById(R.id.recyclerView_landscape_home);
        conSlider = view.findViewById(R.id.con_slider_home);
        conStatus = view.findViewById(R.id.con_status_home);
        floatingActionButton = view.findViewById(R.id.fab_home);
        nestedScrollView = view.findViewById(R.id.nestedScrollView_home);

        conMain.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        viewPager.setLayoutParams(new ConstraintLayout.LayoutParams(columnWidth, columnWidth / 2 + 80));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
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

                    if (totalItemCount > 5) {
                        floatingActionButton.show();
                    } else {
                        floatingActionButton.hide();
                    }

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        if (!isOver) {
                            oldPosition = subCategoryLists.size();
                            new Handler().postDelayed(() -> {
                                paginationIndex++;
                                callData();
                            }, 1000);
                        } else {
                            subCategoryAdapter.hideHeader();
                        }
                    }
                }
            }
        });

        floatingActionButton.setOnClickListener(v -> nestedScrollView.scrollTo(0, 0));

        callData();

        return view;
    }

    private void callData() {
        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                home(method.userId());
            } else {
                home("0");
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
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

    private void home(String userId) {

        if (getActivity() != null) {

            if (subCategoryAdapter == null) {
                subCategoryLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("lang_ids", method.getLanguageIds());
            jsObj.addProperty("method_name", "home");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<HomeRP> call = apiService.getHome(API.toBase64(jsObj.toString()));
            Log.e("home",""+API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<HomeRP>() {
                @Override
                public void onResponse(@NotNull Call<HomeRP> call, @NotNull Response<HomeRP> response) {

                    if (getActivity() != null) {

                        try {
                            HomeRP homeRP = response.body();
                            assert homeRP != null;

                            if (homeRP.getStatus().equals("1")) {

                                if (homeRP.getSliderLists().size() != 0) {

                                    sliderAdapter = new SliderAdapter(getActivity(), "slider", homeRP.getSliderLists(), onClick);
                                    viewPager.setAdapter(sliderAdapter);
                                    viewPager.setOffscreenPageLimit(homeRP.getSliderLists().size() - 1);

                                    Update = () -> {
                                        if (viewPager.getCurrentItem() == (sliderAdapter.getCount() - 1)) {
                                            viewPager.setCurrentItem(0, true);
                                        } else {
                                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                                        }
                                    };

                                    if (sliderAdapter.getCount() > 1) {
                                        timer = new Timer(); // This will create a new Thread
                                        timer.schedule(new TimerTask() { // task to be scheduled
                                            @Override
                                            public void run() {
                                                handler.post(Update);
                                            }
                                        }, DELAY_MS, PERIOD_MS);
                                    }

                                } else {
                                    if (sliderAdapter == null) {
                                        conSlider.setVisibility(View.GONE);
                                    }
                                }

                                if (homeRP.getSubCategoryLists().size() == 0) {
                                    if (subCategoryAdapter != null) {
                                        subCategoryAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    for (int i = 0; i < homeRP.getSubCategoryLists().size(); i++) {
                                        if (Constant.appRP.isNativ_ad()) {
                                            if (j % Integer.parseInt(Constant.appRP.getNativ_ad_position()) == 0) {
                                                subCategoryLists.add(null);
                                                j++;
                                            }
                                        }
                                        subCategoryLists.add(homeRP.getSubCategoryLists().get(i));
                                        j++;
                                    }
                                }

                                if (subCategoryAdapter == null) {
                                    if (subCategoryLists.size() != 0) {
                                        subCategoryAdapter = new SubCategoryAdapter(getActivity(), subCategoryLists, onClick, "home_sub");
                                        recyclerView.setAdapter(subCategoryAdapter);
                                        recyclerView.setLayoutAnimation(animation);
                                    } else {
                                        conStatus.setVisibility(View.GONE);
                                    }
                                } else {
                                    subCategoryAdapter.notifyItemMoved(oldPosition, subCategoryLists.size());
                                }

                                conMain.setVisibility(View.VISIBLE);

                            } else if (homeRP.getStatus().equals("2")) {
                                method.suspend(homeRP.getMessage());
                            } else {
                                method.alertBox(homeRP.getMessage());
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
                public void onFailure(@NotNull Call<HomeRP> call, @NotNull Throwable t) {
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
