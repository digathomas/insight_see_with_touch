package lidar;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.insight.R;
import lidar.DataHandler;
import lidar.LidarHelper;
import lidar.LidarRenderer;

import java.io.IOException;

public class LidarActivity extends AppCompatActivity{

    protected Intent intent;
    protected Button testButton, getInfoBbutton, threeDButton, exitButton;
    protected TextView infoTextView;
    protected ImageView bitmapImageView;
    protected LidarHelper lidarHelper;
    protected Thread dataThread;
    protected Thread renderThread;

    @Override
    protected void onDestroy() {
        try {
            LidarHelper.closePort();
            dataThread.join();
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
        if (dataThread == null) {
            dataThread = new Thread(new DataHandler());
            dataThread.setPriority(Thread.MAX_PRIORITY);
            dataThread.start();
        }
        if (renderThread == null) {
            renderThread = new Thread(new LidarRenderer(this, bitmapImageView));
            renderThread.start();
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