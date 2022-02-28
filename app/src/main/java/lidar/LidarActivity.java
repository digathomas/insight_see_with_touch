package lidar;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.insight.BTSerial.PriorityModule;
import com.example.insight.R;

import java.io.IOException;

import lidar.LidarModule.BitmapGenerator;
import lidar.LidarModule.DataHandler;
import lidar.LidarModule.DataPoolScheduler;
import lidar.LidarModule.LidarHelper;
import lidar.LidarModule.LidarRenderer;

public class LidarActivity extends AppCompatActivity{

    private static final String TAG = "LidarActivity";

    protected Intent intent;
    protected Button testButton, getInfoBbutton, threeDButton, exitButton;
    protected TextView infoTextView;
    protected ImageView bitmapImageView;
    protected LidarHelper lidarHelper;
    protected Thread dataThread;
    protected Thread renderThread;
    protected HandlerThread handlerThread;
    protected Thread bitmapThread;
    protected Thread hapticThread;
    protected Looper looper;
    protected Handler handler;
    protected Thread priorityThread;
    @Override
    protected void onDestroy() {
        try {
            LidarHelper.closePort();
            dataThread.join();
            renderThread.join();
            dataThread.join();
            handlerThread.join();
            hapticThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lidar);
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
        setupUI();
        //TODO: Move priority thread to Main Activity
        if (priorityThread == null){
            priorityThread = new Thread(new PriorityModule(this));
            priorityThread.setName("PriorityThread");
            priorityThread.start();
        }
        if (dataThread == null) {
            dataThread = new Thread(new DataHandler());
            dataThread.setPriority(Thread.MAX_PRIORITY);
            dataThread.setName("dataThread");
            dataThread.start();
        }
        if (renderThread == null) {
            renderThread = new Thread(new LidarRenderer(this, bitmapImageView));
            dataThread.setName("renderThread");
            renderThread.start();
        }
        if (bitmapThread == null){
            bitmapThread = new Thread(new BitmapGenerator());
            bitmapThread.setName("Bitmap Thread");
            bitmapThread.start();
        }
        if (hapticThread == null){
            hapticThread = new Thread(new DataPoolScheduler());
            hapticThread.setName("ThreadPool");
            hapticThread.start();
        }

    }

    public void setupUI() {

        testButton = findViewById(R.id.testButton);
        getInfoBbutton = findViewById(R.id.getInfoButton);
        threeDButton = findViewById(R.id.threeDModeButton);
        exitButton = findViewById(R.id.exitButton);
        infoTextView = findViewById(R.id.infoTextView);
        bitmapImageView = findViewById(R.id.bitmapImageView);

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    lidarHelper.sendStop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    lidarHelper.sendSetBaud();
                } catch (Exception e) {
                    makeToast(e.getMessage());
                }
            }
        });

        getInfoBbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(!lidarHelper.sendInfoRequest()){
                        Toast.makeText(getApplicationContext(),"No USB connection found.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //usbGetInfo();
            }
        });

        threeDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    lidarHelper.sendStart3D();
                    //threeDMode();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void makeToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public ImageView getBitmapImageView(){
        return bitmapImageView;
    }

}