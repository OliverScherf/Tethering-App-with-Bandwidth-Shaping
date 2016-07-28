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

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import tethering.UsbTethering;

public class UsbTetheringFragment extends Fragment {

    UsbTethering usbTethering;
    Switch usbTetheringSwitch;
    View view;

    public UsbTetheringFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_usb_tethering, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.usbTetheringSwitch.setChecked(this.usbTethering.isUsbTetheringEnabled());
    }

    @Override
    public void onStart() {
        super.onStart();
        this.usbTethering = new UsbTethering((ConnectivityManager) this.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE));
        this.usbTetheringSwitch = (Switch) this.view.findViewById(R.id.usb_tethering_switch);
        this.usbTetheringSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    UsbTetheringFragment.this.usbTethering.startTethering();
                } else {
                    UsbTetheringFragment.this.usbTethering.stopTethering();
                }
            }
        });
    }

}
