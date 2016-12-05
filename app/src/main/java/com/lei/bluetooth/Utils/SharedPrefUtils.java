package com.lei.bluetooth.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.camera2.params.Face;
import android.util.Base64;

import com.lei.bluetooth.application.BleApplication;
import com.lei.bluetooth.bean.ModelData;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zycx on 2016/11/17.
 */

public class SharedPrefUtils {
    public static SharedPreferences getSharedPreference(String spName) {
        SharedPreferences sp = BleApplication.getInstance().getSharedPreferences(spName, Context.MODE_PRIVATE);
        return sp;
    }

    /**
     * 将对象储存到sharepreference
     *
     * @param key
     * @param device
     * @param <T>
     */
    public static <T> boolean saveObject(String preferName, String key, T device) {
        SharedPreferences mSharedPreferences = getSharedPreference(preferName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {   //Device为自定义类
            // 创建对象输出流，并封装字节流
            oos = new ObjectOutputStream(baos);
            // 将对象写入字节流
            oos.writeObject(device);
            // 将字节流编码成base64的字符串
            String oAuth_Base64 = new String(Base64.encode(baos
                    .toByteArray(), Base64.DEFAULT));
            mSharedPreferences.edit().putString(key, oAuth_Base64).commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                baos.close();
                if (null != oos) {
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将对象从shareprerence中取出来
     *
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T getObject(String preferName, String key) {
        SharedPreferences mSharedPreferences = getSharedPreference(preferName);
        T device = null;
        String productBase64 = mSharedPreferences.getString(key, null);

        if (productBase64 == null) {
            return null;
        }
        // 读取字节
        byte[] base64 = Base64.decode(productBase64.getBytes(), Base64.DEFAULT);

        // 封装到字节流
        ByteArrayInputStream bais = new ByteArrayInputStream(base64);
        try {
            // 再次封装
            ObjectInputStream bis = new ObjectInputStream(bais);

            // 读取对象
            device = (T) bis.readObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return device;
    }


    public static void saveDataItem(String key, ModelData data) {
        if (data == null) return;
        List<ModelData> list = getObject(Config.SP_NAME_INFO, key);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(0, data);
        saveObject(Config.SP_NAME_INFO, key, list);
    }

    public static void replaceItem(String key, ModelData data) {
        if (data == null) return;
        List<ModelData> list = getObject(Config.SP_NAME_INFO, key);
        if (list == null) return;
        int length = list.size();
        for (int i = 0; i < length; i++) {
            if (data.equals(list.get(i))) {
                list.set(i, data);
                break;
            }
        }
        saveObject(Config.SP_NAME_INFO, key, list);
    }

    public static boolean clearKey(String key) {
        SharedPreferences sp = getSharedPreference(Config.SP_NAME_INFO);
        if (sp.contains(key)) {
            sp.edit().putString(key, null).commit();
            return true;
        }
        return false;

    }
}
