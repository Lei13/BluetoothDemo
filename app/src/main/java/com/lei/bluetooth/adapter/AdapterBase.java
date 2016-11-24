package com.lei.bluetooth.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lei.bluetooth.bean.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lei on 2016/11/12.
 */

public abstract class AdapterBase extends BaseAdapter {
    protected List<Model> mData = new ArrayList<>();
    protected LayoutInflater inflater;
    protected Context context;


    public AdapterBase(Context context, List<Model> models) {
        this.context = context;
        this.mData = models;
        inflater = LayoutInflater.from(context);
        if (models == null) models = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return (mData == null || mData.isEmpty()) ? null : mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
