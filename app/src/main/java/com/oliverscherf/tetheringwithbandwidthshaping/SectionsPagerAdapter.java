package com.oliverscherf.tetheringwithbandwidthshaping;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import layout.BluetoothTetheringFragment;
import layout.PlaceholderFragment;
import layout.WifiTetheringFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private WifiTetheringFragment wifiTetheringFragment;
    private BluetoothTetheringFragment bluetoothTetheringFragment;


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
        this.wifiTetheringFragment = new WifiTetheringFragment();
        this.bluetoothTetheringFragment = new BluetoothTetheringFragment();
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("SectionsPagerAdapter", "getItem");
        // getItem is called to instantiate the fragment for the given page.
        //Wifi
        if (position == 0) {
            Log.d("SectionsPagerAdapter", "getItem: WifiTethering");
            return this.wifiTetheringFragment;
        //Bluetooth
        } else if (position == 1) {
            Log.d("SectionsPagerAdapter", "getItem: BluetoothTethering");
            return this.bluetoothTetheringFragment;
        } else {
            Log.d("SectionsPagerAdapter", "getItem: Position war nicht 0 und nicht 1");
            return null;
        }


    }

    @Override
    public int getCount() {
        Log.d("SectionsPagerAdapter", "getCount");
        // Show 3 total pages.
        return 2;
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