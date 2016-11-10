package com.lei.bluetooth.Utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by lei on 2016/11/9.
 */

public class ToastUtils {
    private static Toast mToast;


    public static void showToastShort(Context context, String str) {
        showToast(context, str, Toast.LENGTH_SHORT);
    }

    public static void showToastShort(Context context, int resId) {
        showToast(context, resId, Toast.LENGTH_SHORT);
    }

    public static void showToastLong(Context context, String str) {
        showToast(context, str, Toast.LENGTH_LONG);
    }

    public static void showToastLong(Context context, int resId) {
        showToast(context, resId, Toast.LENGTH_LONG);
    }


    private static void showToast(Context context, String str, int time) {
        if (mToast == null)
            mToast = Toast.makeText(context, str, time);
        else mToast.setText(str);
        mToast.show();

    }

    private static void showToast(Context context, int resId, int time) {
        if (mToast == null)
            mToast = Toast.makeText(context, resId, time);
        else mToast.setText(resId);
        mToast.show();

    }
}
