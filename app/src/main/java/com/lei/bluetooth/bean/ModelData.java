package com.lei.bluetooth.bean;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by lei on 2016/11/16.
 * 收到数据传到服务器后返回，保存记录
 */

public class ModelData extends Model {
    //后台返回数据
    private String data;
    private String change_data = "";
    //蓝牙接受到的原始数据
    private String oldDataIntStr = "";
    private List<String> oldDataHex;
    //保存服务器的一个状态
    private int status = 0;
    //收到数据的日期
    private String date;
    private String hexStr;//十六进制 字符串

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
                ", oldDataIntStr='" + oldDataIntStr + '\'' +
                ", oldDataHex=" + oldDataHex +
                ", status=" + status +
                ", date='" + date + '\'' +
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

    public String getOldDataIntStr() {
        return oldDataIntStr;
    }

    public void setOldDataIntStr(String oldDataInt) {
        this.oldDataIntStr = oldDataInt;
    }

    public List<String> getOldDataHex() {
        return oldDataHex;
    }

    public void setOldDataHex(List<String> oldDataHex) {
        this.oldDataHex = oldDataHex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelData modelData = (ModelData) o;

        if (oldDataIntStr != null ? !oldDataIntStr.equals(modelData.oldDataIntStr) : modelData.oldDataIntStr != null)
            return false;
        if (oldDataHex != null ? !oldDataHex.equals(modelData.oldDataHex) : modelData.oldDataHex != null)
            return false;
        return date != null ? date.equals(modelData.date) : modelData.date == null;

    }

    public String getHexStr() {
        return hexStr;
    }

    public void setHexStr(String hexStr) {
        this.hexStr = hexStr;
    }
}
