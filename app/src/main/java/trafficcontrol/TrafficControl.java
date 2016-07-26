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

    public TrafficControl(ArrayList<Device> deviceList) {
        this.deviceList = deviceList;
        this.initIpTables();
    }

    /**
     * Insert traffic count chains.
     */
    private void initIpTables() {
        ArrayList<String> cmds = new ArrayList<>(10);
        try {
            String inputChain = "os_cnt_in";
            String outputChain = "os_cnt_out";
            String forwardChain = "os_cnt_fwd";
            String[] chains = {inputChain, outputChain, forwardChain};
            String currentRules = ShellExecutor.getSingleton().executeRoot("iptables -S");
            for (String chain : chains) {
                if (!currentRules.contains(chain)) {
                    cmds.add("iptables -N" + chain);
                    //cmds.add("iptables -A" + chain + " -j RETURN");
                    if (chain.equals(inputChain)) {
                        cmds.add("iptables -A INPUT -j " + chain);
                    } else if (chain.equals(outputChain)) {
                        cmds.add("iptables -A OUTPUT -j " + chain);
                    } else if (chain.equals(forwardChain)) {
                        cmds.add("iptables -A FORWARD -j " + chain);
                    }
                }
            }
            ShellExecutor.getSingleton().executeRoot(cmds);
        } catch (Exception e) {
            this.err("Error while initIpTables", e);
        }
    }

    public void refreshDevices() {
        ArrayList<Device> newDeviceList = new ArrayList<>();
        newDeviceList.add(this.getCurrentInternetDevice());
        newDeviceList.addAll(this.getTetheredDevices());
        if (!this.deviceList.equals(newDeviceList)) {
            this.deviceList.clear();
            this.deviceList.addAll(newDeviceList);
            this.updateIpTables();
        }
    }

    private void updateIpTables() {
        ArrayList<String> cmds = new ArrayList<>(10);
        // TODO: Bei Current Rules könnte mna nach os_ suchen, damit man die unnötigen Regeln erstmal rausfischen kann,
        // das macht auch das löschen einfacher
        String currentRules = "";
        try {
            currentRules = ShellExecutor.getSingleton().executeRoot("iptables -S");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Wir haben hier eine Liste von devices
        // Checken ob die Regeln schon drin sind, also bauen und ggf. einfügen / alte löschen
        Device ownDevice = this.deviceList.get(0);
        String inputRule = "os_cnt_in -d " + ownDevice.ipAddress;
        String outputRule = "os_cnt_out -s "+ ownDevice.ipAddress;
        if (!currentRules.contains(inputRule)) {
            cmds.add("iptables -I " + inputRule);
        }
        if (!currentRules.contains(outputRule)) {
            cmds.add("iptables -I " + outputRule);
        }

        // FWD rules
        for (int i = 1; i < this.deviceList.size(); ++i) {
            Device d = this.deviceList.get(i);
            String inputStream = "os_cnt_fwd -d " + d.ipAddress;
            String outputStream = "os_cnt_fwd -s " + d.ipAddress;
            if (!currentRules.contains(inputStream)) {
                cmds.add("iptables -I " + inputStream);
            }
            if (!currentRules.contains(outputStream)) {
                cmds.add("iptables -I " + outputStream);
            }
        }

        try {
            ShellExecutor.getSingleton().executeRoot(cmds);
        } catch (IOException e) {
            this.err("",e);
        } catch (InterruptedException e) {
            this.err("",e);
        }
    }

    private Device getCurrentInternetDevice() {
        List<String> lines = null;
        try {
            lines = new ArrayList<>(Arrays.asList(ShellExecutor.getSingleton().executeRoot("ip addr show").split("\n")));
        } catch (IOException e) {
            this.err("", e);
        } catch (InterruptedException e) {
            this.err("", e);
        }
        for (String s : lines) {
            String ip = this.extractIpAddress(s);
            if (ip != null) {
                String iface = this.extractIface(s);
                return new Device(ip, iface);
            }
        }
        throw new RuntimeException("Couldn't get current internet device.");
    }

    private List<Device> getTetheredDevices() {
        List<Device> devices = new ArrayList<>();
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
        lines.remove(0);
        for (String s : lines) {
            String ip = this.extractIpAddress(s);
            if (ip != null) {
                String iface = this.extractIface(s);
                devices.add(new Device(ip, iface));
            }
        }
        return devices;
    }

    public void refreshTrafficStats() {
        // Alles holen, schauen ob die IP in der Zeile verbunden ist, wenn ja Bytes extrahieren
        try {
            String[] downloadArr = ShellExecutor.getSingleton().executeRoot("iptables -v -x -n -L os_cnt_in").split("\n");
            String[] uploadArr = ShellExecutor.getSingleton().executeRoot("iptables -v -x -n -L os_cnt_out").split("\n");
            String[] forwardArr = ShellExecutor.getSingleton().executeRoot("iptables -v -x -n -L os_cnt_fwd").split("\n");
            Device ownD = this.deviceList.get(0);
            for (String line : downloadArr) {
                if (ownD.ipAddress.equals(this.extractIpAddress(line))) {
                    ownD.downTrafficBytes = this.extractBytes(line);
                }
            }
            for (String line : uploadArr) {
                if (ownD.ipAddress.equals(this.extractIpAddress(line))) {
                    ownD.upTrafficBytes = this.extractBytes(line);
                }
            }
            for (int i = 1; i < this.deviceList.size(); ++i) {
                Device d = this.deviceList.get(i);
                for (String line : forwardArr) {
                    // wir brauchen eine Methode die Dest und eine die Src sucht
                    if (line.contains(d.ipAddress)) {
                        if (this.isLineDownloadCounter(line, d)) {
                            d.downTrafficBytes = this.extractBytes(line);
                        } else if (this.isLineUploadCounter(line, d)) {
                            d.upTrafficBytes = this.extractBytes(line);
                        }
                    }

                }
            }
        } catch (IOException e) {
            this.err("", e);
        } catch (InterruptedException e) {
            this.err("", e);
        }
    }
    // -s = upload
    private boolean isLineUploadCounter(String line, Device d) {
        int indexOfIp = line.indexOf(d.ipAddress);
        return line.substring(d.ipAddress.length() + indexOfIp).contains("0.0.0.0/0");
    }
    // -d Download
    private boolean isLineDownloadCounter(String line , Device d) {
        int indexOfIp = line.indexOf(d.ipAddress);
        return !line.substring(d.ipAddress.length() + indexOfIp).contains("0.0.0.0/0");
    }


    private long extractBytes(String line) {
        try {
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
            //long kibiByte = byLong / 1048576;
            long kibiByte = byLong / 1024;
            return kibiByte;
        } catch (Exception e) {
            this.err("",e);
            return -1;
        }
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

    private String extractIpAddress(String line) {
        String IPADDRESS_PATTERN =
                "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        line = line.replace("0.0.0.0", "X.X.X.X");
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

    public void writeLimitRule(Device d, int downloadLimit, int uploadLimit) {
        // Check if own device
        // da könnte man es wie BradyBound machen also ne ganze Range abfragen
        if (this.deviceList.get(0).equals(d)) {
            this.limitOwnDevice(d, downloadLimit, uploadLimit);
        } else {
            this.limitDevice(d, downloadLimit, uploadLimit);
        }


    }

    private void limitOwnDevice(Device d, int downloadLimit, int uploadLimit) {
        // TODO Im Init müssen die chains fürs drosseln erstellt werden, alternativ könnte man auch einfach an die counter appenden.
        // TODO: Vielleicht checken ob das Device wirklich noch das eigene ist (Methode getOwnDevice())
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("iptables -A os_cnt_in -d " + d.ipAddress + " -p tcp -m state --state ESTABLISHED -m hashlimit --hashlimit-above " + convertToPacketsPerSecond(downloadLimit)  + "/sec --hashlimit-name OS_IN -j DROP");
        cmds.add("iptables -A os_cnt_out -s " + d.ipAddress + " -p tcp -m state --state ESTABLISHED -m hashlimit --hashlimit-above " + convertToPacketsPerSecond(uploadLimit)  + "/sec --hashlimit-name OS_OUT -j DROP");
        try {
            ShellExecutor.getSingleton().executeRoot(cmds);
        } catch (IOException e) {
            this.err("", e);
        } catch (InterruptedException e) {
            this.err("", e);
        }
    }

    private void limitDevice(Device d, int downloadLimit, int uploadLimit) {
        ArrayList<String> cmds = new ArrayList<>();
        cmds.add("iptables -A os_cnt_fwd -d " + d.ipAddress + " -p tcp -m state --state ESTABLISHED -m hashlimit --hashlimit-above " + convertToPacketsPerSecond(downloadLimit)  + "/sec --hashlimit-name OS_FWD_IN -j DROP");
        cmds.add("iptables -A os_cnt_fwd -s " + d.ipAddress + " -p tcp -m state --state ESTABLISHED -m hashlimit --hashlimit-above " + convertToPacketsPerSecond(uploadLimit)  + "/sec --hashlimit-name OS_FWD_OUT -j DROP");
        try {
            ShellExecutor.getSingleton().executeRoot(cmds);
        } catch (IOException e) {
            this.err("", e);
        } catch (InterruptedException e) {
            this.err("", e);
        }
    }

    public void deleteLimitRules(Device d) {
        try {
            String currentRules = ShellExecutor.getSingleton().executeRoot("iptables -S");
            String[] lines = currentRules.split("\n");
            ArrayList<String> cmds = new ArrayList<>();
            for (String line : lines) {
                if (line.contains(d.ipAddress + "/32 -p tcp ")) {
                    cmds.add("iptables " + line.replace("-A", "-D"));
                }
            }
            ShellExecutor.getSingleton().executeRoot(cmds);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteAllLimitRules() {
        try {
            String currentRules = ShellExecutor.getSingleton().executeRoot("iptables -S");
            String[] lines = currentRules.split("\n");
            ArrayList<String> cmds = new ArrayList<>();
            for (String line : lines) {
                if (line.contains("os_cnt") && line.contains("DROP")) {
                    cmds.add("iptables " + line.replace("-A", "-D"));
                }
            }
            ShellExecutor.getSingleton().executeRoot(cmds);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int convertToPacketsPerSecond(int limit) {
        int packetSize = 1400;
        int bytes = limit * 125;
        return bytes / packetSize;
    }

    public void resetCounters() {
        try {
            ArrayList<String> cmds = new ArrayList<>();
            cmds.add("iptables -Z os_cnt_in");
            cmds.add("iptables -Z os_cnt_out");
            cmds.add("iptables -Z os_cnt_fwd");
            ShellExecutor.getSingleton().executeRoot(cmds);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
