package layout;


import android.app.Dialog;
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
    private Button refreshButton;
    private Button resetCountersButton;
    private Button deleteAllRules;
    private TrafficControl trafficControl;
    private Button refreshTraffic;

    public TrafficControlFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_traffic_control, container, false);
        this.init();
        return view;
    }

    private void init() {
        Log.d("LLO","New Version11111");
        this.deviceListView = (ListView) this.view.findViewById(R.id.device_list_view);
        this.refreshButton = (Button) this.view.findViewById(R.id.connected_devices_refresh_button);
        this.refreshTraffic = (Button) this.view.findViewById(R.id.refresh_traffic_button);
        this.resetCountersButton = (Button) this.view.findViewById(R.id.reset_counters_button);
        this.deleteAllRules = (Button) this.view.findViewById(R.id.delete_all_rules_button);
        final ArrayList<Device> strArr = new ArrayList<Device>();
        this.trafficControl = new TrafficControl(strArr);
        this.adapter = new DeviceArrayAdapter(this.getContext(), R.layout.device_row, strArr);

        this.deviceListView.setAdapter(this.adapter);
        this.deviceListView.setOnItemClickListener(this.openTrafficControlDialog());

        this.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.refreshDevices();
                trafficControl.refreshTrafficStats();
                adapter.notifyDataSetChanged();
                Toast.makeText(TrafficControlFragment.this.getContext(), "Devices refreshed", Toast.LENGTH_SHORT).show();
            }
        });
        this.refreshTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.refreshTrafficStats();
                adapter.notifyDataSetChanged();
                Toast.makeText(TrafficControlFragment.this.getContext(), "Traffic Counter refreshed", Toast.LENGTH_SHORT).show();
            }
        });
        this.resetCountersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.resetCounters();
                trafficControl.refreshTrafficStats();
                adapter.notifyDataSetChanged();
                Toast.makeText(TrafficControlFragment.this.getContext(), "Traffic Counter resetted", Toast.LENGTH_SHORT).show();
            }
        });
        this.deleteAllRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.deleteAllLimitRules();
                adapter.notifyDataSetChanged();
                Toast.makeText(TrafficControlFragment.this.getContext(), "All Rules deleted", Toast.LENGTH_SHORT).show();
            }
        });

        trafficControl.refreshDevices();
        trafficControl.refreshTrafficStats();
        trafficControl.parseLimits();
        adapter.notifyDataSetChanged();
    }

    public AdapterView.OnItemClickListener openTrafficControlDialog() {
        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Device d = (Device) deviceListView.getItemAtPosition(position);
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
                            trafficControl.writeLimitRule(d, downloadLimitInt, uploadLimitInt);
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
                        trafficControl.deleteLimitRules(d);
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
}