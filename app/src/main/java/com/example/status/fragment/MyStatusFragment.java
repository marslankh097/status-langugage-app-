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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.adapter.MyStatusAdapter;
import com.example.status.interfaces.OnClick;
import com.example.status.item.SubCategoryList;
import com.example.status.response.StatusRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
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


public class MyStatusFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private ProgressBar progressBar;
    private ConstraintLayout conNoData;
    private RecyclerView recyclerView;
    private MyStatusAdapter myStatusAdapter;
    private List<SubCategoryList> myVideoLists;
    private LayoutAnimationController animation;
    private Boolean isOver = false;
    private int paginationIndex = 1;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.my_upload_fragment, container, false);

        GlobalBus.getBus().register(this);

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

        myVideoLists = new ArrayList<>();

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        conNoData = view.findViewById(R.id.con_noDataFound);
        progressBar = view.findViewById(R.id.progressbar_myUpload_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_myUpload_fragment);

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
                    myStatusAdapter.hideHeader();
                }
            }
        });

        callData();

        return view;

    }

    private void callData() {
        if (getActivity() != null) {
            if (method.isNetworkAvailable()) {
                if (getActivity() != null) {
                    MyVideo(method.userId());
                }
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        }
    }

    @Subscribe
    public void getMessage(Events.InfoUpdate infoUpdate) {
        if (myStatusAdapter != null) {
            for (int i = 0; i < myVideoLists.size(); i++) {
                if (myVideoLists.get(i).getId().equals(infoUpdate.getId())) {
                    if (myVideoLists.get(i).getStatus_type().equals(infoUpdate.getStatus_type())) {
                        switch (infoUpdate.getType()) {
                            case "all":
                                myVideoLists.get(i).setTotal_viewer(infoUpdate.getView());
                                myVideoLists.get(i).setTotal_likes(infoUpdate.getTotal_like());
                                myVideoLists.get(i).setAlready_like(infoUpdate.getAlready_like());
                                break;
                            case "view":
                                myVideoLists.get(i).setTotal_viewer(infoUpdate.getView());
                                break;
                            case "like":
                                myVideoLists.get(i).setTotal_likes(infoUpdate.getTotal_like());
                                myVideoLists.get(i).setAlready_like(infoUpdate.getAlready_like());
                                break;
                        }
                        myStatusAdapter.notifyItemChanged(i);
                    }
                }
            }
        }
    }

    private void MyVideo(final String id) {

        if (getActivity() != null) {

            if (myStatusAdapter == null) {
                myVideoLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", id);
            if (method.isLogin()) {
                if (id.equals(method.userId())) {
                    jsObj.addProperty("login_user", "true");
                } else {
                    jsObj.addProperty("login_user", "false");
                }
            } else {
                jsObj.addProperty("login_user", "false");
            }
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("method_name", "user_status_list");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<StatusRP> call = apiService.getUserStatusList(API.toBase64(jsObj.toString()));
            Log.e("dataa",""+API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<StatusRP>() {
                @Override
                public void onResponse(@NotNull Call<StatusRP> call, @NotNull Response<StatusRP> response) {

                    if (getActivity() != null) {

                        try {
                            StatusRP statusRP = response.body();
                            assert statusRP != null;

                            if (statusRP.getStatus().equals("1")) {

                                if (statusRP.getSubCategoryLists().size() == 0) {
                                    if (myStatusAdapter != null) {
                                        myStatusAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    myVideoLists.addAll(statusRP.getSubCategoryLists());
                                }

                                if (myStatusAdapter == null) {
                                    if (myVideoLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        myStatusAdapter = new MyStatusAdapter(getActivity(), myVideoLists, id, "my_video", onClick);
                                        recyclerView.setAdapter(myStatusAdapter);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    myStatusAdapter.notifyDataSetChanged();
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
