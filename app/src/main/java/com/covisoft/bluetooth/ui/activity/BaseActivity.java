package com.covisoft.bluetooth.ui.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import com.covisoft.bluetooth.R;
import com.covisoft.bluetooth.utils.LocalIOTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by USER on 8/27/2014.
 */
public class BaseActivity extends Activity{





    protected void enableBackActionBar(){
        ActionBar mActionBar = getActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    protected void saveToExternal(String data){
        String mRoot = null;
        String mFileName = null;
        String mPath = null;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            mRoot = Environment.getExternalStorageDirectory().toString();
        }else{
            return;
        }

        mFileName = (new SimpleDateFormat("MMddHHmmss", Locale.getDefault())).format(new Date()) + ".txt";
        mPath = mRoot.concat("/").concat(getString(R.string.app_name));
        if(LocalIOTools.CoverterByte2File(mPath, mFileName, data.getBytes())){
            String msg = ("Saved to: ").concat(mPath).concat("/").concat(mFileName);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, getString(R.string.text_failed_to_save_file), Toast.LENGTH_SHORT).show();
        }
    }

    public String getStringFromRawFile(int rawID){
        InputStream is = getResources().openRawResource(rawID);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i;
        try{
            i = is.read();
            while (i != -1){
                baos.write(i);
                i = is.read();
            }
            is.close();
            return baos.toString().trim();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }


}
