package DeviceDatabasePackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravinda on 4/2/18.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "devicesManager";
    private static final String TABLE_NAME = "devices";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DEVICES_TABLE = "CREATE TABLE " + TABLE_NAME + "(" + KEY_ID
                + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT KEY UNIQUE" + ")";
        db.execSQL(CREATE_DEVICES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, device.getDeviceId());
        values.put(KEY_NAME, device.getDeviceName());
        long result = db.insert(TABLE_NAME, null, values);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Device getDevice(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[] { KEY_ID, KEY_NAME },
                KEY_ID + " = ?",
                new String[] {String.valueOf(id)}, null, null, null, null );

        if(cursor != null)
            cursor.moveToFirst();

        Device device = new Device(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1));
        return device;
    }

    public Device getDevice(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[] {KEY_ID, KEY_NAME},
                KEY_NAME + " = ?",
                new String[] { name }, null, null, null, null );

        if(cursor != null)
            cursor.moveToFirst();

        Device device = new Device(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1));
        return device;
    }



    public List<Device> getAllDevices() {

        List<Device> deviceList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cs = db.rawQuery(selectQuery, null);

        Device device;
        while(cs.moveToNext()){
            device = new Device(Integer.parseInt(cs.getString(0)), cs.getString(1));
            deviceList.add(device);
        }


        return deviceList;
    }

    public int getDevicesCount() {
        String countQuery = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        return cursor.getCount();
    }

    public int updateDevice(int id, String deviceName, String sameWhat) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, deviceName);
        values.put(KEY_ID, id);

        int sucessStatus = -1;
        try {
            if(sameWhat == "sameId")
                sucessStatus = db.update(TABLE_NAME, values,
                        KEY_ID + " = ?",
                        new String[]{String.valueOf(id)});
            else if(sameWhat == "sameName")
                sucessStatus = db.update(TABLE_NAME, values,
                        KEY_NAME + " = ?",
                        new String[]{deviceName});
        } catch (Exception e) {

        }
        return sucessStatus;
    }

    public void deleteDevice(Device device) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ? " + "OR " + KEY_NAME + " = ?",
                new String[] { String.valueOf(device.getDeviceId()), device.getDeviceName() });
        db.close();
    }

    public boolean checkDeviceExists(String deviceName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME,
                new String[] {KEY_ID, KEY_NAME},
                KEY_NAME + " = ?",
                new String[] { deviceName }, null, null, null, null );

        if(cursor.getCount() == 0) {
            cursor.close();
            return false;
        }
        return true;
    }
}
