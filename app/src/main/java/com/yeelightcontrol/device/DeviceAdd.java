package com.yeelightcontrol.device;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.yeelightcontrol.MainActivity;
import com.yeelightcontrol.R;
import com.yeelightcontrol.database.Db;
import com.yeelightcontrol.database.Bulb;

public class DeviceAdd extends AppCompatActivity {

    private TextView refresh_text;
    private ImageView refresh_retry_img;
    private ProgressBar refresh_progress;
    private ListView lv;
    private Button add_device_button;

    private static final String message = "M-SEARCH * HTTP/1.1\r\n" + "HOST:239.255.255.250:1982\r\n" + "MAN:\"ssdp:discover\"\r\n" + "ST:wifi_bulb\r\n";
    private static final String UDP_HOST = "239.255.255.250";
    private static final int UDP_PORT = 1982;
    private static int timeout = 7000;
    private Db db;
    private ArrayList<Bulb> bulb_ar;
    private ArrayList<Bulb> newBulbs;

    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Db(this);
        bulb_ar = db.getAllBulbs();
        newBulbs = new ArrayList<>();

        setContentView(R.layout.activity_device_add);

        refresh_text = findViewById(R.id.refresh_text);
        refresh_retry_img = findViewById(R.id.refresh_retry_img);
        refresh_progress = findViewById(R.id.refresh_progress);
        lv = findViewById(R.id.device_list);

        getSupportActionBar().setTitle("Add device");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

        final Runnable dialog_wifi = this::wifi_dialog;
        Handler handler = new Handler();
        handler.postDelayed(dialog_wifi, 0);

        add_device_button = findViewById(R.id.next);
        scan_device_update_ui();

        add_device_button.setOnClickListener(v -> {
            db.insertBulb(newBulbs.get(position).getDevice_id(), newBulbs.get(position).getIp(),
                    newBulbs.get(position).getFW(), newBulbs.get(position).getPort(),
                    newBulbs.get(position).getSupport(), newBulbs.get(position).getName());
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);
            finish();
        });
        LinearLayout refresh_layout_group = findViewById(R.id.refresh_layout);
        refresh_layout_group.setOnClickListener(view -> {
            lv.setAdapter(null);
            scan_device_update_ui();
        });
    }

    protected void wifi_dialog() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Warning");
            alertDialogBuilder.setMessage(getResources().getString(R.string.wifi_warning)).setCancelable(false).setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            wifiManager.setWifiEnabled(true);
                        }})
                    .setNegativeButton("Settings",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    protected void onPause() {
        super.onPause();
        refresh_retry_img.setVisibility(View.VISIBLE);
        refresh_progress.setVisibility(View.GONE);
        refresh_text.setText("Retry searching");
    }

    public void scan_device() {
        DatagramSocket datagramSocket;
        ArrayList<String> devices = new ArrayList<>();
        try {
            datagramSocket = new DatagramSocket();
            DatagramPacket dpSend = new DatagramPacket(message.getBytes(), message.getBytes().length, InetAddress.getByName(UDP_HOST), UDP_PORT);
            datagramSocket.send(dpSend);
            while (true) {
                byte[] buff = new byte[1024];
                DatagramPacket dpRecv = new DatagramPacket(buff, buff.length);
                datagramSocket.setSoTimeout(timeout);
                datagramSocket.receive(dpRecv);
                byte[] bytes = dpRecv.getData();
                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i < dpRecv.getLength(); i++) {
                    if (bytes[i] == 13) {
                        continue;
                    }
                    buffer.append((char) bytes[i]);
                }
                if (!buffer.toString().contains("yeelight")) {
                    throw new UnknownHostException("Device not found");
                }
                String[] infos = buffer.toString().split("\n");
                HashMap<String, String> bulbInfo = new HashMap<>();
                for (String str : infos) {
                    int index = str.indexOf(":");
                    if (index == -1) {
                        continue;
                    }
                    String title = str.substring(0, index);
                    String value = str.substring(index + 1);
                    bulbInfo.put(title, value);
                }

                String location = bulbInfo.get("Location");
                assert location != null;
                String ipPort = location.substring(location.lastIndexOf("/") + 1);
                String ip = ipPort.substring(0, ipPort.indexOf(":"));
                String port = ipPort.substring(ipPort.indexOf(":") + 1);
                String support = bulbInfo.get("support");
                String fw = bulbInfo.get("fw_ver");
                String id = bulbInfo.get("id");
                String name = bulbInfo.get("name");
                boolean exists = false;
                if (!devices.contains(ip + ":" + port)) {
                    if (bulb_ar.size() > 0) {
                        for (Bulb bulb : bulb_ar) {
                            if (bulb.getDevice_id().equals(id)) {
                                exists = true;
                                break;
                            }
                        }
                    }
                    if (!exists) {
                        devices.add(ip + ":" + port);
                        newBulbs.add(new Bulb(ip, name, id, port, fw, support));
                    }
                }
            }
        } catch (SocketTimeoutException | UnknownHostException e) {
            e.printStackTrace();
            reset_ui_scan_again();
            timeout = timeout + 5000;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            update_listview(devices.parallelStream().toArray(String[]::new));
        }
    }

    public static void setListViewHeight(ListView lv, List<Bulb> stringBulbList) {
        ListAdapter listAdapter = lv.getAdapter();
        int totalHeight = lv.getPaddingTop() + lv.getPaddingBottom() + 200;
        for (int i = 0; i < (Math.min(stringBulbList.size(), 5)); i++) {
            View listItem = listAdapter.getView(i, null, lv);
            if (listItem instanceof ViewGroup) {
                listItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = lv.getLayoutParams();
        params.height = totalHeight + (lv.getDividerHeight() * (listAdapter.getCount() - 1));
        lv.setLayoutParams(params);
    }

    private void reset_ui_scan_again() {
        runOnUiThread(() -> {
            refresh_retry_img.setVisibility(View.VISIBLE);
            refresh_progress.setVisibility(View.GONE);
            refresh_text.setText("Retry searching");
        });
    }

    private void update_listview(String[] devices) {
        runOnUiThread(() -> {
            reset_ui_scan_again();
            lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devices));
            lv.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
                ListView lv1 = (ListView) arg0;
                position = arg2;
                TextView tv = (TextView) lv1.getChildAt(arg2);
                String s1 = tv.getText().toString();
                Toast.makeText(this, s1, Toast.LENGTH_LONG).show();
                add_device_button.setAlpha(1);
                add_device_button.setEnabled(true);
            });
            lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, devices));
            lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            setListViewHeight(lv, newBulbs);
        });
    }

    private void scan_device_update_ui() {
        runOnUiThread(() -> {
            runOnUiThread(() -> {
                add_device_button.setEnabled(false);
                refresh_retry_img.setVisibility(View.GONE);
                refresh_progress.setVisibility(View.VISIBLE);
                refresh_text.setText("Searching...");
            });

            Thread thread = new Thread() {
                @Override
                public void run() {
                    scan_device();
                }
            };
            thread.start();
        });
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return false;
    }
}