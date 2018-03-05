package com.example.aravinda.androlcf;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.speech.RecognizerIntent;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import DeviceDatabasePackage.DatabaseHandler;
import DeviceDatabasePackage.Device;
import MqttPackage.MqttHelper;
import RecyclerViewUtilitiesPackage.DevicesAdapter;
import RecyclerViewUtilitiesPackage.ItemClickListener;
import RecyclerViewUtilitiesPackage.MyDividerItemDecoration;
import RecyclerViewUtilitiesPackage.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    int DEVICE_NUM_LIMIT = 10;
    private List<Device> deviceList = new ArrayList<>();
    private SparseBooleanArray switchStateArray = new SparseBooleanArray();
    private RecyclerView recyclerView;
    private DevicesAdapter mAdapter;

    public MqttHelper mqttHelper;
    DatabaseHandler db;

    public ProgressDialog uiBlock;

    public List<String> deviceTopics = new ArrayList<String>();

    private static final int REQ_CODE_SPEECH_INPUT = 100;

    private static final String myPreferences = "myUser";
    private static final String[] storedValuesIds = {"userId", "userName", "password"};
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        uiBlock = new ProgressDialog(MainActivity.this);
        uiBlock.setTitle("Network Error");
        uiBlock.setMessage("No Connection");
        uiBlock.setButton(DialogInterface.BUTTON_POSITIVE, "Try Changing Credentials", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        uiBlock.setCancelable(false);



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHandler(this);

        deviceTopics.add("device1");
        deviceList = db.getAllDevices();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new DevicesAdapter(deviceList, switchStateArray);
        recyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(!switchStateArray.get(position, false)) {
                    switchStateArray.put(position, true);
                    sendMessage("outTopic", String.valueOf(deviceList.get(position).getDeviceId()) + " 1");
                }
                else {
                    sendMessage("outTopic", String.valueOf(deviceList.get(position).getDeviceId()) + " 0");
                    switchStateArray.put(position, false);
                }
                mAdapter.notifyItemChanged(position);
            }

            @Override
            public void onLongClick(final View view, final int position) {
                final Device currentDevice = deviceList.get(position);
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                final LinearLayout layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                final EditText editText = new EditText(MainActivity.this);
                editText.setHint("Enter device Id");
                editText.setText(String.valueOf(currentDevice.getDeviceId()));
                layout.addView(editText);
                final EditText editText1 = new EditText(MainActivity.this);
                editText1.setHint("Enter device Name");
                editText1.setText(currentDevice.getDeviceName());
                layout.addView(editText1);
                alert.setMessage("Note: Device Id and Name need to be unique");
                alert.setView(layout);
                alert.setTitle("Refactor Device");
                alert.setPositiveButton("Refactor", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String newId = editText.getText().toString();
                        String newName = editText1.getText().toString().toUpperCase();
                        if (!newName.isEmpty() && !newId.isEmpty()) {
                            String currentId = String.valueOf(deviceList.get(position).getDeviceId());
                            String currentName = deviceList.get(position).getDeviceName();
                            Device tempDevice = new Device(Integer.valueOf(newId), newName);
                            int noClash = 0;
                            if(newId.equals(currentId) && newName.equals(currentName)) {
                                makeToast("No change made");
                                return;
                            }
                            else if(newId.equals(currentId))
                                noClash = db.updateDevice(Integer.valueOf(newId), newName, "sameId");
                            else if(newName.equals(currentName))
                                noClash = db.updateDevice(Integer.valueOf(newId), newName, "sameName");
                            else if(!newId.equals(currentId) && !newName.equals(currentName)) {
                                if(db.checkDeviceExists(newName)) {
                                    makeToast("Device Name and Id should be unique");
                                    return;
                                }
                                db.deleteDevice(deviceList.get(position));
                                db.addDevice(tempDevice);
                            }
                            if(noClash != -1) {
                                deviceList.remove(position);
                                deviceList.add(position, tempDevice);
                                mAdapter.notifyItemChanged(position);
                                makeToast("Change(s) made");
                            }
                            else
                                makeToast("Device Name and Id should be unique");
                        }
                        else
                            makeToast("Fields cannot be empty");
                    }
                });
                alert.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.deleteDevice(deviceList.get(position));
                        mAdapter.remove(position);
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).setCancelable(false).show();
            }
        }));

        for (Device cn : deviceList) {
            String log = "Id: "+cn.getDeviceId()+" ,Name: " + cn.getDeviceName();
            // Writing Contacts to log
            Log.i("Name: ", log);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                speechPromt();

            }
        });

        startCheckingForChanges("inTopic");
        if(mqttHelper.stat)
            uiBlock.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return true;
        }

        if (id == R.id.add_new_device) {

            if(db.getDevicesCount() == DEVICE_NUM_LIMIT) {


                makeToast("Device Limit reached!");
                return true;
            }
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            final EditText editText = new EditText(this);
            editText.setHint("Enter device Id");
            layout.addView(editText);
            final EditText editText1 = new EditText(this);
            editText1.setHint("Enter device Name");
            layout.addView(editText1);
            alert.setMessage("Note: Device Id and Name need to be unique");
            alert.setView(layout);
            alert.setTitle("New Device");
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String name = editText1.getText().toString();
                    String id = editText.getText().toString();
                    if(!name.isEmpty() || !id.isEmpty()) {
                        Device device = new Device(Integer.parseInt(id), name);
                        boolean isInserted = db.addDevice(device);
                        if(isInserted) {
                            deviceList.add(device);
                            mAdapter.notifyItemInserted(deviceList.size());
                            makeToast("Device added: " + name);
                        }
                        else
                            makeToast("Id and Name need to be unique!");
                    }
                    else
                        makeToast("Device name cannot be empty!");
                }
            }).show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startCheckingForChanges(String subscriptionTopic) {
        sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE);

        mqttHelper = new MqttHelper(this, subscriptionTopic,
                sharedPreferences.getString(storedValuesIds[0], ""),
                sharedPreferences.getString(storedValuesIds[1], ""),
                sharedPreferences.getString(storedValuesIds[2], ""));

        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                uiBlock.dismiss();
            }

            @Override
            public void connectionLost(Throwable cause) {
                uiBlock.show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                int temp = message.toString().indexOf(' ');
                int temp1 = message.toString().length();
                String d = message.toString().substring(0, temp);
                int s = Integer.parseInt(message.toString().substring(temp+1, temp1));

                for(int i=0; i<deviceList.size(); i++) {
                    if(deviceList.get(i).getDeviceId() == Integer.parseInt(String.valueOf(d))){
                        if (switchStateArray.get(i) == true && s == 1)
                            continue;
                        else if (switchStateArray.get(i) == false && s == 0)
                            continue;
                        else {
                            if (s == 1 && switchStateArray.get(i) == false)
                                switchStateArray.put(i, true);
                            else
                                switchStateArray.put(i, false);
                            mAdapter.notifyItemChanged(i);
                        }
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }

        });


    }

    private void sendMessage(String topic, String message) {
        mqttHelper.publishMessage(topic, message);
    }

    private void speechPromt() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the device name to switch on or off");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch ( ActivityNotFoundException e) {
            makeToast("Speech input not supported on your phone");
        }
    }

    @Override
    protected  void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );

        switch ( requestCode ) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    compare(result.get(0));
                }
                break;
            }
        }
    }

    private void compare(String voiceCommand) {
        String message = "";

        int[] indexList = new int[mAdapter.getItemCount()];

        for(int i=0; i<mAdapter.getItemCount(); i++)
            indexList[i] = voiceCommand.toLowerCase().indexOf(deviceList.get(i).getDeviceName().toLowerCase());

        int on = voiceCommand.toLowerCase().indexOf("on");
        int off = voiceCommand.toLowerCase().indexOf("off");
        int all = voiceCommand.toLowerCase().indexOf("all");

        boolean flag = true;

        for(int i=0; i<mAdapter.getItemCount(); i++) {
            if(indexList[i] != -1) {
                if(on != -1)
                    message = String.valueOf(deviceList.get(i).getDeviceId()) + " " + "1";
                else if(off != -1)
                    message = String.valueOf(deviceList.get(i).getDeviceId()) + " " + "0";
                flag = false;
                break;
            }
        }

        if(all != -1) {
            if(on != -1)
                message = "*" + " " + "1";
            else if(off != -1)
                message = "*" + " " + "0";
            flag = false;
        }


        if(flag)
            makeToast("Unrecognized voice command");
        else {
            makeToast(message);
            sendMessage("outTopic", message);
        }
    }

}