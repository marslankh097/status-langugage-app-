package com.example.status.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.status.fragment.RewardCurrentFragment;
import com.example.status.fragment.URFragment;

public class ViewpagerRewardAdapter extends FragmentStateAdapter {

    private final int size;

    public ViewpagerRewardAdapter(FragmentManager fm, int size, Lifecycle lifecycle) {
        super(fm, lifecycle);
        this.size = size;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0:
                return new RewardCurrentFragment();

            case 1:
                return new URFragment();

            default:
                return null;
        }

    }

    @Override
    public int getItemCount() {
        return size;
    }

}
