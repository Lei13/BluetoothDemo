package com.lei.bluetooth.network;

import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.lei.bluetooth.Utils.CommonUtils;
import com.lei.bluetooth.Utils.Logs;
import com.lei.bluetooth.application.BleApplication;
import com.lei.bluetooth.bean.Model;
import com.lei.bluetooth.bean.ModelData;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lei on 2016/11/16.
 */

public class NetUtils {
    public static String TAG = NetUtils.class.getSimpleName();
    //    public static final String URL = "http://qyu2508740001.my3w.com/index.php/Api/blueTooth";
    public static final String URL = "http://47.91.77.124/index.php?s=Api/blueTooth";
    // public static final String URL = "http://renrentong.zhiyicx.com/api.php?mod=Weibo&act=getTopics&oauth_token=e61d5360b6001da2a9757a140599695a&oauth_token_secret=7785312ab69a363c4390035817bc6bfb";

    public static void uploadDada(final String data, final OnHttpCompleteListener listener) {
        String url = URL + "&data=" + data + "&imei=" + CommonUtils.getPhoneIMEI();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
                url,
                new Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "onResponse success  " + String.valueOf(response));
                        try {
                            if (listener != null) {
                                if (1 == response.optInt("status")) {
                                    ModelData data = new ModelData(response.optJSONObject("data"));
                                    data.setStatus(1);
                                    listener.onSuccess(data);
                                } else
                                    listener.onFailure(response.optString("msg"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onFailure("数据请求失败");
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "onErrorResponse success  " + String.valueOf(error.getMessage()));
                if (listener != null)
                    listener.onFailure(error.getMessage());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }

        };

// Adding request to request queue
        BleApplication.getInstance().getRequestQueue().add(jsonObjReq);
    }


    public static void postJsonRequest(final String url, final String data, final String ime, final OnHttpCompleteListener listener) {
        Logs.v(URL);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET,
                URL,
                new Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v(TAG, "onResponse success  " + String.valueOf(response));
                        try {
                            if (listener != null) {
                                if (1 == response.optInt("status"))
                                    listener.onSuccess(new ModelData(response.optJSONObject("data")));
                                else
                                    listener.onFailure(response.optString("msg"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onFailure("数据请求失败");
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v(TAG, "onErrorResponse success  " + String.valueOf(error.getMessage()));
                if (listener != null)
                    listener.onFailure(error.getMessage());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("data", data);
                params.put("imei", ime);

                return params;
            }

        };

// Adding request to request queue
        BleApplication.getInstance().getRequestQueue().add(jsonObjReq);
    }

    public interface OnHttpCompleteListener {
        void onSuccess(Model model);

        void onFailure(Object object);
    }

}
