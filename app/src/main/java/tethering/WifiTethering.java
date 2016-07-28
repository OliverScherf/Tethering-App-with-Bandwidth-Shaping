package tethering;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import utils.Loggable;

/**
 * Created by Oliver on 25.05.2016.
 */
public class WifiTethering implements Loggable {

    public static final int WPA2_PSK2 = 4;

    private WifiManager wifiManager;
    private WifiConfiguration config;
    private Method setWifiApEnabled;
    private Method isWifiApEnabled;
    private Method getWifiApConfiguration;

    public WifiTethering(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        for (Method method : this.wifiManager.getClass().getDeclaredMethods()) {
            try {
                if (method.getName().equals("setWifiApEnabled")) {
                    this.setWifiApEnabled = method;
                } else if (method.getName().equals("isWifiApEnabled")) {
                    this.isWifiApEnabled = method;
                } else if (method.getName().equals("getWifiApConfiguration")) {
                    this.getWifiApConfiguration = method;
                }
            } catch (Exception ex) {
                this.err("Exception while iterate though the methods of the WifiManager. ", ex);
            }
        }
        this.setInitialWifiSettings();
    }

    public void startTethering(String ssid, String password, boolean hiddenSSID, boolean encryption, int broadcastChannel) {
        this.updateConfig(ssid, password, hiddenSSID, encryption, broadcastChannel);
        try {
            this.setWifiApEnabled.invoke(this.wifiManager, this.config, true);
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
    }

    public WifiConfiguration getWifiApConfig() {
        try {
            return (WifiConfiguration) this.getWifiApConfiguration.invoke(this.wifiManager);
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
        return null;
    }

    public boolean isWifiApEnabled() {
        try {
            return (boolean) this.isWifiApEnabled.invoke(this.wifiManager);
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
        return false;
    }

    private void setInitialWifiSettings() {
        try {
            this.config = (WifiConfiguration) this.getWifiApConfiguration.invoke(this.wifiManager);
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
    }

    private void updateBroadcastChannel(int channelNumber) {
        for (Field f : this.config.getClass().getFields()) {
            this.log(f.getName());
            if (f.getName().equals("channel")) {
                try {
                    f.setInt(this.config, channelNumber);
                } catch (IllegalAccessException e) {
                    this.err("Reflection Error", e);
                }
            }
        }
    }

    private void updateConfig(String ssid, String password, boolean hiddenSSID, boolean encryption, int broadcastChannel) {
        this.config.SSID = ssid;
        this.config.hiddenSSID = hiddenSSID;
        if (encryption) {
            this.log("Open/Unsecure");
            this.config.allowedKeyManagement.set(4, false);
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            this.log("WPA2 PSK");
            this.config.preSharedKey = password;
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE, false);
            this.config.allowedKeyManagement.set(WPA2_PSK2);
        }
        this.updateBroadcastChannel(broadcastChannel);
        this.wifiManager.saveConfiguration();
    }

    public void stopTethering() {
        try {
            this.setWifiApEnabled.invoke(this.wifiManager, null, false);
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
    }

    @Override
    public void log(String msg) {
        Log.d("WifiTethering", msg);
    }

    @Override
    public void err(String msg, Throwable t) {
        Log.e("WifiTethering", msg, t);
    }
}
