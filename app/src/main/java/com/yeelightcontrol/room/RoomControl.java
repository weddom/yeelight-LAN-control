package com.yeelightcontrol.room;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.SeekBar;
import java.util.ArrayList;
import java.util.Objects;

import com.yeelightcontrol.color.ColorView;
import com.yeelightcontrol.R;
import com.yeelightcontrol.YeelightDevice;
import com.yeelightcontrol.database.Bulb;
import com.yeelightcontrol.database.Room;
import com.yeelightcontrol.yapi.exception.YeelightResultErrorException;
import com.yeelightcontrol.yapi.exception.YeelightSocketException;

public class RoomControl extends AppCompatActivity {

    private Room room;
    private ArrayList<Bulb> bulbs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_control);

        getSupportActionBar().setTitle("Bedroom");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get bulb object
        room = ((RoomObjectWrapperForBinder) Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).getBinder("bulb_v"))).getData();

        ColorView colorView = findViewById(R.id.colorView);
        colorView.listenToColorPallet(this::onEvent);

        ImageButton dateButton = findViewById(R.id.btn_date);
        ImageButton movieButton = findViewById(R.id.btn_movie);

        dateButton.setOnClickListener(arg0 -> {
            try {
                if (bulbs.get(0).getDevice().getState().equals("off")) {
                    bulbs.get(0).getDevice().setPower(true);
                }
                bulbs.get(0).getDevice().setRGB(255,0,255);
                bulbs.get(0).getDevice().setBrightness(60);
            } catch (YeelightResultErrorException | YeelightSocketException e) {
                e.printStackTrace();
                reset_limit();
            }
        });
        movieButton.setOnClickListener(arg0 -> {
            try {
                if (bulbs.get(0).getDevice().getState().equals("off")) {
                    bulbs.get(0).getDevice().setPower(true);
                }
                bulbs.get(0).getDevice().setRGB(20,20,50);
                bulbs.get(0).getDevice().setBrightness(50);
            } catch (YeelightResultErrorException | YeelightSocketException e) {
                e.printStackTrace();
                reset_limit();
            }
        });

        SeekBar brightness_bar = findViewById(R.id.brightness_bar);
        brightness_bar.setOnSeekBarChangeListener(seekBarChangeListener);
        bulbs = room.getBulbList();
        if (bulbs.get(0).getDevice() != null) {

            YeelightDevice device = bulbs.get(0).getDevice();
            try {
                if (device == null) {
                    device = new YeelightDevice(bulbs.get(0).getIp());
                }
                brightness_bar.setProgress(Integer.parseInt(device.getBrightness()));
            } catch (YeelightSocketException | YeelightResultErrorException e) {
                if (Objects.requireNonNull(e.getMessage()).contains("Broken pipe")) {
                    try {
                        device = new YeelightDevice(bulbs.get(0).getIp());
                        bulbs.get(0).setDevice(device);
                    } catch (YeelightSocketException e1) {
                        e1.printStackTrace();
                    }
                }
                //if device is offline
                e.printStackTrace();
            }
        }
    }

    public void onEvent(int color,int touchX,int touchY) {
        String hexColor = String.format("#%06X", (0xFFFFFF & color));
        int red = Integer.valueOf( hexColor.substring( 1, 3 ), 16 );
        int green = Integer.valueOf( hexColor.substring( 3, 5 ), 16 );
        int blue = Integer.valueOf( hexColor.substring( 5, 7 ), 16 );
        //Log.d("Touch event!", "red= "+red+" green: "+green+" blue:"+blue+" valueInt: "+color+" touchX: "+touchX+" touchY: "+touchY);
        try {
            bulbs.get(0).getDevice().setRGB(red, green, blue);
        } catch (YeelightResultErrorException | YeelightSocketException e) {
            e.printStackTrace();
            reset_limit();
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            try {
                for (int i = 0; i < bulbs.size(); i++) {
                    YeelightDevice device = bulbs.get(0).getDevice();
                    device.setBrightness(seekBar.getProgress());
                }
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

    private void reset_limit() {
        for (int i = 0; i < bulbs.size(); i++) {
            try {
                YeelightDevice device = new YeelightDevice(bulbs.get(0).getIp());
                bulbs.get(0).setDevice(device);
            } catch (YeelightSocketException e1) {
                e1.printStackTrace();
            }
        }
    }
}