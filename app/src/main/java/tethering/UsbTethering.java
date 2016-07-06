package tethering;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import java.io.IOException;
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
                this.log("Methode tether gefunden");
                //    method.invoke(systemService, "usb0");
                this.usbTether = method;
                this.usbTether.setAccessible(true);
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
                //this.log("Return: " + (Integer) this.usbTether.invoke(this.connectivityManager, inf));
                this.log("Return: " + (Integer) this.usbTether.invoke(inf));
            }
            this.log("Versuche USB Tethering zu auf " + "rndis0" +  " zu starten");
            //this.log("Return: " + (Integer) this.usbTether.invoke(this.connectivityManager, "rndis0"));
            this.log("Return: " + (Integer) this.usbTether.invoke("rndis0"));
            //this.log("Versuche USB Tethering zu auf " + "usb0" +  " zu starten");
            //this.log("Return: " + (Integer) this.usbTether.invoke(this.connectivityManager, "usb0"));
            //this.log("Versuche USB Tethering zu auf " + "ncm0" +  " zu starten");
            //this.log("Return: " + (Integer) this.usbTether.invoke(this.connectivityManager, "ncm0"));

        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
    }

    @Override
    public void stopTethering() {

    }

    @Override
    public int getTetheringStatus() {
        /*try {
            this.log("USB Tether Status: " + (Integer) this.getWifiApState.invoke(this.wifiManager));
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }*/
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
