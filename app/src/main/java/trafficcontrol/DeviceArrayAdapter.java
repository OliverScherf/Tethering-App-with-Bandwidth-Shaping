package trafficcontrol;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.oliverscherf.tetheringwithbandwidthshaping.R;

import java.util.List;

/**
 * Created by Oliver on 11.07.2016.
 */
public class DeviceArrayAdapter extends ArrayAdapter<Device> {

    private List<Device> devices;
    private Context context;
    private int layoutResourceId;

    public DeviceArrayAdapter(Context context, int resource, List<Device> objects) {
        super(context, resource, objects);
        this.context = context;
        this.devices = objects;
        this.layoutResourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DeviceHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DeviceHolder();
            holder.description = (TextView) row.findViewById(R.id.device_description);
            holder.ipAddress = (TextView) row.findViewById(R.id.device_ipAddress);
            holder.upTraffic = (TextView) row.findViewById(R.id.device_up_traffic);
            holder.downTraffic = (TextView) row.findViewById(R.id.device_down_traffic);
            holder.maxUpSpeed = (TextView) row.findViewById(R.id.device_up_limit);
            holder.maxDownSpeed = (TextView) row.findViewById(R.id.device_down_limit);
            row.setTag(holder);
        }
        else
        {
            holder = (DeviceHolder)row.getTag();
        }

        Device device = this.devices.get(position);
        if (position == 0) {
            holder.description.setText("Smartphone Internet\nInterface: " + device.description);
        } else {
            holder.description.setText("Tethered Internet\nInterface: " + device.description);
        }
        holder.ipAddress.setText(device.ipAddress);
        String download = "Download: " + String.valueOf(device.downTrafficBytes) + " KB";
        holder.downTraffic.setText(download);
        String upload = "Upload: " + String.valueOf(device.upTrafficBytes) + " KB";
        holder.upTraffic.setText(upload);
        if (device.uploadLimit == 0) {
            holder.maxUpSpeed.setText("not set");
        } else {
            holder.maxUpSpeed.setText("Max. Uploadspeed:\n" + device.uploadLimit +  "kbit/s");
        }
        if (device.downloadLimit== 0) {
            holder.maxDownSpeed.setText("not set");
        } else {
            holder.maxDownSpeed.setText("Max. Downloadspeed:\n" + device.uploadLimit +  "kbit/s");
        }


        return row;
    }

    static class DeviceHolder {
        TextView description;
        TextView ipAddress;
        TextView upTraffic;
        TextView downTraffic;
        TextView maxDownSpeed;;
        TextView maxUpSpeed;;
    }
}
