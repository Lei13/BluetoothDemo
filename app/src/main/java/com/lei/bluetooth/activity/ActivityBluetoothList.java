package com.lei.bluetooth.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lei.bluetooth.R;
import com.lei.bluetooth.Utils.CommonUtils;
import com.lei.bluetooth.Utils.Logs;
import com.lei.bluetooth.Utils.ToastUtils;
import com.lei.bluetooth.activity.base.BaseActivity;
import com.lei.bluetooth.adapter.AdapterDeviceList;
import com.lei.bluetooth.bean.Model;
import com.lei.bluetooth.bean.ModelDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lei on 2016/11/9.
 */

public class ActivityBluetoothList extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView lv_list;
    private Button btn_bluetooth_switch;
    private LinearLayout ll_scanning;

    private TextView tv_search;
    private BluetoothAdapter bluetoothAdapter;
    private AdapterDeviceList adapter;
    private boolean mScanning;
    private List<Model> deviceList = new ArrayList<>();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private Handler mHandler = new Handler();
    private boolean isFirst = true;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_bluetooth_list;
    }

    @Override
    protected void initIntent() {


    }

    @Override
    protected void initView() {
        tv_center.setText("设备列表");
        lv_list = (ListView) findViewById(R.id.lv_bluetooth_list);
        tv_search = (TextView) findViewById(R.id.tv_search);
        btn_bluetooth_switch = (Button) findViewById(R.id.btn_bluetooth_switch);
        ll_scanning = (LinearLayout) findViewById(R.id.ll_scanning);
        setScan(false);
        initBluetooth();
    }

    private void initBluetooth() {
        if (bluetoothAdapter == null) {
            BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = manager.getAdapter();
        }
        Logs.v("manager.getAdapter() ==   " + String.valueOf(bluetoothAdapter));
        if (bluetoothAdapter == null) {
            ToastUtils.showToastShort(this, "不支持蓝牙");
            return;
        }
        //确认开启蓝牙
        if (!bluetoothAdapter.isEnabled()) {
//            //请求用户开启
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(intent, RESULT_FIRST_USER);
//            //使蓝牙设备可见，方便配对
//            Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//            in.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
//            startActivity(in);
//            //直接开启，不经过提示
            bluetoothAdapter.enable();
        }
    }


    @Override
    protected void initListener() {
        tv_search.setOnClickListener(this);
        lv_list.setOnItemClickListener(this);
        btn_bluetooth_switch.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        adapter = new AdapterDeviceList(this, deviceList);
        lv_list.setAdapter(adapter);
    }

    private void doScanDevce() {
        if (bluetoothAdapter == null) {
            return;
        }
        if (bluetoothAdapter.isEnabled()) {
            setScan(true);
            deviceList.clear();
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            initBluetooth();
        }
    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String extra = new String(scanRecord);
            String struuid = CommonUtils.bytes2HexString(CommonUtils.reverseBytes(scanRecord)).replace("-", "").toLowerCase();
            String sring = device.getName();
            Logs.v("onLeScan<<<<  scanRecord[]  " + struuid + "  name  " + sring + "  address   " + device.getAddress());
            ModelDevice device1 = new ModelDevice();
            device1.setName(device.getName());
            device1.setAddress(device.getAddress());
            device1.setExtraData(extra);
            if (!deviceList.contains(device1))
                deviceList.add(device1);
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_search:
                if (mScanning) {//在扫描
                    setScan(false);
                    if (bluetoothAdapter != null) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                setScan(true);
                                bluetoothAdapter.startLeScan(leScanCallback);
                            }
                        }, 1000);

                    }
                } else//没有打开扫描
                    doScanDevce();
                break;
            case R.id.btn_bluetooth_switch:
//                if (isOpen) {//打开状态
//                    isOpen = false;
//                    if (mScanning)
//                        bluetoothAdapter.stopLeScan(leScanCallback);
//                    bluetoothAdapter.disable();
//                } else {//关闭状态
//                    initBluetooth();
//                }
//                setSwitchData();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ModelDevice device = (ModelDevice) parent.getAdapter().getItem(position);
        if (device != null /*&& !TextUtils.isEmpty(device.getName())*/) {
            Intent intent = new Intent(this, ActivityConnectDevice.class);
            intent.putExtra("name", device.getName());
            intent.putExtra("address", device.getAddress());
            if (mScanning && bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.stopLeScan(leScanCallback);
                setScan(false);
            }
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            isFirst = false;
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled())
                bluetoothAdapter.enable();
        } else if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == 1 && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Logs.d("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton("ok", null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    /**
     * 设置扫描状态
     *
     * @param isScaning
     */
    private void setScan(boolean isScaning) {
        mScanning = isScaning;
        if (mScanning) {
            ll_scanning.setVisibility(View.VISIBLE);
        } else {
            ll_scanning.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScanning && bluetoothAdapter.isEnabled()) {
            if (mScanning) {
                bluetoothAdapter.stopLeScan(leScanCallback);
                setScan(false);
            }
        }


    }
}