package com.lei.bluetooth.bean;

import java.util.Objects;

/**
 * Created by lei on 2016/11/12.
 * 搜索到蓝牙设备
 */

public class ModelDevice extends Model {
    private String name, address;
    private String extraData;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelDevice device = (ModelDevice) o;
        return String.valueOf(name).equals(String.valueOf(device.name)) &&
                String.valueOf(address).equals(String.valueOf(device.address));
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}
