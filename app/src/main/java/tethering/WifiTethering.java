package tethering;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

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
            if (method.getName().equals("setWifiApEnabled")) {
                this.setWifiApEnabled = method;
            } else if (method.getName().equals("isWifiApEnabled")) {
                this.isWifiApEnabled = method;
            } else if (method.getName().equals("getWifiApConfiguration")) {
                this.getWifiApConfiguration = method;
            }
        }
        this.config = this.getWifiApConfig();
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

    public boolean isTetheringEnabled() {
        try {
            return (boolean) this.isWifiApEnabled.invoke(this.wifiManager);
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
        return false;
    }

    private void updateConfig(String ssid, String password, boolean hiddenSSID, boolean encryption, int broadcastChannel) {
        this.config.SSID = ssid;
        this.config.hiddenSSID = hiddenSSID;
        if (!encryption) {
            this.log("Open/Unsecure");
            this.config.allowedKeyManagement.set(WPA2_PSK2, false);
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            this.log("WPA2 PSK");
            this.config.preSharedKey = password;
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE, false);
            this.config.allowedKeyManagement.set(WPA2_PSK2);
        }
        try {
            this.config.getClass().getField("channel").setInt(this.config, broadcastChannel);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
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
