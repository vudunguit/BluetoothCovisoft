package com.covisoft.bluetooth.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.covisoft.bluetooth.MainApplication;
import com.covisoft.bluetooth.R;
import com.covisoft.bluetooth.controller.BluetoothSPP;
import com.covisoft.bluetooth.controller.BluetoothState;
import com.covisoft.bluetooth.data.BaseStorage;
import com.covisoft.bluetooth.data.JsonStorage;
import com.covisoft.bluetooth.spp.BluetoothSPPClient;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by USER on 9/3/2014.
 */
public class BaseCommunicationActivity extends BaseActivity{
    private TextView mSentDataCount;
    private TextView mReceivedCount;
    private TextView mConnectHoldTime;

    protected final static String[] msEND_FLGS = {"\r\n", "\n"};
    protected final static String KEY_IO_MODE = "key_io_mode";
    protected static final String KEY_HISTORY = "send_history";
    protected static final String HISTORY_SPLIT = "&#&";
    protected ArrayList<String> mArrayCmdHistory = new ArrayList<String>();

    protected boolean isThreadStop = false;
    protected byte mByteInputMode = BluetoothSPPClient.IO_MODE_STRING;
    protected byte mByteOutputMode = BluetoothSPPClient.IO_MODE_STRING;
    protected BluetoothSPPClient mSPPClient = null;
    protected BaseStorage mStorage = null;
    protected static ExecutorService FULL_TASK_EXECUTOR;
    static{
        FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
    }

    protected BluetoothSPP mBluetoothSPP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothSPP = new BluetoothSPP(this);
        if(!mBluetoothSPP.isServiceAvailable()){
            mBluetoothSPP.setupService();
            mBluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
        }
        mSPPClient = MainApplication.getInstance().mBluetoothSPPClient;
        mStorage = MainApplication.getInstance().mStorage;
        if(mSPPClient == null || !mSPPClient.isConnected()){
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }
    }

    protected void usedDataCount(){
        mSentDataCount = (TextView)findViewById(R.id.tvSentCount);
        mReceivedCount = (TextView)findViewById(R.id.tvReceivedCount);
        mConnectHoldTime = (TextView)findViewById(R.id.tvConnectHoldTime);
        refreshSentDataCount();
        refreshReceivedDataCount();

    }

    protected void refreshSentDataCount(){
        long temp = 0;
        if(mSentDataCount != null){
            temp = mSPPClient.getNumberByteSent();
            mSentDataCount.setText(String.format(getString(R.string.text_sent), temp));
            temp = mSPPClient.getConnectHoldTime();
            mConnectHoldTime.setText(String.format(getString(R.string.text_running), temp));

        }
    }
    protected void refreshReceivedDataCount(){
        long temp = 0;
        if(mReceivedCount != null){
            temp = mSPPClient.getNumberByteReceived();
            mReceivedCount.setText(String.format(getString(R.string.text_received), temp));
            temp = mSPPClient.getConnectHoldTime();
            mConnectHoldTime.setText(String.format(getString(R.string.text_running), temp));
        }
    }
    protected void refreshHoldTime(){
        if(mConnectHoldTime != null){
            long temp = mSPPClient.getConnectHoldTime();
            mConnectHoldTime.setText(String.format(getString(R.string.text_running), temp));
        }
    }

    protected void initIOMode(){
        mByteInputMode = (byte)mStorage.getIntVal(KEY_IO_MODE, "input_mode");
        if(mByteInputMode == 0){
            mByteInputMode = BluetoothSPPClient.IO_MODE_STRING;
        }

        mByteOutputMode = (byte)mStorage.getIntVal(KEY_IO_MODE, "output_mode");
        if(mByteOutputMode == 0){
            mByteOutputMode = BluetoothSPPClient.IO_MODE_STRING;
        }
        mSPPClient.setSendMode(mByteOutputMode);
        mSPPClient.setReceiveMode(mByteInputMode);
    }

    protected void setIOModeDialog(){
        final RadioButton rbInputChar, rbInputHex;
        final RadioButton rbOutputChar, rbOutputHex;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_io_mode_set));

        LayoutInflater inflater = LayoutInflater.from(this);
        final View viewDialog = inflater.inflate(R.layout.dialog_id_mode, null);
        rbInputChar = (RadioButton)viewDialog.findViewById(R.id.rbInputChar);
        rbInputHex = (RadioButton)viewDialog.findViewById(R.id.rbInputHex);
        rbOutputChar = (RadioButton)viewDialog.findViewById(R.id.rbOutputChar);
        rbOutputHex = (RadioButton)viewDialog.findViewById(R.id.rbOutputHex);

        if(BluetoothSPPClient.IO_MODE_STRING == mByteInputMode){
            rbInputChar.setChecked(true);
        }else{
            rbInputHex.setChecked(true);
        }

        if(BluetoothSPPClient.IO_MODE_STRING == mByteOutputMode){
            rbOutputChar.setChecked(true);
        }else{
            rbOutputHex.setChecked(true);
        }

        builder.setView(viewDialog);
        builder.setPositiveButton(getString(R.string.btn_text_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mByteInputMode = (rbInputChar.isChecked())? BluetoothSPPClient.IO_MODE_STRING : BluetoothSPPClient.IO_MODE_HEX;
                mByteOutputMode = (rbOutputChar.isChecked())? BluetoothSPPClient.IO_MODE_STRING : BluetoothSPPClient.IO_MODE_HEX;
                mStorage.setVal(KEY_IO_MODE, "input_mode", mByteInputMode)
                        .setVal(KEY_IO_MODE, "output_mode", mByteOutputMode)
                        .saveStorage();
                mSPPClient.setReceiveMode(mByteInputMode);
                mSPPClient.setSendMode(mByteOutputMode);
            }
        });

        builder.create().show();
    }

    protected void saveAutoComplateCmdHistory(String sClass){
        BaseStorage kvAutoComplate = new JsonStorage(this, getString(R.string.app_name), "AutoComplateList");
        if(mArrayCmdHistory.isEmpty())
            kvAutoComplate.setVal(KEY_HISTORY, sClass, "").saveStorage();
        else{
            StringBuilder sbBuf = new StringBuilder();
            String sTmp = null;
            for(int i=0; i<mArrayCmdHistory.size(); i++)
                sbBuf.append(mArrayCmdHistory.get(i) + HISTORY_SPLIT);
            sTmp = sbBuf.toString();
            kvAutoComplate.setVal(KEY_HISTORY, sClass, sTmp.substring(0, sTmp.length()-3)).saveStorage();
        }
        kvAutoComplate = null;
    }

    protected void loadAutoComplateCmdHistory(String sClass, AutoCompleteTextView v){
        BaseStorage kvAutoComplate = new JsonStorage(this, getString(R.string.app_name), "AutoComplateList");
        String sTmp = kvAutoComplate.getStringVal(KEY_HISTORY, sClass);
        kvAutoComplate = null;
        if(!sTmp.equals("")){
            String[] sT = sTmp.split(HISTORY_SPLIT);
            for (int i=0;i<sT.length; i++)
                this.mArrayCmdHistory.add(sT[i]);
            v.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, sT)
            );
        }
    }

    protected void addAutoComplateVal(String sData, AutoCompleteTextView v){
        if (mArrayCmdHistory.indexOf(sData) == -1){
            mArrayCmdHistory.add(sData);
            v.setAdapter(new ArrayAdapter<String>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            mArrayCmdHistory.toArray(new String[mArrayCmdHistory.size()]))
            );
        }
    }

    protected void clearAutoComplate(AutoCompleteTextView v){
        mArrayCmdHistory.clear();
        v.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line));
    }

}
