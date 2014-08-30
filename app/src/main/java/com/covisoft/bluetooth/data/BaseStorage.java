package com.covisoft.bluetooth.data;

/**
 * Created by USER on 8/28/2014.
 */
public abstract class BaseStorage {

    protected boolean isStorageReady = false;

    public boolean isReady(){
        return isStorageReady;
    }

    public abstract boolean saveStorage();

    public abstract BaseStorage setVal(String sKey, String sSubKey, String sVal);

    public abstract BaseStorage setVal(String sKey, String sSubKey, int iVal);

    public abstract BaseStorage setVal(String sKey, String sSubKey, double dbVal);

    public abstract BaseStorage setVal(String sKey, String sSubKey, long lVal);

    public abstract BaseStorage setVal(String sKey, String sSubKey, boolean bVal);

    public abstract String getStringVal(String sKey, String sSubKey);

    public abstract double getDoubleVal(String sKey, String sSubKey);

    public abstract int getIntVal(String sKey, String sSubKey);

    public abstract long getLongVal(String sKey, String sSubKey);

    public abstract boolean getBooleanVal(String sKey, String sSubKey);

    public abstract BaseStorage removeVal(String sKey, String sSubKey);
}
