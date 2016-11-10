package com.lei.bluetooth.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.widget.ListView;

import com.lei.bluetooth.R;
import com.lei.bluetooth.Utils.Logs;
import com.lei.bluetooth.Utils.ToastUtils;

/**
 * Created by lei on 2016/11/9.
 */

public class ActivityBluetoothList extends BaseActivity {
    private ListView lv_list;
    private BluetoothAdapter bluetoothAdapter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_bluetooth_list;
    }

    @Override
    protected void initIntent() {

    }

    @Override
    protected void initView() {
        lv_list = (ListView) findViewById(R.id.lv_bluetooth_list);
        initBluetooth();
    }

    private void initBluetooth() {
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = manager.getAdapter();
        Logs.v("manager.getAdapter() ==   " + String.valueOf(bluetoothAdapter));
        if (bluetoothAdapter == null) {
            ToastUtils.showToastShort(this, "不支持蓝牙");
            return;
        }


    }


    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }

    private void searchDevice() {
        if (bluetoothAdapter != null)
            bluetoothAdapter.startLeScan(leScanCallback);
    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

        }
    };
}
