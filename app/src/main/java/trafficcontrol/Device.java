package trafficcontrol;

/**
 * Created by Oliver on 11.07.2016.
 */
public class Device {

    public int id;
    public String ipAddress;
    public String description;

    public Device(int id, String ipAddress, String description) {
        this.id = id;
        this.ipAddress = ipAddress;
        this.description = description;
    }


}
