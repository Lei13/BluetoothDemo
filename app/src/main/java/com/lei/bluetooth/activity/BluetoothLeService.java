package com.lei.bluetooth.activity;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.lei.bluetooth.Utils.CommonUtils;
import com.lei.bluetooth.Utils.SharedPrefUtils;
import com.lei.bluetooth.Utils.ToastUtils;
import com.lei.bluetooth.bean.Model;
import com.lei.bluetooth.bean.ModelData;
import com.lei.bluetooth.network.NetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
    public static final int MSG_UPLOAD = 0X2;
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_UPLOAD:
                    break;
                default:
                    writeValue("O");//通知下机位准备好接受数据
                    break;
            }

        }
    };

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.lei.bledemo.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.lei.bledemo.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.lei.bledemo.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.lei.bledemo.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.lei.bledemo.EXTRA_DATA";
    //0000ff02-0000-1000-8000-00805f9b34fb
    public final static UUID UUID_NOTIFY =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_SERVICE =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    BluetoothGattCharacteristic mNotifyCharacteristic;
    private List<Integer> receivedData = new ArrayList<>();
    private List<String> oldData = new ArrayList<>();


    // Implements callback methods for GATT events that the app cares about. For
    // example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                mBluetoothGatt.discoverServices();
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                mBluetoothGatt.discoverServices();
                Log.i(TAG, "Attempting to start service discovery:");
                broadcastUpdate(intentAction);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                oldData.clear();
                receivedData.clear();
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.w(TAG, "onServicesDiscovered:  " + status + " " + gatt.getServices().size());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                findService(mBluetoothGatt.getServices());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead:" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite.... : " + status);
            System.out.println("--------write success----- status:" + status);
        }

        ;

        /*
         * when connected successfully will callback this method
         * this method can dealwith send password or data analyze
         *
         * */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged.... : " + characteristic.getProperties());
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getCharacteristic().getUuid();
            Log.d(TAG, "onDescriptorWrite.... :rssi  " + descriptor.toString() + "  status   " + status + "  uuid   " + uuid);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "onReadRemoteRssi.... :rssi  " + rssi + "  status   " + status);
            System.out.println("rssi = " + rssi);
        }
    };

    public void writeValue(String strValue) {
        if (mNotifyCharacteristic == null) {
            Log.v(TAG, "mNotifyCharacteristic is null");
            return;
        }
        mNotifyCharacteristic.setValue(strValue.getBytes());
        mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
    }

    public void findService(List<BluetoothGattService> gattServices) {
        Log.i(TAG, "Count is:" + gattServices.size());
        for (BluetoothGattService gattService : gattServices) {//遍历处所有的service
            Log.i(TAG, "BluetoothGattService   " + gattService.getUuid().toString());
            if (String.valueOf(gattService.getUuid()).equalsIgnoreCase(String.valueOf(UUID_SERVICE))) {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                Log.i(TAG, "Count is:" + gattCharacteristics.size());
                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {///遍历 Characteristic
                    Log.i(TAG, "BluetoothGattCharacteristic:   " + gattCharacteristic.getUuid().toString());
                    if (String.valueOf(gattCharacteristic.getUuid()).equalsIgnoreCase(String.valueOf(UUID_NOTIFY))) {

                        mNotifyCharacteristic = gattCharacteristic;
                        setCharacteristicNotification(gattCharacteristic, true);
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mHandler.sendEmptyMessage(0);

                            }
                        }, 1000);
                        return;
                    }
                }
            }
        }

    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }


        //00002902-0000-1000-8000-00805f9b34fb
        //00002901-0000-1000-8000-00805f9b34fb

        // This is specific to Heart Rate Measurement.
        // if ("00002901-0000-1000-8000-00805f9b34fb".equals(characteristic.getUuid())) {
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
//        if (descriptor != null) {
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
        //  }

        //  mBluetoothGatt.writeCharacteristic(characteristic);
        boolean flag = mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        Log.v(TAG, "notify flag   " + flag);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        //if ("".equals(characteristic.getUuid())) {
        int flag = characteristic.getProperties();
        int format = -1;
        if ((flag & 0x01) != 0) {
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
            Log.d(TAG, "Heart rate format UINT16.");
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
            Log.d(TAG, "Heart rate format UINT8.");
        }
//        final int heartRate = characteristic.getIntValue(format, 1);
//        Log.d(TAG, String.format("Received heart rate: %d", heartRate));
//        intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        // } else {
        // For all other profiles, writes the data formatted in HEX.对于所有的文件，写入十六进制格式的文件
        //这里读取到数据
        final byte[] data = characteristic.getValue();
        for (int i = 0; i < data.length; i++) {
            System.out.println("data......" + data[i]);
        }


        if (data != null && data.length > 0) {
            //以十六进制的形式输出
            String str = CommonUtils.bytes2HexString(data);
            intent.putExtra(EXTRA_DATA, str);
            saveData(str);
            sendBroadcast(intent);
        }


    }

    private void saveData(String data) {
        try {
            oldData.add(data);
            int value = Integer.valueOf(data, 16);
            Log.v(TAG, "value   " + value);
            receivedData.add(value);
            if (receivedData.size() >= 25) {
                Log.v(TAG, "saveData >=48  size = " + receivedData.size());
                int sum = 0;
                for (int i = 0; i < 47; i++) {
                    sum += receivedData.get(i);
                }
                if (sum == receivedData.get(47)) {
                    writeValue("Y");
                    uploadDataToService();
                } else {
                    oldData.clear();
                    receivedData.clear();
                    writeValue("N");
                }
            } else {

            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Log.v(TAG, "saveData  NumberFormatException...");
        }
    }

    private void uploadDataToService() {
        ModelData data = new ModelData();
        String str = "";
        for (int i = 1; i <= 20; i++) {
            str += receivedData.get(i) + ",";
        }
        data.setOldDataIntStr(str.substring(0, str.length() - 1));
        data.setOldDataHex(oldData);
        doUploadData(data);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that
        // BluetoothGatt.close() is called
        // such that resources are cleaned up properly. In this particular
        // example, close() is
        // invoked when the UI is disconnected from the Service.
        disconnect();
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */

    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG,
                    "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device. Try to reconnect. (锟斤拷前锟斤拷锟接碉拷锟借备锟斤拷 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷)
        if (mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            Log.d(TAG, "没有设备");
            return false;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void wirteCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);

    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "readCharacteristic: " + characteristic.getProperties());
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothAdapter为空");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void read() {
        if (mNotifyCharacteristic != null)
            readCharacteristic(mNotifyCharacteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled, String uuid) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "BluetoothAdapter为空");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID
                .fromString(/*CLIENT_CHARACTERISTIC_CONFIG*/uuid));
        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);

        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }

    /**
     * Read the RSSI for a connected remote device.
     */
    public boolean getRssiVal() {
        if (mBluetoothGatt == null)
            return false;

        return mBluetoothGatt.readRemoteRssi();
    }

    /**
     * 保存服务器
     *
     * @param modelData
     */
    private void doUploadData(final ModelData modelData) {
        modelData.setDate(String.valueOf(System.currentTimeMillis() / 1000));
        NetUtils.uploadDada(modelData.getOldDataIntStr(), new NetUtils.OnHttpCompleteListener() {
            @Override
            public void onSuccess(Model model) {
                ModelData saveData = (ModelData) model;
                saveData.setOldDataIntStr(modelData.getOldDataIntStr());
                saveData.setDate(modelData.getDate());
                SharedPrefUtils.saveDataItem(saveData);
                ToastUtils.showToastShort(BluetoothLeService.this, "保存数据成功");
            }

            @Override
            public void onFailure(Object object) {
                ToastUtils.showToastShort(BluetoothLeService.this, "保存数据失败");
                SharedPrefUtils.saveDataItem(modelData);
            }
        });
    }
}
