package com.covisoft.bluetooth.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.covisoft.bluetooth.R;
import com.covisoft.bluetooth.controller.BluetoothSPP;
import com.covisoft.bluetooth.controller.BluetoothState;

import org.w3c.dom.Text;

/**
 * Created by USER on 9/5/2014.
 */
public class TestActivity extends Activity implements View.OnClickListener, BluetoothSPP.OnDataReceivedListener, BluetoothSPP.BluetoothConnectionListener{
    private TextView tvDataCommunication;
    private EditText txtDataInput;
    private Button btnSendDataCom;
    private TextView tvConnectionStatuc;

    BluetoothSPP mBluetoothSPP;

    Bundle mBundle;
    private final static String MAC = "mac_address";
    private final static String NAME = "name_device";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mBluetoothSPP = new BluetoothSPP(this);
        if(!mBluetoothSPP.isServiceAvailable()){
            mBluetoothSPP.setupService();
            mBluetoothSPP.startService(BluetoothState.DEVICE_ANDROID);
        }


        String mac = getIntent().getStringExtra(MAC);
        String name = getIntent().getStringExtra(NAME);

        if(mBluetoothSPP.getServiceState() != BluetoothState.STATE_CONNECTED){
            mBluetoothSPP.connect(mac);
        }

        tvConnectionStatuc =(TextView)findViewById(R.id.textStatus);

        tvDataCommunication = (TextView)findViewById(R.id.textRead);
        txtDataInput = (EditText)findViewById(R.id.etMessage);
        txtDataInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(txtDataInput.getText().toString().length() > 0){
                    btnSendDataCom.setEnabled(true);
                }else{
                    btnSendDataCom.setEnabled(false);
                }
            }
        });
        btnSendDataCom = (Button)findViewById(R.id.btnSend);
        btnSendDataCom.setOnClickListener(this);



        mBluetoothSPP.setOnDataReceivedListener(this);
        mBluetoothSPP.setBluetoothConnectionListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == btnSendDataCom){
            if(!txtDataInput.getText().toString().isEmpty()){
                mBluetoothSPP.send(txtDataInput.getText().toString().trim());
                tvDataCommunication.append("Sent: ");
                tvDataCommunication.append(txtDataInput.getText().toString());
                tvDataCommunication.append("\n");
                txtDataInput.setText("");
            }
        }
    }

    @Override
    public void onDataReceived(byte[] data, String message) {
        if(tvDataCommunication != null){
            tvDataCommunication.append("Received: ");
            tvDataCommunication.append(message);
            tvDataCommunication.append("(" + data.length);
            tvDataCommunication.append(")");
            tvDataCommunication.append("\n");
        }
    }

    @Override
    public void onDeviceConnectionFailed() {
        tvConnectionStatuc.setText("Statuc: connection failed");
    }

    @Override
    public void onDeviceDisconnected() {
        tvConnectionStatuc.setText("Statuc: Disconnected");
    }

    @Override
    public void onDeviceConnected(String name, String address) {
        tvConnectionStatuc.setText("Status : Connected to " + name);
    }
}
