package com.example.status.fragment;

import android.os.Bundle;
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
import com.example.status.adapter.RewardPointAdapter;
import com.example.status.response.URPListRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Method;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardCurrentFragment extends Fragment {

    private Method method;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ConstraintLayout conNoData;
    private RewardPointAdapter rewardPointAdapter;
    private LayoutAnimationController layoutAnimationController;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.user_rm_fragment, container, false);

        method = new Method(getActivity());

        int resId = R.anim.layout_animation_fall_down;
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        conNoData = view.findViewById(R.id.con_noDataFound);
        progressBar = view.findViewById(R.id.progressbar_user_rm_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_user_rm_fragment);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        if (method.isNetworkAvailable()) {
            rewardPoint(method.userId());
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

        return view;
    }

    private void rewardPoint(String id) {

        if (getActivity() != null) {

            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", id);
            jsObj.addProperty("method_name", "user_rewads_point");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<URPListRP> call = apiService.getUserRewardPointList(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<URPListRP>() {
                @Override
                public void onResponse(@NotNull Call<URPListRP> call, @NotNull Response<URPListRP> response) {

                    if (getActivity() != null) {

                        try {
                            URPListRP urpListRP = response.body();
                            assert urpListRP != null;

                            if (urpListRP.getStatus().equals("1")) {

                                if (urpListRP.getRewardPointLists().size() == 0) {
                                    conNoData.setVisibility(View.VISIBLE);
                                } else {
                                    rewardPointAdapter = new RewardPointAdapter(getActivity(), urpListRP.getRewardPointLists());
                                    recyclerView.setAdapter(rewardPointAdapter);
                                    recyclerView.setLayoutAnimation(layoutAnimationController);
                                }

                            } else if (urpListRP.getStatus().equals("2")) {
                                method.suspend(urpListRP.getMessage());
                            } else {
                                method.alertBox(urpListRP.getMessage());
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
                public void onFailure(@NotNull Call<URPListRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressBar.setVisibility(View.GONE);
                    conNoData.setVisibility(View.VISIBLE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }
}
