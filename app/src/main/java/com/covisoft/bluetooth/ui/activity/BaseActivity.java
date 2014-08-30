package com.covisoft.bluetooth.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.covisoft.bluetooth.R;
import com.covisoft.bluetooth.controller.BluetoothSPP;
import com.covisoft.bluetooth.controller.BluetoothState;

import org.w3c.dom.Text;


/**
 * Created by USER on 8/27/2014.
 */
public class BaseActivity extends Activity{

    private String MAC;
    private String NAME;

    private TextView mSentDataCount;
    private TextView mReceivedCount;
    private TextView mConnectHoldTime;

    protected BluetoothSPP mBluetoothSPP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extra = getIntent().getExtras();
        MAC = extra.getString("MAC");
        NAME = extra.getString("NAME");

        initHeaderView();
        mBluetoothSPP = new BluetoothSPP(this);
        if(!mBluetoothSPP.isServiceAvailable()){
            mBluetoothSPP.setupService();
            mBluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
        }
        if(mBluetoothSPP.getServiceState() == BluetoothState.STATE_CONNECTED){
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Disconnect", Toast.LENGTH_SHORT).show();
        }
    }

    private void initHeaderView(){
        mSentDataCount = (TextView)findViewById(R.id.tvSentCount);
        mReceivedCount = (TextView)findViewById(R.id.tvReceivedCount);
        mConnectHoldTime = (TextView)findViewById(R.id.tvConnectHoldTime);
    }
}
