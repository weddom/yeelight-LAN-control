package com.yeelightcontrol.room;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import com.yeelightcontrol.MainActivity;
import com.yeelightcontrol.R;
import com.yeelightcontrol.database.Bulb;
import com.yeelightcontrol.database.Db;

public class RoomAdd extends AppCompatActivity {

    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_add);

        getSupportActionBar().setTitle("Add room");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        EditText room_name = findViewById(R.id.room_name);
        room_name.requestFocus();

        String roomName = "" + getIntent().getStringExtra("ROOM_NAME");
        List<String> stringBulbList = new ArrayList<>();

        Button next_button = findViewById(R.id.next);
        next_button.setAlpha(.5f);
        next_button.setEnabled(false);

        room_name.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 2) {
                    next_button.setEnabled(true);
                    next_button.setAlpha(1);
                }
            }
        });

        next_button.setOnClickListener(v -> {
            Db db = new Db(getApplicationContext());
            db.insertRoom(roomName);
            int len = lv.getCount();
            SparseBooleanArray checked = lv.getCheckedItemPositions();
            for (int i = 0; i < len; i++) {
                if (checked.get(i)) {
                    String[] lines = stringBulbList.get(i).split("\\r?\\n");
                    db.insertBulbInRoom(roomName, lines[1].replaceAll("\\s+", ""));
                }
            }
            db.close();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        Db db = new Db(getApplicationContext());
        ArrayList<Bulb> bulb_ar = db.getAllBulbs();
        db.close();
        for (Bulb b : bulb_ar) {
            stringBulbList.add(b.getIp() + "\n" + b.getDevice_id());
        }
        lv = findViewById(R.id.added_device_list);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_2, stringBulbList));
        lv.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            if (lv.getCheckedItemCount() > 0) {
                next_button.setEnabled(true);
                next_button.setAlpha(1);
            } else {
                next_button.setAlpha(.5f);
                next_button.setEnabled(false);
            }
        });
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, stringBulbList));
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        setListViewHeight(lv, stringBulbList);
    }

    public static void setListViewHeight(ListView lv,List<String> stringBulbList) {
        ListAdapter listAdapter = lv.getAdapter();
        int totalHeight = lv.getPaddingTop() + lv.getPaddingBottom();
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

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return false;
    }
}