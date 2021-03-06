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
    this.initCounterChains();
    this.initLimitChains();
  }

  public void refreshConnectedDevices() {
    ArrayList<Device> newDeviceList = new ArrayList<>();
    newDeviceList.add(this.getSmartphoneDevice());
    newDeviceList.addAll(this.getTetheredDevices());
    if (!this.deviceList.equals(newDeviceList)) {
      this.deviceList.clear();
      this.deviceList.addAll(newDeviceList);
      this.updateCounterChains();
    }
  }

  public synchronized void refreshTrafficCounter() {
    try {
      String[] downloadArr = ShellExecutor.executeRoot("iptables -v -x -n -L os_count_inp").split("\n");
      String[] uploadArr = ShellExecutor.executeRoot("iptables -v -x -n -L os_count_out").split("\n");
      String[] forwardArr = ShellExecutor.executeRoot("iptables -v -x -n -L os_count_fwd").split("\n");
      Device ownD = this.deviceList.get(0);
      for (String line : downloadArr) {
        if (ownD.ipAddress.equals(this.extractIpAddress(line))) {
          ownD.downTrafficBytes = this.extractKibiBytes(line);
          break;
        }
      }
      for (String line : uploadArr) {
        if (ownD.ipAddress.equals(this.extractIpAddress(line))) {
          ownD.upTrafficBytes = this.extractKibiBytes(line);
          break;
        }
      }
      for (int i = 1; i < this.deviceList.size(); ++i) {
        Device d = this.deviceList.get(i);
        for (String line : forwardArr) {
          if (line.contains(d.ipAddress)) {
            if (this.isLineDownloadCounter(line, d)) {
              d.downTrafficBytes = this.extractKibiBytes(line);
              break;
            } else if (this.isLineUploadCounter(line, d)) {
              d.upTrafficBytes = this.extractKibiBytes(line);
              break;
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

  public void resetTrafficCounter() {
    try {
      ArrayList<String> cmds = new ArrayList<>();
      cmds.add("iptables -Z os_count_inp");
      cmds.add("iptables -Z os_count_out");
      cmds.add("iptables -Z os_count_fwd");
      ShellExecutor.executeRoot(cmds);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void setSpeedLimits(Device d, int downloadLimit, int uploadLimit) {
    this.removeSpeedLimits(d);
    if (this.deviceList.get(0).equals(d)) {
      this.limitSmartphoneDevice(d, downloadLimit, uploadLimit);
    } else {
      this.limitTetheredDevice(d, downloadLimit, uploadLimit);
    }
    d.uploadLimit = uploadLimit;
    d.downloadLimit = downloadLimit;
  }

  public void removeSpeedLimits(Device d) {
    try {
      String currentRules = ShellExecutor.executeRoot("iptables -S");
      String[] lines = currentRules.split("\n");
      ArrayList<String> cmds = new ArrayList<>();
      for (String line : lines) {
        if (line.contains(d.ipAddress + "/32 -p tcp ")) {
          cmds.add("iptables " + line.replace("-A", "-D"));
        }
      }
      ShellExecutor.executeRoot(cmds);
      d.downloadLimit = 0;
      d.uploadLimit = 0;
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void removeAllSpeedLimits() {
    try {
      String currentRules = ShellExecutor.executeRoot("iptables -S");
      String[] lines = currentRules.split("\n");
      ArrayList<String> cmds = new ArrayList<>();
      for (String line : lines) {
        if (line.contains("os_limit") && line.contains("DROP")) {
          cmds.add("iptables " + line.replace("-A", "-D"));
        }
      }
      ShellExecutor.executeRoot(cmds);
      for (Device d : this.deviceList) {
        d.downloadLimit = 0;
        d.uploadLimit = 0;
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void parseSpeedLimits() {
    String[] currentRules = null;
    try {
      currentRules = ShellExecutor.executeRoot("iptables -S").split("\n");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    for (Device d : this.deviceList) {
      d.uploadLimit = 0;
      d.downloadLimit = 0;
      for (String rule : currentRules) {
        if (rule.contains(d.ipAddress) && rule.contains("hashlimit")) {
          if (rule.contains("-d")) {
            d.downloadLimit = this.parseSpeedLimitFromRule(rule);
          } else if (rule.contains("-s")) {
            d.uploadLimit = this.parseSpeedLimitFromRule(rule);
          }
        }
      }
    }
  }

  /**
   * Insert traffic count chains.
   */
  private void initCounterChains() {
    ArrayList<String> cmds = new ArrayList<>(10);
    try {
      String inputChain = "os_count_inp";
      String outputChain = "os_count_out";
      String forwardChain = "os_count_fwd";
      String[] chains = {inputChain, outputChain, forwardChain};
      String currentRules = ShellExecutor.executeRoot("iptables -S");
      for (String chain : chains) {
        if (!currentRules.contains(chain)) {
          cmds.add("iptables -N" + chain);
          if (chain.equals(inputChain)) {
            cmds.add("iptables -I INPUT -j " + chain);
          } else if (chain.equals(outputChain)) {
            cmds.add("iptables -I OUTPUT -j " + chain);
          } else if (chain.equals(forwardChain)) {
            cmds.add("iptables -I FORWARD -j " + chain);
          }
        }
      }
      ShellExecutor.executeRoot(cmds);
    } catch (Exception e) {
      this.err("Error while initCounterChains", e);
    }
  }

  /**
   * Insert traffic count chains.
   */
  private void initLimitChains() {
    ArrayList<String> cmds = new ArrayList<>(10);
    try {
      String inputChain = "os_limit_inp";
      String outputChain = "os_limit_out";
      String forwardChain = "os_limit_fwd";
      String[] chains = {inputChain, outputChain, forwardChain};
      String currentRules = ShellExecutor.executeRoot("iptables -S");
      for (String chain : chains) {
        if (!currentRules.contains(chain)) {
          cmds.add("iptables -N" + chain);
          if (chain.equals(inputChain)) {
            cmds.add("iptables -A INPUT -j " + chain);
          } else if (chain.equals(outputChain)) {
            cmds.add("iptables -A OUTPUT -j " + chain);
          } else if (chain.equals(forwardChain)) {
            cmds.add("iptables -A FORWARD -j " + chain);
          }
        }
      }
      ShellExecutor.executeRoot(cmds);
    } catch (Exception e) {
      this.err("Error while initLimitChains", e);
    }
  }


  private void updateCounterChains() {
    ArrayList<String> cmds = new ArrayList<>(10);
    String currentRules = "";
    try {
      currentRules = ShellExecutor.executeRoot("iptables -S");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Device ownDevice = this.deviceList.get(0);
    String inputRule = "os_count_inp -d " + ownDevice.ipAddress;
    String outputRule = "os_count_out -s " + ownDevice.ipAddress;
    if (!currentRules.contains(inputRule)) {
      cmds.add("iptables -I " + inputRule);
    }
    if (!currentRules.contains(outputRule)) {
      cmds.add("iptables -I " + outputRule);
    }
    for (int i = 1; i < this.deviceList.size(); ++i) {
      Device d = this.deviceList.get(i);
      String inputStream = "os_count_fwd -d " + d.ipAddress;
      String outputStream = "os_count_fwd -s " + d.ipAddress;
      if (!currentRules.contains(inputStream)) {
        cmds.add("iptables -I " + inputStream);
      }
      if (!currentRules.contains(outputStream)) {
        cmds.add("iptables -I " + outputStream);
      }
    }
    try {
      ShellExecutor.executeRoot(cmds);
    } catch (IOException e) {
      this.err("", e);
    } catch (InterruptedException e) {
      this.err("", e);
    }
  }

  private Device getSmartphoneDevice() {
    List<String> lines = null;
    try {
      lines = new ArrayList<>(Arrays.asList(ShellExecutor.executeRoot("ip addr show").split("\n")));
    } catch (IOException e) {
      this.err("", e);
    } catch (InterruptedException e) {
      this.err("", e);
    }
    for (String s : lines) {
      String ip = this.extractIpAddress(s);
      if (ip != null) {
        String iface = this.extractInterface(s);
        if (iface.contains("wlan") || iface.contains("rmnet")) {
          return new Device(ip, iface);
        }
      }
    }
    return new Device("-", "No Interface active");
  }

  private ArrayList<Device> getTetheredDevices() {
    ArrayList<Device> devices = new ArrayList<>();
    ArrayList<String> lines = null;
    try {
      lines = new ArrayList<>(Arrays.asList(ShellExecutor.executeRoot("cat /proc/net/arp").split("\n")));
    } catch (IOException e) {
      this.err("", e);
    } catch (InterruptedException e) {
      this.err("", e);
    }
    for (String s : lines) {
      String ip = this.extractIpAddress(s);
      if (ip != null) {
        String iface = this.extractInterface(s);
        devices.add(new Device(ip, iface));
      }
    }
    return devices;
  }

  // -s = upload
  private boolean isLineUploadCounter(String line, Device d) {
    int indexOfIp = line.indexOf(d.ipAddress);
    return line.substring(d.ipAddress.length() + indexOfIp).contains("0.0.0.0/0");
  }

  // -d Download
  private boolean isLineDownloadCounter(String line, Device d) {
    int indexOfIp = line.indexOf(d.ipAddress);
    return !line.substring(d.ipAddress.length() + indexOfIp).contains("0.0.0.0/0");
  }

  private long extractKibiBytes(String line) {
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
      this.err("", e);
      return -1;
    }
  }

  private String extractInterface(String line) {
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

  private void limitSmartphoneDevice(Device d, int downloadLimit, int uploadLimit) {
    ArrayList<String> cmds = new ArrayList<>();
    cmds.add("iptables -A os_limit_inp -d " + d.ipAddress + " -p tcp -m state --state ESTABLISHED -m hashlimit --hashlimit-above " + convertToPacketsPerSecond(downloadLimit) + "/sec --hashlimit-name OS_IN -j DROP");
    cmds.add("iptables -A os_limit_out -s " + d.ipAddress + " -p tcp -m state --state ESTABLISHED -m hashlimit --hashlimit-above " + convertToPacketsPerSecond(uploadLimit) + "/sec --hashlimit-name OS_OUT -j DROP");
    try {
      ShellExecutor.executeRoot(cmds);
    } catch (IOException e) {
      this.err("", e);
    } catch (InterruptedException e) {
      this.err("", e);
    }
  }

  private void limitTetheredDevice(Device d, int downloadLimit, int uploadLimit) {
    ArrayList<String> cmds = new ArrayList<>();
    cmds.add("iptables -A os_limit_fwd -d " + d.ipAddress + " -p tcp -m state --state ESTABLISHED -m hashlimit --hashlimit-above " + convertToPacketsPerSecond(downloadLimit) + "/sec --hashlimit-name OS_FWD_IN -j DROP");
    cmds.add("iptables -A os_limit_fwd -s " + d.ipAddress + " -p tcp -m state --state ESTABLISHED -m hashlimit --hashlimit-above " + convertToPacketsPerSecond(uploadLimit) + "/sec --hashlimit-name OS_FWD_OUT -j DROP");
    try {
      ShellExecutor.executeRoot(cmds);
    } catch (IOException e) {
      this.err("", e);
    } catch (InterruptedException e) {
      this.err("", e);
    }
  }

  private int convertToPacketsPerSecond(int limit) {
    int packetSize = 1400;
    int bytes = limit * 125;
    return bytes / packetSize;
  }

  private int parseSpeedLimitFromRule(String rule) {
    int indexOfAbove = rule.indexOf("above");
    if (indexOfAbove == -1) {
      return -1;
    }
    String limit = "";
    int index = indexOfAbove + "above".length() + 1;
    while (true) {
      char newDigit = rule.charAt(index++);
      if (Character.isDigit(newDigit)) {
        limit += newDigit;
      } else {
        break;
      }
    }
    int limitInt = Integer.parseInt(limit);
    return limitInt * 1400 / 125;
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
