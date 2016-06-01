package tethering;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import utils.Loggable;

/**
 * Created by Oliver on 25.05.2016.
 */
public class WifiTethering implements Tetherable, Loggable {

    private WifiManager wifiManager;
    private WifiConfiguration config;
    private Method setWifiApEnabled;
    private Method isWifiApEnabled;
    private Method getWifiApState;
    private Method getWifiApConfiguration;
    private View view;

    public WifiTethering(View view) {
        this.view = view;
        if (view == null) {
            this.log("View war null");
        }
        if (view.getContext() == null) {
            this.log("Context war null");
        }
        this.config = new WifiConfiguration();
        this.wifiManager = (WifiManager) this.view.getContext().getSystemService(Context.WIFI_SERVICE);
        Method[] methods = this.wifiManager.getClass().getDeclaredMethods();
        for (Method method : methods) {
            try {
                if (method.getName().equals("setWifiApEnabled")) {
                    this.setWifiApEnabled = method;
                } else if (method.getName().equals("isWifiApEnabled")) {
                    this.isWifiApEnabled = method;
                } else if (method.getName().equals("getWifiApState")) {
                    this.getWifiApState = method;
                } else if (method.getName().equals("getWifiApConfiguration")) {
                    this.getWifiApConfiguration = method;
                }
            } catch (Exception ex) {
                this.err("Exception while iterate though the methods of the WifiManager. ", ex);
            }
        }
    }

    @Override
    public void startTethering() {
        this.updateConfig();
        try {
            this.setWifiApEnabled.invoke(this.wifiManager, this.config, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void updateConfig() {
        this.config.SSID = ((EditText) this.view.findViewById(R.id.wifi_ssid_edit_text)).getText().toString();
        this.config.hiddenSSID = ((CheckBox) this.view.findViewById(R.id.wifi_hide_ssid_check_box)).isChecked();
        String encryptionType = ((Spinner) this.view.findViewById(R.id.wifi_encryption_spinner)).getSelectedItem().toString();
        String[] encryptionTypes = this.view.getContext().getResources().getStringArray(R.array.wifi_security_spinner);
        if (encryptionType.equals(encryptionTypes[0])) {
            // Open/Unsecure
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (encryptionType.equals(encryptionTypes[1])) {
            // WPA2 PSK
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        }
        this.config.preSharedKey = ((EditText) this.view.findViewById(R.id.wifi_password_edit_text)).toString();
        // TODO: Broadcast Channels
    }

    @Override
    public void stopTethering() {
        try {
            this.setWifiApEnabled.invoke(this.wifiManager, null, false);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getTetheringStatus() {
        try {
            return (Integer) this.getWifiApState.invoke(this.wifiManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return -1;
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
