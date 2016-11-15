package com.lei.bluetooth.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.lei.bluetooth.R;
import com.lei.bluetooth.Utils.Logs;
import com.lei.bluetooth.Utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;

public class ActivityConnectDevice extends BaseActivity {
    TextView tv_device_info, tv_data, tv_connect_state;
    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private EditText et_send;
    private Button btn_send, btn_read;

    private boolean mConnected = false;
    private Set<BluetoothGattCharacteristic> charactics;


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
        tv_device_info = (TextView) findViewById(R.id.tv_device_info);
        tv_data = (TextView) findViewById(R.id.tv_data);
        tv_connect_state = (TextView) findViewById(R.id.tv_connect_state);
        btn_read = (Button) findViewById(R.id.btn_read);
        et_send = (EditText) findViewById(R.id.et_send);
        btn_send = (Button) findViewById(R.id.btn_send);
        charactics = new HashSet<>();
        registerBluetoothReceiver();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        boolean bll = bindService(gattServiceIntent, mServiceConnection,
                BIND_AUTO_CREATE);
    }

    @Override
    protected void initListener() {
        btn_send.setOnClickListener(this);
        btn_read.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        tv_device_info.setText("  name  " + mDeviceName + "\n" + " address " + mDeviceAddress);

    }

    BluetoothGattCharacteristic characteristic = null;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_read:
                if (characteristic == null) return;
                Logs.v("read...........");
                Iterator iterator = charactics.iterator();
                while (iterator.hasNext()) {
                    mBluetoothLeService.readCharacteristic((BluetoothGattCharacteristic) iterator.next());
                }
                break;
            case R.id.btn_send:
                if (mBluetoothLeService == null) {
                    ToastUtils.showToastShort(v.getContext(), "mBluetoothLeService isi null");
                    return;

                }
                Logs.v("send data ..........");
                String data = String.valueOf(et_send.getText());
                characteristic.setValue(TextUtils.isEmpty(data) ? "test" : data);
                mBluetoothLeService.WriteValue(data);
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
        filter.addAction(BluetoothLeService.EXTRA_DATA);
        receiver = new BluetoothReceiver();
        registerReceiver(receiver, filter);
    }

    class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                tv_connect_state.setText("connect");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnected = false;
                        tv_connect_state.setText("unconnect");
                    }
                });

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
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
        tv_data.append(data + "\n");
        Logs.d("displayData: " + sb.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceConnection != null)
            unbindService(mServiceConnection);
        unregisterReceiver(receiver);
    }
}
