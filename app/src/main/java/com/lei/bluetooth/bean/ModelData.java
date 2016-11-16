package com.lei.bluetooth.bean;

import org.json.JSONObject;

/**
 * Created by lei on 2016/11/16.
 */

public class ModelData extends Model{
    private String data;
    private String change_data;

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
}
