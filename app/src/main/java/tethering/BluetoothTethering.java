package tethering;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import utils.Loggable;

/**
 * Created by Oliver on 25.05.2016.
 */
public class BluetoothTethering implements Loggable {

    private BluetoothAdapter bluetoothAdapter;
    private Constructor bluetoothPanConstructor;
    private Context context;
    private ConnectivityManager connectivityManager;
    private Method isTetheringOn;
    private Method getTetheredIfaces;

    public BluetoothTethering(Context context, ConnectivityManager connectivityManager) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.connectivityManager = connectivityManager;
        try {
            Class classBluetoothPan = Class.forName("android.bluetooth.BluetoothPan");
            this.isTetheringOn = classBluetoothPan.getDeclaredMethod("isTetheringOn");
            this.bluetoothPanConstructor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);
            this.bluetoothPanConstructor.setAccessible(true);
        } catch (ClassNotFoundException e) {
            this.err("Reflection Error", e);
        } catch (Exception e) {
            this.err("Reflection Error", e);
        }

        try {
            this.getTetheredIfaces = this.connectivityManager.getClass().getDeclaredMethod("getTetheredIfaces");
        } catch (NoSuchMethodException e) {
            this.err("Reflection Error", e);
        }


    }

    public void startTethering() {
        if (this.isBluetoothTetheringEnabled()) {
            return;
        }
        try {
            if (!this.bluetoothAdapter.isEnabled()) {
                this.bluetoothAdapter.enable();
                Thread.sleep(100);
            }
            this.bluetoothPanConstructor.newInstance(this.context, new BluetoothPanServiceListener(this.context, true));
        } catch (InstantiationException e) {
            this.err("Fehler beim Instanz erzeugen vom BluetoothPan", e);
        } catch (IllegalAccessException e) {
            this.err("Fehler beim Instanz erzeugen vom BluetoothPan", e);
        } catch (InvocationTargetException e) {
            this.err("Fehler beim Instanz erzeugen vom BluetoothPan", e);
        } catch (InterruptedException e) {
            this.err("Fehler beim Instanz erzeugen vom BluetoothPan", e);
        }
    }

    public void stopTethering() {
        try {
            this.bluetoothPanConstructor.newInstance(this.context, new BluetoothPanServiceListener(this.context, false));
        } catch (InstantiationException e) {
            this.err("err",e);
        } catch (IllegalAccessException e) {
            this.err("err",e);
        } catch (InvocationTargetException e) {
            this.err("err",e);
        }
    }

    public boolean isBluetoothTetheringEnabled() {
        try {
            String[] tethInf = (String[]) getTetheredIfaces.invoke(this.connectivityManager);
            for (String inf : tethInf) {
                this.log(inf);
                if (inf.contains("bt-pan")) {
                    return true;
                }
            }
            return false;
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        }
        return false;
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

    private Context context;
    private boolean enable;

    BluetoothPanServiceListener(Context context, boolean enable){
        this.context = context;
        this.enable = enable;
    }

    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        try {
            proxy.getClass().getMethod("setBluetoothTethering",Boolean.TYPE).invoke(proxy, Boolean.valueOf(this.enable));
        } catch (Exception e) {
            this.err("Fehler beim Aktivieren/Deaktivieren des Bluetooth Tetherings", e);
        }
    }

    @Override
    public void onServiceDisconnected(int profile) {

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
