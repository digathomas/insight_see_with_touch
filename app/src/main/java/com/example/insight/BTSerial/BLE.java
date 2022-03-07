package com.example.insight.BTSerial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

public class BLE {
    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                System.out.println("Connected to BLE.");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                System.out.println("Disconnected from BLE.");
            }
        }
    };
    private static final String LEFT_ADDRESS = "E6:E9:54:BB:C7:4C";
    private static final String RIGHT_ADDRESS = "";
    private BluetoothDevice leftDevice;
    private BluetoothDevice rightDevice;
    private BluetoothGatt leftGATT;
    private BluetoothGatt rightGATT;

    public BLE(Context context){
        this.context = context;
        connect();
    }

    public void connect(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null){
            System.out.println("Could not get BT adapter.");
        }
        else{
            try {
                leftDevice = bluetoothAdapter.getRemoteDevice(LEFT_ADDRESS);
                leftGATT = leftDevice.connectGatt(context, true, bluetoothGattCallback);
            }catch (IllegalArgumentException e){
                System.out.println("Left device not found/could not connect.");
            }
            try {
                rightDevice = bluetoothAdapter.getRemoteDevice(RIGHT_ADDRESS);
                rightGATT = rightDevice.connectGatt(context, true, bluetoothGattCallback);
            }catch (IllegalArgumentException e){
                System.out.println("Right device not found/could not connect.");
            }

        }
    }
    public void close() {
        if (rightGATT != null){
            rightGATT.close();
            rightGATT = null;
        }
        if (leftGATT != null){
            leftGATT.close();
            leftGATT = null;
        }
    }
}
