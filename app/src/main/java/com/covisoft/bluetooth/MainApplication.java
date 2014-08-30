package com.covisoft.bluetooth;

import android.app.Application;
import android.nfc.Tag;
import android.util.Log;

import com.covisoft.bluetooth.controller.BluetoothSPP;
import com.covisoft.bluetooth.controller.BluetoothState;
import com.covisoft.bluetooth.data.BaseStorage;
import com.covisoft.bluetooth.data.JsonStorage;
import com.covisoft.bluetooth.spp.BluetoothSPPClient;

/**
 * Created by USER on 8/27/2014.
 */
public class MainApplication extends Application{

    private static MainApplication instance;

    public BluetoothSPPClient mBluetoothSPPClient = null;

    public BaseStorage mStorage;
    private static final String TAG = MainApplication.class.getSimpleName();

    public static MainApplication getInstance(){
        if(instance == null){
            instance = new MainApplication();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        mStorage = new JsonStorage(this, getString(R.string.app_name));

    }

    public boolean createConnect(String mac){
        if(mBluetoothSPPClient == null){
            mBluetoothSPPClient = new BluetoothSPPClient(mac);
            if(mBluetoothSPPClient.createConnect()){
                Log.e(TAG, "Create connect true");
                return true;
            }else{
                mBluetoothSPPClient = null;
                Log.e(TAG, "Create connect false");
                return false;
            }
        }else{
            Log.e(TAG, "Create connect false");
            return false;
        }
    }

    public void closeConnect(){
        if(mBluetoothSPPClient != null){
            mBluetoothSPPClient.closeConnect();
            mBluetoothSPPClient = null;
        }
    }



}
