package com.example.insight;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.insight.BTSerial.BLE;

import detection.DetectorActivity;
import detection.customview.OverlayView;
import lidar.LidarModule.LidarHelper;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity {

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    private Intent intent;
    protected LidarHelper lidarHelper;
    private DetectorActivity detectorActivity;

    private Thread detectorThread;
    private static FrameLayout container;
    private static ImageView bitmapImageView;

    // menu variables to keep track of what's on and off
    // assuming everything starts at "on" state
    private static Boolean lidarState = true;
    private static Boolean objectDetectionState = true;
    public static Boolean lidarUiState = true;
    private static Boolean objectDetectionUiState = true;

    //BLE
    private static BLE ble;

    //Wake Lock
    private static PowerManager powerManager;
    private static PowerManager.WakeLock wakeLock;

    //lidar to ble semaphore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Checking for permissions
        PackageManager pm = this.getPackageManager();
        int hasPerm2 = pm.checkPermission(
                Manifest.permission.WAKE_LOCK,
                this.getPackageName());
        if (hasPerm2 == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Has permission: wake");
        }
        else System.out.println("Doesn't have permission: wake");


        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        powerManager = getApplicationContext().getSystemService(PowerManager.class);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Insight::wakeLockTag");

        if (powerManager.isIgnoringBatteryOptimizations("insight"))
            System.out.println("Is ignoring");
        else System.out.println("Is not ignoring");

        //BLE
        if (ble == null){
            ble = new BLE(this);
        }

        //Camera Permissions
        if(!hasPermission()){
            requestPermission();
        }

        container = findViewById(R.id.container);
        bitmapImageView = findViewById(R.id.bitmapImageView);

        //Lidar Helper
        try {
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            lidarHelper = new LidarHelper(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intent = getIntent();
        if (intent != null) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                Toast.makeText(getBaseContext(), "thing: " + device, Toast.LENGTH_SHORT).show();
                lidarHelper.connectUsb();
            }
        }
    }

    @Override
    public synchronized void onStart() {
    super.onStart();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (ble == null){
            ble = new BLE(this);
        }
        if (detectorActivity == null) {
            detectorThread = new Thread(() -> {
                detectorActivity = new DetectorActivity(MainActivity.this, this, getSupportFragmentManager());
            });
            detectorThread.start();
        }else{
            resumeHandlerThread();
        }

        // turn on lidar on resume by default
        try {
          lidarHelper.sendStart3D();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        pauseHandlerThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            wakeLock.release();
        } catch (Exception ignored){}
        if (ble != null){
            try{
                ble.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (!allPermissionsGranted(grantResults)) {
                requestPermission();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.lidar_state:
                try {
                    if(lidarState){
                        // turn off lidar sensor
                        lidarHelper.sendStop();
                        lidarState = false;
                        item.setTitle("Lidar: OFF");
                        return true;
                    }
                    // turn on lidar sensor
                    lidarHelper.sendStart3D();
                    lidarState = true;
                    item.setTitle("Lidar: ON");

                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    break;
                }
            case R.id.bitmap_state:
                if(bitmapImageView.getVisibility() == View.VISIBLE){
                    bitmapImageView.setVisibility(View.INVISIBLE);
                } else {
                    bitmapImageView.setVisibility(View.VISIBLE);
                }
                if(lidarUiState) {
                    // turn off bitmap
                    lidarUiState = false; // LidarRenderer runnable checks for this variable state
                    item.setTitle("Bitmap: OFF");
                    return true;
                }
                // turn on bitmap
                lidarUiState = true;
                item.setTitle("Bitmap: ON");
                return true;
            case R.id.object_detection_state:
                if (objectDetectionState) {
                    // turn off object detection
                    pauseHandlerThread();
                    objectDetectionState = false;
                    item.setTitle("Obj Det: OFF");
                    return true;
                }
                // turn on object detection
                resumeHandlerThread();
                objectDetectionState = true;
                item.setTitle("Obj Det: ON");
                return true;
            case R.id.camera_state:
                if(container.getVisibility() == View.VISIBLE){
                    container.setVisibility(View.GONE);
                } else {
                    container.setVisibility(View.VISIBLE);
                }
                if(objectDetectionUiState){
                    // turn off camera feed
                    // TODO: turn off camera feed
                    objectDetectionUiState = false;
                    item.setTitle("Cam Feed: OFF");
                    return true;
                }
                // turn on camera feed
                // TODO: turn on camera feed
                objectDetectionUiState = true;
                item.setTitle("Cam Feed: ON");
                return true;
        }
        return false;
    }

    private void resumeHandlerThread(){
        detectorThread = new Thread(() -> {
            detectorActivity = new DetectorActivity(MainActivity.this, this, getSupportFragmentManager());
        });
        detectorThread.start();
    }

    private void pauseHandlerThread(){
        if (detectorActivity != null) {
            detectorActivity.handlerThread.quitSafely();
            try {
                detectorActivity.handlerThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            detectorActivity.handlerThread = null;
            detectorActivity.handler = null;
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
                Toast.makeText(
                                this,
                                "Camera permission is required",
                                Toast.LENGTH_LONG)
                        .show();
            }
            requestPermissions(new String[] {PERMISSION_CAMERA}, 1);
        }
    }

    private static boolean allPermissionsGranted(final int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static ImageView getBitmapImageView(){
        return bitmapImageView;
    }

    public static BLE getBle() {
        return ble;
    }

    public static PowerManager.WakeLock getWakeLock() {
        return wakeLock;
    }
}