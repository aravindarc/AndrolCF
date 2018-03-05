package DeviceDatabasePackage;

/**
 * Created by aravinda on 1/2/18.
 */

public class Device {
    int id;
    String deviceName;

    public Device() {}
    public Device(int id, String name) {
        setDeviceId(id);
        setDeviceName(name);
    }

    public int getDeviceId() { return  this.id; }
    public String getDeviceName() { return this.deviceName; }

    public void setDeviceId( int id ) { this.id = id; }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName.toUpperCase();
    }
}
