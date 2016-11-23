package com.lei.bluetooth.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lei.bluetooth.R;
import com.lei.bluetooth.Utils.Config;
import com.lei.bluetooth.Utils.SharedPrefUtils;
import com.lei.bluetooth.activity.base.BaseActivity;
import com.lei.bluetooth.adapter.AdapterReceiveDataList;
import com.lei.bluetooth.bean.Model;

import java.util.ArrayList;
import java.util.List;

public class ActivityInfoList extends BaseActivity {
    private ListView listview;
    private AdapterReceiveDataList adapter;
    private List<Model> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_info_list;
    }

    @Override
    protected void initIntent() {

    }

    @Override
    protected void initView() {
        tv_center.setText("接收信息列表");
        listview = (ListView) findViewById(R.id.listview);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        data = new ArrayList<>();
        data = SharedPrefUtils.getObject(Config.SP_NAME_INFO, Config.KEY_INFO);
        adapter = new AdapterReceiveDataList(this, data);
        listview.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

    }
}
