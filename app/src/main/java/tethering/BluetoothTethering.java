package tethering;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import utils.Loggable;

/**
 * Created by Oliver on 25.05.2016.
 */
public class BluetoothTethering implements Tetherable, Loggable {

    private WifiManager wifiManager;
    private Context context;
    private Method setWifiApEnabled;
    private Method isWifiApEnabled;
    private Method getWifiApState;
    private Method getWifiApConfiguration;

    public BluetoothTethering(Context context) {
        this.context = context;
        this.wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
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
        try {
            this.setWifiApEnabled.invoke(this.wifiManager, null, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
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
