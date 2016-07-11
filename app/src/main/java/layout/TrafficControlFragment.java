package layout;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrafficControlFragment extends Fragment {

    View view;
    private ListView listView;
    private ArrayList<String> strArr;
    private ArrayAdapter<String> adapter;
    private Button button;


    public TrafficControlFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_traffic_control, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.listView = (ListView) this.view.findViewById(R.id.device_list_view);
        this.button = (Button) this.view.findViewById(R.id.connected_devices_refresh_button);
        this.strArr = new ArrayList<String>();
        for (int i = 0; i < 5; ++i) {
            this.strArr.add("Row" + i);
        }
        adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, strArr);
        this.listView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                strArr.add("hallo");
                adapter.notifyDataSetChanged();
            }
        });
    }
}
