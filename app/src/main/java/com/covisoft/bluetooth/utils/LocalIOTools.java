package com.covisoft.bluetooth.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * Created by USER on 8/28/2014.
 */
public class LocalIOTools {

    public static String LoadFromFile(String path){
        StringBuffer sbOutBuf = new StringBuffer();
        String read;
        BufferedReader bufread;
        try	{
            File fhd = new File(path);
            bufread = new BufferedReader(new FileReader(fhd));

            while ((read = bufread.readLine()) != null)
                sbOutBuf.append(read);
            bufread.close();
        }catch (Exception d){
            d.printStackTrace();
            return null;
        }
        return sbOutBuf.toString();
    }

    public static boolean AppendByteToFile(String sPath, String sFile, byte[] bData){
        try	{
            File fhd = new File(sPath);
            if (!fhd.exists())
                if (!fhd.mkdirs())
                    return false;

            fhd = new File(sPath +"\\"+ sFile);
            if (!fhd.exists())
                if (!fhd.createNewFile())
                    return false;


            FileOutputStream fso = new FileOutputStream(fhd, true);
            fso.write(bData);
            fso.close();
            return true;
        }catch (Exception d){
            d.printStackTrace();
            return false;
        }
    }

    public static boolean CoverterByte2File(String sPath, String sFile, byte[] bData){
        try	{

            File fhd = new File(sPath);
            if (!fhd.exists())
                if (!fhd.mkdirs())
                    return false;

            fhd = new File(sPath +"/"+ sFile);
            if (fhd.exists())
                fhd.delete();

            FileOutputStream fso = new FileOutputStream(fhd);
            fso.write(bData);
            fso.close();
            return true;
        }catch (Exception d){
            System.out.println(d.getMessage());
            return false;
        }
    }
}
