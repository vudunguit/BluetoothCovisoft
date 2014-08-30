package com.covisoft.bluetooth.utils;

import java.util.Locale;

/**
 * Created by USER on 8/28/2014.
 */
public class CharHexConverter {
    private final static char[] mArrayChar = "0123456789ABCDEF".toCharArray();
    private final static String mHexString = "0123456789ABCDEF";

    public static boolean isHexString(String hexString){
        String sTmp = hexString.toString().trim().replace(" ", "").toUpperCase(Locale.US);
        int iLength = sTmp.length();

        if (iLength > 1 && iLength%2 == 0){
            for(int i=0; i<iLength; i++) {
                if (!hexString.contains(sTmp.substring(i, i + 1))) {
                    return false;
                }
            }
            return true;
        }else{
            return false;
        }
    }

    public static String StringToHexString(String mString){
        StringBuilder sb = new StringBuilder();
        byte[] bs = mString.getBytes();

        for (int i = 0; i < bs.length; i++){
            sb.append(mArrayChar[(bs[i] & 0xFF) >> 4]);
            sb.append(mArrayChar[bs[i] & 0x0F]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    private static String HexStringToString(String hexString){
        hexString = hexString.toString().trim().replace(" ", "").toUpperCase(Locale.US);
        char[] arrayChar = hexString.toCharArray();
        byte[] bytes = new byte[hexString.length() / 2];
        int iTmp = 0x00;;

        for (int i = 0; i < bytes.length; i++){
            iTmp = mHexString.indexOf(arrayChar[2 * i]) << 4;
            iTmp |= mHexString.indexOf(arrayChar[2 * i + 1]);
            bytes[i] = (byte) (iTmp & 0xFF);
        }
        return new String(bytes);
    }

    public static String ByteToHexString(byte[] b, int iLen){
        StringBuilder sb = new StringBuilder();
        for (int n=0; n<iLen; n++){
            sb.append(mArrayChar[(b[n] & 0xFF) >> 4]);
            sb.append(mArrayChar[b[n] & 0x0F]);
            sb.append(' ');
        }
        return sb.toString().trim().toUpperCase(Locale.US);
    }

    public static byte[] hexStringToBytes(String src){
        src = src.trim().replace(" ", "").toUpperCase(Locale.US);
        int m=0,n=0;
        int iLen=src.length()/2;
        byte[] ret = new byte[iLen];

        for (int i = 0; i < iLen; i++){
            m=i*2+1;
            n=m+1;
            ret[i] = (byte)(Integer.decode("0x"+ src.substring(i*2, m) + src.substring(m,n)) & 0xFF);
        }
        return ret;
    }

    public static String StringToUnicode(String strText) throws Exception
    {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++){
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append("\\u");
            else
                str.append("\\u00");
            str.append(strHex);
        }
        return str.toString();
    }

    public static String UnicodeToString(String hex){
        int t = hex.length() / 6;
        int iTmp = 0;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++){
            String s = hex.substring(i * 6, (i + 1) * 6);

            iTmp = (Integer.valueOf(s.substring(2, 4), 16) << 8) | Integer.valueOf(s.substring(4), 16);

            str.append(new String(Character.toChars(iTmp)));
        }
        return str.toString();
    }
}
