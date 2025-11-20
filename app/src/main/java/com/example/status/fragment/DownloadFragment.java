package com.example.status.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.status.R;
import com.example.status.adapter.DownloadAdapter;
import com.example.status.database.DatabaseHandler;
import com.example.status.item.SubCategoryList;
import com.example.status.util.Method;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DownloadFragment extends Fragment {

    private Method method;
    private DatabaseHandler db;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ConstraintLayout conNoData;
    private List<File> inFiles;
    private List<SubCategoryList> subCategoryLists;
    private List<SubCategoryList> downloadListsCompair;
    private DownloadAdapter downloadAdapter;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.sub_cat_fragment, container, false);



        method = new Method(getActivity());

        db = new DatabaseHandler(getActivity());

        inFiles = new ArrayList<>();
        subCategoryLists = new ArrayList<>();
        downloadListsCompair = new ArrayList<>();

        conNoData = view.findViewById(R.id.con_noDataFound);
        progressBar = view.findViewById(R.id.progressbar_sub_category);
        recyclerView = view.findViewById(R.id.recyclerView_sub_category);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        new Execute().execute();

        setHasOptionsMenu(true);
        return view;

    }



    @SuppressLint("StaticFieldLeak")
    class Execute extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {

            progressBar.setVisibility(View.VISIBLE);
            subCategoryLists.clear();
            inFiles.clear();
            downloadListsCompair.clear();

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            //get data in database
            subCategoryLists.addAll(db.getStatusDownload());

            //video file
            File file = new File(method.videoPath());
            getVideoFileList(file);

            //image and gif file
            File imageFile = new File(method.imagePath());
            getImageFileList(imageFile);

            getDownloadLists(inFiles);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (downloadListsCompair.size() == 0) {
                conNoData.setVisibility(View.VISIBLE);
            } else {
                downloadAdapter = new DownloadAdapter(getActivity(), downloadListsCompair);
                recyclerView.setAdapter(downloadAdapter);
            }

            progressBar.setVisibility(View.GONE);

            super.onPostExecute(s);
        }
    }

    private void getVideoFileList(File parentDir) {
        try {
            Queue<File> files = new LinkedList<>();
            files.addAll(Arrays.asList(parentDir.listFiles()));
            while (!files.isEmpty()) {
                File file = files.remove();
                if (file.isDirectory()) {
                    files.addAll(Arrays.asList(file.listFiles()));
                } else if (file.getName().endsWith(".mp4")) {
                    inFiles.add(file);
                }
            }
        } catch (Exception e) {
            Log.d("error", e.toString());
        }
    }


    private void getImageFileList(File parentDir) {
        try {
            Queue<File> files = new LinkedList<>(Arrays.asList(parentDir.listFiles()));
            while (!files.isEmpty()) {
                File file = files.remove();
                if (file.isDirectory()) {
                    files.addAll(Arrays.asList(file.listFiles()));
                } else if (file.getName().endsWith(".gif") || file.getName().endsWith(".jpg")) {
                    inFiles.add(file);
                }
            }
        } catch (Exception e) {
            Log.d("error", e.toString());
        }
    }

    //check image, gif, video file available or not
    private void getDownloadLists(List<File> list) {

        for (int i = 0; i < subCategoryLists.size(); i++) {

            String statusType = subCategoryLists.get(i).getStatus_type();

            if (statusType.equals("video")) {
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).toString().contains(subCategoryLists.get(i).getVideo_url())) {
                        downloadListsCompair.add(subCategoryLists.get(i));
                        break;
                    } else {
                        if (j == list.size() - 1) {
                            db.deleteStatusDownload(subCategoryLists.get(i).getId(), subCategoryLists.get(i).getStatus_type());
                        }
                    }
                }
            } else if (statusType.equals("gif")) {
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).toString().contains(subCategoryLists.get(i).getStatus_thumbnail_b())) {
                        downloadListsCompair.add(subCategoryLists.get(i));
                        break;
                    } else {
                        if (j == list.size() - 1) {
                            db.deleteStatusDownload(subCategoryLists.get(i).getId(), subCategoryLists.get(i).getStatus_type());
                        }
                    }
                }
            } else {
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).toString().contains(subCategoryLists.get(i).getGif_url())) {
                        downloadListsCompair.add(subCategoryLists.get(i));
                        break;
                    } else {
                        if (j == list.size() - 1) {
                            db.deleteStatusDownload(subCategoryLists.get(i).getId(), subCategoryLists.get(i).getStatus_type());
                        }
                    }
                }
            }
        }
    }

}
