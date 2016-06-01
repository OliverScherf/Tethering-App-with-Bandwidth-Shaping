package layout;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BluetoothTetheringFragment extends Fragment {


    public BluetoothTetheringFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth_tethering, container, false);
    }

}
