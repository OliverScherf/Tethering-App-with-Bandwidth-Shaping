package trafficcontrol;

import java.util.ArrayList;

/**
 * Created by Oliver on 11.07.2016.
 */
public class TrafficControl {

    private ArrayList<Device> deviceList;

    public TrafficControl(ArrayList<Device> deviceList) {
        this.deviceList = deviceList;
    }

    public void refreshDevices() {
        this.deviceList.add(new Device(1, "lol", "lol"));
    }


}
