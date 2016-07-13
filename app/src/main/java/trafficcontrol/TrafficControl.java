package trafficcontrol;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.Loggable;
import utils.ShellExecutor;

/**
 * Created by Oliver on 11.07.2016.
 */
public class TrafficControl implements Loggable {

    private ArrayList<Device> deviceList;
    private ArrayList<String> removeList = new ArrayList<>();

    public TrafficControl(ArrayList<Device> deviceList) {
        this.deviceList = deviceList;
    }



    private boolean firstRun = true;

    public void refreshDevices() {
        this.log("new version!");
        if (firstRun) {
            this.deviceList.clear();
            this.deviceList.addAll(this.getTetheredDevices());
            this.initIpTables();
            firstRun = false;
            this.fetchBytesTransmitted();
            return;
        }
        //this.removceRules();
        //this.firstRun = true;
        this.fetchBytesTransmitted();
    }

    private void initIpTables() {
        try {
            for (Device d : this.deviceList) {

                // Download
                ShellExecutor.getSingleton().executeRoot("iptables -N oli_" + d.description + "_down_counter");
                ShellExecutor.getSingleton().executeRoot("iptables -A oli_" + d.description + "_down_counter -j RETURN");
                ShellExecutor.getSingleton().executeRoot("iptables -I oli_" + d.description + "_down_counter -d " + d.ipAddress);
                ShellExecutor.getSingleton().executeRoot("iptables -A FORWARD -j oli_" + d.description + "_down_counter ");

                // Upload
                ShellExecutor.getSingleton().executeRoot("iptables -N oli_" + d.description + "_up_counter");
                ShellExecutor.getSingleton().executeRoot("iptables -A oli_" + d.description + "_up_counter -j RETURN");
                ShellExecutor.getSingleton().executeRoot("iptables -I oli_" + d.description + "_up_counter -s " + d.ipAddress);
                ShellExecutor.getSingleton().executeRoot("iptables -A FORWARD -j oli_" + d.description + "_up_counter ");

                this.removeList.add("iptables -D FORWARD -j oli_" + d.description + "_down_counter");
                this.removeList.add("iptables -D FORWARD -j oli_" + d.description + "_up_counter");
                this.removeList.add("iptables -F oli_" + d.description +  "_down_counter");
                this.removeList.add("iptables -F oli_" + d.description +  "_up_counter");
                this.removeList.add("iptables -X oli_" + d.description +  "_down_counter");
                this.removeList.add("iptables -X oli_" + d.description +  "_up_counter");
            }
        } catch (Exception e) {
            this.err("", e);
        }
    }

    public void removceRules() {
        for (String cmd : this.removeList) {
            try {
                ShellExecutor.getSingleton().executeRoot(cmd);
            } catch (IOException e) {
                this.err("",e);
            } catch (InterruptedException e) {
                this.err("",e);
            }
        }
        this.removeList.clear();
    }


    private void fetchBytesTransmitted() {
        try {
            for (Device d : this.deviceList) {
                // Download
                String[] downloadArr = ShellExecutor.getSingleton().executeRoot("iptables -v -x -L oli_" + d.description + "_down_counter").split("\n");
                String[] uploadArr = ShellExecutor.getSingleton().executeRoot("iptables -v -x -L oli_" + d.description + "_up_counter").split("\n");

                for (String line : downloadArr) {
                    this.log(line);
                }
                for (String line : uploadArr) {
                    this.log(line);
                }

                //return;
                String downloadLine = downloadArr[2];
                String uploadLine = uploadArr[2];

                d.downTrafficBytes = this.extractBytes(downloadLine);
                d.upTrafficBytes = this.extractBytes(uploadLine);
            }
        } catch (IOException e) {
            this.err("", e);
        } catch (InterruptedException e) {
            this.err("", e);
        }
    }

    private long extractBytes(String line) {
        int firstPaketDigitIndex = -1;
        int lastPaketDigitIndex = -1;
        for (int i = 0; i < line.length(); ++i) {
            char c = line.charAt(i);
            if (c != ' ') {
                firstPaketDigitIndex = i;
                break;
            }
        }
        for (int i = firstPaketDigitIndex; i < line.length(); ++i) {
            char c = line.charAt(i);
            if (!Character.isDigit(c)) {
                lastPaketDigitIndex = i;
                break;
            }
        }
        int firstByteIndex = -1;
        for (int i = lastPaketDigitIndex + 1; i < line.length(); ++i) {
            char c = line.charAt(i);
            if (c != ' ') {
                firstByteIndex = i;
                break;
            }
        }
        String bytes = "";
        for (int i = firstByteIndex; i < line.length(); ++i) {
            char c = line.charAt(i);
            if (Character.isDigit(c)) {
                bytes += c;
            } else {
                break;
            }
        }
        long byLong = Long.valueOf(bytes);
        long kibiByte = byLong / 1048576;
        return kibiByte;
    }


    private List<Device> getTetheredDevices() {
        List<Device> devices = new ArrayList<>();
        devices.add(this.getOwnIp());
        devices.addAll(this.getArpDevices());
        this.fetchBytesTransmitted();
        return devices;
    }

    private Device getOwnIp() {
        List<String> lines = null;
        try {
            lines = new ArrayList<>(Arrays.asList(ShellExecutor.getSingleton().executeRoot("ip addr show").split("\n")));
        } catch (IOException e) {
            this.err("", e);
        } catch (InterruptedException e) {
            this.err("", e);
        }
        if (lines == null) {
            throw new RuntimeException("ip addr show error");
        }
        for (String s : lines) {
            this.log(s);
            String ip = this.extractIpAddress(s);
            if (ip != null) {
                String iface = this.extractIface(s);
                return new Device(1, ip, iface);
            }
        }
        throw new RuntimeException("Couldn't get own IP");
    }

    private String extractIface(String line) {
        int indexOfGlobal = line.indexOf("global");
        if (indexOfGlobal != -1) {
            return line.substring(indexOfGlobal + "global".length() + 1);
        }
        int indexOfMask = line.indexOf("*");
        if (indexOfMask != -1) {
            return line.substring(indexOfMask + 9);
        }
        return "nodevice";
    }

    private List<Device> getArpDevices() {
        ArrayList<String> lines = null;
        try {
            lines = new ArrayList<>(Arrays.asList(ShellExecutor.getSingleton().executeRoot("cat /proc/net/arp").split("\n")));
        } catch (IOException e) {
            this.err("", e);
        } catch (InterruptedException e) {
            this.err("", e);
        }
        if (lines == null) {
            return null;
        }
        List<Device> devices = new ArrayList<>(10);
        lines.remove(0);
        int deviceCount = 2;
        for (String s : lines) {
            String ip = this.extractIpAddress(s);
            if (ip != null) {
                String iface = this.extractIface(s);
                devices.add(new Device(deviceCount++, ip, iface));
            }
        }
        return devices;
    }

    private String extractIpAddress(String line) {
        String IPADDRESS_PATTERN =
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String ip = matcher.group();
            if (!ip.endsWith(".1") && !ip.startsWith("127.") && !ip.startsWith("0.") && !ip.endsWith("255")) {
                return ip;
            }
        }
        return null;
    }


    @Override
    public void log(String msg) {
        Log.d("TrafficControl", msg);
    }

    @Override
    public void err(String msg, Throwable t) {
        Log.d("TrafficControl", msg, t);
    }
}
