package com.example.status.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.status.R;
import com.example.status.activity.MainActivity;
import com.example.status.response.DataRP;
import com.example.status.response.ProfileRP;
import com.example.status.rest.ApiClient;
import com.example.status.rest.ApiInterface;
import com.example.status.util.API;
import com.example.status.util.Events;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {

    private Method method;
    private ProgressBar progressBar;
    private String imageProfile;
    private InputMethodManager imm;
    private ProgressDialog progressDialog;
    private ScrollView conMain;
    private ConstraintLayout conNoData;
    private MaterialButton buttonSubmit;
    private CircleImageView imageViewUser;
    private boolean isProfile = false, isRemove = false;
    private TextInputEditText editTextName, editTextEmail, editTextPhoneNo, editTextInstagram, editTextYoutube;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.edit_profile_fragment, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.edit_profile));
        }

        GlobalBus.getBus().register(this);

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        method = new Method(getActivity());
        progressDialog = new ProgressDialog(getActivity());

        progressBar = view.findViewById(R.id.progressbar_editPro);
        conMain = view.findViewById(R.id.scroll);
        conNoData = view.findViewById(R.id.con_noDataFound);
        imageViewUser = view.findViewById(R.id.imageView_user_editPro);
        editTextName = view.findViewById(R.id.editText_name_editPro);
        editTextEmail = view.findViewById(R.id.editText_email_editPro);
        editTextPhoneNo = view.findViewById(R.id.editText_phone_editPro);
        editTextInstagram = view.findViewById(R.id.editText_instagram_editPro);
        editTextYoutube = view.findViewById(R.id.editText_youtube_editPro);
        buttonSubmit = view.findViewById(R.id.button_editPro);
        TextInputLayout textInputEmail = view.findViewById(R.id.textInput_email_editPro);

        if (method.getLoginType().equals("google") || method.getLoginType().equals("facebook")) {
            editTextName.setCursorVisible(false);
            editTextName.setFocusable(false);
            textInputEmail.setVisibility(View.GONE);
        } else {
            textInputEmail.setVisibility(View.VISIBLE);
        }

        conMain.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        buttonSubmit.setVisibility(View.GONE);

        if (method.isNetworkAvailable()) {
            profile(method.userId());
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

        setHasOptionsMenu(true);
        return view;

    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Subscribe
    public void getData(Events.ProImage proImage) {
        isProfile = proImage.isProfile();
        isRemove = proImage.isRemove();
        if (proImage.isProfile()) {
            imageProfile = proImage.getImagePath();
            Uri uri = Uri.fromFile(new File(imageProfile));
            Glide.with(getActivity()).load(uri)
                    .placeholder(R.drawable.profile)
                    .into(imageViewUser);
        }
        if (proImage.isRemove()) {
            Glide.with(getActivity()).load(R.drawable.profile)
                    .placeholder(R.drawable.profile)
                    .into(imageViewUser);
        }
    }

    private void profile(String userId) {

        if (getActivity() != null) {

            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("method_name", "user_profile");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<ProfileRP> call = apiService.getProfile(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<ProfileRP>() {
                @Override
                public void onResponse(@NotNull Call<ProfileRP> call, @NotNull Response<ProfileRP> response) {

                    if (getActivity() != null) {

                        try {

                            ProfileRP profileRP = response.body();
                            assert profileRP != null;

                            if (profileRP.getStatus().equals("1")) {

                                if (profileRP.getSuccess().equals("1")) {

                                    imageProfile = profileRP.getUser_image();

                                    Glide.with(getActivity()).load(profileRP.getUser_image())
                                            .placeholder(R.drawable.profile).into(imageViewUser);

                                    editTextName.setText(profileRP.getName());
                                    editTextEmail.setText(profileRP.getEmail());
                                    editTextPhoneNo.setText(profileRP.getPhone());
                                    editTextYoutube.setText(profileRP.getUser_youtube());
                                    editTextInstagram.setText(profileRP.getUser_instagram());

                                    imageViewUser.setOnClickListener(v -> {
                                        BottomSheetDialogFragment fragment = new ProImage();
                                        fragment.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
                                    });

                                    imageViewUser.setOnClickListener(V -> {
                                        BottomSheetDialogFragment fragment = new ProImage();
                                        fragment.show(getActivity().getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
                                    });

                                    buttonSubmit.setOnClickListener(v -> save());

                                    conMain.setVisibility(View.VISIBLE);
                                    buttonSubmit.setVisibility(View.VISIBLE);

                                } else {
                                    conNoData.setVisibility(View.VISIBLE);
                                    method.alertBox(profileRP.getMsg());
                                }

                            } else if (profileRP.getStatus().equals("2")) {
                                method.suspend(profileRP.getMessage());
                            } else {
                                conNoData.setVisibility(View.VISIBLE);
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
                    conNoData.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }

    private void save() {

        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String phoneNo = editTextPhoneNo.getText().toString();
        String instagram = editTextInstagram.getText().toString();
        String youtube = editTextYoutube.getText().toString();

        editTextName.setError(null);
        editTextEmail.setError(null);
        editTextPhoneNo.setError(null);

        if (name.equals("") || name.isEmpty()) {
            editTextName.requestFocus();
            editTextName.setError(getResources().getString(R.string.please_enter_name));
        } else if ((method.getLoginType().equals("normal")) && (!isValidMail(email) || email.isEmpty())) {
            editTextEmail.requestFocus();
            editTextEmail.setError(getResources().getString(R.string.please_enter_email));
        } else if (phoneNo.equals("") || phoneNo.isEmpty()) {
            editTextPhoneNo.requestFocus();
            editTextPhoneNo.setError(getResources().getString(R.string.please_enter_phone));
        } else {
            if (method.isNetworkAvailable()) {

                editTextName.clearFocus();
                editTextEmail.clearFocus();
                editTextPhoneNo.clearFocus();
                imm.hideSoftInputFromWindow(editTextName.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextEmail.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextPhoneNo.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextInstagram.getWindowToken(), 0);
                imm.hideSoftInputFromWindow(editTextYoutube.getWindowToken(), 0);

                profileUpdate(method.userId(), name, phoneNo, youtube, instagram, imageProfile);

            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }
        }

    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void profileUpdate(String userId, String sendName, String sendPhone, String userYoutube, String userInstagram, String profileImage) {

        if (getActivity() != null) {

            progressDialog.show();
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);

            MultipartBody.Part body = null;

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("name", sendName);
            jsObj.addProperty("phone", sendPhone);
            jsObj.addProperty("is_remove", isRemove);
            jsObj.addProperty("user_youtube", userYoutube);
            jsObj.addProperty("user_instagram", userInstagram);
            jsObj.addProperty("method_name", "user_profile_update");
            if (isProfile) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), new File(profileImage));
                // MultipartBody.Part is used to send also the actual file name
                body = MultipartBody.Part.createFormData("user_image", new File(profileImage).getName(), requestFile);
            }
            // add another part within the multipart request
            RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), API.toBase64(jsObj.toString()));
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataRP> call = apiService.getEditProfile(requestBody, body);
            call.enqueue(new Callback<DataRP>() {
                @Override
                public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                    if (getActivity() != null) {

                        try {
                            DataRP dataRP = response.body();
                            assert dataRP != null;

                            if (dataRP.getStatus().equals("1")) {
                                if (dataRP.getSuccess().equals("1")) {
                                    Toast.makeText(getActivity(), dataRP.getMsg(), Toast.LENGTH_SHORT).show();
                                    Events.ProfileUpdate profileUpdate = new Events.ProfileUpdate("");
                                    GlobalBus.getBus().post(profileUpdate);
                                    getActivity().getSupportFragmentManager().popBackStack();
                                } else {
                                    method.alertBox(dataRP.getMsg());
                                }
                            } else if (dataRP.getStatus().equals("2")) {
                                method.suspend(dataRP.getMessage());
                            } else {
                                method.alertBox(dataRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressDialog.dismiss();

                }

                @Override
                public void onFailure(@NotNull Call<DataRP> call, @NotNull Throwable t) {
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

}
