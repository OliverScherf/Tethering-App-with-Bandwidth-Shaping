package layout;


import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import java.lang.reflect.Field;

import tethering.WifiTethering;

public class WifiTetheringFragment extends Fragment {

    private WifiTethering wifiTethering;
    private Switch wifiSwitch;
    private View view;

    public WifiTetheringFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_wifi_tethering, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateConfig();
    }

    private void updateConfig() {
        WifiConfiguration config = this.wifiTethering.getWifiApConfig();
        ((Switch) this.view.findViewById(R.id.wifi_tethering_switch)).setChecked(this.wifiTethering.isWifiApEnabled());
        ((EditText) this.view.findViewById(R.id.wifi_ssid_edit_text)).setText(config.SSID);
        ((EditText) this.view.findViewById(R.id.wifi_password_edit_text)).setText(config.preSharedKey);
        ((CheckBox) this.view.findViewById(R.id.wifi_hide_ssid_check_box)).setChecked(config.hiddenSSID);
        Spinner encryptSpinner = ((Spinner) this.view.findViewById(R.id.wifi_encryption_spinner));
        if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
            encryptSpinner.setSelection(0);
        } else if (config.allowedKeyManagement.get(WifiTethering.WPA2_PSK2)) {
            encryptSpinner.setSelection(1);
        }

        Spinner broadcastSpinner = ((Spinner) this.view.findViewById(R.id.wifi_broadcast_channel_spinner));
        int channelNumber = 0;
        for (Field f : config.getClass().getFields()) {
            if (f.getName().equals("channel")) {
                try {
                    channelNumber = f.getInt(config);
                } catch (IllegalAccessException e) {
                    Log.e("Fragment", "", e);
                }
            }
        }
        broadcastSpinner.setSelection(channelNumber);
    }

    @Override
    public void onStart() {
        super.onStart();
        this.wifiTethering = new WifiTethering((WifiManager) this.view.getContext().getSystemService(Context.WIFI_SERVICE));
        this.wifiSwitch = (Switch) this.view.findViewById(R.id.wifi_tethering_switch);
        this.wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(view.getContext(), "Turning WLAN tethering on", Toast.LENGTH_SHORT).show();
                    String ssid = ((EditText) view.findViewById(R.id.wifi_ssid_edit_text)).getText().toString();
                    String password = ((EditText) view.findViewById(R.id.wifi_password_edit_text)).getText().toString();
                    boolean hiddenSSID = ((CheckBox) view.findViewById(R.id.wifi_hide_ssid_check_box)).isChecked();
                    boolean encryption = ((Spinner) view.findViewById(R.id.wifi_encryption_spinner)).getSelectedItemPosition() == 1;
                    int broadcastChannel = ((Spinner) view.findViewById(R.id.wifi_encryption_spinner)).getSelectedItemPosition();
                    wifiTethering.startTethering(ssid, password, hiddenSSID, encryption, broadcastChannel);
                } else {
                    Toast.makeText(view.getContext(), "Turning WLAN tethering off", Toast.LENGTH_SHORT).show();
                    WifiTetheringFragment.this.wifiTethering.stopTethering();
                }
            }
        });
    }

}
