package trafficcontrol;

/**
 * Created by Oliver on 11.07.2016.
 */
public class Device {

    public String ipAddress;
    public String description;
    public long upTrafficBytes;
    public long downTrafficBytes;


    public Device(String ipAddress, String description, long upTrafficBytes, long downTrafficBytes) {
        this.ipAddress = ipAddress;
        this.description = description;
        this.upTrafficBytes = upTrafficBytes;
        this.downTrafficBytes = downTrafficBytes;
    }

    public Device(String ipAddress, String description) {
        this.ipAddress = ipAddress;
        this.description = description;
        this.upTrafficBytes = 0;
        this.downTrafficBytes = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        Device od = null;
        try {
            od = (Device) o;
        } catch (ClassCastException e) {
            return false;
        }
        if (this.ipAddress.equals(od.ipAddress) &&
                this.description.equals(od.description)) {
            return true;
        } else {
            return false;
        }
    }
}
