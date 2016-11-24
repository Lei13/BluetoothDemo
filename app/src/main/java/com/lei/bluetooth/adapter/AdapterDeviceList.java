package com.lei.bluetooth.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lei.bluetooth.R;
import com.lei.bluetooth.bean.Model;
import com.lei.bluetooth.bean.ModelDevice;

import java.util.List;

/**
 * Created by lei on 2016/11/12.
 */

public class AdapterDeviceList extends AdapterBase {
    public AdapterDeviceList(Context context, List<Model> models) {
        super(context, models);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item_device, parent, false);
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        ModelDevice device = (ModelDevice) getItem(position);
        holder.tv_name.setText("设备名称:  " + device.getName() + "\nextra_data: " + device.getExtraData());
        holder.tv_address.setText("设备地址:  "+device.getAddress());
        return convertView;
    }


    static class ViewHolder {
        static TextView tv_name, tv_address;

    }
}
