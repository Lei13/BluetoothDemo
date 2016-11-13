package com.lei.bluetooth.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lei.bluetooth.R;
import com.lei.bluetooth.Utils.CommonUtils;
import com.lei.bluetooth.Utils.Logs;
import com.lei.bluetooth.Utils.ToastUtils;
import com.lei.bluetooth.adapter.AdapterDeviceList;
import com.lei.bluetooth.bean.Model;
import com.lei.bluetooth.bean.ModelDevice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by lei on 2016/11/9.
 */

public class ActivityBluetoothList extends BaseActivity implements AdapterView.OnItemClickListener {
    private ListView lv_list;
    private BluetoothAdapter bluetoothAdapter;
    private TextView tv_search;
    private List<Model> deviceList = new ArrayList<>();
    private AdapterDeviceList adapter;
    private boolean mScanning;


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
        tv_search = (TextView) findViewById(R.id.tv_search);
        mScanning = false;
        //initOath();
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
        //确认开启蓝牙
        if (!bluetoothAdapter.isEnabled()) {
            //请求用户开启
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RESULT_FIRST_USER);
            //使蓝牙设备可见，方便配对
            Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            in.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
            startActivity(in);
            //直接开启，不经过提示
            bluetoothAdapter.enable();
        }


    }


    @Override
    protected void initListener() {
        tv_search.setOnClickListener(this);
        lv_list.setOnItemClickListener(this);
    }

    @Override
    protected void initData() {
        adapter = new AdapterDeviceList(this, deviceList);
        lv_list.setAdapter(adapter);
    }

    private void searchDevice() {


        bluetoothAdapter.startLeScan(leScanCallback);
//        //获取可配对蓝牙设备
//        Set<BluetoothDevice> device = bluetoothAdapter.getBondedDevices();
//
//        if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
//            deviceList.clear();
//            adapter.notifyDataSetChanged();
//        }
//        if (device.size() > 0) { //存在已经配对过的蓝牙设备
//            deviceList.clear();
//            ModelDevice modelDevice;
//            for (Iterator<BluetoothDevice> it = device.iterator(); it.hasNext(); ) {
//                BluetoothDevice btd = it.next();
//                modelDevice = new ModelDevice();
//                modelDevice.setAddress(btd.getAddress());
//                modelDevice.setName(btd.getName());
//                deviceList.add(modelDevice);
//                adapter.notifyDataSetChanged();
//            }
//        } else {  //不存在已经配对过的蓝牙设备
//            // deviceList.add("No can be matched to use bluetooth");
//            ToastUtils.showToastShort(this, "not find matched bluetooth device");
//            adapter.notifyDataSetChanged();
//        }
    }

    BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String struuid = CommonUtils.bytes2HexString(CommonUtils.reverseBytes(scanRecord)).replace("-", "").toLowerCase();
            ;
            Logs.d("run: " + struuid);
            String sring = device.getName();
            Logs.v(" device.getName()  " + sring);
            //if("CardioChek Meter:".contains(sring)) {
            ModelDevice device1 = new ModelDevice();
            device1.setName(device.getName());
            device1.setAddress(device.getAddress());
            if (!deviceList.contains(device1))
                deviceList.add(device1);
            adapter.notifyDataSetChanged();
            //}
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_search:
                mScanning = true;
                searchDevice();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ModelDevice device = (ModelDevice) parent.getAdapter().getItem(position);
        if (device != null && !TextUtils.isEmpty(device.getName())) {
            Intent intent = new Intent(this, ActivityConnectDevice.class);
            intent.putExtra("name", device.getName());
            intent.putExtra("address", device.getAddress());
            if (mScanning) {
                bluetoothAdapter.stopLeScan(leScanCallback);
                mScanning = false;
            }
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bluetoothAdapter.isEnabled()) {
            if (!bluetoothAdapter.isEnabled()) {
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

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    /*private void initOath() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this); 
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton("ok", null); 
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION); 

                    }
                }); 
                builder.show(); 
            }
             }
    }
*/
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
}