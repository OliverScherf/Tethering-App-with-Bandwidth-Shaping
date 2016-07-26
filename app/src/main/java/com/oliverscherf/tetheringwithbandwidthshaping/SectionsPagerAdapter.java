package com.oliverscherf.tetheringwithbandwidthshaping;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import layout.BluetoothTetheringFragment;
import layout.TrafficControlFragment;
import layout.UsbTetheringFragment;
import layout.WifiTetheringFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private WifiTetheringFragment wifiTetheringFragment;
    private BluetoothTetheringFragment bluetoothTetheringFragment;
    private UsbTetheringFragment usbTetheringFragment;
    private TrafficControlFragment trafficControlFragment;


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        this.wifiTetheringFragment = new WifiTetheringFragment();
        this.bluetoothTetheringFragment = new BluetoothTetheringFragment();
        this.usbTetheringFragment = new UsbTetheringFragment();
        this.trafficControlFragment = new TrafficControlFragment();
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return this.trafficControlFragment;
        } else if (position == 1) {
            return this.wifiTetheringFragment;
        } else if (position == 2) {
            return this.usbTetheringFragment;
        } else if (position == 3) {
            return this.bluetoothTetheringFragment;
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}