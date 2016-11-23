package com.lei.bluetooth.activity.base;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.lei.bluetooth.R;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    protected ImageView iv_left, iv_right;
    protected TextView tv_center;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        iv_right = (ImageView) findViewById(R.id.iv_right);
        tv_center = (TextView) findViewById(R.id.tv_center);
        iv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected abstract void initListener();

    protected abstract void initData();
}
