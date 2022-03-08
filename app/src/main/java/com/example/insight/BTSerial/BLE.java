package com.example.insight.BTSerial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

public class BLE {
    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                System.out.println("Connected to BLE.");
                leftGATT.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                System.out.println("Disconnected from BLE.");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                leftServices = leftGATT.getServices();
                //System.out.println(leftServices.toString());
                BluetoothGattService leftService = leftGATT.getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"));
                BluetoothGattCharacteristic leftWriteCharacteristic = leftService.getCharacteristic(UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"));
                leftWriteCharacteristic.setValue(1, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                if(leftGATT.writeCharacteristic(leftWriteCharacteristic)){
                    System.out.println("EMOTIONAL DAMAGE");
                }

//                    BluetoothSocket leftSocket = leftDevice.createInsecureRfcommSocketToServiceRecord(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"));
//                    System.out.println("left locket: ");
//                    System.out.println(leftSocket);
//                    leftSocket.
//                    OutputStream leftOutStream = leftSocket.getOutputStream();
//                    leftOutStream.write(1);
                for (BluetoothGattService service : leftServices){
                    //System.out.println(service.getCharacteristics());
                }
                //System.out.println(leftDevice.getName());
            }
        }
    };
    private static final String LEFT_ADDRESS = "E6:E9:54:BB:C7:4C";
    private static final String RIGHT_ADDRESS = "";
    private BluetoothDevice leftDevice;
    private BluetoothDevice rightDevice;
    private BluetoothGatt leftGATT;
    private BluetoothGatt rightGATT;
    private List<BluetoothGattService> leftServices;
    private List<BluetoothGattService> rightServices;


    public BLE(Context context){
        this.context = context;
        connect();
    }

    public void connect(){
        System.out.println("Connecting...");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null){
            System.out.println("Could not get BT adapter.");
        }
        else{
            try {
                leftDevice = bluetoothAdapter.getRemoteDevice(LEFT_ADDRESS);
                leftGATT = leftDevice.connectGatt(context, true, bluetoothGattCallback);

            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Left device not found/could not connect.");
            }
            try {
//                rightDevice = bluetoothAdapter.getRemoteDevice(RIGHT_ADDRESS);
//                rightGATT = rightDevice.connectGatt(context, true, bluetoothGattCallback);
//                rightGATT.discoverServices();
//                rightServices = rightGATT.getServices();
            }catch (Exception e){
                e.printStackTrace();
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
