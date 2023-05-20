package com.yeelightcontrol.device;

import android.os.Binder;

import com.yeelightcontrol.database.Bulb;

public class BulbObjectWrapperForBinder extends Binder {

    private final Bulb mData;

    public BulbObjectWrapperForBinder(Bulb data) {
        mData = data;
    }

    public Bulb getData() {
        return mData;
    }
}