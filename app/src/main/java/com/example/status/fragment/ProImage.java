package com.example.status.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.status.R;
import com.example.status.util.Events;
import com.example.status.util.GetPath;
import com.example.status.util.GlobalBus;
import com.example.status.util.Method;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.jetbrains.annotations.NotNull;

import static android.os.Build.VERSION.SDK_INT;

public class ProImage extends BottomSheetDialogFragment {

    private Method method;
    private final int REQUEST_CODE_CHOOSE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pro_image, container, false);

        method = new Method(getActivity());
        if (method.isRtl()) {
            view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        ConstraintLayout conRemove = view.findViewById(R.id.con_remove_proImage);
        ConstraintLayout conImage = view.findViewById(R.id.con_image_proImage);

        conRemove.setOnClickListener(v -> {
            Events.ProImage proImage = new Events.ProImage("", "", false, true);
            GlobalBus.getBus().post(proImage);
            dismiss();
        });

        conImage.setOnClickListener(v -> {
            Dexter.withContext(getActivity())
                    .withPermission(permission())
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            chooseGalleryImage();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            // check for permanent denial of permission
                            method.alertBox(getResources().getString(R.string.allow_storage));

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();

        });

        return view;
    }

    private String permission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        }else {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            try {
                String filePath = GetPath.getPath(getActivity(), data.getData());
                if (filePath != null) {
                    dismiss();
                    Events.ProImage proImage = new Events.ProImage("", filePath, true, false);
                    GlobalBus.getBus().post(proImage);
                } else {
                    method.alertBox(getResources().getString(R.string.upload_folder_error));
                }
            } catch (Exception e) {
                method.alertBox(getResources().getString(R.string.upload_folder_error));
            }
        }
    }

    private void chooseGalleryImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_CODE_CHOOSE);
    }

}
