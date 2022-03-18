package com.example.insight;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.example.insight.BTSerial.BLE;
import detection.CameraActivity;
import detection.DetectorActivity;
import detection.customview.OverlayView;
import lidar.LidarModule.LidarHelper;
import lidar.LidarModule.LidarRenderer;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity {

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    private Intent intent;
    protected LidarHelper lidarHelper;
    private DetectorActivity detectorActivity;

    private Thread detectorThread;
    private static OverlayView trackingOverlay ;
    private static LinearLayout userLayout;
    private static LinearLayout devLayout;
    private static FrameLayout container;
    private static ImageView bitmapImageView;
    private static ImageView app_logo;
    private static Button button1;
    private static Button button2;
    private static Button button3;
    private static TextView instructionTextView;

    // menu variables to keep track of what's on and off
    // assuming everything starts at "on" state
    private static Boolean userModeState = true;
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

        userLayout = findViewById(R.id.userLayout);
        devLayout = findViewById(R.id.devLayout);
        container = findViewById(R.id.container);
        bitmapImageView = findViewById(R.id.bitmapImageView);
        app_logo = findViewById(R.id.app_logo);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        instructionTextView = findViewById(R.id.instructionTextView );

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

        // set up voice instructions
        final MediaPlayer[] mp1 = {MediaPlayer.create(this, R.raw.song1)};
        final MediaPlayer[] mp2 = {MediaPlayer.create(this, R.raw.song2)};
        final MediaPlayer[] mp3 = {MediaPlayer.create(this, R.raw.song3)};
        app_logo.setOnClickListener(v -> {
            try {
                if (mp1[0].isPlaying() || mp2[0].isPlaying() || mp3[0].isPlaying()) {
                    mp1[0].stop();
                    mp1[0].release();
                    mp1[0] = MediaPlayer.create(this, R.raw.song1);
                    mp2[0].stop();
                    mp2[0].release();
                    mp2[0] = MediaPlayer.create(this, R.raw.song2);
                    mp3[0].stop();
                    mp3[0].release();
                    mp3[0] = MediaPlayer.create(this, R.raw.song3);
                }
                instructionTextView.setText(R.string.instructions);
            } catch(Exception e) { e.printStackTrace(); }
        });
        button1.setOnClickListener(v -> {
            try {
                if (mp1[0].isPlaying() || mp2[0].isPlaying() || mp3[0].isPlaying()) {
                    mp1[0].stop();
                    mp1[0].release();
                    mp1[0] = MediaPlayer.create(this, R.raw.song1);
                    mp2[0].stop();
                    mp2[0].release();
                    mp2[0] = MediaPlayer.create(this, R.raw.song2);
                    mp3[0].stop();
                    mp3[0].release();
                    mp3[0] = MediaPlayer.create(this, R.raw.song3);
                }
                mp1[0].start();
                instructionTextView.setText(R.string.instructions1);
            } catch(Exception e) { e.printStackTrace(); }
        });
        button2.setOnClickListener(v -> {
            try {
                if (mp1[0].isPlaying() || mp2[0].isPlaying() || mp3[0].isPlaying()) {
                    mp1[0].stop();
                    mp1[0].release();
                    mp1[0] = MediaPlayer.create(this, R.raw.song1);
                    mp2[0].stop();
                    mp2[0].release();
                    mp2[0] = MediaPlayer.create(this, R.raw.song2);
                    mp3[0].stop();
                    mp3[0].release();
                    mp3[0] = MediaPlayer.create(this, R.raw.song3);
                }
                mp2[0].start();
                instructionTextView.setText(R.string.instructions2);
            } catch(Exception e) { e.printStackTrace(); }
        });
        button3.setOnClickListener(v -> {
            try {
                if (mp1[0].isPlaying() || mp2[0].isPlaying() || mp3[0].isPlaying()) {
                    mp1[0].stop();
                    mp1[0].release();
                    mp1[0] = MediaPlayer.create(this, R.raw.song1);
                    mp2[0].stop();
                    mp2[0].release();
                    mp2[0] = MediaPlayer.create(this, R.raw.song2);
                    mp3[0].stop();
                    mp3[0].release();
                    mp3[0] = MediaPlayer.create(this, R.raw.song3);
                }
                mp3[0].start();
                instructionTextView.setText(R.string.instructions3);
            } catch(Exception e) { e.printStackTrace(); }
        });
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
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
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
        MenuItem i = menu.findItem(R.id.user_mode_state);
        if (userModeState) {
            i.setTitle("USER MODE");
        } else {
            i.setTitle("DEV MODE");
        }
        MenuItem[] item = {menu.findItem(R.id.lidar_state), menu.findItem(R.id.bitmap_state), menu.findItem(R.id.object_detection_state), menu.findItem(R.id.camera_state)};
        for (MenuItem it : item) {
            it.setVisible(!userModeState);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.user_mode_state:
                switchUserModeState(item);
                break;
            case R.id.lidar_state:
                switchLidarState(item);
                break;
            case R.id.bitmap_state:
                switchLidarUiState(item);
                break;
            case R.id.object_detection_state:
                switchObjectDetectionState(item);
                break;
            case R.id.camera_state:
                switchObjectDetectionUiState(item);
                break;
            default:
                return false;
        }
        return true;
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

    public static OverlayView getTrackingOverlay(){
        return trackingOverlay;
    }

    public static ImageView getBitmapImageView(){
        return bitmapImageView;
    }

    private void switchUserModeState(MenuItem item) {
        if(userModeState) {
            // go into developer mode
            userModeState = false;
            invalidateOptionsMenu();
            userLayout.setVisibility(View.GONE);
            devLayout.setVisibility(View.VISIBLE);
        } else {
            // go into user mode
            userModeState = true;
            invalidateOptionsMenu();
            devLayout.setVisibility(View.INVISIBLE);
            userLayout.setVisibility(View.VISIBLE);
        }
    }

    private void switchLidarState(MenuItem item) {
        try {
            if(lidarState){
                // turn off lidar sensor
                lidarHelper.sendStop();
                lidarState = false;
                item.setTitle("Lidar: OFF");
                return;
            }
            // turn on lidar sensor
            lidarHelper.sendStart3D();
            lidarState = true;
            item.setTitle("Lidar: ON");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchLidarUiState(MenuItem item) {
        if(bitmapImageView.getVisibility() == View.VISIBLE){
            bitmapImageView.setVisibility(View.GONE);
        } else {
            bitmapImageView.setVisibility(View.VISIBLE);
        }
        if(lidarUiState) {
            // turn off bitmap
            lidarUiState = false; // LidarRenderer runnable checks for this variable state
            item.setTitle("Bitmap: OFF");
            return;
        }
        // turn on bitmap
        lidarUiState = true;
        item.setTitle("Bitmap: ON");
    }

    private void switchObjectDetectionState(MenuItem item) {
        if (objectDetectionState) {
            // turn off object detection
            pauseHandlerThread();
            objectDetectionState = false;
            item.setTitle("Obj Det: OFF");
            return;
        }
        // turn on object detection
        resumeHandlerThread();
        objectDetectionState = true;
        item.setTitle("Obj Det: ON");
    }

    private void switchObjectDetectionUiState(MenuItem item) {
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
            return;
        }
        // turn on camera feed
        // TODO: turn on camera feed
        objectDetectionUiState = true;
        item.setTitle("Cam Feed: ON");
    }

    public static BLE getBle() {
        return ble;
    }

    public static PowerManager.WakeLock getWakeLock() {
        return wakeLock;
    }
}