package com.covisoft.bluetooth.spp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by USER on 8/28/2014.
 */
public abstract class BluetoothCommunication {
    public final static String UUID_SPP = "00001101-0000-1000-8000-00805F9B34FB";
    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "Bluetooth Secure";
    //Constains BUFFER 50kb
    private static final int BUFFER_TOTAL = 1024*50;
    private final byte[] mReceivedBuffer = new byte[BUFFER_TOTAL];
    private int mBufferSite = 0;

    private String mMAC;
    private boolean isConnected = false;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private long mNumberByteReceived = 0;
    private long mNumberByteSent = 0;

    private long mConnectEnableTime = 0;
    private long mConnectDisableTime = 0;

    private boolean isReceiveThread = false;
    private final ResourceOperation mResourceReceiveBuffer = new ResourceOperation(1);

    private boolean isStopReceiveData = false;

    private static ExecutorService FULL_TASK_EXECUTOR;
    private static final int SDK_VER;
    static {
        FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
        SDK_VER = Build.VERSION.SDK_INT;
    }


    private static final String TAG = BluetoothCommunication.class.getSimpleName();
    public BluetoothCommunication(String mac){
        mMAC = mac;
    }


    public long getConnectHoldTime(){
        if(mConnectEnableTime == 0){
            return 0;
        }else if(mConnectDisableTime == 0){
            return (System.currentTimeMillis() - mConnectEnableTime)/1000;
        }else{
            return (mConnectDisableTime - mConnectEnableTime)/1000;
        }
    }

    public void closeConnect(){
        if(isConnected){
            try{
                if(mInputStream != null){
                    mInputStream.close();
                }
                if(mOutputStream != null){
                    mOutputStream.close();
                }
                if(mBluetoothSocket != null){
                    mBluetoothSocket.close();
                }
                isConnected = false;
            }catch (IOException e){
                mInputStream = null;
                mOutputStream = null;
                mBluetoothSocket = null;
                isConnected = false;
            } finally {
                mConnectDisableTime = System.currentTimeMillis();
            }
        }
    }

    final public boolean createConnect(){
        if(!mBluetoothAdapter.isEnabled()){
            return false;
        }
        if(isConnected){
            closeConnect();
        }
        final BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mMAC);
        final UUID uuidSPP = UUID.fromString(UUID_SPP);
        try{
            if (SDK_VER >= 10)  //version 2.3.3
                mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuidSPP);
            else    //API level 5
                mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuidSPP);
//            mBluetoothSocket = device.createRfcommSocketToServiceRecord(uuidSPP);

            mBluetoothSocket.connect();
            mOutputStream = mBluetoothSocket.getOutputStream();
            mInputStream = mBluetoothSocket.getInputStream();
            isConnected = true;
            mConnectEnableTime = System.currentTimeMillis();
            Log.e(TAG, "Create connect success");
        }catch (IOException e){
            Log.e(TAG, "Create connect false");
            closeConnect();
            return false;
        }finally {
            mConnectDisableTime = 0;
        }
        Log.e(TAG, "Create connect true");
        return true;
    }

    public boolean isConnected(){
        return isConnected;
    }

    public long getNumberByteReceived(){
        return mNumberByteReceived;
    }

    public long getNumberByteSent(){
        return mNumberByteSent;
    }

    public int getReceiveBufferLength(){
        int bufferSize = 0;
        captureRes(mResourceReceiveBuffer);
        bufferSize = mBufferSite;
        revertRes(mResourceReceiveBuffer);
        return bufferSize;
    }

    /*
    ** seize resources
     */
    private void captureRes(ResourceOperation res){
        while (!res.seizeResource()){
            //Resources are occupied, delayed checks
            SystemClock.sleep(2);
        }
    }
    /*
    ** release resources
     */
    private void revertRes(ResourceOperation res){
        res.revert();
    }

    protected int sendData(byte[] arrByteData){
        if(isConnected){
            try{
                mOutputStream.write(arrByteData);
                mNumberByteSent += arrByteData.length;
                return arrByteData.length;
            }catch (IOException e){
                e.printStackTrace();
                return -3;
            }
        }else{
            return -2;
        }
    }

    final protected synchronized byte[] receiveData(){
        byte[] byteBuffers = null;
        if(isConnected){
            if(!isReceiveThread){
                if(SDK_VER >= 11){
                    new ReceiveThread().executeOnExecutor(FULL_TASK_EXECUTOR);
                }else{
                    new ReceiveThread().execute("");
                }
                return null;
            }
            captureRes(mResourceReceiveBuffer);
            if(mBufferSite > 0){
                byteBuffers = new byte[mBufferSite];
                for(int i = 0; i < mBufferSite; i++){
                    byteBuffers[i] = mReceivedBuffer[i];
                }
                mBufferSite = 0;
            }
            revertRes(mResourceReceiveBuffer);
        }
        return byteBuffers;
    }

    private static boolean compareTwoArrayByte(byte[] arrByteSource, byte[] arrByteTarget){
        if(arrByteSource.length != arrByteTarget.length){
            return false;
        }
        int iSourceLength = arrByteSource.length;
        for(int i = 0; i < iSourceLength; i++){
            if(arrByteSource[i] != arrByteTarget[i]){
                return false;
            }
        }
        return true;
    }

    public void killReceiveData(){
        isStopReceiveData = true;
    }

    final protected byte[] receiveDataStop(byte[] byteStop){
        int iStopCharLength = byteStop.length;
        int iReceiveLength = 0;
        byte[] arrByteCmp = new byte[iStopCharLength];
        byte[] arrByteBuffer = null;
        if(isConnected){
            if(!isReceiveThread){
                if(SDK_VER >= 11){
                    new ReceiveThread().executeOnExecutor(FULL_TASK_EXECUTOR);
                }else{
                    new ReceiveThread().execute("");
                }
                SystemClock.sleep(50);
            }
            while (true){
                captureRes(mResourceReceiveBuffer);
                iReceiveLength = mBufferSite - iStopCharLength;
                revertRes(mResourceReceiveBuffer);
                if(iReceiveLength > 0){
                    break;
                }else{
                    SystemClock.sleep(50);
                }
            }
            isStopReceiveData = false;
            while(isConnected && !isStopReceiveData){
                captureRes(mResourceReceiveBuffer);
                for(int i = 0; i < iStopCharLength; i++){
                    arrByteCmp[i] = mReceivedBuffer[mBufferSite - iStopCharLength + i];
                }
                revertRes(mResourceReceiveBuffer);
                if(compareTwoArrayByte(arrByteCmp, byteStop)){
                    captureRes(mResourceReceiveBuffer);
                    arrByteBuffer = new byte[mBufferSite - iStopCharLength];
                    int iLength = mBufferSite - iStopCharLength;
                    for(int i = 0; i < iLength; i++){
                        arrByteBuffer[i] = mReceivedBuffer[i];
                    }
                    mBufferSite = 0;
                    revertRes(mResourceReceiveBuffer);
                    break;
                }else{
                    SystemClock.sleep(10);
                }
            }

        }
        return arrByteBuffer;
    }

    private class ReceiveThread extends AsyncTask<String, String, Integer>{
        //Buffer maximum space
        private static final int BUFFER_MAX_SPACE = 1024*5;
        //Connection Lost
        private static final int CONNECTION_LOST = 0x01;
        //Receive thread end
        private static final int THREAD_END = 0x02;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            isReceiveThread = true;
            mBufferSite = 0;
        }

        @Override
        protected Integer doInBackground(String... strings) {
            int numberByteRead = 0;
            byte[] arrByteBufferTemp = new byte[BUFFER_MAX_SPACE];
            while (isConnected){
                try{
                    numberByteRead = mInputStream.read(arrByteBufferTemp);

                }catch (IOException e){
                    e.printStackTrace();
                    return CONNECTION_LOST;
                }
                captureRes(mResourceReceiveBuffer);
                mNumberByteReceived += numberByteRead;
                if((mBufferSite + numberByteRead) > BUFFER_TOTAL){
                    mBufferSite = 0;

                }
                for(int i=0; i < numberByteRead; i++){
                    mReceivedBuffer[mBufferSite + i] = arrByteBufferTemp[i];

                }
                mBufferSite += numberByteRead;
                revertRes(mResourceReceiveBuffer);
            }
            return THREAD_END;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            isReceiveThread = false;
            if(integer == CONNECTION_LOST){
                closeConnect();
            }else {
                try{
                    mInputStream.close();
                    mInputStream = null;
                }catch (IOException e){
                    e.printStackTrace();
                    mInputStream = null;
                }

            }

        }
    }



}
