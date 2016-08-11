package layout;


import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import java.util.ArrayList;

import trafficcontrol.Device;
import trafficcontrol.DeviceArrayAdapter;
import trafficcontrol.TrafficControl;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrafficControlFragment extends Fragment {

    private View view;
    private ListView deviceListView;
    private DeviceArrayAdapter adapter;
    private TrafficControl trafficControl;
    private TrafficStatsRefresher refresher;

    public TrafficControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStop() {
        Log.d("LOL", "stopped");
        this.refresher.cancel(true);
        super.onStop();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!this.refresher.isCancelled()) {
            this.refresher.cancel(true);
        }
        this.refresher = new TrafficStatsRefresher((Activity) this.getContext());
        this.refresher.execute(null, null, null);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_traffic_control, container, false);
        this.init();
        return view;
    }

    private void init() {
        this.deviceListView = (ListView) this.view.findViewById(R.id.device_list_view);
        Button refreshButton = (Button) this.view.findViewById(R.id.connected_devices_refresh_button);
        Button resetCountersButton = (Button) this.view.findViewById(R.id.reset_counters_button);
        Button deleteAllRules = (Button) this.view.findViewById(R.id.delete_all_rules_button);
        final ArrayList<Device> strArr = new ArrayList<Device>();
        this.trafficControl = new TrafficControl(strArr);
        this.adapter = new DeviceArrayAdapter(this.getContext(), R.layout.device_row, strArr);

        this.deviceListView.setAdapter(this.adapter);
        this.deviceListView.setOnItemClickListener(this.openTrafficControlDialog());

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.refreshConnectedDevices();
                trafficControl.refreshTrafficCounter();
                adapter.notifyDataSetChanged();
                Toast.makeText(TrafficControlFragment.this.getContext(), "Devices refreshed", Toast.LENGTH_SHORT).show();
            }
        });
        resetCountersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.resetTrafficCounter();
                trafficControl.refreshTrafficCounter();
                adapter.notifyDataSetChanged();
                Toast.makeText(TrafficControlFragment.this.getContext(), "Traffic Counter resetted", Toast.LENGTH_SHORT).show();
            }
        });
        deleteAllRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.removeAllSpeedLimits();
                adapter.notifyDataSetChanged();
                Toast.makeText(TrafficControlFragment.this.getContext(), "All Rules deleted", Toast.LENGTH_SHORT).show();
            }
        });

        this.trafficControl.refreshConnectedDevices();
        this.trafficControl.refreshTrafficCounter();
        this.trafficControl.parseSpeedLimits();
        this.adapter.notifyDataSetChanged();
        this.refresher = new TrafficStatsRefresher((Activity) this.getContext());
        this.refresher.execute(null, null, null);
    }

    public AdapterView.OnItemClickListener openTrafficControlDialog() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Device d = (Device) deviceListView.getItemAtPosition(position);
                if (d.ipAddress.equals("-")) {
                    return;
                }
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.traffic_limit_dialog);
                final EditText downloadLimit = (EditText) dialog.findViewById(R.id.download_limit);
                final EditText uploadLimit = (EditText) dialog.findViewById(R.id.upload_limit);
                downloadLimit.setText("0");
                uploadLimit.setText("0");
                dialog.setTitle("Traffic Limiting");
                dialog.show();
                final Button confirm = (Button) dialog.findViewById(R.id.confirm_traffic_limit_button);
                final Button deleteExistingRule = (Button) dialog.findViewById(R.id.delete_existing_rule_button);
                final Button cancel = (Button) dialog.findViewById(R.id.cancel_button);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {
                        Log.d("",confirm.toString());
                        String downloadLimitStr = downloadLimit.getText().toString();
                        String uploadLimitStr = uploadLimit.getText().toString();
                        try {
                            int downloadLimitInt = Integer.parseInt(downloadLimitStr);
                            int uploadLimitInt = Integer.parseInt(uploadLimitStr);
                            if (downloadLimitInt < 0 || uploadLimitInt < 0
                                    || downloadLimitInt > 100000 || uploadLimitInt > 100000) {
                                throw new Exception();
                            }
                            trafficControl.setSpeedLimits(d, downloadLimitInt, uploadLimitInt);
                        } catch (Exception e) {
                            Toast.makeText(TrafficControlFragment.this.getContext(), "Invalid limit", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                        dialog.dismiss();
                    }
                });
                deleteExistingRule.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {
                        trafficControl.removeSpeedLimits(d);
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        };
        return listener;
    }

    private class TrafficStatsRefresher extends AsyncTask<Integer, Integer, Integer> {

        private Activity activity;

        TrafficStatsRefresher(Activity activity) {
            super();
            this.activity = activity;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            while (true) {
                try {
                    Thread.sleep(1000);
                    trafficControl.refreshTrafficCounter();
                    Log.d("LOL", "Its refreshed!");
                } catch (InterruptedException e) {
                    break;
                }
                if (this.isCancelled()) {
                    break;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
            return 0;
        }
    }
}