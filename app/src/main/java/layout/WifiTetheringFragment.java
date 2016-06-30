package layout;


import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import tethering.WifiTethering;

public class WifiTetheringFragment extends Fragment {

    WifiTethering wifiTethering;
    Switch wifiSwitch;
    View view;

    public WifiTetheringFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_wifi_tethering, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.wifiTethering = new WifiTethering(this.view);
        this.wifiSwitch = (Switch) this.view.findViewById(R.id.wifi_tethering_switch);
        this.wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    WifiTetheringFragment.this.wifiTethering.startTethering();
                } else {
                    WifiTetheringFragment.this.wifiTethering.stopTethering();
                }
            }
        });
    }

}
