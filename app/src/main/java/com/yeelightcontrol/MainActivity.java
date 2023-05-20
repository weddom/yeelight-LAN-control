package com.yeelightcontrol;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import com.yeelightcontrol.device.DeviceFragment;
import com.yeelightcontrol.room.RoomFragment;

public class MainActivity extends AppCompatActivity {

    private AlertDialog alertDialog = null;
    DeviceFragment deviceFragment = new DeviceFragment();
    RoomFragment roomFragment = new RoomFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.pager, deviceFragment).commit();

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.device:
                        getSupportFragmentManager().beginTransaction().replace(R.id.pager, deviceFragment).commit();
                        return true;
                    case R.id.room:
                        getSupportFragmentManager().beginTransaction().replace(R.id.pager, roomFragment).commit();
                        return true;
                }
                return false;
            }
        });

        final Runnable dialog_wifi = this::wifi_dialog;
        Handler handler2 = new Handler();
        handler2.postDelayed(dialog_wifi, 0);
    }

    @Override
    public void onResume(){
        super.onResume();
        final Runnable dialog_wifi = this::wifi_dialog;
        Handler handler2 = new Handler();
        handler2.postDelayed(dialog_wifi, 0);
    }

    protected void wifi_dialog() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if(!(wifiManager != null && wifiManager.isWifiEnabled()) && alertDialog == null){
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Attention");
                alertDialogBuilder.setMessage(getResources().getString(R.string.wifi_warning)).setCancelable(false).setPositiveButton("Yes", (dialog, id) -> {
                    assert wifiManager != null;
                    wifiManager.setWifiEnabled(true);
                    alertDialog = null;
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.wifi_enabled), Toast.LENGTH_LONG).show();
                })
                        .setNegativeButton("Preferences", (dialog, id) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }else{
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("Attention");
                alertDialogBuilder.setMessage(getResources().getString(R.string.wifi_warning)).setCancelable(false).setPositiveButton("Preferences", (dialog, id) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
                alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        }
    }
}
