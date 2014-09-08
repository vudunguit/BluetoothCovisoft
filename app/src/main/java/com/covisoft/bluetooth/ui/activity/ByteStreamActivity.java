package com.covisoft.bluetooth.ui.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.covisoft.bluetooth.R;
import com.covisoft.bluetooth.spp.BluetoothSPPClient;
import com.covisoft.bluetooth.utils.CharHexConverter;

import org.w3c.dom.Text;

import java.sql.RowId;

/**
 * Created by USER on 9/4/2014.
 */
public class ByteStreamActivity extends BaseCommunicationActivity implements View.OnClickListener{

    private ImageButton imgBtnSend;
    private AutoCompleteTextView actvByteInput;
    private TextView tvByteReceive;
    private ScrollView svByteReceive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("Byte stream mode");
        setContentView(R.layout.activity_byte_stream);

        initView();
        initControl();
        loadAutoComplateCmdHistory(getLocalClassName(), actvByteInput);

        enableBackActionBar();
        initIOMode();
        usedDataCount();

        new ReceiveTask().executeOnExecutor(FULL_TASK_EXECUTOR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveAutoComplateCmdHistory(getLocalClassName());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actvByteInput.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.byte_stream, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                mSPPClient.killReceiveData();
                isThreadStop = true;
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
            case R.id.menuByteClear:
                actvByteInput.setText("");
                return true;
            case R.id.menuByteClearHistory:
                clearAutoComplate(actvByteInput);
                return true;

            case R.id.menuSetIOMode:
                setIOModeDialog();
                return true;
            case R.id.menuSaveToFile:
                saveDataToFile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveDataToFile(){
        if(tvByteReceive.length() > 0){
            saveToExternal(tvByteReceive.getText().toString().trim());
        }
    }
    private void initView(){
        imgBtnSend = (ImageButton)findViewById(R.id.imgBtnSend);
        actvByteInput = (AutoCompleteTextView)findViewById(R.id.actvByteInput);
        tvByteReceive = (TextView)findViewById(R.id.tvByteStreamReceive);
        svByteReceive = (ScrollView)findViewById(R.id.svByteReceive);
    }

    private void initControl(){
        imgBtnSend.setEnabled(false);
        refreshSentDataCount();
        refreshReceivedDataCount();
        actvByteInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() > 0){
                    imgBtnSend.setEnabled(true);
                }else{
                    imgBtnSend.setEnabled(false);
                }
            }
        });

        imgBtnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == imgBtnSend){
            String sendValue = actvByteInput.getText().toString().trim();
            if(BluetoothSPPClient.IO_MODE_HEX == mByteOutputMode){
                if(!CharHexConverter.isHexString(sendValue)){
                    Toast.makeText(ByteStreamActivity.this, getString(R.string.msg_not_hex_string), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            imgBtnSend.setEnabled(false);
            if(mSPPClient.send(sendValue) >= 0){
                refreshSentDataCount();
                imgBtnSend.setEnabled(true);
                addAutoComplateVal(sendValue, actvByteInput);
            }else{
                Toast.makeText(ByteStreamActivity.this, getString(R.string.msg_bt_connect_lost), Toast.LENGTH_SHORT).show();
                actvByteInput.setEnabled(false);
            }
        }
    }

    private class ReceiveTask extends AsyncTask<String, String, Integer>{

        private static final byte CONNECT_LOST = 0x01;
        private static final byte THREAD_END = 0x02;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvByteReceive.setText(getString(R.string.msg_receive_data_wating));
            isThreadStop = false;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            mSPPClient.receive();
            while(!isThreadStop){
                if(!mSPPClient.isConnected()){
                    return (int)CONNECT_LOST;
                }
                if(mSPPClient.getReceiveBufferLength() > 0){
                    SystemClock.sleep(20);
                    publishProgress(mSPPClient.receive());
                }

            }
            return (int)THREAD_END;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == CONNECT_LOST){
                tvByteReceive.append(getString(R.string.msg_bt_connect_lost));
            }else{
                tvByteReceive.append(getString(R.string.msg_receive_data_stop));
            }
            imgBtnSend.setEnabled(false);
            refreshHoldTime();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(values[0] != null){
                tvByteReceive.append(values[0]);
                autoScroll();
                refreshReceivedDataCount();
            }
        }
    }

    private void autoScroll(){
        int offSet = tvByteReceive.getMeasuredHeight() - svByteReceive.getHeight();
        if(offSet > 0){
            svByteReceive.scrollTo(0, offSet);
        }
    }
}
