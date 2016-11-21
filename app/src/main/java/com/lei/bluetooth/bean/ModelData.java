package com.lei.bluetooth.bean;

import org.json.JSONObject;

/**
 * Created by lei on 2016/11/16.
 * 收到数据传到服务器后返回，保存记录
 */

public class ModelData extends Model {
    //后台返回数据
    private String data;
    private String change_data;
    //蓝牙接受到的原始数据
    private String oldData;
    //保存服务器的一个状态
    private int status;
    //收到数据的日期
    private String date;

    public ModelData() {
    }

    public ModelData(JSONObject jsonObject) {
        if (jsonObject == null) return;
        if (jsonObject.has("change_data")) {
            setChange_data(jsonObject.optString("change_data"));
        }
        if (jsonObject.has("data"))
            setChange_data(jsonObject.optString("data"));
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getChange_data() {
        return change_data;
    }

    public void setChange_data(String change_data) {
        this.change_data = change_data;
    }

    @Override
    public String toString() {
        return "ModelData{" +
                "data='" + data + '\'' +
                ", change_data='" + change_data + '\'' +
                '}';
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOldData() {
        return oldData;
    }

    public void setOldData(String oldData) {
        this.oldData = oldData;
    }
}
