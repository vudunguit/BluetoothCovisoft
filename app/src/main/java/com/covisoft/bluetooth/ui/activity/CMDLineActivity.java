package com.covisoft.bluetooth.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.covisoft.bluetooth.R;
import com.covisoft.bluetooth.utils.CharHexConverter;

/**
 * Created by USER on 8/27/2014.
 */
public class CMDLineActivity extends BaseCommunicationActivity{

    private final static byte TYPE_RECEIVE = 0x01;
    private final static byte TYPE_SEND = 0x02;

    private final static String SUB_KEY_END_FLG = "SUB_KEY_END_FLG";
    private final static String SUB_KEY_MODULE_IS_USED = "SUB_KEY_MODULE_IS_USED";

    private String msEndFlag = msEND_FLGS[0];
    private AutoCompleteTextView mAutoCompleteInput;
    private TextView mTextDataContent;
    private ScrollView mScrollViewContent;

    private static final String TAG = CMDLineActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmd_line);

        initView();
        initControl();
        loadAutoComplateCmdHistory(getLocalClassName(), mAutoCompleteInput);
        loadProfile();

        enableBackActionBar();
        initIOMode();
        usedDataCount();

        new ReceiveTask().executeOnExecutor(FULL_TASK_EXECUTOR);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSPPClient.killReceiveData();
        saveAutoComplateCmdHistory(getLocalClassName());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mAutoCompleteInput.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cmd_line, menu);
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
            case R.id.menuClear:
                mAutoCompleteInput.setText("");
                return true;
            case R.id.menuClearHistory:
                clearAutoComplate(mAutoCompleteInput);
                return true;
            case R.id.menuSetEndFlag:
                selectEndFlag();
                return true;
            case R.id.menuSaveToFile:
                saveDataToFile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void saveDataToFile(){
        if(mTextDataContent.length() > 0){
            saveToExternal(mTextDataContent.getText().toString().trim());
        }
    }

    private void selectEndFlag(){
        /*
        **Not yet improve
         */
        final AlertDialog dialog;
        final RadioGroup rgEndFlag;
        final RadioButton rbSetRn, rbSetN, rbSetOther;
        final EditText txtEndFlagValue;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_end_flg));

        LayoutInflater inflater = LayoutInflater.from(this);
        final View viewEndFlag = inflater.inflate(R.layout.dialog_end_flag, null);
        rgEndFlag = (RadioGroup)viewEndFlag.findViewById(R.id.rgEndFlag);
        rbSetRn = (RadioButton)viewEndFlag.findViewById(R.id.rbSetRn);
        rbSetN = (RadioButton)viewEndFlag.findViewById(R.id.rbSetN);
        rbSetOther = (RadioButton)viewEndFlag.findViewById(R.id.rbSetOther);
        txtEndFlagValue = (EditText)viewEndFlag.findViewById(R.id.txtEndFlagValue);

        builder.setView(viewEndFlag);
        builder.setPositiveButton(R.string.btn_text_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String hexEndFlag = txtEndFlagValue.getText().toString().trim();
                if(hexEndFlag.isEmpty()){
                    msEndFlag = new String();
                    mSPPClient.setReceiveStopFlag(msEndFlag);
                    mStorage.setVal(getLocalClassName(), SUB_KEY_END_FLG, hexEndFlag).saveStorage();
                    showEndFlag();
                }else if(CharHexConverter.isHexString(hexEndFlag)){
                    msEndFlag = CharHexConverter.HexStringToString(hexEndFlag);
                    mSPPClient.setReceiveStopFlag(msEndFlag);
                    mStorage.setVal(getLocalClassName(), SUB_KEY_END_FLG, hexEndFlag).saveStorage();
                    showEndFlag();
                }else{
                    Toast.makeText(CMDLineActivity.this, getString(R.string.msg_not_hex_string), Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialog = builder.create();
        dialog.show();

        txtEndFlagValue.setText(CharHexConverter.StringToHexString(msEndFlag));
        if(msEndFlag.equals(msEND_FLGS[0])){
            rbSetRn.setChecked(true);
        }else if(msEndFlag.equals(msEND_FLGS[1])){
            rbSetN.setChecked(true);
        }else{
            rbSetOther.setChecked(true);
        }

        rgEndFlag.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(rbSetRn.getId() == i){
                    msEndFlag = msEND_FLGS[0];
                    txtEndFlagValue.setEnabled(false);
                }else if(rbSetN.getId() == i){
                    msEndFlag = msEND_FLGS[1];
                    txtEndFlagValue.setEnabled(false);
                }else{
                    txtEndFlagValue.setEnabled(true);
                }
                txtEndFlagValue.setText(CharHexConverter.StringToHexString(msEndFlag));
            }
        });

        txtEndFlagValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String endFlag = txtEndFlagValue.getText().toString().trim();
                if(endFlag.isEmpty() || CharHexConverter.isHexString(endFlag)){
                    txtEndFlagValue.setTextColor(Color.BLACK);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }else{
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    txtEndFlagValue.setTextColor(Color.RED);
                }


            }
        });
    }

    private void initView(){
        mAutoCompleteInput = (AutoCompleteTextView)findViewById(R.id.actvCmdInput);
        mTextDataContent = (TextView)findViewById(R.id.tvDataContent);
        mScrollViewContent = (ScrollView)findViewById(R.id.svDataContent);

        mAutoCompleteInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(EditorInfo.IME_ACTION_SEND == i
                        || EditorInfo.IME_ACTION_DONE == i
                        || EditorInfo.IME_ACTION_UNSPECIFIED == i){
                    String tmp = mAutoCompleteInput.getText().toString().trim();
                    if(mAutoCompleteInput.length() > 0){
                        mAutoCompleteInput.setText("");
                        if(mSPPClient.send(tmp.concat(msEndFlag)) >= 0){
                            appendToDataView(TYPE_SEND, tmp);
                            addAutoComplateVal(tmp, mAutoCompleteInput);
                        }else{
                            Toast.makeText(CMDLineActivity.this, getString(R.string.msg_bt_connect_lost), Toast.LENGTH_LONG).show();
                        }
                        refreshSentDataCount();
                    }
                    return true;
                }else{
                    return false;
                }
            }
        });
    }

    private void appendToDataView(byte b, String data){
        StringBuilder sbTemp = new StringBuilder();
        if(b == TYPE_RECEIVE){
            sbTemp.append("Received: ");
        }else{
            sbTemp.append("Sent: ");
        }
        sbTemp.append(data);
        sbTemp.append("\t(");
        sbTemp.append(data.length() + msEndFlag.length());
        sbTemp.append("B");
        sbTemp.append("\n");
        mTextDataContent.append(sbTemp);
    }

    private void initControl(){
        refreshSentDataCount();
        refreshReceivedDataCount();
    }

    private void loadProfile(){
        String hexEndFlag = mStorage.getStringVal(getLocalClassName(), SUB_KEY_END_FLG);
        boolean isModuleUsed = mStorage.getBooleanVal(getLocalClassName(), SUB_KEY_MODULE_IS_USED);
        if(!isModuleUsed){
            msEndFlag = msEND_FLGS[0];
            mStorage.setVal(getLocalClassName(), SUB_KEY_MODULE_IS_USED, true)
                    .setVal(getLocalClassName(), SUB_KEY_END_FLG, CharHexConverter.StringToHexString(msEndFlag))
                    .saveStorage();

        }else if(hexEndFlag.isEmpty()){
            msEndFlag = "";
        }else{
            msEndFlag = CharHexConverter.HexStringToString(hexEndFlag);
        }
        showEndFlag();
        mSPPClient.setReceiveStopFlag(msEndFlag);
    }

    private void showEndFlag(){
        if(msEndFlag.equals(msEND_FLGS[0])){
            mTextDataContent.setText(String.format(getString(R.string.actCmdLine_msg_helper), getString(R.string.dialog_end_flg_rn)));
        }else if(msEndFlag.equals(msEND_FLGS[1])){
            mTextDataContent.setText(String.format(getString(R.string.actCmdLine_msg_helper), getString(R.string.dialog_end_flg_n)));
        }else{
            String tmp = null;
            if(msEndFlag.isEmpty()){
                tmp = getString(R.string.msg_helper_endflg_nothing);
            }else{
                tmp = String.format(getString(R.string.actCmdLine_msg_helper), "(" + CharHexConverter.StringToHexString(msEndFlag) + ")");
            }
            mTextDataContent.setText(tmp);
        }
    }

    private class ReceiveTask extends AsyncTask<String, String, Integer>{

        private static final byte CONNECT_LOST = 0x01;
        private static final byte THREAD_END = 0x02;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mTextDataContent.append(getString(R.string.msg_receive_data_wating));
            mTextDataContent.append("\n");
            isThreadStop = false;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            mSPPClient.receive();
            while(!isThreadStop){
                if(!mSPPClient.isConnected()){
                    return (int)CONNECT_LOST;
                }
                publishProgress(mSPPClient.receiveStopFlag());
            }
            return (int) THREAD_END;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(values[0] != null){
                appendToDataView(TYPE_RECEIVE, values[0]);
                autoScroll();
                refreshReceivedDataCount();
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == CONNECT_LOST){
                mTextDataContent.append(getString(R.string.msg_bt_connect_lost));
            }else{
                mTextDataContent.append(getString(R.string.msg_receive_data_stop));
            }
            mAutoCompleteInput.setEnabled(true);
            refreshHoldTime();
        }
    }

    private void autoScroll(){
        int iOffSet = mAutoCompleteInput.getMeasuredHeight() - mScrollViewContent.getHeight();
        if(iOffSet > 0){
            mScrollViewContent.scrollTo(0, iOffSet);
        }
    }
}
