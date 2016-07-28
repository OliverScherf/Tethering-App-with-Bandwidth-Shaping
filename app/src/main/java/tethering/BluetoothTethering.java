package tethering;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import utils.Loggable;

/**
 * Created by Oliver on 25.05.2016.
 */
public class BluetoothTethering implements Loggable {

    private Method setBluetoothTethering;
    private Method isTetheringOn;
    private Constructor bluetoothPanConstructor;
    private Context context;
    private ConnectivityManager connectivityManager;
    BluetoothProfile panProxy;

    public BluetoothTethering(Context context, ConnectivityManager connectivityManager) {
        this.log("new version");
        this.context = context;
        this.connectivityManager = connectivityManager;
        try {
            Class classBluetoothPan = Class.forName("android.bluetooth.BluetoothPan");
            for (Method method : classBluetoothPan.getDeclaredMethods()) {
                if (method.getName().equals("setBluetoothTethering")) {
                    this.setBluetoothTethering = method;
                    this.log("Found SetBluetooth");
                } else if (method.getName().equals("isTetheringOn")) {
                    this.isTetheringOn = method;
                    this.log("Found isTeth");
                }
            }
            this.bluetoothPanConstructor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);
            this.bluetoothPanConstructor.setAccessible(true);
        } catch (ClassNotFoundException e) {
            this.err("Reflection Error", e);
        } catch (Exception e) {
            this.err("Reflection Error", e);
        }
        this.createBluetoothPanInstance();
    }

    void createBluetoothPanInstance()  {
        try {
            this.bluetoothPanConstructor.newInstance(this.context, new BluetoothPanServiceListener(this));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void startTethering() {
        if (this.isBluetoothTetheringEnabled()) {
            return;
        }
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            this.setBluetoothTethering.invoke(this.panProxy, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void stopTethering() {
        try {
            this.setBluetoothTethering.invoke(this.panProxy, false);
        } catch (IllegalAccessException e) {
            this.err("err",e);
        } catch (InvocationTargetException e) {
            this.err("err",e);
        }
    }

    public boolean isBluetoothTetheringEnabled() {
        try {
            return (boolean) this.isTetheringOn.invoke(this.panProxy);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return false;
    }

    public BluetoothProfile getPanProxy() {
        return panProxy;
    }

    public void setPanProxy(BluetoothProfile panProxy) {
        this.panProxy = panProxy;
    }

    @Override
    public void log(String msg) {
        Log.d("BluetoothTethering", msg);
    }

    @Override
    public void err(String msg, Throwable t) {
        Log.e("BluetoothTethering", msg, t);
    }
}

class BluetoothPanServiceListener implements BluetoothProfile.ServiceListener, Loggable {

    private BluetoothTethering bluetoothTethering;

    BluetoothPanServiceListener(BluetoothTethering bluetoothTethering){
        this.bluetoothTethering = bluetoothTethering;
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        try {
            this.bluetoothTethering.setPanProxy(proxy);
            this.log("bin connected: " + proxy);
        } catch (Exception e) {
            this.err("Fehler beim Aktivieren/Deaktivieren des Bluetooth Tetherings", e);
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {
        this.log("bin disconnected");
        this.bluetoothTethering.createBluetoothPanInstance();
    }

    @Override
    public void log(String msg) {
        Log.d("BluetoothPanServiceLstn", msg);
    }

    @Override
    public void err(String msg, Throwable t) {
        Log.e("BluetoothPanServiceLstn", msg, t);
    }
}
