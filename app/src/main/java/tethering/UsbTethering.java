package tethering;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
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
public class UsbTethering implements Tetherable, Loggable {


    private Method usbTether;
    private View view;
    private ConnectivityManager connectivityManager;
    private String[] usbInterfaces;

    public UsbTethering(View view, ConnectivityManager connectivityManager) {
        this.view = view;
        this.connectivityManager = connectivityManager;
        for (Method method : this.connectivityManager.getClass().getDeclaredMethods()) {
            if (method.getName().equals("tether")) {
                //    method.invoke(systemService, "usb0");
                this.usbTether = method;
            } else if(method.getName().equals("getTetherableUsbRegexs")) {
                try {
                    this.usbInterfaces = (String[]) method.invoke(connectivityManager);
                } catch (IllegalAccessException e) {
                    this.err("Reflection Error", e);
                } catch (InvocationTargetException e) {
                    this.err("Reflection Error", e);
                } catch (Exception e) {
                    this.err("Other error", e);
                }
            }
        }
        for (String curInterface : usbInterfaces) {
            this.log("Interface " + curInterface);
        }
    }

    private void debugPrintConfig() {
        /*EditText debugTextArea = ((EditText) this.view.findViewById(R.id.debug_messages));
        //debugTextArea.setText("");
        String configContent = "SSID: " + this.config.SSID + '\n';
        configContent += "Key: " + this.config.preSharedKey + '\n';
        configContent += "hiddenSSID: " + this.config.hiddenSSID + '\n';
        debugTextArea.append(configContent); */
    }

    @Override
    public void startTethering() {

        try {
            for (String inf : this.usbInterfaces) {
                this.log("Versuche USB Tethering zu auf " + inf +  " zu starten");
                this.usbTether.invoke(this.connectivityManager, inf);
            }
            this.log("Versuche USB Tethering zu auf " + "rndis0" +  " zu starten");
            this.usbTether.invoke(this.connectivityManager, "rndis0");
            this.log("Versuche USB Tethering zu auf " + "usb0" +  " zu starten");
            this.usbTether.invoke(this.connectivityManager, "usb0");
            this.log("Versuche USB Tethering zu auf " + "ncm0" +  " zu starten");
            this.usbTether.invoke(this.connectivityManager, "ncm0");

        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
    }

    private void setInitialWifiSettings() {
       /* try {
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
        ((CheckBox) this.view.findViewById(R.id.wifi_hide_ssid_check_box)).setChecked(this.config.hiddenSSID); */
    }

    @Override
    public void stopTethering() {
        /* try {
            this.setWifiApEnabled.invoke(this.wifiManager, null, false);
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        } */
    }

    @Override
    public int getTetheringStatus() {
        /* try {
            return (Integer) this.getWifiApState.invoke(this.wifiManager);
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
        return -1; */
        return -1;
    }

    @Override
    public void log(String msg) {
        Log.d("UsbTethering", msg);
        EditText debugTextArea = ((EditText) this.view.findViewById(R.id.debug_messages));
        debugTextArea.append(msg + '\n');

    }

    @Override
    public void err(String msg, Throwable t) {
        Log.e("UsbTethering", msg, t);
    }
}
