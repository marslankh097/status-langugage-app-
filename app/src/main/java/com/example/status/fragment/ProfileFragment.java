package com.example.status.fragment;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.activity.ActivityChangePassword;
import com.example.status.activity.DeleteAccount;
import com.example.status.activity.Login;
import com.example.status.activity.MainActivity;
import com.example.status.activity.ReferenceCodeActivity;
import com.example.status.activity.Spinner;
import com.example.status.activity.ViewImage;
import com.example.status.response.ProfileRP;
import com.example.status.response.UserFollowStatusRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private Method method;
    private Animation myAnim;
    private String type, getUserId;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private CoordinatorLayout coordinatorLayout;
    private MaterialButton buttonFollow, buttonLogin;
    private CircleImageView imageViewProfile;
    ConstraintLayout conNoData, conStatus;
    LinearLayout conFollowings, conFollower;
    private ImageView imageViewData, imageViewLoginType, imageViewYoutube, imageViewInstagram;
    private MaterialTextView textViewData, textViewFollowing, textViewFollower, textViewTotalVideo, textViewUserName;
    ViewPager vpTab;
    TabLayout tabLayout;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.profile_fragment, container, false);

        GlobalBus.getBus().register(this);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.profile));
        }

        progressDialog = new ProgressDialog(getActivity());

        assert getArguments() != null;
        type = getArguments().getString("type");
        getUserId = getArguments().getString("id");

        myAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);

        method = new Method(getActivity());

        coordinatorLayout = view.findViewById(R.id.coordinatorLayout_pro);
        conNoData = view.findViewById(R.id.con_not_login);
        progressBar = view.findViewById(R.id.progressbar_profile);
        imageViewData = view.findViewById(R.id.imageView_not_login);
        buttonLogin = view.findViewById(R.id.button_not_login);
        conStatus = view.findViewById(R.id.constrainLayout_pro);
        textViewData = view.findViewById(R.id.textView_not_login);
        textViewUserName = view.findViewById(R.id.textView_name_pro);
        imageViewProfile = view.findViewById(R.id.imageView_pro);
        imageViewLoginType = view.findViewById(R.id.imageView_loginType_pro);
        imageViewYoutube = view.findViewById(R.id.imageView_youtube_pro);
        imageViewInstagram = view.findViewById(R.id.imageView_instagram_pro);
        conFollowings = view.findViewById(R.id.con_followings_pro);
        conFollower = view.findViewById(R.id.con_follower_pro);
        textViewTotalVideo = view.findViewById(R.id.textView_video_pro);
        textViewFollowing = view.findViewById(R.id.textView_following_pro);
        textViewFollower = view.findViewById(R.id.textView_followers_pro);
        buttonFollow = view.findViewById(R.id.button_follow_pro);

        progressBar.setVisibility(View.GONE);
        coordinatorLayout.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);
        imageViewLoginType.setVisibility(View.GONE);
        data(false, false);
        vpTab = view.findViewById(R.id.vpTab);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupViewPager(vpTab);
        vpTab.setSaveEnabled(false);
        tabLayout.setupWithViewPager(vpTab);

        if (method.isDarkMode()) {
            imageViewInstagram.setImageDrawable(getResources().getDrawable(R.drawable.insta_ic));
            imageViewYoutube.setImageDrawable(getResources().getDrawable(R.drawable.youtube_ic));
        } else {
            imageViewInstagram.setImageDrawable(getResources().getDrawable(R.drawable.insta_ic_pro));
            imageViewYoutube.setImageDrawable(getResources().getDrawable(R.drawable.youtube_ic_pro));
        }

        buttonLogin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), Login.class));
            getActivity().finishAffinity();
        });

        callData();

        setHasOptionsMenu(true);
        return view;

    }

    private void setupViewPager(final ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new MyStatusFragment(), getString(R.string.post));
        adapter.addFragment(new DownloadFragment(), getString(R.string.download));
        adapter.addFragment(new FavouriteFragment(), getString(R.string.favorites));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
    }

    public Parcelable saveState() {
        return null;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        final List<Fragment> mFragmentList = new ArrayList<>();
        final List<String> mFragmentTitleList = new ArrayList<>();

        private ViewPagerAdapter(FragmentManager activity) {
            super(activity);
        }

        private void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.profile_menu, menu);

        MenuItem moreItem = menu.findItem(R.id.more_profile);
        moreItem.setVisible(method.isLogin());
        moreItem.setOnMenuItemClickListener(item -> {
            profileMenuDialog();
            return false;
        });


        super.onCreateOptionsMenu(menu, inflater);
    }

    private void callData() {
        if (getActivity() != null) {
            if (method.isNetworkAvailable()) {
                if (method.isLogin()) {
                    profile(method.userId(), getUserId);
                } else {
                    if (!type.equals("user")) {
                        profile("", getUserId);
                    } else {
                        data(true, true);
                    }
                }
            } else {
                data(true, false);
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        }
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

    @Subscribe
    public void getData(Events.ProfileUpdate profileUpdate) {
        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.profile));
        }
        data(false, false);
        coordinatorLayout.setVisibility(View.GONE);
        callData();
    }

    public void profile(final String id, final String otherUserId) {

        if (getActivity() != null) {

            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            if (id.equals(getUserId)) {
                jsObj.addProperty("method_name", "user_profile");
            } else {
                jsObj.addProperty("method_name", "other_user_profile");
                jsObj.addProperty("other_user_id", otherUserId);
            }
            jsObj.addProperty("user_id", id);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<ProfileRP> call = apiService.getProfile(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<ProfileRP>() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onResponse(@NotNull Call<ProfileRP> call, @NotNull Response<ProfileRP> response) {

                    if (getActivity() != null) {

                        try {

                            ProfileRP profileRP = response.body();
                            assert profileRP != null;

                            if (profileRP.getStatus().equals("1")) {

                                if (profileRP.getSuccess().equals("1")) {

                                    if (id.equals(getUserId)) {
                                        method.editor.putString(method.userImage, profileRP.getUser_image());
                                        method.editor.commit();
                                    }

                                    if (method.isLogin()) {
                                        if (id.equals(otherUserId)) {
                                            buttonFollow.setText(getResources().getString(R.string.edit_profile));
                                            if (method.getLoginType().equals("google")) {
                                                imageViewLoginType.setVisibility(View.VISIBLE);
                                                imageViewLoginType.setImageDrawable(getResources().getDrawable(R.drawable.google_user_pro));
                                            } else {
                                                imageViewLoginType.setVisibility(View.GONE);
                                            }
                                        } else {
                                            if (profileRP.getAlready_follow().equals("true")) {
                                                buttonFollow.setText(getResources().getString(R.string.unfollow));
                                            } else {
                                                buttonFollow.setText(getResources().getString(R.string.follow));
                                            }
                                        }
                                    } else {
                                        buttonFollow.setText(getResources().getString(R.string.follow));
                                    }

                                    Glide.with(getActivity()).load(profileRP.getUser_image())
                                            .placeholder(R.drawable.user_profile).into(imageViewProfile);

                                    textViewUserName.setText(profileRP.getName());
                                    if (profileRP.getIs_verified().equals("true")) {
                                        textViewUserName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_verification, 0);
                                    }
                                    textViewFollower.setText(method.format(Double.parseDouble(profileRP.getTotal_followers())));
                                    textViewFollowing.setText(method.format(Double.parseDouble(profileRP.getTotal_following())));
                                    textViewTotalVideo.setText(method.format(Double.parseDouble(profileRP.getTotal_status())));

                                    imageViewProfile.setOnClickListener(v -> startActivity(new Intent(getActivity(), ViewImage.class)
                                            .putExtra("path", profileRP.getUser_image())));

                                    imageViewYoutube.setOnClickListener(v -> {
                                        imageViewYoutube.startAnimation(myAnim);
                                        String string = profileRP.getUser_youtube();
                                        if (string.equals("")) {
                                            method.alertBox(getResources().getString(R.string.user_not_youtube_link));
                                        } else {
                                            try {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse(string));
                                                startActivity(intent);
                                            } catch (Exception e) {
                                                method.alertBox(getResources().getString(R.string.wrong));
                                            }
                                        }
                                    });

                                    imageViewInstagram.setOnClickListener(v -> {
                                        imageViewInstagram.startAnimation(myAnim);
                                        String string = profileRP.getUser_instagram();
                                        if (string.equals("")) {
                                            method.alertBox(getResources().getString(R.string.user_not_instagram_link));
                                        } else {
                                            Uri uri = Uri.parse(string);
                                            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                                            likeIng.setPackage("com.instagram.android");
                                            try {
                                                startActivity(likeIng);
                                            } catch (ActivityNotFoundException e) {
                                                try {
                                                    startActivity(new Intent(Intent.ACTION_VIEW,
                                                            Uri.parse(string)));
                                                } catch (Exception e1) {
                                                    method.alertBox(getResources().getString(R.string.wrong));
                                                }
                                            }
                                        }
                                    });

                                    buttonFollow.setOnClickListener(v -> {
                                        if (method.isNetworkAvailable()) {
                                            if (method.isLogin()) {
                                                if (id.equals(otherUserId)) {
                                                    getActivity().getSupportFragmentManager().beginTransaction()
                                                            .add(R.id.frameLayout_main, new EditProfileFragment(), getResources().getString(R.string.edit_profile))
                                                            .addToBackStack(getResources().getString(R.string.edit_profile)).commitAllowingStateLoss();
                                                } else {
                                                    follow(id, otherUserId);
                                                }
                                            } else {
                                                method.alertBox(getResources().getString(R.string.you_have_not_login));
                                            }
                                        } else {
                                            method.alertBox(getResources().getString(R.string.internet_connection));
                                        }
                                    });

                                    conFollowings.setOnClickListener(v -> {
                                        if (!profileRP.getTotal_following().equals("0")) {
                                            UserFollowFragment userFollowFragment = new UserFollowFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("type", "following");
                                            bundle.putString("user_id", profileRP.getUser_id());
                                            bundle.putString("search", "");
                                            userFollowFragment.setArguments(bundle);
                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, userFollowFragment, getResources().getString(R.string.following)).addToBackStack(getResources().getString(R.string.following)).commitAllowingStateLoss();
                                        } else {
                                            method.alertBox(getResources().getString(R.string.not_following));
                                        }

                                    });

                                    conFollower.setOnClickListener(v -> {
                                        if (!profileRP.getTotal_followers().equals("0")) {
                                            UserFollowFragment userFollowFragment = new UserFollowFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putString("type", "follower");
                                            bundle.putString("user_id", profileRP.getUser_id());
                                            bundle.putString("search", "");
                                            userFollowFragment.setArguments(bundle);
                                            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, userFollowFragment, getResources().getString(R.string.following)).addToBackStack("sub").commitAllowingStateLoss();
                                        } else {
                                            method.alertBox(getResources().getString(R.string.not_follower));
                                        }
                                    });

                                    coordinatorLayout.setVisibility(View.VISIBLE);


                                } else {
                                    data(true, false);
                                    method.alertBox(profileRP.getMsg());
                                }

                            } else if (profileRP.getStatus().equals("2")) {
                                method.suspend(profileRP.getMessage());
                            } else {
                                data(true, false);
                                method.alertBox(profileRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NotNull Call<ProfileRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    data(true, false);
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });


        }

    }

    private void follow(final String userId, final String otherUser) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        if (getActivity() != null) {

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "user_follow");
            jsObj.addProperty("user_id", otherUser);
            jsObj.addProperty("follower_id", userId);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<UserFollowStatusRP> call = apiService.getUserFollowStatus(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<UserFollowStatusRP>() {
                @Override
                public void onResponse(@NotNull Call<UserFollowStatusRP> call, @NotNull Response<UserFollowStatusRP> response) {

                    if (getActivity() != null) {

                        try {
                            UserFollowStatusRP userFollowStatusRP = response.body();
                            assert userFollowStatusRP != null;

                            if (userFollowStatusRP.getStatus().equals("1")) {

                                if (userFollowStatusRP.getSuccess().equals("1")) {

                                    if (userFollowStatusRP.getActivity_status().equals("1")) {
                                        buttonFollow.setText(getResources().getString(R.string.unfollow));
                                    } else {
                                        buttonFollow.setText(getResources().getString(R.string.follow));
                                    }
                                    getUserId = otherUser;
                                    profile(userId, otherUser);

                                } else {
                                    method.alertBox(userFollowStatusRP.getMsg());
                                }

                            } else if (userFollowStatusRP.getStatus().equals("2")) {
                                method.suspend(userFollowStatusRP.getMessage());
                            } else {
                                method.alertBox(userFollowStatusRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressDialog.dismiss();

                }


                @Override
                public void onFailure(@NotNull Call<UserFollowStatusRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressDialog.dismiss();
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

    public void profileMenuDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
        dialog.setContentView(R.layout.layout_option_profile);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        RelativeLayout layReCode = dialog.findViewById(R.id.layReCode);
        RelativeLayout layLuckyWheel = dialog.findViewById(R.id.layLuckyWheel);
        RelativeLayout layLogout = dialog.findViewById(R.id.layLogout);
        RelativeLayout layDeleteAcc = dialog.findViewById(R.id.layDeleteAcc);
        RelativeLayout layChangePass = dialog.findViewById(R.id.layChangePass);

        assert layReCode != null;
        layReCode.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), ReferenceCodeActivity.class));
            dialog.dismiss();
        });

        assert layLuckyWheel != null;
        layLuckyWheel.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), Spinner.class));
            dialog.dismiss();
        });

        assert layLogout != null;
        layLogout.setOnClickListener(v -> {
            logout();
            dialog.dismiss();
        });

        assert layDeleteAcc != null;
        layDeleteAcc.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), DeleteAccount.class));
            dialog.dismiss();
        });

        assert layChangePass != null;
        layChangePass.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), ActivityChangePassword.class));
            dialog.dismiss();
        });


        dialog.show();
    }

    public void logout() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity(), R.style.DialogTitleTextStyle);
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(R.string.logout_message));
        builder.setPositiveButton(getResources().getString(R.string.logout),
                (arg0, arg1) -> {
                    OneSignal.sendTag("user_id", method.userId());
                    if (method.getLoginType().equals("google")) {

                        // Configure sign-in to request the ic_user_login's ID, email address, and basic
                        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .build();

                        // Build a GoogleSignInClient with the options specified by gso.
                        //Google login
                        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

                        mGoogleSignInClient.signOut()
                                .addOnCompleteListener(requireActivity(), task -> {
                                    method.editor.putBoolean(method.prefLogin, false);
                                    method.editor.commit();
                                    startActivity(new Intent(requireActivity(), Login.class));
                                    requireActivity().finishAffinity();
                                });
                    } else {
                        method.editor.putBoolean(method.prefLogin, false);
                        method.editor.commit();
                        startActivity(new Intent(requireActivity(), Login.class));
                        requireActivity().finishAffinity();
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                (dialogInterface, i) -> {

                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
