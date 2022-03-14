package lidar;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.insight.BTSerial.Scheduler;
import com.example.insight.MainActivity;

import java.io.IOException;

import lidar.LidarModule.BitmapGenerator;
import lidar.LidarModule.DataHandler;
import lidar.LidarModule.DataPoolScheduler;
import lidar.LidarModule.LidarHelper;
import lidar.LidarModule.LidarRenderer;

public class LidarActivity {

    private static final String TAG = "LidarActivity";

    protected Intent intent;
    protected ImageView bitmapImageView;
    protected Thread dataThread;
    protected Thread renderThread;
    protected HandlerThread handlerThread;
    protected Thread bitmapThread;
    protected Thread hapticThread;
    private Context context;
    protected Scheduler scheduler;
    protected LidarRenderer lidarRenderer;

    public LidarActivity(Context context,Scheduler scheduler){
        this.context = context;
        this.scheduler = scheduler;
        this.onCreate();
    }

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
    }

    protected void onCreate() {
        this.bitmapImageView = MainActivity.getBitmapImageView();

//        if (dataThread == null) {
//            dataThread = new Thread(new DataHandler());
//            dataThread.setPriority(Thread.MAX_PRIORITY);
//            dataThread.setName("dataThread");
//            dataThread.start();
//        }
//        if (renderThread == null) {
//            renderThread = new Thread(new LidarRenderer(bitmapImageView));
//            renderThread.setPriority(Thread.MAX_PRIORITY);
//            renderThread.setName("renderThread");
//            renderThread.start();
//        }
//        if (bitmapThread == null){
//            bitmapThread = new Thread(new BitmapGenerator());
//            bitmapThread.setPriority(Thread.MAX_PRIORITY);
//            bitmapThread.setName("Bitmap Thread");
//            bitmapThread.start();
//        }
//        if (hapticThread == null){
//            hapticThread = new Thread(new DataPoolScheduler());
//            hapticThread.setPriority(Thread.MAX_PRIORITY);
//            hapticThread.setName("ThreadPool");
//            hapticThread.start();
//        }
    }
}