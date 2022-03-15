package lidar.LidarModule;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.widget.ImageView;

import com.example.insight.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class LidarRenderer{
    private static ArrayBlockingQueue<byte[]> frameQ;
    private static ArrayBlockingQueue<Bitmap> bitmapQ;
    private static ArrayBlockingQueue<int[]> colorQ;
    private static ArrayBlockingQueue<int[]> hapticQ;
    private static int[] frameInt;
    private static int[] hapticInt;
    private static Bitmap oldBitmap;
    private static Bitmap newBitmap;

    private Handler uiHandler;
    private HandlerThread handlerThread;
    private ThreadPoolExecutor executor;
    private Handler handler;

    private DataPoolScheduler dataPoolScheduler;

    public LidarRenderer(Handler handler, ThreadPoolExecutor executor) {
        if (frameQ == null) LidarRenderer.frameQ = DataHandler.getFrameQ();
        LidarRenderer.frameInt = new int[9600];
        LidarRenderer.hapticInt = new int [9600];
        LidarRenderer.bitmapQ = new ArrayBlockingQueue<>(100);
        LidarRenderer.colorQ = new ArrayBlockingQueue<>(100);
        LidarRenderer.hapticQ = new ArrayBlockingQueue<>(100);
        dataPoolScheduler = new DataPoolScheduler(executor);
        this.handler = handler;
        this.executor = executor;
        initializeHandlers();
    }

    private void initializeHandlers() {
        uiHandler = new Handler(Looper.getMainLooper());
//        handlerThread = new HandlerThread("LidarHandler");
//        handlerThread.start();
//        handler = new Handler(handlerThread.getLooper());
//        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool
//                (2,  new ThreadFactory() {
//                    int threadNo = 1;
//                    @Override
//                    public Thread newThread(Runnable runnable) {
//                        return new Thread(runnable,"LidarExecutor:"+ threadNo++);
//                    }
//                });
    }

//    @Override
//    public void run() {
//        while (true) {
//            try {
//                //Since the data is in bytes (0x__) and each pixel is three hex values (0x___)
//                //It is necessary to split a byte such that (0x11)(0x22)(0x33) --> (0x112)(0x233)
//                byte[] frame = frameQ.take();
//                frameProcessing(frame);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public void frameProcessing(byte[] frame){
        try {
            //Since the data is in bytes (0x__) and each pixel is three hex values (0x___)
            //It is necessary to split a byte such that (0x11)(0x22)(0x33) --> (0x112)(0x233)
            int frameIndex = 0;

            //List<Callable<Void>> callables = new ArrayList<>();

            for (int i = 0; i < 4799; i++) {
                byteToHexHandling(i,frame);
//                int finalI = i;
//                callables.add(() -> {
//                    byteToHexHandling(finalI,frame);
//                    return null;
//                });
            }

            //executor.invokeAll(callables);

            dataPoolScheduler.sectorGenerator(hapticInt.clone(),frameInt.clone());
            //hapticQ.put(hapticInt.clone());
            if (MainActivity.lidarUiState) {
//                colorQ.put(frameInt.clone());
//                oldBitmap = newBitmap;
//                newBitmap = bitmapQ.take();
//                //newBitmap = Bitmap.createBitmap(frameInt, 160, 60, Bitmap.Config.ARGB_8888);
//                setBitmapOnUIThread(newBitmap);
//                oldBitmap.recycle();
//                System.gc();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void byteToHexHandling(int index, final byte[] frame){
        int nIndex = 1+3*index;
        int a = frame[nIndex]&0x0ff;
        int b = frame[nIndex + 1]&0x0ff;
        int c = frame[nIndex + 2]&0x0ff;

        int first = a;
        int second = b;
        first <<= 4;
        first = first | b >>> 4;

        second &= 0x0f;
        second <<= 8;
        second |= c;

        int frameIndex = index*2;
        hapticInt[frameIndex] = first;
        if (MainActivity.lidarUiState) {
            frameInt[frameIndex] = makeColor(first);
            frameInt[frameIndex+1] = makeColor(second);
        }
        hapticInt[frameIndex+1] = second;
    }

    public void postToLidarHandler(byte[] frame){
        executor.submit(() ->{
           frameProcessing(frame);
        });
    }

    private static int makeColor(int value) {
        int rValue = (value+1)/4;
        int gValue = (value+1)/4;
        int bValue = (value+1)/4;
        //mvalue = 128;
        //return Color.argb(255,0, 0 ,0);
        return Color.argb(255, rValue, gValue, bValue);
    }

    public static void stopRender() {
        colorQ.clear();
        oldBitmap.recycle();
        newBitmap.recycle();
    }

    public static ArrayBlockingQueue<Bitmap> getBitmapQ() {
        return bitmapQ;
    }

    public static ArrayBlockingQueue<int[]> getColorQ() {
        return colorQ;
    }

    public static ArrayBlockingQueue<int[]> getHapticQ() {
        return hapticQ;
    }


}
