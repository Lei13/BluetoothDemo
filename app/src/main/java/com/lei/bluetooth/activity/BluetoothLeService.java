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
import android.text.TextUtils;
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
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.lei.bledemo.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING = "com.lei.bledemo.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED = "com.lei.bledemo.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.lei.bledemo.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.lei.bledemo.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_WRITE = "com.lei.bledemo.ACTION_WRITE_DATA";
    public final static String ACTION_DATA_APPEND = "com.lei.bledemo.ACTION_APPEND_DATA";
    public final static String ACTION_DATA_START = "com.lei.bledemo.ACTION_START_RECEIVE_DATA";
    public final static String ACTION_DATA_END = "com.lei.bledemo.ACTION_END_RECEIVE_DATA";
    public final static String EXTRA_DATA = "com.lei.bledemo.EXTRA_DATA";

    //0000ff02-0000-1000-8000-00805f9b34fb
    public final static UUID UUID_NOTIFY =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_SERVICE =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");


    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;


    BluetoothGattCharacteristic mNotifyCharacteristic;
    private List<Long> receivedData = new ArrayList<>();
    private List<String> oldData = new ArrayList<>();
    private String mReceiveHexStr = "";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ToastUtils.showToastShort(BluetoothLeService.this, String.valueOf(msg.obj));
        }
    };

    // Implements callback methods for GATT events that the app cares about. For
    // example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mReceiveHexStr = "";
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
                broadcastUpdate(intentAction);
                mReceiveHexStr = "";
                Log.i(TAG, "Disconnected from GATT server.");

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
            Log.d(TAG, "onCharacteristicWrite.... : " + status + CommonUtils.byte2HexStr(characteristic.getValue()));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Intent intent = new Intent(ACTION_DATA_WRITE);
                intent.putExtra("data", new String(characteristic.getValue()));
                sendBroadcast(intent);
            }
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
                        writeValue("O");//通知下位机
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
            //intent.putExtra(EXTRA_DATA, str);
            //sendBroadcast(intent);
            saveData(intent, str);
        }


    }

    private void saveData(Intent intent1, String data) {
        intent1.putExtra(EXTRA_DATA, data+"  ");
        sendBroadcast(intent1);
        Log.v(TAG, "data.length() " + data.length() + "   " + data);
        if (data.length() == 2 && isReceiveDataY(data)) return;
        if (data.length() >= 2 && data.length() <= 40 && mReceiveHexStr.length() <= 80) {
            mReceiveHexStr += data;
        }
        if (mReceiveHexStr.length() > 80 && mReceiveHexStr.length() < 96) {
            writeValue("N");
            Message msg1 = Message.obtain();
            msg1.obj = "接收数据格式不正确...";
            handler.sendMessage(msg1);
            mReceiveHexStr = "";
            return;

        } else if (mReceiveHexStr.length() >= 96) {
            if (parseData(mReceiveHexStr)) {
                writeValue("Y");
                Message msg1 = Message.obtain();
                msg1.obj = "校验成功,即将上传服务器中...";
                handler.sendMessage(msg1);
                uploadDataToService(receivedData, oldData);
            } else {
                writeValue("N");
                Message msg2 = Message.obtain();
                msg2.obj = "校验失败";
                handler.sendMessage(msg2);

            }
            mReceiveHexStr = "";
        }
    }


    private boolean parseData(String hexStr) {
        try {
            oldData.clear();
            receivedData.clear();
            long totalSum = 0;
            String code = hexStr.substring(0, 8);//串码
            long codeValue = Long.valueOf(code, 16);
            totalSum += codeValue;
            oldData.add(code);
            receivedData.add(codeValue);
            int pos = 8;
            for (int i = 0; i < 20; i++) {//20个数据
                String dataI = hexStr.substring(pos, pos + 4);
                long dataValue = Long.valueOf(dataI, 16);
                totalSum += dataValue;
                oldData.add(dataI);
                receivedData.add(dataValue);
                pos = pos + 4;
            }
            for (int i = 0; i < 3; i++) {//状态字
                String status = hexStr.substring(pos, pos + 2);
                long dataValue = Long.valueOf(status, 16);
                totalSum += dataValue;
                oldData.add(status);
                receivedData.add(dataValue);
                pos = pos + 2;
            }
            String sum = hexStr.substring(pos);//校验位
            oldData.add(sum);
            receivedData.add(Long.valueOf(sum, 16));

            //开始校验
            String sumHex = Long.toHexString(totalSum);//校验和转换成16进制
            String lastTwo = sumHex.substring(sumHex.length() - sum.length());
            String debugStr = "";
            if (sum.equalsIgnoreCase(lastTwo)) {//校验成功
                debugStr = "\n---接收数据成功----校验和： " + sumHex + " 校验值 " + sum + "\n";
                Intent intent = new Intent(ACTION_DATA_AVAILABLE);
                intent.putExtra(EXTRA_DATA, debugStr);
                sendBroadcast(intent);
                return true;
            }  //失败
            debugStr = "\n---接收数据失败----校验和： " + sumHex + " 校验值 " + sum + "\n";
            Intent intent = new Intent(ACTION_DATA_AVAILABLE);
            intent.putExtra(EXTRA_DATA, debugStr);
            sendBroadcast(intent);
        } catch (Exception e) {
            Message msg1 = Message.obtain();
            msg1.obj = "数据解析错误";
            handler.sendMessage(msg1);
            writeValue("N");
        }
        return false;
    }

    private void notifyReceiveDataStart() {
        Intent intent = new Intent(ACTION_DATA_START);
        sendBroadcast(intent);
    }

    private void notifyReceiveDataEnd() {
        Intent intent = new Intent(ACTION_DATA_END);
        sendBroadcast(intent);
    }

    //发送“Y”给主机，0.5秒后将断开蓝牙连接，进入休眠状态。
    private boolean isReceiveDataY(String data) {
        String str = CommonUtils.print10(data);
        if ("Y".equals(str)) {
            Intent intent = new Intent(ACTION_DATA_AVAILABLE);
            intent.putExtra(BluetoothLeService.EXTRA_DATA, "\n---断开连接----收到回复数据：十六进制： " + data + " 十进制： " + str + "\n");
            sendBroadcast(intent);
            disconnect();
            return true;
        }
        return false;
    }

    private void uploadDataToService(List<Long> receivedDat, List<String> oldDat) {
        Log.d(TAG, "uploadDataToService: " + String.valueOf(receivedDat) + String.valueOf(oldDat));
        ModelData data = new ModelData();
        data.setDate(String.valueOf(System.currentTimeMillis() / 1000));
        String str = "";
        for (int i = 1; i <= 20; i++) {
            str += receivedDat.get(i) + ",";
        }
        data.setOldDataIntStr(str.substring(0, str.length() - 1));
        data.setOldDataHex(oldDat);
        data.setHexStr(String.valueOf(oldDat));
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
                broadcastUpdate(ACTION_GATT_CONNECTING);
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
        Log.d(TAG, "doUploadData: " + String.valueOf(modelData));

        NetUtils.uploadDada(modelData.getOldDataIntStr(), new NetUtils.OnHttpCompleteListener() {
            @Override
            public void onSuccess(Model model) {
                ModelData saveData = (ModelData) model;
                saveData.setOldDataIntStr(modelData.getOldDataIntStr());
                saveData.setDate(modelData.getDate());
                saveData.setHexStr(modelData.getHexStr());
                saveData.setOldDataHex(modelData.getOldDataHex());
                saveData.setAddress(mBluetoothDeviceAddress);
                SharedPrefUtils.saveDataItem(mBluetoothDeviceAddress, saveData);
                ToastUtils.showToastShort(BluetoothLeService.this, "上传服务器成功");
                notifyAppendData();
            }

            @Override
            public void onFailure(Object object) {
                modelData.setAddress(mBluetoothDeviceAddress);
                SharedPrefUtils.saveDataItem(mBluetoothDeviceAddress, modelData);
                notifyAppendData();
                ToastUtils.showToastShort(BluetoothLeService.this, object == null ? "上传服务器失败" : String.valueOf(object));

            }
        });
    }

    private void notifyAppendData() {
        Intent intent = new Intent(ACTION_DATA_APPEND);
        sendBroadcast(intent);
    }
}
