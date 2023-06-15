package com.example.app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabPagerAdapter extends FragmentStateAdapter {

    Fragment[] fragments = new Fragment[] { new Fragment1(), new Fragment2(), new Fragment3() };
    private final Fragment[] fragments;

    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity) { super(fragmentActivity);}

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments[position];
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }



    public Fragment getFragment(int position) {    // Get fragment by position
        if (position < 0 || position >= fragments.length) {
            throw new IndexOutOfBoundsException("Invalid fragment index");
        }
        return fragments[position];
    }
}