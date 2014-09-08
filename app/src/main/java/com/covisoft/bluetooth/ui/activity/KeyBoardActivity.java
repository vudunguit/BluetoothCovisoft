package com.covisoft.bluetooth.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.covisoft.bluetooth.R;
import com.covisoft.bluetooth.spp.BluetoothSPPClient;
import com.covisoft.bluetooth.ui.view.RepeatingButton;
import com.covisoft.bluetooth.ui.view.RepeatingButtonListener;
import com.covisoft.bluetooth.utils.CharHexConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by USER on 9/4/2014.
 */
public class KeyBoardActivity extends BaseCommunicationActivity{

    private TextView tvKeyboardReceive;
    private TextView tvKeyboardSend;
    private TextView tvKeyboardReceiveTitle;

    private ScrollView svKeyboardReceive;
    private ScrollView svKeyboardSend;
    private RelativeLayout rlKeyboardSend;

    private static final int NUMBER_BUTTON = 12;
    private RepeatingButton[] repButtons = new RepeatingButton[NUMBER_BUTTON];
    private List<HashMap<String, String>> mListButtonSendValue = new ArrayList<HashMap<String, String>>();

    private boolean isHiddenSendArea = false;

    private String msEndFlg = msEND_FLGS[0];

//    private final static byte MEMU_SET_END_FLG          = 0x21;
//    private final static byte MENU_SET_KEY_BOARD        = 0x22;
//    private final static byte MENU_SET_LONG_PASS_REPEAT = 0x24;
    private final static String SUB_KEY_END_FLG         = "SUB_KEY_END_FLG";
    private final static String SUB_KEY_MODULE_IS_USED  = "SUB_KEY_MODULE_IS_USED";
    private final static String SUB_KEY_BTN_NAME        = "SUB_KEY_BTN_NAME";
    private final static String SUB_KEY_BTN_DOWN_VAL    = "SUB_KEY_BTN_VAL";
    private final static String SUB_KEY_BTN_HOLD_VAL    = "SUB_KEY_BTN_HOLD_VAL";
    private final static String SUB_KEY_BTN_UP_VAL      = "SUB_KEY_BTN_UP_VAL";
    private final static String SUB_KEY_BTN_REPEAT_FREQ = "SUB_KEY_BTN_REPEAT_FREQ";
    private final static int BTN_REPEAT_MIN_FREQ        = 50;

    private int iRepeatFreq = 500;
    public enum TYPE{
        DOWN,
        HOLD,
        UP
    };

    private boolean isSetMode = false;
    private class Listener implements RepeatingButtonListener{
        @Override
        public void onRepeat(View v, long duration, int repeatcount) {
            if(isSetMode){
                return;
            }else{
                onClickArrayRepButton(v, TYPE.HOLD);
            }
        }
        @Override
        public void onUp(View v) {
            if(isSetMode){
                return;
            }else{
                onClickArrayRepButton(v, TYPE.UP);
            }
        }
        @Override
        public void onDown(View v) {
            onClickArrayRepButton(v, TYPE.DOWN);
        }
    }

    public Listener mListener = new Listener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("Keyboard mode");
        setContentView(R.layout.activity_keyboard);

        initView();

        enableBackActionBar();
        initIOMode();
        usedDataCount();
        loadProfile();

        new ReceiveTask().executeOnExecutor(FULL_TASK_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.keyboard, menu);
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
            case R.id.menuKeyboardClear:
                tvKeyboardReceive.setText("");
                tvKeyboardSend.setText("");
                return true;
            case R.id.menuKeyboardIOMode:
                setIOModeDialog();
                return true;
            case R.id.menuKeyboardLongPressRepeat:
                selectRepeatFreq();
                return true;
            case R.id.menuKeyboardSaveToFile:
                saveToExternal(tvKeyboardReceive.getText().toString().trim());
                return true;
            case R.id.menuKeyboardSetButton:
                if(isSetMode){
                    item.setTitle(R.string.menu_set_key_board_start);
                    tvKeyboardSend.setText(getString(R.string.actKeyBoard_init));
                }else{
                    item.setTitle(R.string.menu_set_key_board_end);
                    tvKeyboardSend.setText(getString(R.string.actKeyBoard_set_keyboard_helper));
                }
                isSetMode = !isSetMode;
                return true;
            case R.id.menuKeyboardSetEndFlag:
                selectEndFlag();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void selectRepeatFreq(){
        final AlertDialog dialog;
        final EditText txtFreq = new EditText(this);
        txtFreq.setHint(String.format(
                getString(R.string.actKeyBoard_long_pass_freq_hint)
                ,BTN_REPEAT_MIN_FREQ));
        txtFreq.setInputType(InputType.TYPE_CLASS_NUMBER);
        txtFreq.setText(String.valueOf(iRepeatFreq));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_keyboard_long_pass_frea));
        builder.setView(txtFreq);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int iFreq;
                if(txtFreq.getText().toString().isEmpty()){
                    iFreq = 0;
                }else{
                    iFreq = Integer.valueOf(txtFreq.getText().toString());
                }
                setButtonRepeatFreq(iFreq);
                String temp = String.format(getString(R.string.actKeyBoard_msg_repeat_freq_set) + "\n", iFreq);
                tvKeyboardSend.setText(temp);
            }
        });
        builder.setCancelable(false);
        dialog = builder.create();
        dialog.show();

        txtFreq.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int iFreq;
                if(txtFreq.getText().toString().isEmpty()){
                    iFreq = 0;
                }else{
                    iFreq = Integer.valueOf(txtFreq.getText().toString());
                }
                if(iFreq >= BTN_REPEAT_MIN_FREQ){
                    txtFreq.setTextColor(Color.BLACK);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }else{
                    txtFreq.setTextColor(Color.RED);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
    }
    private void selectEndFlag(){
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
                    msEndFlg = new String();
                    mSPPClient.setReceiveStopFlag(msEndFlg);
                    mStorage.setVal(getLocalClassName(), SUB_KEY_END_FLG, hexEndFlag).saveStorage();
                    showEndFlag();
                }else if(CharHexConverter.isHexString(hexEndFlag)){
                    msEndFlg = CharHexConverter.HexStringToString(hexEndFlag);
                    mSPPClient.setReceiveStopFlag(msEndFlg);
                    mStorage.setVal(getLocalClassName(), SUB_KEY_END_FLG, hexEndFlag).saveStorage();
                    showEndFlag();
                }else{
                    Toast.makeText(KeyBoardActivity.this, getString(R.string.msg_not_hex_string), Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialog = builder.create();
        dialog.show();

        txtEndFlagValue.setText(CharHexConverter.StringToHexString(msEndFlg));
        if(msEndFlg.equals(msEND_FLGS[0])){
            rbSetRn.setChecked(true);
        }else if(msEndFlg.equals(msEND_FLGS[1])){
            rbSetN.setChecked(true);
        }else{
            rbSetOther.setChecked(true);
        }

        rgEndFlag.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(rbSetRn.getId() == i){
                    msEndFlg = msEND_FLGS[0];
                    txtEndFlagValue.setEnabled(false);
                }else if(rbSetN.getId() == i){
                    msEndFlg = msEND_FLGS[1];
                    txtEndFlagValue.setEnabled(false);
                }else{
                    txtEndFlagValue.setEnabled(true);
                }
                txtEndFlagValue.setText(CharHexConverter.StringToHexString(msEndFlg));
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

    @Override
    protected void onResume() {
        super.onResume();
        Display display = getWindowManager().getDefaultDisplay();
        Point size  = new Point();
        display.getSize(size);

        int iHeight = (size.x / 3) * 2 / 3;
        for(int i = 0; i < NUMBER_BUTTON; i++){
            ViewGroup.LayoutParams btnParam = repButtons[i].getLayoutParams();
            btnParam.height = iHeight;
            repButtons[i].setLayoutParams(btnParam);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSPPClient.killReceiveData();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initView(){
        tvKeyboardReceive = (TextView)findViewById(R.id.tvKeyboardReceive);
        tvKeyboardSend = (TextView)findViewById(R.id.tvKeyboardSend);
        tvKeyboardReceiveTitle = (TextView)findViewById(R.id.tvKeyboardReceiveTitle);

        svKeyboardReceive = (ScrollView)findViewById(R.id.svKeyboardReceive);
        svKeyboardSend = (ScrollView)findViewById(R.id.svKeyboardSend);
        rlKeyboardSend = (RelativeLayout)findViewById(R.id.rlKeyboardSend);

        tvKeyboardReceive.setText("");
        tvKeyboardReceiveTitle.append("\t\t(");
        tvKeyboardReceiveTitle.append(getString(R.string.tips_click_to_hide));
        tvKeyboardReceiveTitle.append(":" + getString(R.string.tv_send_area_title));
        tvKeyboardReceiveTitle.append(")");
        tvKeyboardReceiveTitle.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(view == tvKeyboardReceiveTitle){
                    String title = getString(R.string.tv_receive_area_title);
                    TextView tv = ((TextView)view);
                    if(isHiddenSendArea){
                        title += "\t\t(" + getString(R.string.tips_click_to_hide);
                        title += ":" + getString(R.string.tv_send_area_title) + ")";
                        tv.setText(title);
                        rlKeyboardSend.setVisibility(View.VISIBLE);
                    }else{
                        title += "\t\t(" + getString(R.string.tips_click_to_show);
                        title += ":" + getString(R.string.tv_send_area_title) + ")";
                        tv.setText(title);
                        rlKeyboardSend.setVisibility(View.GONE);
                    }
                    rlKeyboardSend.refreshDrawableState();
                    isHiddenSendArea = !isHiddenSendArea;
                }
            }
        });

        String mTitle = getString(R.string.tv_receive_area_title);
        mTitle += "\t\t(" + getString(R.string.tips_click_to_show);
        mTitle += ":" + getString(R.string.tv_send_area_title) + ")";
        tvKeyboardReceiveTitle.setText(mTitle);
        rlKeyboardSend.refreshDrawableState();
        rlKeyboardSend.setVisibility(View.GONE);
        isHiddenSendArea = true;

        repButtons[0] = (RepeatingButton)findViewById(R.id.btnKeyboard1);
        repButtons[1] = (RepeatingButton)findViewById(R.id.btnKeyboard2);
        repButtons[2] = (RepeatingButton)findViewById(R.id.btnKeyboard3);
        repButtons[3] = (RepeatingButton)findViewById(R.id.btnKeyboard4);
        repButtons[4] = (RepeatingButton)findViewById(R.id.btnKeyboard5);
        repButtons[5] = (RepeatingButton)findViewById(R.id.btnKeyboard6);
        repButtons[6] = (RepeatingButton)findViewById(R.id.btnKeyboard7);
        repButtons[7] = (RepeatingButton)findViewById(R.id.btnKeyboard8);
        repButtons[8] = (RepeatingButton)findViewById(R.id.btnKeyboard9);
        repButtons[9] = (RepeatingButton)findViewById(R.id.btnKeyboard10);
        repButtons[10] = (RepeatingButton)findViewById(R.id.btnKeyboard11);
        repButtons[11] = (RepeatingButton)findViewById(R.id.btnKeyboard12);

        loadButtonProfile();

    }

    private void loadButtonProfile(){
        String temp;
        final String model = getLocalClassName();
        for(int i = 0; i < NUMBER_BUTTON; i++){
            String btnName = mStorage.getStringVal(model, SUB_KEY_BTN_NAME.concat(String.valueOf(i)));
            String btnDown = mStorage.getStringVal(model, SUB_KEY_BTN_DOWN_VAL.concat(String.valueOf(i)));
            String btnHold = mStorage.getStringVal(model, SUB_KEY_BTN_HOLD_VAL.concat(String.valueOf(i)));
            String btnUp = mStorage.getStringVal(model, SUB_KEY_BTN_UP_VAL.concat(String.valueOf(i)));
            if(!btnName.isEmpty()){
                repButtons[i].setText(btnName);
            }
            HashMap<String, String> mMapButtonSend = new HashMap<String, String>();
            mMapButtonSend.put("DOWN", (btnDown.isEmpty()? "" : btnDown));
            mMapButtonSend.put("HOLD", (btnHold.isEmpty()? "" : btnHold));
            mMapButtonSend.put("UP", (btnUp.isEmpty()? "" : btnUp));
            mListButtonSendValue.add(mMapButtonSend);
        }
        for(int i = 0; i < NUMBER_BUTTON; i++){
            repButtons[i].bindListener(mListener, 500);
        }
        int iRepeat = mStorage.getIntVal(getLocalClassName(), SUB_KEY_BTN_REPEAT_FREQ);
        if(iRepeat == 0){
            setButtonRepeatFreq(500);
        }else{
            setButtonRepeatFreq(iRepeat);
        }
        temp = String.format(getString(R.string.actKeyBoard_msg_repeat_freq_set) + "\n", iRepeat);
        tvKeyboardReceive.append(temp);
    }

    private void setButtonRepeatFreq(int interval){
        iRepeatFreq = interval;
        for (int i = 0; i < NUMBER_BUTTON; i++){
            repButtons[i].setRepeatFreq(interval);
        }
        mStorage.setVal(getLocalClassName(), SUB_KEY_BTN_REPEAT_FREQ, interval).saveStorage();
    }

    public void onClickArrayRepButton(View v, TYPE type){
        int btnID = v.getId();
        for(int i = 0; i < NUMBER_BUTTON; i++){
            if(repButtons[i].getId() == btnID){
                if(isSetMode){
                    setButtonKeyboard(i);
                }else{
                    if(TYPE.DOWN == type && !mListButtonSendValue.get(i).get("DOWN").isEmpty()){
                        //send method
                        send(mListButtonSendValue.get(i).get("DOWN"));
                    }else if(TYPE.HOLD == type && !mListButtonSendValue.get(i).get("HOLD").isEmpty()){
                        //send method
                        send(mListButtonSendValue.get(i).get("HOLD"));
                    }else if(TYPE.UP == type && !mListButtonSendValue.get(i).get("UP").isEmpty()){
                        //send method
                        send(mListButtonSendValue.get(i).get("UP"));
                    }

                }
                break;
            }
        }
    }

    private void send(String data){
        if(!data.equals("")){
            String sendValue = data;
            int iReturn = 0;
            if(!msEndFlg.isEmpty()){
                iReturn = mSPPClient.send(sendValue.concat(msEndFlg));
            }else{
                iReturn = mSPPClient.send(sendValue);
            }
            if(iReturn >= 0){
                if(rlKeyboardSend.getVisibility() == View.VISIBLE){
                    if(iReturn == 0){
                        tvKeyboardSend.append(sendValue.concat("(fail) "));
                    }else{
                        tvKeyboardSend.append(sendValue.concat("(success) "));
                    }
                }
            }else{
                Toast.makeText(KeyBoardActivity.this, getString(R.string.msg_bt_connect_lost), Toast.LENGTH_SHORT).show();
                tvKeyboardReceive.append(getString(R.string.msg_bt_connect_lost) + "\n");
            }
            refreshSentDataCount();
            autoScroll();
        }
    }

    private void autoScroll(){
        int offSet = 0;
        offSet = tvKeyboardReceive.getMeasuredHeight() - svKeyboardReceive.getHeight();
        if(offSet > 0){
            svKeyboardReceive.scrollTo(0, offSet);
        }
        if(rlKeyboardSend.getVisibility() == View.VISIBLE){
            offSet = tvKeyboardSend.getMeasuredHeight() - svKeyboardSend.getHeight();
            if(offSet > 0){
                svKeyboardSend.scrollTo(0, offSet);
            }
        }
    }

    private void setButtonKeyboard(int id){
        final AlertDialog dialog;
        final String model = getLocalClassName();
        final int buttonSite = id;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_title_keyboard_set));
        LayoutInflater inflater = LayoutInflater.from(this);

        final View viewDialog = inflater.inflate(R.layout.dialog_set_keyboard, null);
        final EditText txtSetButtonName = (EditText)viewDialog.findViewById(R.id.txtSetButtonName);
        final EditText txtSetButtonDown = (EditText)viewDialog.findViewById(R.id.txtSetButtonDown);
        final EditText txtSetButtonUp = (EditText)viewDialog.findViewById(R.id.txtSetButtonUp);
        final EditText txtSetButtonHold = (EditText)viewDialog.findViewById(R.id.txtSetButtonHold);

        txtSetButtonName.setText(repButtons[buttonSite].getText().toString());
        txtSetButtonDown.setText(mListButtonSendValue.get(buttonSite).get("DOWN"));
        txtSetButtonUp.setText(mListButtonSendValue.get(buttonSite).get("UP"));
        txtSetButtonHold.setText(mListButtonSendValue.get(buttonSite).get("HOLD"));
        builder.setView(viewDialog);

        builder.setPositiveButton(R.string.btn_text_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String nameValue = txtSetButtonName.getText().toString().trim();
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("DOWN", txtSetButtonDown.getText().toString().trim());
                map.put("UP", txtSetButtonUp.getText().toString().trim());
                map.put("HOLD", txtSetButtonHold.getText().toString().trim());

                repButtons[buttonSite].setText(nameValue);
                mListButtonSendValue.set(buttonSite, map);

                mStorage.setVal(model, SUB_KEY_BTN_NAME.concat(String.valueOf(buttonSite)), nameValue)
                        .setVal(model, SUB_KEY_BTN_DOWN_VAL.concat(String.valueOf(buttonSite)), map.get("DOWN"))
                        .setVal(model, SUB_KEY_BTN_UP_VAL.concat(String.valueOf(buttonSite)), map.get("UP"))
                        .saveStorage();

            }
        });
        dialog = builder.create();
        dialog.show();

        txtSetButtonDown.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String sendDown = txtSetButtonDown.getText().toString().trim();
                if(sendDown.length() == 0
                        &&txtSetButtonUp.getText().toString().trim().isEmpty()
                        &&txtSetButtonHold.getText().toString().trim().isEmpty()){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    return;
                }
                if(BluetoothSPPClient.IO_MODE_HEX == mByteOutputMode){
                    if(CharHexConverter.isHexString(sendDown)){
                        txtSetButtonDown.setTextColor(Color.BLACK);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }else{
                        txtSetButtonDown.setTextColor(Color.RED);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            }
        });
        txtSetButtonHold.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String holdValue = txtSetButtonHold.getText().toString().trim();
                if(holdValue.length() == 0
                        &&txtSetButtonDown.getText().toString().trim().isEmpty()
                        &&txtSetButtonUp.getText().toString().trim().isEmpty()){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    return;
                }
                if(BluetoothSPPClient.IO_MODE_HEX == mByteOutputMode){
                    if(CharHexConverter.isHexString(holdValue)){
                        txtSetButtonHold.setTextColor(Color.BLACK);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }else{
                        txtSetButtonHold.setTextColor(Color.RED);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            }
        });
        txtSetButtonUp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String upValue = txtSetButtonUp.getText().toString().trim();
                if(upValue.length() == 0
                        &&txtSetButtonDown.getText().toString().trim().isEmpty()
                        &&txtSetButtonHold.getText().toString().trim().isEmpty()){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    return;
                }
                if(BluetoothSPPClient.IO_MODE_HEX == mByteOutputMode){
                    if(CharHexConverter.isHexString(upValue)){
                        txtSetButtonUp.setTextColor(Color.BLACK);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }else{
                        txtSetButtonUp.setTextColor(Color.RED);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            }
        });
    }

    private void loadProfile(){
        String hexEndFlag = mStorage.getStringVal(getLocalClassName(), SUB_KEY_END_FLG);
        boolean isModuleUsed = mStorage.getBooleanVal(getLocalClassName(), SUB_KEY_MODULE_IS_USED);
        if(!isModuleUsed){
            msEndFlg = msEND_FLGS[0];
            mStorage.setVal(getLocalClassName(), SUB_KEY_MODULE_IS_USED, true)
                    .setVal(getLocalClassName(), SUB_KEY_END_FLG, CharHexConverter.StringToHexString(msEndFlg))
                    .saveStorage();
        }else if(hexEndFlag.isEmpty()){
            msEndFlg = "";
        }else{
            msEndFlg = CharHexConverter.HexStringToString(hexEndFlag);
        }

        showEndFlag();
        mSPPClient.setReceiveStopFlag(msEndFlg);
    }

    private void showEndFlag(){
        if(msEndFlg.equals(msEND_FLGS[0])){
            tvKeyboardReceive.append(String.format(
                    getString(R.string.actKeyBoard_msg_helper_endflg),
                    getString(R.string.dialog_end_flg_rn)));
        }else if(msEndFlg.equals(msEND_FLGS[1])){
            tvKeyboardReceive.append(String.format(
                    getString(R.string.actKeyBoard_msg_helper_endflg),
                    getString(R.string.dialog_end_flg_n)));
        }else{
            if(msEndFlg.isEmpty()){
                tvKeyboardReceive.append(getString(R.string.msg_helper_endflg_nothing));
            }else{
                tvKeyboardReceive.append(String.format(
                        getString(R.string.actKeyBoard_msg_helper_endflg),
                        "(" + CharHexConverter.StringToHexString(msEndFlg) + ")"
                ));
            }
        }
    }

    private class ReceiveTask extends AsyncTask<String, String, Integer>{

        private static final int CONNECT_LOST = 0x01;
        private static final int THREAD_END = 0x02;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvKeyboardReceive.append(getString(R.string.msg_receive_data_wating));
            isThreadStop = false;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            mSPPClient.receive();
            while (!isThreadStop){
                if(!mSPPClient.isConnected()){
                    return CONNECT_LOST;
                }else{
                    SystemClock.sleep(10);
                }
                if(mSPPClient.getReceiveBufferLength() > 0){
                    publishProgress(mSPPClient.receive());
                }
            }
            return THREAD_END;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == CONNECT_LOST){
                tvKeyboardReceive.append(getString(R.string.msg_bt_connect_lost));
            }else{
                tvKeyboardReceive.append(getString(R.string.msg_receive_data_stop));
            }
            refreshHoldTime();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            tvKeyboardReceive.append(values[0]);
            autoScroll();
            refreshReceivedDataCount();
        }
    }
}
