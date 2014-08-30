package com.covisoft.bluetooth.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.covisoft.bluetooth.R;

/**
 * Created by USER on 8/27/2014.
 */
public class CMDLineActivity extends BaseActivity{



    private static final String TAG = CMDLineActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmd_line);



        Log.e(TAG, "Connected device name: "+ mBluetoothSPP.getConnectedDeviceAddress());
    }
}
