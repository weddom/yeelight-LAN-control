package com.yeelightcontrol.room;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import com.yeelightcontrol.MainActivity;
import com.yeelightcontrol.R;
import com.yeelightcontrol.database.Room;
import com.yeelightcontrol.database.Db;

public class RoomFragment extends Fragment {
    View view;

    @SuppressLint("StaticFieldLeak")
    private ArrayList<Room> Room_ar;
    private ListView lView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.room_fragment, container, false);

        setHasOptionsMenu(true);
        getActivity().setTitle("Room");

        Db db = new Db(getContext());
        Room_ar = db.getAllRooms();
        db.close();
        if (Room_ar.size() > 0) {
            TextView textView_deviceFragment = view.findViewById(R.id.textView_roomFragment);
            textView_deviceFragment.setVisibility(View.GONE);
            new update_ui().execute("");
        } else {
            TextView textView_deviceFragment = view.findViewById(R.id.textView_roomFragment);
            textView_deviceFragment.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void update_listview(View view) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }

        RoomFragmentAdapter adapter = new RoomFragmentAdapter(Room_ar, getContext());
        lView = view.findViewById(R.id.room_list);
        lView.setAdapter(adapter);
        lView.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
            Object obj = lView.getItemAtPosition(arg2);
            Room r = (Room) obj;
            final Bundle bundle = new Bundle();
            bundle.putBinder("bulb_v", new RoomObjectWrapperForBinder(r));
            startActivity(new Intent(getContext(), RoomControl.class).putExtras(bundle));
        });
        lView.setOnItemLongClickListener((arg0, arg1, pos, id) -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete room")
                    .setMessage("Are you sure you want to remove this room?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                        Room temp = (Room) lView.getItemAtPosition(pos);
                        ProgressDialog mDialog = new ProgressDialog(getContext());
                        mDialog.setMessage("Loading...");
                        mDialog.setCancelable(false);
                        mDialog.show();
                        Db db = new Db(getContext());
                        db.deleteRoom(temp.getName());
                        db.close();
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mDialog.dismiss();
                        requireContext().startActivity(intent);
                    })
                    .setNegativeButton(android.R.string.no, null).show();
            return true;
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.extra_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Intent myIntent = new Intent(getActivity(), RoomAdd.class);
            startActivity(myIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class update_ui extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            update_listview(view);
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

}