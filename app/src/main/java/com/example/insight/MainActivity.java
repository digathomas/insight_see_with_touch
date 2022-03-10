package com.example.insight;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import detection.DetectorActivity;
import detection.customview.OverlayView;
import lidar.LidarActivity;
import lidar.LidarModule.LidarHelper;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    private Intent intent;
    private LidarActivity lidarActivity;
    protected LidarHelper lidarHelper;
    private DetectorActivity detectorActivity;

    private Thread detectorThread;
    private static OverlayView trackingOverlay ;
    private static LinearLayout userLayout;
    private static LinearLayout devLayout;
    private static FrameLayout container;
    private static ImageView bitmapImageView;

    // menu variables to keep track of what's on and off
    // assuming everything starts at "on" state
    private static Boolean userModeState = true;
    private static Boolean lidarState = true;
    private static Boolean objectDetectionState = true;
    public static Boolean lidarUiState = true;
    private static Boolean objectDetectionUiState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Camera Permissions
        if(!hasPermission()){
            requestPermission();
        }

        userLayout = findViewById(R.id.userLayout);
        devLayout = findViewById(R.id.devLayout);
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
        lidarActivity = new LidarActivity(this);

        // set up voice instructions
        FrameLayout frame1 = (FrameLayout) this.findViewById(R.id.frame1);
        FrameLayout frame2 = (FrameLayout) this.findViewById(R.id.frame2);
        FrameLayout frame3 = (FrameLayout) this.findViewById(R.id.frame3);
        final MediaPlayer[] mp1 = {MediaPlayer.create(this, R.raw.song1)};
        final MediaPlayer[] mp2 = {MediaPlayer.create(this, R.raw.song2)};
        final MediaPlayer[] mp3 = {MediaPlayer.create(this, R.raw.song3)};
        frame1.setOnClickListener(v -> {
            try {
                if (mp1[0].isPlaying()) {
                    mp1[0].stop();
                    mp1[0].release();
                    mp1[0] = MediaPlayer.create(this, R.raw.song1);
                }
                mp1[0].start();
            } catch(Exception e) { e.printStackTrace(); }
        });
        frame2.setOnClickListener(v -> {
            try {
                if (mp2[0].isPlaying()) {
                    mp2[0].stop();
                    mp2[0].release();
                    mp2[0] = MediaPlayer.create(this, R.raw.song2);
                }
                mp2[0].start();
            } catch(Exception e) { e.printStackTrace(); }
        });
        frame3.setOnClickListener(v -> {
            try {
                if (mp3[0].isPlaying()) {
                    mp3[0].stop();
                    mp3[0].release();
                    mp3[0] = MediaPlayer.create(this, R.raw.song3);
                }
                mp3[0].start();
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
            item.setTitle("DEV MODE");
            userLayout.setVisibility(View.GONE);
            devLayout.setVisibility(View.VISIBLE);
        } else {
            // go into user mode
            userModeState = true;
            item.setTitle("USER MODE");
            devLayout.setVisibility(View.GONE);
            userLayout.setVisibility(View.VISIBLE);
        }

        //TODO: dynamically remove menu items
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
}