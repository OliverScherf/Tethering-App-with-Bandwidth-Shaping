package trafficcontrol;

/**
 * Created by Oliver on 11.07.2016.
 */
public class Device {

    public int id;
    public String ipAddress;
    public String description;
    public long upTrafficBytes;
    public long downTrafficBytes;


    public Device(int id, String ipAddress, String description, long upTrafficBytes, long downTrafficBytes) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.description = description;
        this.upTrafficBytes = upTrafficBytes;
        this.downTrafficBytes = downTrafficBytes;
    }

    public Device(int id, String ipAddress, String description) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.description = description;
        this.upTrafficBytes = 0;
        this.downTrafficBytes = 0;
    }


}
