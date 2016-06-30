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
        EditText debugTextArea = ((EditText) this.view.findViewById(R.id.debug_messages));
        //debugTextArea.setText("");
        String configContent = "SSID: " + this.config.SSID + '\n';
        configContent += "Key: " + this.config.preSharedKey + '\n';
        configContent += "hiddenSSID: " + this.config.hiddenSSID + '\n';
        debugTextArea.append(configContent);
    }

    @Override
    public void startTethering() {
        this.updateConfig();
        this.debugPrintConfig();
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
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
        //((Switch) this.view.findViewById(R.id.wifi_tethering_switch)).setChecked(this.getTetheringStatus() == WifiManager.WIFI_STATE_ENABLED
        //        || this.getTetheringStatus() == WifiManager.WIFI_STATE_ENABLING);
        ((EditText) this.view.findViewById(R.id.wifi_ssid_edit_text)).setText(this.config.SSID);
        ((EditText) this.view.findViewById(R.id.wifi_password_edit_text)).setText(this.config.preSharedKey);
        ((CheckBox) this.view.findViewById(R.id.wifi_hide_ssid_check_box)).setChecked(this.config.hiddenSSID);
    }

    // TODO: Config funktioniert noch nicht bei Verschlüsselung, der Rest läuft soweit
    private void updateConfig() {
        this.config.SSID = "\"" +((EditText) this.view.findViewById(R.id.wifi_ssid_edit_text)).getText().toString() +  "\"";
        this.config.hiddenSSID = ((CheckBox) this.view.findViewById(R.id.wifi_hide_ssid_check_box)).isChecked();


        String encryptionType = ((Spinner) this.view.findViewById(R.id.wifi_encryption_spinner)).getSelectedItem().toString();
        String[] encryptionTypes = this.view.getContext().getResources().getStringArray(R.array.wifi_security_spinner);
        if (encryptionType.equals(encryptionTypes[0])) {
            // Open/Unsecure
            this.log("Open/Unsecure");
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (encryptionType.equals(encryptionTypes[1])) {
            // WPA2 PSK
            this.log("WPA2 PSK");
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            this.config.preSharedKey = "\"" + ((EditText) this.view.findViewById(R.id.wifi_password_edit_text)).toString() + "\"";
            this.config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            this.config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            this.config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            this.config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            this.config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            this.config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        }


        this.wifiManager.saveConfiguration();
        // TODO: Broadcast Channels
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
