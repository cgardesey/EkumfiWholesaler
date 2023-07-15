package com.ekumfi.wholesaler.pagerAdapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ekumfi.wholesaler.fragment.WholesalerAccountFragment1;
import com.ekumfi.wholesaler.fragment.WholesalerAccountFragment2;
import com.ekumfi.wholesaler.fragment.WholesalerAccountFragment3;

public class WholesalerAccountPageAdapter extends FragmentPagerAdapter {

    public WholesalerAccountPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                WholesalerAccountFragment1 wholesalerAccountFragment1 = new WholesalerAccountFragment1();
                return wholesalerAccountFragment1;
            case 1:
                WholesalerAccountFragment2 wholesalerAccountFragment2 = new WholesalerAccountFragment2();
                return wholesalerAccountFragment2;
            case 2:
                WholesalerAccountFragment3 wholesalerAccountFragment3 = new WholesalerAccountFragment3();
                return wholesalerAccountFragment3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}