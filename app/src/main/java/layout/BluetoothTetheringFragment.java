package layout;


import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import tethering.BluetoothTethering;

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
        this.bluetoothTethering = new BluetoothTethering(this.getContext(), (ConnectivityManager) this.getContext().getSystemService(Context.CONNECTIVITY_SERVICE));
        this.bluetoothSwitch = (Switch) this.view.findViewById(R.id.bluetooth_tethering_switch);
        this.bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Toast.makeText(getContext(), "Turning Bluetooth tethering on", Toast.LENGTH_SHORT).show();
                    BluetoothTetheringFragment.this.bluetoothTethering.startTethering();
                } else {
                    Toast.makeText(getContext(), "Turning Bluetooth tethering off", Toast.LENGTH_SHORT).show();
                    BluetoothTetheringFragment.this.bluetoothTethering.stopTethering();
                }
            }
        });
    }
}
