package tethering;

import android.net.ConnectivityManager;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import utils.Loggable;
import utils.ShellExecutor;

public class UsbTethering implements Loggable {

    private ConnectivityManager connectivityManager;
    private Method tether;
    private Method untether;
    private Method getTetheredIfaces;
    private String oldUsbFunction;

    public UsbTethering(ConnectivityManager connectivityManager) {
        this.connectivityManager = connectivityManager;
        for (Method method : this.connectivityManager.getClass().getDeclaredMethods()) {
            if (method.getName().equals("tether")) {
                this.tether = method;
            } else if (method.getName().equals("untether")) {
                this.untether = method;
            } else if (method.getName().equals("getTetheredIfaces")) {
                this.getTetheredIfaces = method;
            }
        }
    }

    public void startTethering() {
        if (this.isTetheringEnabled()) {
            return;
        }
        try {
            String execRet = ShellExecutor.executeRoot("getprop sys.usb.config");
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
            ShellExecutor.executeRoot("setprop sys.usb.config rndis,adb");
            Thread.sleep(100);
            this.tether.invoke(this.connectivityManager, "rndis0");
            // http://redmine.replicant.us/attachments/435/replicant_usb_networking_device.sh
        } catch (IllegalAccessException e) {
            this.err("Reflection Error", e);
        } catch (InvocationTargetException e) {
            this.err("Reflection Error", e);
        } catch (Exception e) {
            this.err("Shell error", e);
        }
    }

    public void stopTethering() {
        try {
            this.untether.invoke(this.connectivityManager, "rndis0");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (this.oldUsbFunction == null || this.oldUsbFunction.equals("")) {
            this.oldUsbFunction = "mtp";
        }
        try {
            ShellExecutor.executeRoot("setprop sys.usb.config " + this.oldUsbFunction + ",adb");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //  Membervariablen von TetherSettings.java
    public boolean isTetheringEnabled() {
        try {
            String[] tethInf = (String[]) this.getTetheredIfaces.invoke(this.connectivityManager);
            for (String inf : tethInf) {
                if (inf.contains("rndis")) {
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
        Log.d("UsbTethering", msg);
    }

    @Override
    public void err(String msg, Throwable t) {
        Log.e("UsbTethering", msg, t);
    }
}









