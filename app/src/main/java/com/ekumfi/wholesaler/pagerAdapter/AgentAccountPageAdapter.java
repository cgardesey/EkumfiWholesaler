package com.ekumfi.wholesaler.pagerAdapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ekumfi.wholesaler.fragment.AgentAccountFragment1;
import com.ekumfi.wholesaler.fragment.AgentAccountFragment2;
import com.ekumfi.wholesaler.fragment.AgentAccountFragment3;

public class AgentAccountPageAdapter extends FragmentPagerAdapter {

    public AgentAccountPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                AgentAccountFragment1 agentAccountFragment1 = new AgentAccountFragment1();
                return agentAccountFragment1;
            case 1:
                AgentAccountFragment2 agentAccountFragment2 = new AgentAccountFragment2();
                return agentAccountFragment2;
            case 2:
                AgentAccountFragment3 agentAccountFragment3 = new AgentAccountFragment3();
                return agentAccountFragment3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}