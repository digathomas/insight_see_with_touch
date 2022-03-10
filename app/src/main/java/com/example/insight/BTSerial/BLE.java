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
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BLE {
    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    public static final int LEFT_GATT = 0;
    public static final int RIGHT_GATT = 1;
    private static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID CHARCTERISTIC_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private static final String LEFT_ADDRESS = "E6:E9:54:BB:C7:4C";
    private static final String RIGHT_ADDRESS = "";
    private BluetoothGattService leftService;
    private BluetoothGattService rightService;
    private BluetoothDevice leftDevice;
    private BluetoothDevice rightDevice;
    private BluetoothGatt leftGATT;
    private BluetoothGatt rightGATT;
    private BluetoothGattCharacteristic leftWriteCharacteristic;
    private BluetoothGattCharacteristic rightWriteCharacteristic;

    public BLE(Context context){
        this.context = context;
        connect();
    }

    //Connect Both left and right bluetooth devices.
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
                Log.w("BLE", "leftConnect: ", e);
                Toast.makeText(context,"left bluetooth not connected",Toast.LENGTH_SHORT);
            }
            try {
                rightDevice = bluetoothAdapter.getRemoteDevice(RIGHT_ADDRESS);
                rightGATT = rightDevice.connectGatt(context, true, bluetoothGattCallback);
            }catch (Exception e){
                Log.w("BLE", "rightConnect: ", e);
                Toast.makeText(context,"right bluetooth not connected",Toast.LENGTH_SHORT);

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

    //BluetoothGatt Callback used for discovering services and getting the characteristics
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // successfully connected to the GATT Server
                System.out.println("Connected to BLE.");
                if (gatt == leftGATT)
                    leftGATT.discoverServices();
                else
                    rightGATT.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // disconnected from the GATT Server
                System.out.println("Disconnected from BLE.");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                if (gatt == leftGATT && leftService == null){
                    leftService = leftGATT.getService(SERVICE_UUID);
                    if (leftService != null)
                        leftWriteCharacteristic = leftService.getCharacteristic(CHARCTERISTIC_UUID);
                }
                else if (gatt == rightGATT && rightService == null){
                    rightService = rightGATT.getService(SERVICE_UUID);
                    if (rightService != null)
                        rightWriteCharacteristic = rightService.getCharacteristic(CHARCTERISTIC_UUID);
                }
            }
        }
    };

    //write 20 int values with conversion to gatts
    public void writeToGatt(int[]value){
        if (value.length == 20) {
            int leftHandValues[] = Arrays.copyOfRange(value,0,9);
            int rightHandValues[] = Arrays.copyOfRange(value,10,19);

            writeToGatt(LEFT_GATT,leftHandValues);
            writeToGatt(RIGHT_GATT,rightHandValues);
        }
        else{
            Log.w("WriteToGatt","Unable to write to Gatt with int array not of size 20");
        }
    }

    //write 10 int values with conversion to L or R gatt
    public void writeToGatt(int gattLR, int[] value){
        if (value.length == 10) {
            writeToGatt(gattLR,intToByte(value));
        }
        else{
            Log.w("WriteToGatt","Unable to write to Gatt with int array not of size 10");
        }
    }

    //write the byte values to gatt by the characteristics
    public void writeToGatt(int gattLR, byte[] value){
        if (gattLR == LEFT_GATT){
            if (leftGATT != null && leftWriteCharacteristic != null){
                leftWriteCharacteristic.setValue(value);
                if(!leftGATT.writeCharacteristic(leftWriteCharacteristic)){
                    Log.w("BLE_Write", "Unable to write to left BLE");
                }
            }
            else{
                Log.w("BLE_Write", "Left Gatt Service/Characteristics not set");
                return;
            }
        }
        else{
            if(rightGATT != null && rightWriteCharacteristic != null){
                rightWriteCharacteristic.setValue(value);
                if(!rightGATT.writeCharacteristic(rightWriteCharacteristic)){
                    Log.w("BLE_Write", "Unable to write to left BLE");
                }
            }
            else{
                Log.w("BLE_Write", "Right Gatt Service/Characteristics not set");
                return;
            }
        }
    }

    //Conversion from int array to byte array with
    //correct position of bytes. Follow notes at bottom of BLE.java
    public byte[] intToByte(int[] arr){
        byte[] byteArr =  new byte[10];
        if (arr.length == 10) {
            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0)
                    byteArr[i / 2] = inBoundValue(arr[i]);
                else {
                    byteArr[i / 2 + 5] = inBoundValue(arr[i]);
                }
            }
        }
        return byteArr;
    }

    //Sending correct byte values between 0 and 16
    private byte inBoundValue(int value){
        if (value > 17)
            return 16;
        if (value < 0)
            return 0;
        return (byte)value;
    }



    /*
    Send 2 bytes in the byte array
    {0,   0}
    uLH   lLh

    {0,   0}
    uRh   lRh

    int arr  -> byte arr
    Left hand-> Left hand
    0 1 2 3 4   0 2 4 6 8
    o o o o o   o o o o o

    5 6 7 8 9   1 3 5 7 9
    o o o o o   o o o o o

    Right hand
    0 2 4 6 8
    o o o o o

    1 3 5 6 9
    o o o o o

    conversion from int to byte array with right positions
    int array  -> {0,1,2,3,4,5,6,7,8,9}
    byte array -> {0,2,4,6,8,1,3,5,7,9}
     */
}
