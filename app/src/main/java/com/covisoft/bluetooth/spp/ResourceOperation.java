package com.covisoft.bluetooth.spp;

/**
 * Created by USER on 8/28/2014.
 */
public class ResourceOperation {

    private int mCount = 0;

    public ResourceOperation(int count){
        mCount = count;
    }

    public boolean isExist(){
        synchronized (this){
            return (mCount == 0);
        }
    }

    public boolean seizeResource(){
        synchronized (this){
            if(mCount > 0){
                mCount--;
                return true;
            }else{
                return false;
            }
        }
    }

    public void revert(){
        synchronized (this){
            mCount++;
        }
    }
}
