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
    private ListView listView;
    private DeviceArrayAdapter adapter;
    private Button refreshButton;
    private Button deleteButton;
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
        Log.d("LOL", "New Version222222222");
        this.listView = (ListView) this.view.findViewById(R.id.device_list_view);
        this.refreshButton = (Button) this.view.findViewById(R.id.connected_devices_refresh_button);
        this.refreshTraffic = (Button) this.view.findViewById(R.id.refresh_traffic_button);
        this.deleteButton = (Button) this.view.findViewById(R.id.delete_rules_button);

        final ArrayList<Device> strArr = new ArrayList<Device>();
        this.trafficControl = new TrafficControl(strArr);

        adapter = new DeviceArrayAdapter(this.getContext(), R.layout.device_row, strArr);


        this.listView.setAdapter(adapter);
        this.listView.setOnItemClickListener(this.getTrafficControlDialog());
        Button confirmButton = (Button) this.view.findViewById(R.id.confirm_traffic_limit_button);
        Button cancelButton = (Button) this.view.findViewById(R.id.cancel_traffic_limit_button);


        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.refreshDevices();
                adapter.notifyDataSetChanged();
                Toast.makeText(TrafficControlFragment.this.getContext(), "Done", Toast.LENGTH_SHORT).show();
            }
        });
        this.refreshTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.refreshTrafficStats();
                adapter.notifyDataSetChanged();
                Toast.makeText(TrafficControlFragment.this.getContext(), "Done", Toast.LENGTH_SHORT).show();
            }
        });
        this.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.flushChains();
                Toast.makeText(TrafficControlFragment.this.getContext(), "Done", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    public AdapterView.OnItemClickListener getTrafficControlDialog() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Device d = (Device) listView.getItemAtPosition(position);
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.traffic_limit_dialog);
                final EditText downloadLimit = (EditText) dialog.findViewById(R.id.download_limit);
                final EditText uploadLimit = (EditText) dialog.findViewById(R.id.upload_limit);
                dialog.setTitle("Traffic Control");
                dialog.show();
                final Button confirm = (Button) dialog.findViewById(R.id.confirm_traffic_limit_button);
                final Button cancel = (Button) dialog.findViewById(R.id.cancel_traffic_limit_button);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {
                        Log.d("",confirm.toString());
                        String downloadLimitStr = downloadLimit.getText().toString();
                        String uploadLimitStr = uploadLimit.getText().toString();
                        int downloadLimitInt = Integer.parseInt(downloadLimitStr);
                        int uploadLimitInt = Integer.parseInt(uploadLimitStr);
                        trafficControl.writeLimitRule(d, downloadLimitInt, uploadLimitInt);
                        dialog.dismiss();
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {
                        trafficControl.deleteLimitRules(d);
                        dialog.dismiss();
                    }
                });
            }
        };
    }
}