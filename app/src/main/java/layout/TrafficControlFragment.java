package layout;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

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
        this.listView = (ListView) this.view.findViewById(R.id.device_list_view);
        this.refreshButton = (Button) this.view.findViewById(R.id.connected_devices_refresh_button);
        this.deleteButton = (Button) this.view.findViewById(R.id.delete_rules_button);

        final ArrayList<Device> strArr = new ArrayList<Device>();
        this.trafficControl = new TrafficControl(strArr);

        adapter = new DeviceArrayAdapter(this.getContext(), R.layout.device_row, strArr);
        //View header = (View)getLayoutInflater().inflate(R.layout.listview_header_row, null);
        //listView1.addHeaderView(header);
        this.listView.setAdapter(adapter);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.refreshDevices();
                adapter.notifyDataSetChanged();
            }
        });
        this.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trafficControl.removceRules();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
