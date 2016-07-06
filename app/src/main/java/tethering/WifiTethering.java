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

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import utils.Loggable;
import utils.ShellExecutor;

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
        this.setInitialWifiSettings();
        this.debugPrintConfig();
    }

    private void debugPrintConfig() {
        /*this.log(this.config.toString());*/
    }

    @Override
    public void startTethering() {
        this.updateConfig();
        //this.debugPrintConfig();
        try {
            this.setWifiApEnabled.invoke(this.wifiManager, this.config, true);
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
    }

    private void setInitialWifiSettings() {
        try {
            this.config = (WifiConfiguration) this.getWifiApConfiguration.invoke(this.wifiManager);
            this.debugPrintConfig();
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
        ((Switch) this.view.findViewById(R.id.wifi_tethering_switch)).setChecked(this.getTetheringStatus() == WifiManager.WIFI_STATE_ENABLED
                || this.getTetheringStatus() == WifiManager.WIFI_STATE_ENABLING);
        ((EditText) this.view.findViewById(R.id.wifi_ssid_edit_text)).setText(this.config.SSID);
        ((EditText) this.view.findViewById(R.id.wifi_password_edit_text)).setText(this.config.preSharedKey);
        ((CheckBox) this.view.findViewById(R.id.wifi_hide_ssid_check_box)).setChecked(this.config.hiddenSSID);
    }

    private void updateConfig() {
        this.config.SSID = ((EditText) this.view.findViewById(R.id.wifi_ssid_edit_text)).getText().toString();
        this.config.hiddenSSID = ((CheckBox) this.view.findViewById(R.id.wifi_hide_ssid_check_box)).isChecked();
        String chosenEncryptionType = ((Spinner) this.view.findViewById(R.id.wifi_encryption_spinner)).getSelectedItem().toString();
        String[] availableEncryptionTypes = this.view.getContext().getResources().getStringArray(R.array.wifi_security_spinner);
        if (chosenEncryptionType.equals(availableEncryptionTypes[0])) {
            // Open/Unsecure
            this.log("Open/Unsecure");
            this.config.allowedKeyManagement.set(4, false);
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (chosenEncryptionType.equals(availableEncryptionTypes[1])) {
            // WPA2 PSK
            this.log("WPA2 PSK");
            this.config.preSharedKey = ((EditText) this.view.findViewById(R.id.wifi_password_edit_text)).getText().toString();
            //this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            // TODO: 4 als Konstante einführen. 4 ist WPA2_PSK Konstante im WifiConfiguration.KeyMgmt, jedoch kommt ein Compiler error wenn ich versuche die Konstante zu verwenden
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE, false);
            this.config.allowedKeyManagement.set(4);
        }

        /*
        int channelNumber = -1;
        Spinner broadcastChannelSpinner = (Spinner) this.view.findViewById(R.id.wifi_broadcast_channel_spinner);
         if (broadcastChannelSpinner.getSelectedItem().toString().equals("Auto")) {
            channelNumber = 0;
        } else {
             channelNumber = Integer.parseInt(broadcastChannelSpinner.getSelectedItem().toString());
         }
        if (channelNumber != -1) {
            this.setBroadcastChannel(channelNumber);
        } else {
            this.err("Fehler beim Channel Number bekommen.", null);
        }

        this.wifiManager.saveConfiguration();*/
    }

    private void setBroadcastChannel(int channelNumber) {
        try {
            String configContent = ShellExecutor.execute("cat /data/misc/wifi/testing.conf");
            String[] lines = configContent.split("\n");
            ShellExecutor.execute("rm /data/misc/wifi/written.conf");
            ShellExecutor.execute("touch /data/misc/wifi/written.conf");
            for(String line : lines) {
                this.log("Lines line:" + line.toString());
                this.log("Response: " + ShellExecutor.execute("echo " + line + " >> /data/misc/wifi/written.conf"));
            }
        } catch (IOException e) {
            this.err("",e);
        } catch (InterruptedException e) {
            this.err("", e);
        } catch (Exception e) {
            this.err("",e);
        }
    }

    @Override
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
    public int getTetheringStatus() {
        try {
            return (Integer) this.getWifiApState.invoke(this.wifiManager);
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
        return -1;
    }

    @Override
    public void log(String msg) {
        Log.d("WifiTethering", msg);
        EditText debugTextArea = ((EditText) this.view.findViewById(R.id.debug_messages));
        debugTextArea.append(msg + '\n');

    }

    @Override
    public void err(String msg, Throwable t) {
        Log.e("WifiTethering", msg, t);
    }
}
