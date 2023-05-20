package com.yeelightcontrol.device;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import com.yeelightcontrol.R;

public class DeviceError extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_error);

        getSupportActionBar().setTitle("Desk");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return false;
    }
}