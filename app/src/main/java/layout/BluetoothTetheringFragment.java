package layout;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import tethering.BluetoothTethering;
import tethering.WifiTethering;

/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothTetheringFragment extends Fragment {

    private BluetoothTethering bluetoothTethering;
    private View view;
    private Switch bluetoothSwitch;

    public BluetoothTetheringFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.view = inflater.inflate(R.layout.fragment_bluetooth_tethering, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.bluetoothTethering = new BluetoothTethering(this.getContext());
        this.bluetoothSwitch = (Switch) this.view.findViewById(R.id.bluetooth_tethering_switch);
        this.bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    BluetoothTetheringFragment.this.bluetoothTethering.startTethering();
                } else {
                    BluetoothTetheringFragment.this.bluetoothTethering.stopTethering();
                }
            }
        });
    }
}
