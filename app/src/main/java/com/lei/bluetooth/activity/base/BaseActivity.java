package com.lei.bluetooth.activity.base;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lei.bluetooth.R;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initIntent();
        initView();
        initListener();
        initData();
    }


    protected abstract int getLayoutId();
    protected abstract void initIntent();
    protected abstract void initView();
    protected abstract void initListener();
    protected abstract void initData();
}
