package com.covisoft.bluetooth.ui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.covisoft.bluetooth.MainApplication;
import com.covisoft.bluetooth.R;
import com.covisoft.bluetooth.controller.BluetoothSPP;
import com.covisoft.bluetooth.data.BaseStorage;
import com.covisoft.bluetooth.data.SharePreferenceStorage;
import com.covisoft.bluetooth.spp.BluetoothControl;
import com.covisoft.bluetooth.controller.BluetoothState;

import java.util.ArrayList;
import java.util.Hashtable;


public class MainActivity extends Activity implements View.OnClickListener, BluetoothSPP.BluetoothConnectionListener{


    private LinearLayout mLayoutInfoDevice;
    private LinearLayout mLayoutUUID;
    private LinearLayout mLayoutControlMode;

    private TextView mNameDevice;
    private TextView mAddressMac;
    private TextView mBond;
    private TextView mUUID;

    private Button mButtonPair;
    private Button mButtonConnect;
    private Button mButtonByteMode;
    private Button mButtonKeyboard;
    private Button mButtonCMDMode;

    private ProgressBar mProgressUUID;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSPP mBluetoothSPP;

    private Hashtable<String, String> mDeviceInfo;
    private ArrayList<String> mArrayUUID;

    private final static String MAC = "mac_address";
    private final static String NAME = "name_device";
    private final static String BOND = "bond";

    public static final byte REQUEST_BYTE_STREAM = 0x02;
    public static final byte REQUEST_CMD_LINE = 0x03;
    public static final byte REQUEST_KEY_BOARD = 0x04;

    public static final byte REQUEST_TEST_ACTIVITY = 0x05;

    private boolean isPaired = false;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBluetoothSPP = new BluetoothSPP(this);
        if(!mBluetoothSPP.isBluetoothAvailable()){
            Toast.makeText(MainActivity.this, getString(R.string.text_bluetooth_not_availeble), Toast.LENGTH_LONG).show();
            finish();
        }
        initFirstTimeInstall();
        initView();
        if(!mBluetoothSPP.isBluetoothEnabled()){
            requestEnableBluetooth();
        }else{
            requestScanDevice();
        }

//        mBluetoothSPP.setBluetoothConnectionListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBluetoothSPP.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!mBluetoothSPP.isServiceAvailable()) {
                mBluetoothSPP.setupService();
                mBluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothSPP.stopService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BluetoothState.REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                mBluetoothSPP.setupService();
                mBluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
                requestScanDevice();
            }else{
                Toast.makeText(getApplicationContext(), getString(R.string.text_bluetooth_not_enable), Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE){
            if(resultCode == Activity.RESULT_OK){

                mDeviceInfo = new Hashtable<String, String>();
                mDeviceInfo.put(NAME, data.getStringExtra(NAME));
                mDeviceInfo.put(MAC, data.getStringExtra(MAC));
                mDeviceInfo.put(BOND, data.getStringExtra(BOND));
                showDeviceInfo();


            }else if (resultCode == Activity.RESULT_CANCELED){
                finish();
            }

        }else if(requestCode == REQUEST_CMD_LINE
                ||requestCode == REQUEST_BYTE_STREAM
                ||requestCode == REQUEST_KEY_BOARD){
            if(MainApplication.getInstance().mBluetoothSPPClient == null
                    || !MainApplication.getInstance().mBluetoothSPPClient.isConnected()){
                isConnected = false;
                setViewVisibility();
                MainApplication.getInstance().closeConnect();
                Toast.makeText(MainActivity.this, getString(R.string.msg_bt_connect_lost), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeviceInfo(){
        mNameDevice.setText(mDeviceInfo.get(NAME));
        mAddressMac.setText(mDeviceInfo.get(MAC));
        mBond.setText(mDeviceInfo.get(BOND));

        mLayoutInfoDevice.setVisibility(View.VISIBLE);
        if(mDeviceInfo.get(BOND).equalsIgnoreCase(getString(R.string.device_bonded))){
            isPaired = true;
            // Remote device with address mac
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mDeviceInfo.get(MAC));
//            mButtonPair.setVisibility(View.GONE);
//            mLayoutControlMode.setVisibility(View.GONE);
//
//            mButtonConnect.setVisibility(View.VISIBLE);
            setViewVisibility();
            showUUIDDevice();
        }else{
            isPaired = false;
//            mButtonPair.setVisibility(View.VISIBLE);
//            mLayoutControlMode.setVisibility(View.GONE);
//            mLayoutUUID.setVisibility(View.GONE);
//            mButtonConnect.setVisibility(View.GONE);
            setViewVisibility();
        }
    }

    private void requestEnableBluetooth(){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
    }
    private void requestScanDevice(){
        Intent i = new Intent(this, DeviceListActivity.class);
        startActivityForResult(i, BluetoothState.REQUEST_CONNECT_DEVICE);
    }

    private void showUUIDDevice(){
        mLayoutUUID.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= 15){
            if(mArrayUUID == null){
                mArrayUUID = new ArrayList<String>();
            }
            mArrayUUID.clear();
            mUUID.setText(null);
            new UUIDTask().execute();
        }else{
            mUUID.setText(getString(R.string.text_does_not_support_uuid_service));
        }
    }

    private void setViewVisibility(){
        if(!isPaired){
            mButtonPair.setVisibility(View.VISIBLE);
            mLayoutControlMode.setVisibility(View.GONE);
            mLayoutUUID.setVisibility(View.GONE);
            mButtonConnect.setVisibility(View.GONE);
        }else if(isPaired && !isConnected){
            mButtonPair.setVisibility(View.GONE);
            mButtonConnect.setVisibility(View.VISIBLE);
//            mLayoutUUID.setVisibility(View.VISIBLE);
            mLayoutControlMode.setVisibility(View.GONE);
        }else if(isPaired && isConnected){
            mButtonPair.setVisibility(View.GONE);
            mButtonConnect.setVisibility(View.GONE);
//            mLayoutUUID.setVisibility(View.VISIBLE);
            mLayoutControlMode.setVisibility(View.VISIBLE);
        }
    }

    private void initFirstTimeInstall(){
        BaseStorage storage = new SharePreferenceStorage(this);
        if(storage.getLongVal("SYSTEM", "FIRST_TIME_INSTALL_TIMESTEMP") == 0){
            storage.setVal("SYSTEM", "FIRST_TIME_INSTALL_TIMESTEMP", System.currentTimeMillis()).saveStorage();
        }
    }
    private void initView(){
        mLayoutInfoDevice = (LinearLayout)findViewById(R.id.llInfoDevice);
        mLayoutUUID = (LinearLayout)findViewById(R.id.llServiceUUID);
        mLayoutControlMode = (LinearLayout)findViewById(R.id.llControlMode);

        mNameDevice = (TextView)findViewById(R.id.textName);
        mAddressMac = (TextView)findViewById(R.id.textMac);
        mBond = (TextView)findViewById(R.id.textBond);
        mUUID = (TextView)findViewById(R.id.textUUID);
        mProgressUUID = (ProgressBar)findViewById(R.id.pbUUID);

        mButtonPair = (Button)findViewById(R.id.btnPair);
        mButtonConnect = (Button)findViewById(R.id.btnConnect);
        mButtonByteMode = (Button)findViewById(R.id.btnByte);
        mButtonKeyboard = (Button)findViewById(R.id.btnKeyboard);
        mButtonCMDMode = (Button)findViewById(R.id.btnCMD);

        mButtonConnect.setOnClickListener(this);
        mButtonPair.setOnClickListener(this);
        mButtonByteMode.setOnClickListener(this);
        mButtonCMDMode.setOnClickListener(this);
        mButtonKeyboard.setOnClickListener(this);
        /*
        ** set view visibility
         */
        mLayoutUUID.setVisibility(View.GONE);
        mLayoutControlMode.setVisibility(View.GONE);
        mLayoutInfoDevice.setVisibility(View.GONE);

        mButtonPair.setVisibility(View.GONE);
        mButtonConnect.setVisibility(View.GONE);
        mLayoutControlMode.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
//        switch (view.getId()){
//            case R.id.btnConnect:
//                new ConnectTask().execute(mDeviceInfo.get(MAC));
//            case R.id.btnByte:
//
//            case R.id.btnCMD:
//
//            case R.id.btnKeyboard:
//
//            case R.id.btnPair:
//                mButtonPair.setEnabled(false);
//                new PairTask().execute(mDeviceInfo.get(MAC));
//        }
        if(view == mButtonConnect){
//            mBluetoothSPP.connect(mDeviceInfo.get(MAC));
            new ConnectTask().execute(mDeviceInfo.get(MAC));
        }else if(view == mButtonPair){
            mButtonPair.setEnabled(false);
            new PairTask().execute(mDeviceInfo.get(MAC));
        }else if(view == mButtonCMDMode){
//            mBluetoothSPP.disconnect();
//            Intent i = new Intent(MainActivity.this, TestActivity.class);

            Intent i = new Intent(MainActivity.this, CMDLineActivity.class);
//            i.putExtra(MAC, mDeviceInfo.get(MAC));
//            i.putExtra(NAME, mDeviceInfo.get(NAME));
//            startActivityForResult(i, REQUEST_TEST_ACTIVITY);
//            startActivity(i);
            startActivityForResult(i, REQUEST_CMD_LINE);
        }else if(view == mButtonKeyboard){
            Intent i = new Intent(MainActivity.this, KeyBoardActivity.class);
            startActivityForResult(i, REQUEST_KEY_BOARD);
        }else if(view == mButtonByteMode){
            Intent i = new Intent(MainActivity.this, ByteStreamActivity.class);
            startActivityForResult(i, REQUEST_BYTE_STREAM);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.actionRescan) {
            requestScanDevice();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class PairTask extends AsyncTask<String, String, Integer>{
        private static final int BOND_SUCCESS = 0x01;
        private static final int BOND_FAILED = 0x02;

        private static final int iTIMEOUT = 1000 * 10;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Pairing...", Toast.LENGTH_SHORT).show();
            registerReceiver(PairReceiver, new IntentFilter(BluetoothControl.PAIRING_REQUEST));
            registerReceiver(PairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
        }

        @Override
        protected Integer doInBackground(String... strings) {
            final int iStepTime = 150;
            int iWait = iTIMEOUT;
            try{

                mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(strings[0]);
                BluetoothControl.createBond(mBluetoothDevice);
                isPaired = false;
            }catch (Exception e){
                e.printStackTrace();
                return BOND_FAILED;
            }
            while(!isPaired && iWait > 0){
                SystemClock.sleep(iStepTime);
                iWait -= iStepTime;
            }
            return (int) ((iWait > 0)? BOND_SUCCESS : BOND_FAILED);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            unregisterReceiver(PairReceiver);
            if(integer == BOND_SUCCESS){
                Toast.makeText(MainActivity.this, "Paired success", Toast.LENGTH_SHORT).show();
                isPaired = true;
                mDeviceInfo.put(BOND, getString(R.string.device_bonded));
                showDeviceInfo();
//                mButtonPair.setVisibility(View.GONE);
//                mButtonConnect.setVisibility(View.VISIBLE);
//                mLayoutControlMode.setVisibility(View.GONE);
                setViewVisibility();
                showUUIDDevice();

            }else{
                try{
                    BluetoothControl.removeBond(mBluetoothDevice);
                }catch (Exception e){
                    e.printStackTrace();
                }
                Toast.makeText(MainActivity.this, "Paired Fail", Toast.LENGTH_SHORT).show();
                isPaired = false;
                mButtonPair.setEnabled(true);
                mDeviceInfo.put(BOND, getString(R.string.device_not_bonded));
//                mButtonPair.setVisibility(View.VISIBLE);
//                mButtonConnect.setVisibility(View.GONE);
//                mLayoutUUID.setVisibility(View.GONE);
//                mLayoutControlMode.setVisibility(View.GONE);
                setViewVisibility();
            }
        }
    }

    private class UUIDTask extends AsyncTask<String, String, Integer>{
        private static final int miWATI_TIME = 4 * 1000;
        private static final int miREF_TIME = 200;
        private boolean isFindServiceRun = false;
        @Override
        protected Integer doInBackground(String... strings) {
            int iWait = miWATI_TIME;

            if (!isFindServiceRun)
                return null;

            while(iWait > 0){
                if (mArrayUUID.size() > 0 && iWait > 1500){
                    iWait = 1500;
                }
                SystemClock.sleep(miREF_TIME);
                iWait -= miREF_TIME;
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
        @Override
        protected void onPreExecute() {
            mProgressUUID.setVisibility(View.VISIBLE);
            mArrayUUID.clear();
            registerReceiver(UUIDReceiver, new IntentFilter(BluetoothDevice.ACTION_UUID));
            isFindServiceRun = mBluetoothDevice.fetchUuidsWithSdp();
        }

        @Override
        protected void onPostExecute(Integer integer) {
            mProgressUUID.setVisibility(View.GONE);
            StringBuilder tempString = new StringBuilder();
            try{
                unregisterReceiver(UUIDReceiver);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }

            if(mArrayUUID.size() > 0){
                for(int i = 0; i < mArrayUUID.size(); i++){
                    tempString.append(mArrayUUID.get(i) + "\n");
                }
                mUUID.setText(tempString);
            }else{
                mUUID.setText(getString(R.string.text_not_find_service_uuid));
            }
        }
    }

    private BroadcastReceiver PairReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = null;
            if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED)
                    isPaired = true;
                else
                    isPaired = false;
            }
        }
    };

    private BroadcastReceiver UUIDReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int iLoop = 0;
            if (BluetoothDevice.ACTION_UUID.equals(action)){
                Parcelable[] uuidExtra =
                        intent.getParcelableArrayExtra("android.bluetooth.device.extra.UUID");
                if (null != uuidExtra)
                    iLoop = uuidExtra.length;
				/*uuidExtra should contain my service's UUID among his files, but it doesn't!!*/
                for(int i=0; i<iLoop; i++)
                    mArrayUUID.add(uuidExtra[i].toString());
            }
        }
    };

    private class ConnectTask extends AsyncTask<String, String, Integer>{

        private ProgressDialog mProgressDialog;
        private static final int CONNECT_FAILED = 0x01;
        private static final int CONNECT_SUCCESS = 0x02;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = ProgressDialog.show(MainActivity.this, "Connecting", "Please wait ...", true, false);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            if(MainApplication.getInstance().createConnect(strings[0])){
                return CONNECT_SUCCESS;
            }else{
                return CONNECT_FAILED;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mProgressDialog.dismiss();
            if(integer == CONNECT_SUCCESS){
                isConnected = true;
                setViewVisibility();
                Toast.makeText(MainActivity.this, getString(R.string.text_connected), Toast.LENGTH_SHORT).show();
            }else{
                isConnected = false;
                setViewVisibility();
                Toast.makeText(MainActivity.this, getString(R.string.text_connect_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDeviceConnectionFailed() {
        isConnected = false;
        setViewVisibility();
//        mButtonConnect.setVisibility(View.VISIBLE);
//        mLayoutControlMode.setVisibility(View.GONE);
        Toast.makeText(this, getString(R.string.text_connect_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeviceDisconnected() {
        isConnected = false;
        setViewVisibility();
//        mButtonConnect.setVisibility(View.VISIBLE);
//        mLayoutControlMode.setVisibility(View.GONE);
        Toast.makeText(this, getString(R.string.text_disconnect), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeviceConnected(String name, String address) {
        isConnected = true;
        setViewVisibility();
//        mButtonConnect.setVisibility(View.GONE);
//        mLayoutControlMode.setVisibility(View.VISIBLE);
        Toast.makeText(this, getString(R.string.text_connected) + name, Toast.LENGTH_SHORT).show();
    }
}
