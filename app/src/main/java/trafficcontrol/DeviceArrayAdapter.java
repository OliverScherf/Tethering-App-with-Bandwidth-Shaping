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
        WeatherHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity) this.context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new WeatherHolder();
            holder.id = (TextView) row.findViewById(R.id.device_id);
            holder.description = (TextView) row.findViewById(R.id.device_description);
            holder.ipAddress = (TextView) row.findViewById(R.id.device_ipAddress);
            holder.upTraffic = (TextView) row.findViewById(R.id.device_up_traffic);
            holder.downTraffic = (TextView) row.findViewById(R.id.device_down_traffic);
            row.setTag(holder);
        }
        else
        {
            holder = (WeatherHolder)row.getTag();
        }

        Device device = this.devices.get(position);
        holder.id.setText(String.valueOf(position + 1));
        holder.description.setText(device.description);
        holder.ipAddress.setText(device.ipAddress);
        holder.upTraffic.setText(String.valueOf(device.upTrafficBytes));
        holder.downTraffic.setText(String.valueOf(device.downTrafficBytes));
        return row;
    }

    static class WeatherHolder {
        TextView id;
        TextView description;
        TextView ipAddress;
        TextView upTraffic;
        TextView downTraffic;
    }
}
