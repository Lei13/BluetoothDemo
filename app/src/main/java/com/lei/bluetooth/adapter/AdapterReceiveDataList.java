package com.lei.bluetooth.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lei.bluetooth.R;
import com.lei.bluetooth.Utils.CommonUtils;
import com.lei.bluetooth.Utils.Config;
import com.lei.bluetooth.Utils.SharedPrefUtils;
import com.lei.bluetooth.Utils.ToastUtils;
import com.lei.bluetooth.activity.BluetoothLeService;
import com.lei.bluetooth.activity.base.BaseActivity;
import com.lei.bluetooth.bean.Model;
import com.lei.bluetooth.bean.ModelData;
import com.lei.bluetooth.bean.ModelDevice;
import com.lei.bluetooth.network.NetUtils;

import java.util.List;

/**
 * Created by lei on 2016/11/12.
 */

public class AdapterReceiveDataList extends AdapterBase {
    public AdapterReceiveDataList(BaseActivity context, List<Model> models) {
        super(context, models);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_item_data, parent, false);
            holder.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
            holder.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
            holder.tv_old_data = (TextView) convertView.findViewById(R.id.tv_old_data);
            holder.tv_change_data = (TextView) convertView.findViewById(R.id.tv_change_data);
            holder.tv_send = (TextView) convertView.findViewById(R.id.tv_send);
            holder.ll_old_data = (LinearLayout) convertView.findViewById(R.id.ll_old_data);
            holder.tv_code = (TextView) convertView.findViewById(R.id.tv_code);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ModelData data = (ModelData) getItem(position);
        holder.tv_change_data.setText(TextUtils.isEmpty(data.getChange_data()) ? "" : (data.getChange_data()));
        holder.tv_old_data.setText(data.getHexStr());
        if (Config.DEBUG) {
            holder.ll_old_data.setVisibility(View.VISIBLE);
        } else {
            holder.ll_old_data.setVisibility(View.GONE);
        }
        if (data.getStatus() == 1) {//已经存服务器
            holder.tv_send.setVisibility(View.GONE);
        } else {
            holder.tv_send.setTag(data);
            holder.tv_send.setVisibility(View.VISIBLE);
            holder.tv_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.showDialog();
                    doUploadData(v, (ModelData) v.getTag());
                }
            });
        }
        try {
            holder.tv_date.setText("" + CommonUtils.friendlyTime(data.getDate()));
            String hexStr = data.getHexStr();
            holder.tv_code.setText("串        码：" + hexStr.substring(1, hexStr.indexOf(",")));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    /**
     * 保存服务器
     *
     * @param modelData
     */
    private void doUploadData(final View view, final ModelData modelData) {
        //modelData.setDate(String.valueOf(System.currentTimeMillis() / 1000));
        NetUtils.uploadDada(modelData.getOldDataIntStr(), new NetUtils.OnHttpCompleteListener() {
            @Override
            public void onSuccess(Model model) {
                modelData.setStatus(1);
                modelData.setData(((ModelData) model).getData());
                modelData.setChange_data(((ModelData) model).getChange_data());
                replaceItem(modelData);
                view.setTag(modelData);
                context.dialogDismiss();
                ToastUtils.showToastShort(context, "上传服务器成功");
                SharedPrefUtils.replaceItem(modelData.getAddress(),modelData);
            }

            @Override
            public void onFailure(Object object) {
                context.dialogDismiss();
                ToastUtils.showToastShort(context, object == null ? "上传服务器失败" : String.valueOf(object));

            }
        });
    }

    class ViewHolder {
        TextView tv_date, tv_status, tv_old_data, tv_change_data, tv_code;
        TextView tv_send;
        LinearLayout ll_old_data;

    }


    public void replaceItem(ModelData data) {
        if (mData == null || mData.isEmpty()) return;
        int length = mData.size();
        for (int i = 0; i < length; i++) {
            if (data.equals(mData.get(i))) {
                mData.set(i, data);
                notifyDataSetChanged();
                return;
            }
        }
    }
}
