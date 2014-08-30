package com.covisoft.bluetooth.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

/**
 * Created by USER on 8/28/2014.
 */
public final class SharePreferenceStorage extends BaseStorage {
    private Context mContext;
    private String mPackageName;
    private SharedPreferences.Editor mEditor;
    private SharedPreferences mPreference;

    private static final String DELIMITER = "|_|";

    public SharePreferenceStorage(Context context){
        mContext = context;
        try{
            mPackageName = (mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0)).packageName;
            mPreference = mContext.getSharedPreferences(mPackageName, Context.MODE_PRIVATE);
            isStorageReady = true;
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

    private void newPrefStorage(){
        if(mEditor == null){
            mEditor = mContext.getSharedPreferences(mPackageName, Context.MODE_PRIVATE).edit();
        }
    }

    private String getIdKey(String sKey, String subKey){
        return sKey + DELIMITER + subKey;
    }
    @Override
    public boolean saveStorage() {
        if(mEditor != null){
            mEditor.commit();
            mEditor = null;
            return true;
        }else {
            return false;
        }
    }

    @Override
    public BaseStorage setVal(String sKey, String sSubKey, String sVal) {
        newPrefStorage();
        mEditor.putString(getIdKey(sKey, sSubKey), sVal);
        return this;
    }

    @Override
    public BaseStorage setVal(String sKey, String sSubKey, int iVal) {
        newPrefStorage();
        mEditor.putInt(getIdKey(sKey, sSubKey), iVal);
        return this;
    }

    @Override
    public BaseStorage setVal(String sKey, String sSubKey, double dbVal) {
        newPrefStorage();
        mEditor.putFloat(getIdKey(sKey, sSubKey), (float)dbVal);
        return this;
    }

    @Override
    public BaseStorage setVal(String sKey, String sSubKey, long lVal) {
        newPrefStorage();
        mEditor.putLong(getIdKey(sKey, sSubKey), lVal);
        return this;
    }

    @Override
    public BaseStorage setVal(String sKey, String sSubKey, boolean bVal) {
        newPrefStorage();
        mEditor.putBoolean(getIdKey(sKey, sSubKey), bVal);
        return this;
    }

    @Override
    public String getStringVal(String sKey, String sSubKey) {
        return mPreference.getString(getIdKey(sKey, sSubKey), "");
    }

    @Override
    public double getDoubleVal(String sKey, String sSubKey) {
        return mPreference.getFloat(getIdKey(sKey, sSubKey), 0.0f);
    }

    @Override
    public int getIntVal(String sKey, String sSubKey) {
        return mPreference.getInt(getIdKey(sKey, sSubKey), 0);
    }

    @Override
    public long getLongVal(String sKey, String sSubKey) {
        return mPreference.getLong(getIdKey(sKey, sSubKey), 0);
    }

    @Override
    public boolean getBooleanVal(String sKey, String sSubKey) {
        return mPreference.getBoolean(getIdKey(sKey, sSubKey), false);
    }

    @Override
    public BaseStorage removeVal(String sKey, String sSubKey) {
        mEditor.remove(getIdKey(sKey, sSubKey));
        return this;
    }
}
