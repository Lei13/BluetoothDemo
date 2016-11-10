package com.lei.bluetooth.Utils;

import android.util.Log;

/**
 * Created by lei on 2016/11/9.
 */

public class Logs {
    public static boolean flag = true;
    public static String TAG = "BLUETOOTH";

    public static void v(String str) {
        if (flag)
            Log.v(TAG, str);
    }

    public static void e(String str) {
        if (flag)
            Log.e(TAG, str);
    }

    public static void i(String str) {
        if (flag)
            Log.i(TAG, str);
    }

    public static void d(String str) {
        if (flag)
            Log.d(TAG, str);
    }
    public static void w(String str) {
        if (flag)
            Log.w(TAG, str);
    }
}
