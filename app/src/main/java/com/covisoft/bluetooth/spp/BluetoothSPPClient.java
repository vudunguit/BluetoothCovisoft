package com.covisoft.bluetooth.spp;

import com.covisoft.bluetooth.utils.CharHexConverter;

import java.io.UnsupportedEncodingException;

/**
 * Created by USER on 8/29/2014.
 */
public final class BluetoothSPPClient extends BluetoothCommunication{
    public static final byte IO_MODE_HEX = 0x01;
    public static final byte IO_MODE_STRING = 0x02;

    private byte mByteSendMode = IO_MODE_STRING;
    private byte mByteReceiveMode = IO_MODE_STRING;

    private byte[] mArrayByteEnd = null;
    protected String mCharsetName = null;

    public BluetoothSPPClient(String mac) {
        super(mac);
    }

    public void setSendMode(byte outPutMode){
        mByteSendMode = outPutMode;
    }
    public byte getSendMode(){
        return mByteSendMode;
    }

    public void setReceiveMode(byte outPutMode){
        mByteReceiveMode = outPutMode;
    }

    public int send(String strData){
        if(IO_MODE_HEX == mByteSendMode){
            if(CharHexConverter.isHexString(strData)){
                return sendData(CharHexConverter.hexStringToBytes(strData));
            }else {
                return 0;
            }
        }else{
            if(mCharsetName != null){
                try{
                    return sendData(strData.getBytes(mCharsetName));
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                    return sendData(strData.getBytes());
                }
            }else{
                return sendData(strData.getBytes());
            }
        }
    }

    public String receive(){
        byte[] arrByteTemp = receiveData();
        if(arrByteTemp != null){
            if(IO_MODE_HEX == mByteReceiveMode){
                return (CharHexConverter.ByteToHexString(arrByteTemp, arrByteTemp.length)).concat(" ");
            }else{
                return new String(arrByteTemp);
            }
        }else{
            return null;
        }

    }

    public void setReceiveStopFlag(String strStopFlag){
        mArrayByteEnd = strStopFlag.getBytes();
    }

    public void setCharsetName(String charsetName){
        mCharsetName = charsetName;
    }

    public String receiveStopFlag(){
        byte[] arrByteTemp = null;
        if(mArrayByteEnd == null){
            return new String();
        }
        arrByteTemp = receiveDataStop(mArrayByteEnd);
        if(arrByteTemp == null){
            return null;
        }else{
            if(mCharsetName == null){
                return new String(arrByteTemp);
            }else{
                try{
                    return new String(arrByteTemp, mCharsetName);
                }catch (UnsupportedEncodingException e){
                    return new String(arrByteTemp);
                }
            }
        }

//        return  arrByteTemp;
    }
}
