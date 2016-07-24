package tethering;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
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
    private Method isTetheringOn;

    public BluetoothTethering(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Class classBluetoothPan = Class.forName("android.bluetooth.BluetoothPan");
            this.isTetheringOn = classBluetoothPan.getDeclaredMethod("isTetheringOn");
            this.bluetoothPanConstructor = classBluetoothPan.getDeclaredConstructor(Context.class, BluetoothProfile.ServiceListener.class);
            this.bluetoothPanConstructor.setAccessible(true);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startTethering() {
        Toast.makeText(this.context, "Turning Bluetooth tethering on", Toast.LENGTH_SHORT).show();
        try {
            if (!this.bluetoothAdapter.isEnabled()) {
                this.bluetoothAdapter.enable();
                Thread.sleep(100);
            }
            Object obj = this.bluetoothPanConstructor.newInstance(this.context, new BluetoothPanServiceListener(this.context, true));
            this.log("Klassenvariable asd : " + obj.hashCode());
        } catch (InstantiationException e) {
            this.err("Fehler beim Instanz erzeugen vom BluetoothPan", e);
        } catch (IllegalAccessException e) {
            this.err("Fehler beim Instanz erzeugen vom BluetoothPan", e);
        } catch (InvocationTargetException e) {
            this.err("Fehler beim Instanz erzeugen vom BluetoothPan", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopTethering() {
        Toast.makeText(context, "Turning Bluetooth tethering off", Toast.LENGTH_SHORT).show();
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

    public int getTetheringStatus() {
        return 0;
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
        //Log.i("MyApp", "BTPan proxy connected");
        try {
            Log.d("ProxyKlasse", proxy.getClass().getName());
            Log.d("ProxyKlasse", "" + proxy.hashCode());
            proxy.getClass().getMethod("setBluetoothTethering", new Class[]{Boolean.TYPE}).invoke(proxy, new Object[]{Boolean.valueOf(this.enable)});
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
