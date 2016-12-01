package com.lei.bluetooth.activity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lei.bluetooth.R;
import com.lei.bluetooth.Utils.Config;
import com.lei.bluetooth.Utils.Logs;
import com.lei.bluetooth.Utils.SharedPrefUtils;
import com.lei.bluetooth.Utils.ToastUtils;
import com.lei.bluetooth.activity.base.BaseActivity;
import com.lei.bluetooth.adapter.AdapterReceiveDataList;
import com.lei.bluetooth.bean.Model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ActivityConnectDevice extends BaseActivity {
    private TextView tv_device_info, tv_data, tv_connect_state, tv_connect, tv_send_data;
    private TextView tv_data_state;
    private LinearLayout ll_debug_info;//调式接收数据
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private EditText et_send;
    private Button btn_send, btn_read;
    private ListView listview;//以往数据记录
    private AdapterReceiveDataList mAdapter;
    private List<Model> mData;
    private boolean mConnected = false;


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Logs.e("Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_connect_device;
    }

    @Override
    protected void initIntent() {
        mDeviceName = getIntent().getStringExtra("name");
        mDeviceAddress = getIntent().getStringExtra("address");
    }

    @Override
    protected void initView() {
        tv_center.setText(mDeviceName);
        ll_debug_info = (LinearLayout) findViewById(R.id.ll_debug_info);
        tv_device_info = (TextView) findViewById(R.id.tv_device_info);
        tv_data = (TextView) findViewById(R.id.tv_data);
        tv_connect_state = (TextView) findViewById(R.id.tv_connect_state);
        btn_read = (Button) findViewById(R.id.btn_read);
        et_send = (EditText) findViewById(R.id.et_send);
        btn_send = (Button) findViewById(R.id.btn_send);
        listview = (ListView) findViewById(R.id.listview);
        tv_connect = (TextView) findViewById(R.id.tv_connect);
        tv_send_data = (TextView) findViewById(R.id.tv_send_data);
        tv_data_state = (TextView) findViewById(R.id.tv_data_state);
        registerBluetoothReceiver();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        boolean bll = bindService(gattServiceIntent, mServiceConnection,
                BIND_AUTO_CREATE);

        setDebugMode();
    }

    @Override
    protected void initListener() {
        btn_send.setOnClickListener(this);
        btn_read.setOnClickListener(this);
        tv_connect.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        tv_device_info.setText("设备名称:  " + mDeviceName + "\n" + "设备地址: " + mDeviceAddress);
        initRecordData();
    }

    //展示过往数据列表
    private void initRecordData() {
        mData = SharedPrefUtils.getObject(Config.SP_NAME_INFO, Config.KEY_INFO);
        if (mData == null) {
            mData = new ArrayList<>();
        }
        mAdapter = new AdapterReceiveDataList(this, mData);
        listview.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_read:
                mBluetoothLeService.read();
                break;
            case R.id.btn_send:
                if (mBluetoothLeService == null) {
                    ToastUtils.showToastShort(v.getContext(), "mBluetoothLeService isi null");
                    return;

                }
                Logs.v("send data ..........");
                String data = String.valueOf(et_send.getText());
                mBluetoothLeService.writeValue(data);
                break;
            case R.id.tv_connect:
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.connect(mDeviceAddress);
                }
                break;
        }
    }

    BluetoothReceiver receiver;

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        filter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        filter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        filter.addAction(BluetoothLeService.EXTRA_DATA);
        receiver = new BluetoothReceiver();
        registerReceiver(receiver, filter);
    }

    class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {//匹配连接成功
                mConnected = true;
                tv_connect_state.setText("连接成功");
                tv_connect.setVisibility(View.GONE);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {//断开连接
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnected = false;
                        tv_connect.setVisibility(View.VISIBLE);
                        tv_connect_state.setText("连接失败");
                    }
                });

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {//发现服务
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {//接受到数据
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)) {
                tv_send_data.append(intent.getStringExtra("data") + "   ");
            }
        }
    }


    /**
     * @param gattServices
     */
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;

    }

    StringBuffer sb = new StringBuffer();

    private void displayData(String data) {
        sb.append(data);
        tv_data.append(data + "  ");
        Logs.d("displayData: " + sb.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null)
            unbindService(mServiceConnection);
        unregisterReceiver(receiver);
    }


    private void setDebugMode() {
        if (Config.DEBUG) {
            tv_right.setText("调式：关闭");
            ll_debug_info.setVisibility(View.VISIBLE);
        } else {
            tv_right.setText("调式：开启");
            ll_debug_info.setVisibility(View.GONE);
        }

    }

    @Override
    public View.OnClickListener getRightListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Config.DEBUG) {
                    Config.DEBUG = false;
                } else {
                    Config.DEBUG = true;
                }
                setDebugMode();
                initRecordData();
            }
        };
    }
}
