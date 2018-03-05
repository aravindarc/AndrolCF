package RecyclerViewUtilitiesPackage;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.example.aravinda.androlcf.R;

import java.util.List;

import DeviceDatabasePackage.Device;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.MyViewHolder>{

    private List<Device> devicesList;
    private SparseBooleanArray switchStateArray = new SparseBooleanArray();

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView deviceId;
        public TextView deviceName;
        public Switch deviceSwitch;
        public LinearLayout layout;

        public MyViewHolder(View view) {
            super(view);
            deviceId = view.findViewById(R.id.deviceId);
            deviceName = (TextView)view.findViewById(R.id.deviceName);
            deviceSwitch = (Switch)view.findViewById(R.id.deviceSwitch);
            layout = itemView.findViewById(R.id.layout);
        }

        @Override
        public void onClick(View view) {

        }
    }

    public void add(int position, Device device) {
        devicesList.add(position, device);
    }

    public void remove(int position) {
        devicesList.remove(position);
        switchStateArray.delete(position);
        notifyItemRemoved(position);
    }

    public DevicesAdapter(List<Device> devicesList, SparseBooleanArray switchStateArray) {
        this.devicesList = devicesList;
        this.switchStateArray = switchStateArray;
        notifyItemRangeChanged(0,devicesList.size());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.device_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Device device = devicesList.get(position);
        holder.deviceId.setText(String.valueOf(device.getDeviceId()));
        holder.deviceName.setText(device.getDeviceName());
        holder.deviceSwitch.setChecked(switchStateArray.get(position));
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }


}
