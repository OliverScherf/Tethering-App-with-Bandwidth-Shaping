package com.oliverscherf.tetheringwithbandwidthshaping;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import layout.BluetoothTetheringFragment;
import layout.UsbTetheringFragment;
import layout.WifiTetheringFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private WifiTetheringFragment wifiTetheringFragment;
    private BluetoothTetheringFragment bluetoothTetheringFragment;
    private UsbTetheringFragment usbTetheringFragment;


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        this.wifiTetheringFragment = new WifiTetheringFragment();
        this.bluetoothTetheringFragment = new BluetoothTetheringFragment();
        this.usbTetheringFragment = new UsbTetheringFragment();
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        //Wifi
        if (position == 0) {
            return this.wifiTetheringFragment;
        //Bluetooth
        } else if (position == 1) {
            return this.usbTetheringFragment;
        } else if (position == 2) {
            return this.bluetoothTetheringFragment;
        } else {
            return null;
        }


    }

    @Override
    public int getCount() {
        return 3;
    }

    /*@Override
    public CharSequence getPageTitle(int position) {
        Log.d("SectionsPagerAdapter", "getPageTitle");
        switch (position) {
            case 0:
                return "SECTION 1";
            case 1:
                return "SECTION 2";
            case 2:
                return "SECTION 3";
        }
        return null;
    }*/
}