package com.example.insight;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import detection.CameraActivity;
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
    private static ImageView bitmapImageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Camera Permissions
        if(!hasPermission()){
            requestPermission();
        }

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
  }

  @Override
  public synchronized void onPause() {
      pauseHandlerThread();
      super.onPause();
  }

//  @Override
//  public synchronized void onStop() {
////    LOGGER.d("onStop " + this);
//    super.onStop();
//  }
//
//  @Override
//  public synchronized void onDestroy() {
////    LOGGER.d("onDestroy " + this);
//    super.onDestroy();
//  }

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
            case R.id.lidar_on:
                try {
                    lidarHelper.sendStart3D();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return (true);
            case R.id.lidar_off:
                try {
                    lidarHelper.sendStop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return (true);
            case R.id.lidar_bitmap:
                //Turn off bitmap
                bitmapImageView.setEnabled(!bitmapImageView.isEnabled());
                return (true);
            case R.id.objDetectSwitch:
                if (detectorActivity.handlerThread != null) {
                    pauseHandlerThread();
                }
                else{
                    resumeHandlerThread();
                }
                return (true);
        }
        return false;
    }

    private void resumeHandlerThread(){
        if (detectorActivity != null) {
            HandlerThread hThread = new HandlerThread("inference");
            detectorActivity.handlerThread = hThread;
            hThread.start();
            detectorActivity.handler = new Handler(hThread.getLooper());
        }
    }

    private void pauseHandlerThread(){
        if (detectorActivity != null) {
            detectorActivity.handlerThread.quitSafely();
            try {
                detectorActivity.handlerThread.join();
                detectorActivity.handlerThread = null;
                detectorActivity.handler = null;
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
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
}