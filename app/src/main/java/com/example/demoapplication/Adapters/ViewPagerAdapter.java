package com.example.demoapplication.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.demoapplication.FirstPageFragment;
import com.example.demoapplication.SecondFragment;

/**
 * Created by Mehul Bisht on 17-02-2021
 */

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 1) {
            return new SecondFragment();
        } else {
            return new FirstPageFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
