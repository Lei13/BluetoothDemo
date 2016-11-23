package com.lei.bluetooth.Utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.lei.bluetooth.application.BleApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by lei on 2016/11/12.
 */

public class CommonUtils {
    public static byte[] reverseBytes(byte[] a) {
        int len = a.length;
        byte[] b = new byte[len];
        for (int k = 0; k < len; k++) {
            b[k] = a[a.length - 1 - k];
        }
        return b;
    }

    // byte转十六进制字符串
    public static String bytes2HexString(byte[] bytes) {
        String ret = "";
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }

    /**
     * byte转16进制
     *
     * @param b
     * @return
     */
    public static String byte2HexStr(byte[] b) {

        String stmp = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * 将16进制 转换成10进制
     *
     * @param str
     * @return
     */
    public static String print10(String str) {

        StringBuffer buff = new StringBuffer();
        String array[] = str.split(" ");
        for (int i = 0; i < array.length; i++) {
            int num = Integer.parseInt(array[i], 16);
            buff.append(String.valueOf((char) num));
        }
        return buff.toString();
    }

    /**
     * 字符串转16进制
     * @param s
     * @return
     */
    public static String toHexString(String s)
    {
        String str="";
        for (int i=0;i<s.length();i++)
        {
            int ch = (int)s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }
    public static String getPhoneIMEI() {
        TelephonyManager manager = (TelephonyManager) BleApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getDeviceId();
    }private static final String TAG = "TimeHelper";

    public static String friendlyTime(String timestamp) {
        try {
            return friendlyTime(Integer.valueOf(timestamp));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "刚刚";

    }

    public static String friendlyTime(int timestamp) throws Exception {

        long currentSeconds = System.currentTimeMillis() / 1000;
        long timeGap = currentSeconds - timestamp;// 与现在时间相差秒数

        long toZero = currentSeconds / (24 * 60 * 60) * (24 * 60 * 60);
        long todayGap = currentSeconds - toZero;

        String timeStr = null;
        Log.d(TAG, "timeGap=" + timeGap);
        if (timeGap >= 24 * 60 * 60 || timeGap > todayGap) {// 1天以上
            // timeStr = timeGap/(24*60*60)+"天前";
            timeStr = getStandardTimeWithDate(timestamp);
        } else if (timeGap >= 60 * 60 && timeGap < todayGap) {// 1小时-24小时
            timeStr = "今天  " + getStandardTimeWithHour(timestamp);
        } else if (timeGap >= 60 && timeGap < 3600) {// 1分钟-59分钟
            timeStr = timeGap / 60 + "分钟前";
        } else if (timeGap >= 0 && timeGap < 60) {// 1秒钟-59秒钟
            timeStr = "刚刚";
        } else {
            throw new Exception();
        }
        return timeStr;
    }

    public static String friendlyTimeFromStringTime(String timeTemp)
            throws Exception {

        long currentSeconds = System.currentTimeMillis() / 1000;
        long timeGap = currentSeconds - getTimeInt(timeTemp);// 与现在时间相差秒数

        long toZero = currentSeconds / (24 * 60 * 60) * (24 * 60 * 60);
        long todayGap = currentSeconds - toZero;

        String timeStr = null;
        if (timeGap > 24 * 60 * 60 || timeGap > todayGap) {// 1天以上
            // timeStr = timeGap/(24*60*60)+"天前";
            timeStr = getStandardTimeWithDate(getTimeInt(timeTemp));
        } else if (timeGap > 60 * 60 && timeGap < todayGap) {// 1小时-24小时
            timeStr = "今天  " + getStandardTimeWithHour(getTimeInt(timeTemp));
        } else if (timeGap > 60 && timeGap < 3600) {// 1分钟-59分钟
            timeStr = timeGap / 60 + "分钟前";
        } else if (timeGap > 0 && timeGap < 60) {// 1秒钟-59秒钟
            timeStr = "刚刚";
        } else {
            throw new Exception();
        }
        return timeStr;
    }

    public static String getStandardTimeWithYeay(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(timestamp * 1000);
        return sdf.format(date);
    }

    public static String getStandardTimeWithDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        Date date = new Date(timestamp * 1000);
        return sdf.format(date);
    }

    public static String getStandardTimeWithHour(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = new Date(timestamp * 1000);
        return sdf.format(date);
    }

    public static long getTimeInt(String timeTemp) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = sdf.parse(timeTemp);
        return date.getTime() / 1000;

    }

    public static String getStandardTimeWithSen(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        Date date = new Date(timestamp * 1000);
        return sdf.format(date);
    }

    public static String getCurrentTime(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        String currentTime = sdf.format(date);
        return currentTime;
    }

    public static String getCurrentTime() {
        return getCurrentTime("yyyy-MM-dd  HH:mm:ss");
    }
}
