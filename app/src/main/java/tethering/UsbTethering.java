package tethering;

import android.net.ConnectivityManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import utils.Loggable;
import utils.ShellExecutor;

/**
 * Created by Oliver on 25.05.2016.
 */
public class UsbTethering implements Tetherable, Loggable {


    private Method tether;
    private Method untether;
    private View view;
    private ConnectivityManager connectivityManager;
    private String[] usbInterfaces;
    private String oldUsbFunction;



    public UsbTethering(View view, ConnectivityManager connectivityManager) {
        this.view = view;
        this.log(connectivityManager.getClass().getName());
        this.connectivityManager = connectivityManager;
        for (Method method : this.connectivityManager.getClass().getDeclaredMethods()) {
            //if (method.getName().equals("setUsbTethering")) {
            if (method.getName().equals("tether")) {
                this.tether = method;
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
            } else if(method.getName().equals("untether")) {
                this.untether = method;
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
        //Intent tetherSettings = new Intent();
        //tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
        //this.view.getContext().startActivity(tetherSettings);
        try {
            // alten stand saven
            String execRet = ShellExecutor.getSingleton().executeRoot("getprop sys.usb.config");
            if (execRet.contains("mtp")) {
                this.oldUsbFunction = "mtp";
            } else if (execRet.contains("ptp")) {
                this.oldUsbFunction = "ptp";
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            this.log("Mache setprop sys.usb.config rndis,adb");
            ShellExecutor.getSingleton().executeRoot("setprop sys.usb.config rndis,adb");
            Thread.sleep(100);
            this.log("Versuche USB Tethering zu auf " + "rndis0" +  " zu starten");
            this.log("Return: " + (Integer) this.tether.invoke(this.connectivityManager, "rndis0"));
            // http://redmine.replicant.us/attachments/435/replicant_usb_networking_device.sh
            // setprop sys.usb.config rndis,adb
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        } catch (Exception e) {
            this.err("Shell error", e);
        }
    }

    @Override
    public void stopTethering() {
        try {
            this.log("Untether Return: " + (Integer) this.untether.invoke(this.connectivityManager, "rndis0"));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (this.oldUsbFunction == null || this.oldUsbFunction.equals("")) {
            this.oldUsbFunction = "mtp";
        }
        try {
            ShellExecutor.getSingleton().executeRoot("setprop sys.usb.config " + this.oldUsbFunction + ",adb");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    /*  Intent tetherSettings = new Intent();
        tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
        this.view.getContext().startActivity(tetherSettings); */
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
