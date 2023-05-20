package com.yeelightcontrol.room;

import android.app.ProgressDialog;
import android.os.Binder;

import com.yeelightcontrol.database.Room;

public class RoomObjectWrapperForBinder extends Binder {

    private Room mData;
    private ProgressDialog dialog;

    public RoomObjectWrapperForBinder(Room data) {
        this.mData = data;
    }

    public RoomObjectWrapperForBinder(ProgressDialog dialog) {
        this.dialog = dialog;
    }

    public Room getData() {
        return mData;
    }
    public ProgressDialog getDataDialog() {
        return dialog;
    }
}