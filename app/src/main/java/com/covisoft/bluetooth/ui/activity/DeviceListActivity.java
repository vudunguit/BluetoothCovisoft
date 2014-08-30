package com.covisoft.bluetooth.ui.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.covisoft.bluetooth.R;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by USER on 8/27/2014.
 */
public class DeviceListActivity extends Activity{

    private ListView mListViewDevice;
    private SimpleAdapter mSimpleAdapter;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private Hashtable<String, Hashtable<String, String>>    mTableDevice = null;
    private ArrayList<HashMap<String, Object>>              mArrayDevice = null;

    private Set<BluetoothDevice> mPairedDevice;

    private final static String MAC = "mac_address";
    private final static String NAME = "name_device";
    private final static String BOND = "bond";

    private boolean isScanFinished = false;
    private static final String TAG = DeviceListActivity.class.getSimpleName();
    private BroadcastReceiver mSCANReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "Scanning...");
            Hashtable<String, String> deviceInfo = new Hashtable<String, String>();

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            deviceInfo.put(MAC, device.getAddress());
            if(device.getName() != null){
                deviceInfo.put(NAME,device.getName());
            }else{
                deviceInfo.put(NAME, "NULL");
            }
            if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                deviceInfo.put(BOND, getString(R.string.device_bonded));
            }else{
                deviceInfo.put(BOND, getString(R.string.device_not_bonded));
            }

            mTableDevice.put(device.getAddress(), deviceInfo);
            showDeviceList();
        }
    };

    private BroadcastReceiver mFINISHReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "SCAN finished");
            isScanFinished = true;
            unregisterReceiver(mSCANReceiver);
            unregisterReceiver(mFINISHReceiver);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle("Bluetooth Device");

        setContentView(R.layout.activity_device_list);

        initView();
        showPairedDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    private void initView(){
        mListViewDevice = (ListView)findViewById(R.id.lvDevice);
        mListViewDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent result = new Intent();
                String sKey = ((TextView)adapterView.findViewById(R.id.textMacDevice)).getText().toString();
                result.putExtra(MAC, sKey);
                result.putExtra(NAME, mTableDevice.get(sKey).get(NAME));
                result.putExtra(BOND, mTableDevice.get(sKey).get(BOND));
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
    }

    private void showPairedDevice(){
        mPairedDevice = mBluetoothAdapter.getBondedDevices();
        if(mPairedDevice.size() > 0){
            if(mTableDevice == null){
                mTableDevice = new Hashtable<String, Hashtable<String, String>>();
            } else{
                mTableDevice.clear();
            }
            for(BluetoothDevice device : mPairedDevice){
                Hashtable<String, String> deviceInfo = new Hashtable<String, String>();
                deviceInfo.put(MAC, device.getAddress());
                deviceInfo.put(NAME, device.getName());
                deviceInfo.put(BOND, getString(R.string.device_bonded));

                mTableDevice.put(device.getAddress(), deviceInfo);
            }
            showDeviceList();
        }
    }

    private void showDeviceList(){
        if(mArrayDevice == null){
            mArrayDevice = new ArrayList<HashMap<String, Object>>();
        }
        if(mSimpleAdapter == null){
            mSimpleAdapter = new SimpleAdapter(
                this,//context
                mArrayDevice, //data
                R.layout.item_device_info, //resource
                new String[]{ //from
                        NAME,
                        MAC,
                        BOND
                },
                new int[]{ //to
                        R.id.textNameDevice,
                        R.id.textMacDevice,
                        R.id.textBondDevice
                }
            );
            mListViewDevice.setAdapter(mSimpleAdapter);
        }
        mArrayDevice.clear();
        Enumeration<String> e = mTableDevice.keys();
        while (e.hasMoreElements()){
            HashMap<String, Object> map = new HashMap<String, Object>();
            String sKeys = e.nextElement();
            map.put(NAME, mTableDevice.get(sKeys).get(NAME));
            map.put(MAC, mTableDevice.get(sKeys).get(MAC));
            map.put(BOND, mTableDevice.get(sKeys).get(BOND));
            mArrayDevice.add(map);
        }
        mSimpleAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.actionScan){
            new DiscoveryTask().execute();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doDiscovery(){
        isScanFinished = false;
        if(mTableDevice == null){
            mTableDevice = new Hashtable<String, Hashtable<String,String>>();
        }else{
            mTableDevice.clear();
        }

        //Register scan finished receiver
        IntentFilter foundReceiver = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mSCANReceiver, foundReceiver);
        //Register scan receiver
        IntentFilter finishFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mFINISHReceiver, finishFilter);

        mBluetoothAdapter.startDiscovery();
        showDeviceList();
    }

    private class DiscoveryTask extends AsyncTask<String, String, Integer>{
        private ProgressDialog mProgressDialog = null;
        private static final int BLUETOOTH_NOT_ENABLED = 0x01;
        private static final int SCAN_DEVICE_FINISHED = 0x02;

        private static final int MILI_WAIT_TIME = 10;
        private static final int MILI_SLEEP_TIME = 150;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(DeviceListActivity.this, "Please wait", "Scanning...", true, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    isScanFinished = true;
                }
            });
            doDiscovery();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
            }
            if(mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
            }
            if(integer == SCAN_DEVICE_FINISHED){

            }else{
                Toast.makeText(DeviceListActivity.this, "Bluetooth is not enabled", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            if(!mBluetoothAdapter.isEnabled()){
                return BLUETOOTH_NOT_ENABLED;
            }

            int iWait = MILI_WAIT_TIME * 1000;
            while(iWait > 0){
                if(isScanFinished){
                    return SCAN_DEVICE_FINISHED;
                }else{
                    iWait -= MILI_SLEEP_TIME;
                    SystemClock.sleep(MILI_SLEEP_TIME);
                }
            }
            return SCAN_DEVICE_FINISHED;
        }
    }
}
