package com.example.status.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.adapter.UserRMAdapter;
import com.example.status.interfaces.OnClick;
import com.example.status.response.UserRedeemRP;
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


public class URFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private ProgressBar progressBar;
    private ConstraintLayout conNoData;
    private RecyclerView recyclerView;
    private UserRMAdapter userRMAdapter;
    private LayoutAnimationController animation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.user_rm_fragment, container, false);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        onClick = (position, title, type, status_type, id, tag) -> {
            if (tag.equals("td")) {
                TDFragment tdFragment = new TDFragment();
                Bundle bundle = new Bundle();
                bundle.putString("redeem_id", id);
                tdFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, tdFragment, type).addToBackStack(type).commitAllowingStateLoss();
            } else {
                URHistoryFragment urHistoryFragment = new URHistoryFragment();
                Bundle bundle = new Bundle();
                bundle.putString("redeem_id", id);
                urHistoryFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, urHistoryFragment, type).addToBackStack(type).commitAllowingStateLoss();
            }
        };
        method = new Method(getActivity(), onClick);

        conNoData = view.findViewById(R.id.con_noDataFound);
        progressBar = view.findViewById(R.id.progressbar_user_rm_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_user_rm_fragment);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        progressBar.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);

        if (method.isNetworkAvailable()) {
            history(method.userId());
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

        return view;

    }

    private void history(final String id) {

        if (getActivity() != null) {

            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", id);
            jsObj.addProperty("method_name", "user_redeem_history");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<UserRedeemRP> call = apiService.getUserRedeemHistory(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<UserRedeemRP>() {
                @Override
                public void onResponse(@NotNull Call<UserRedeemRP> call, @NotNull Response<UserRedeemRP> response) {

                    if (getActivity() != null) {
                        try {
                            UserRedeemRP userRedeemRP = response.body();
                            assert userRedeemRP != null;

                            if (userRedeemRP.getStatus().equals("1")) {

                                if (userRedeemRP.getUserRMLists().size() == 0) {
                                    conNoData.setVisibility(View.VISIBLE);
                                } else {
                                    userRMAdapter = new UserRMAdapter(getActivity(), userRedeemRP.getUserRMLists(), onClick, "ur");
                                    recyclerView.setAdapter(userRMAdapter);
                                    recyclerView.setLayoutAnimation(animation);
                                }

                            } else if (userRedeemRP.getStatus().equals("2")) {
                                method.suspend(userRedeemRP.getMessage());
                            } else {
                                method.alertBox(userRedeemRP.getMessage());
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
                public void onFailure(@NotNull Call<UserRedeemRP> call, @NotNull Throwable t) {
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
