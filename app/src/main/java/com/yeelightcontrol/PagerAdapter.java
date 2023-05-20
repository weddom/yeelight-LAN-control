package com.yeelightcontrol;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.yeelightcontrol.device.DeviceFragment;
import com.yeelightcontrol.room.RoomFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new DeviceFragment();
            case 1:
                return new RoomFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}