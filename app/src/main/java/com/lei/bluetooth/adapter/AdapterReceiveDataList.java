package com.lei.bluetooth.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.lei.bluetooth.R;
import com.lei.bluetooth.bean.Model;
import com.lei.bluetooth.bean.ModelData;
import com.lei.bluetooth.bean.ModelDevice;

import java.util.List;

/**
 * Created by lei on 2016/11/12.
 */

public class AdapterReceiveDataList extends AdapterBase {
    public AdapterReceiveDataList(Context context, List<Model> models) {
        super(context, models);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item_device, parent, false);
            holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
            holder.tv_old_data = (TextView) convertView.findViewById(R.id.tv_old_data);
            holder.tv_change_data = (TextView) convertView.findViewById(R.id.tv_change_data);
            holder.btn_send = (Button) convertView.findViewById(R.id.btn_send);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        ModelData data = (ModelData) getItem(position);
        holder.tv_date.setText(data.getDate());
        holder.tv_change_data.setText(data.getChange_data());
        holder.tv_old_data.setText(data.getOldData());
        return convertView;
    }


    static class ViewHolder {
        static TextView tv_date, tv_status, tv_old_data, tv_change_data;
        static Button btn_send;

    }
}
