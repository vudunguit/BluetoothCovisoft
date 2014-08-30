package com.covisoft.bluetooth.data;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by USER on 8/28/2014.
 */
public final class JsonStorage extends BaseStorage {
    private Context     mContext;
    private String      mPackageName;
    private String      PROFILE_JSON_NAME = "profiles.json";
    private JSONObject  mJsonObject;

    public JsonStorage (Context context){
        mContext = context;
        try{
            mPackageName = (mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0)).packageName;
            isStorageReady = readStorage();
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

    public JsonStorage(Context context, String rootPath){
        mContext = context;
        mPackageName = rootPath;
        isStorageReady = readStorage();
    }

    public JsonStorage(Context context, String rootPath, String configFile){
        mContext = context;
        mPackageName = rootPath;
        PROFILE_JSON_NAME = configFile.concat(".json");
        isStorageReady = readStorage();
    }

    private boolean readStorage(){
        char[] cBuf = new char[512];
        StringBuilder sb = new StringBuilder();
        int iRet = 0;
        try {
            FileInputStream fis = new FileInputStream(getFilehd());
            InputStreamReader reader = new InputStreamReader(fis);
            while((iRet = reader.read(cBuf)) > 0)
                sb.append(cBuf, 0, iRet);
            reader.close();
            fis.close();
            String sTmp = sb.toString();
            if (sTmp.length() > 0)
                mJsonObject = new JSONObject(sTmp);
            else
                mJsonObject = new JSONObject();
            return true;
        } catch (FileNotFoundException e) {
            mJsonObject = new JSONObject();
            return true;
        } catch (IOException e) {
            mJsonObject = new JSONObject();
            return true;
        } catch (JSONException e) {
            mJsonObject = new JSONObject();
            return true;
        }
    }
    private File getFilehd() {
        File f = null;
        String sRoot = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            sRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
            f = new File(sRoot.concat("/").concat(mPackageName).concat("/"));
            if (!f.exists())
                f.mkdirs();
            f = new File(sRoot.concat("/").concat(mPackageName).concat("/"), PROFILE_JSON_NAME);
            Log.v(mPackageName, sRoot.concat("/").concat(mPackageName).concat("/") + PROFILE_JSON_NAME);
        }
        else{
            f = new File(mContext.getFilesDir(), PROFILE_JSON_NAME);
        }
        return f;
    }

    @Override
    public boolean saveStorage() {
        File f = getFilehd();
        if (f.exists())
            f.delete();
        try {
            FileOutputStream fso = new FileOutputStream(f);
            fso.write(mJsonObject.toString().getBytes());
            fso.close();
            fso = null;
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public BaseStorage setVal(String sKey, String sSubKey, String sVal) {
        if (isReady()){
            try {
                JSONObject jTmp = mJsonObject.optJSONObject(sKey);
                if (null == jTmp){
                    if (null == sVal)
                        sVal = "";
                    mJsonObject.put(sKey, new JSONObject().put(sSubKey, sVal));
                }
                else
                    jTmp.put(sSubKey, sVal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public BaseStorage setVal(String sKey, String sSubKey, int iVal) {
        if (isReady()){
            try {
                JSONObject jTmp = mJsonObject.optJSONObject(sKey);
                if (null == jTmp)
                    mJsonObject.put(sKey, new JSONObject().put(sSubKey, iVal));
                else
                    jTmp.put(sSubKey, iVal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public BaseStorage setVal(String sKey, String sSubKey, double dbVal) {
        if (isReady()){
            try {
                JSONObject jTmp = mJsonObject.optJSONObject(sKey);
                if (null == jTmp)
                    mJsonObject.put(sKey, new JSONObject().put(sSubKey, dbVal));
                else
                    jTmp.put(sSubKey, dbVal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public BaseStorage setVal(String sKey, String sSubKey, long lVal) {
        if (isReady()){
            try {
                JSONObject jTmp = mJsonObject.optJSONObject(sKey);
                if (null == jTmp)
                    mJsonObject.put(sKey, new JSONObject().put(sSubKey, lVal));
                else
                    jTmp.put(sSubKey, lVal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public BaseStorage setVal(String sKey, String sSubKey, boolean bVal) {
        if (isReady()){
            try {
                JSONObject jTmp = mJsonObject.optJSONObject(sKey);
                if (null == jTmp)
                    mJsonObject.put(sKey, new JSONObject().put(sSubKey, bVal));
                else
                    jTmp.put(sSubKey, bVal);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    @Override
    public String getStringVal(String sKey, String sSubKey) {
        JSONObject jsObj = null;
        if (isReady()){
            if (null != (jsObj = mJsonObject.optJSONObject(sKey))){
                try {
                    return jsObj.getString(sSubKey);
                } catch (JSONException e) {
                    return "";
                }
            }
        }
        return "";
    }

    @Override
    public double getDoubleVal(String sKey, String sSubKey) {
        JSONObject jsObj = null;
        if (isReady()){
            if (null != (jsObj = mJsonObject.optJSONObject(sKey))){
                try {
                    return jsObj.getDouble(sSubKey);
                } catch (JSONException e) {
                    return 0.0d;
                }
            }
        }
        return 0.0d;
    }

    @Override
    public int getIntVal(String sKey, String sSubKey) {
        JSONObject jsObj = null;
        if (isReady()){
            if (null != (jsObj = mJsonObject.optJSONObject(sKey))){
                try {
                    return jsObj.getInt(sSubKey);
                } catch (JSONException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    @Override
    public long getLongVal(String sKey, String sSubKey) {
        JSONObject jsObj = null;
        if (isReady()){
            if (null != (jsObj = mJsonObject.optJSONObject(sKey))){
                try {
                    return jsObj.getLong(sSubKey);
                } catch (JSONException e) {
                    return 0l;
                }
            }
        }
        return 0l;
    }

    @Override
    public boolean getBooleanVal(String sKey, String sSubKey) {
        JSONObject jsObj = null;
        if (isReady()){
            if (null != (jsObj = mJsonObject.optJSONObject(sKey))){
                try {
                    return jsObj.getBoolean(sSubKey);
                } catch (JSONException e) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public BaseStorage removeVal(String sKey, String sSubKey) {
        JSONObject jsObj = null;
        if (isReady()){
            if (null != (jsObj = mJsonObject.optJSONObject(sKey))){
                jsObj.remove(sSubKey);
                if (jsObj.length() == 0)
                    jsObj.remove(sKey);
            }
        }
        return this;
    }
}
