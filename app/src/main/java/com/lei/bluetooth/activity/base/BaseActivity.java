package com.lei.bluetooth.activity.base;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.lei.bluetooth.R;
import com.lei.bluetooth.activity.ActivityInfoList;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    protected ImageView iv_left;
    protected TextView tv_center, tv_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initIntent();
        initTitle();
        initView();
        initListener();
        initData();
    }


    protected abstract int getLayoutId();

    protected abstract void initIntent();

    protected abstract void initView();

    protected void initTitle() {
        iv_left = (ImageView) findViewById(R.id.iv_left);
        tv_right = (TextView) findViewById(R.id.tv_right);
        tv_center = (TextView) findViewById(R.id.tv_center);
        iv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ActivityInfoList.class);
                startActivity(intent);
            }
        });
    }

    protected abstract void initListener();

    protected abstract void initData();
}
