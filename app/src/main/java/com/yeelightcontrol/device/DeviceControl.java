package com.yeelightcontrol.device;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.SeekBar;

import java.util.Objects;

import com.yeelightcontrol.color.ColorView;
import com.yeelightcontrol.R;
import com.yeelightcontrol.YeelightDevice;
import com.yeelightcontrol.database.Bulb;
import com.yeelightcontrol.yapi.exception.YeelightResultErrorException;
import com.yeelightcontrol.yapi.exception.YeelightSocketException;

public class DeviceControl extends AppCompatActivity {

    private Bulb bulb;
    private YeelightDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        getSupportActionBar().setTitle("Desk");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if( savedInstanceState != null) {
            Bundle bundle = savedInstanceState;
            bulb = bundle.getParcelable("bulb_v");
            if(bulb !=null) {
                device = bulb.getDevice();
                try {
                    if(device==null) {
                        device = new YeelightDevice(bulb.getIp());
                    }
                } catch (YeelightSocketException e) {
                    e.printStackTrace();
                }
            }
        }
        ColorView colorView = findViewById(R.id.colorView);
        colorView.listenToColorPallet(this::onEvent);

        ImageButton dateButton = findViewById(R.id.btn_date);
        ImageButton movieButton = findViewById(R.id.btn_movie);

        dateButton.setOnClickListener(arg0 -> {
            try {
                if (device.getState().equals("off")) {
                    device.setPower(true);
                }
                device.setRGB(255,0,255);
                device.setBrightness(60);
            } catch (YeelightResultErrorException | YeelightSocketException e) {
                e.printStackTrace();
                reset_limit();
            }
        });
        movieButton.setOnClickListener(arg0 -> {
            try {
                if (device.getState().equals("off")) {
                    device.setPower(true);
                }
                device.setRGB(20,20,50);
                device.setBrightness(50);
            } catch (YeelightResultErrorException | YeelightSocketException e) {
                e.printStackTrace();
                reset_limit();
            }
        });

        bulb = ((BulbObjectWrapperForBinder) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).getBinder("bulb_v"))).getData();

        SeekBar brightness_bar = findViewById(R.id.brightness_bar);
        brightness_bar.setOnSeekBarChangeListener(seekBarChangeListener);

        if(bulb!=null) {

            device = bulb.getDevice();
            try {
                if(device==null) {
                    device = new YeelightDevice(bulb.getIp());
                }
                brightness_bar.setProgress(Integer.parseInt(device.getBrightness()));
            } catch (YeelightSocketException | YeelightResultErrorException e) {
                if(e.getMessage().contains("Broken pipe")){
                    try {
                        device = null;
                        device = new YeelightDevice(bulb.getIp());
                        bulb.setDevice(device);
                    } catch (YeelightSocketException e1) {
                        e1.printStackTrace();
                    }
                }
                Intent myIntent = new Intent(getApplicationContext(), DeviceError.class);
                startActivity(myIntent);
                finish();
                e.printStackTrace();
            }
        }else{
            Intent myIntent = new Intent(getApplicationContext(), DeviceError.class);
            startActivity(myIntent);
            finish();
        }
        Context context = DeviceControl.this;
    }

    public void onEvent(int color,int touchX,int touchY) {
        String hexColor = String.format("#%06X", (0xFFFFFF & color));
        int red = Integer.valueOf( hexColor.substring( 1, 3 ), 16 );
        int green = Integer.valueOf( hexColor.substring( 3, 5 ), 16 );
        int blue = Integer.valueOf( hexColor.substring( 5, 7 ), 16 );
        try {
            device.setRGB(red,green,blue);
        } catch (YeelightResultErrorException | YeelightSocketException e) {
            e.printStackTrace();
            reset_limit();
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            try {
                device.setBrightness(seekBar.getProgress());
            } catch (YeelightResultErrorException | YeelightSocketException e) {
                e.printStackTrace();
                reset_limit();
            }
        }
    };

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return false;
    }

    private void reset_limit(){
        device = null;
        try {
            device = new YeelightDevice(bulb.getIp());
        } catch (YeelightSocketException e1) {
            e1.printStackTrace();
        }
    }
}