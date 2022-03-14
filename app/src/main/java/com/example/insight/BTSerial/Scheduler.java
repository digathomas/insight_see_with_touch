package com.example.insight.BTSerial;

import android.content.Context;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.example.insight.MainActivity;

import detection.DetectorActivity;
import org.tensorflow.lite.examples.detection.tflite.Detector;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

public class Scheduler{
    private static ArrayBlockingQueue<ThreeTuple<int[]>> liDARQ;
    private static ArrayBlockingQueue<ThreeTuple> mapQ;
    private static PriorityBlockingQueue<ThreeTuple<Detector.Recognition>> cameraQ;
    private int[] lastLidarData = new int[20];
    private String cameraMessage;
    private int cameraMessageIndex = 0;
    private static final int CHAR_DURATION = 500;
    private final Comparator<ThreeTuple> comparator = Comparator.comparingInt(ThreeTuple::getPriority);
    private BLE ble;
    private HandlerThread handlerThread;
    private Handler handler;

    private static Semaphore cameraSemaphore = null;
    private static Semaphore completeCameraSemaphore = null;
    private long cameraDelay;


    public Scheduler(){
        if (Scheduler.liDARQ == null){
            Scheduler.liDARQ = new ArrayBlockingQueue<>(100);
        }
        if (Scheduler.mapQ == null){
            Scheduler.mapQ = new ArrayBlockingQueue<>(100);
        }
        if (Scheduler.cameraQ == null){
            Scheduler.cameraQ = new PriorityBlockingQueue<>(100,comparator);
        }
        if (cameraSemaphore == null){
            cameraSemaphore = new Semaphore(0);
        }
        if (completeCameraSemaphore == null){
            completeCameraSemaphore = new Semaphore(0);
        }

        ble = MainActivity.getBle();
        initializeHandlers();
    }

    public void run(){
        //Lidar take and write to ble
        //task will try to write to ble with items in queue
        //then recreate task again
//        runInBackground(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    int[] lidarTT = liDARQ.take().getData();
//                    ble.writeToGatt(BLE.LEFT_GATT,lidarTT);
//                    runInBackground(this);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        //Camera obj detect has 2 waiting locks
        //cameraSemaphore locks will be used to allow detectorActivity to compute calculations
        //in the sendToPriotittyModule function.
        //completeCameraSemaphore waits for the opertaions in the detectorActivity to be complete
        //then
        runInBackground(new Runnable() {
            @Override
            public void run() {
                try {
                    //release semaphore after post delayed time
                    //for calculations to start
                    cameraSemaphore.release();

                    //wait for calculations to be done in DetectorActivity
                    completeCameraSemaphore.acquireUninterruptibly();

                    //send obj to ble or if null then check again
                    Detector.Recognition cameraT = DetectorActivity.sharedRecognition;
                    if (DetectorActivity.sharedRecognition != null) {
                        sendCameraDetectionToBle(cameraT.getTitle());
                        long delay = cameraT.getTitle().length() > 3500 ?
                                cameraT.getTitle().length() * 500 + 1500: 3000;
                        runDelayed(this,delay);
                    }
                    else{
                        runDelayed(this,300);
                    }


                }    catch (Exception e) {
                    Log.w("CameraScheduler",e.getMessage());
                }
            }
        });

    }

    //Initialize Handler and create task
    //to remove items in camera queue that are expired
    public void initializeHandlers() {
        handlerThread = new HandlerThread("CameraBleHandler");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }


    private int[] mergeOutput(int[] lidarArr, int[] otherArr, boolean isMap){
        int[] outArr = lidarArr.clone();
        Arrays.fill(outArr, 0);
        if (isMap){
            //do map merge
            outArr[0] = otherArr[0];
            outArr[1] = otherArr[1];
            outArr[18] = otherArr[18];
            outArr[19] = otherArr[19];
        }
        else{
            //do braille merge
            for (int i = 14; i < 20; i++){
                outArr[i] = otherArr[i];
            }
        }
        return outArr;
    }

    private void sendCameraDetectionToBle(String detectString){
        char[] detectChars = detectString.toCharArray();

        for(char c: detectChars) {
            runDelayed(new Runnable() {
                @Override
                public void run() {
                    int [] a = BrailleParser.parse(c);
                    ble.writeToGatt(BLE.RIGHT_GATT,BrailleParser.parse(c));
                }
            },500);
        }
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    protected synchronized void runDelayed(final Runnable r, long milliseconds) {
        if (handler != null) {
            handler.postDelayed(r,milliseconds);
        }
    }

    public Semaphore getCameraSemaphore(){
        return cameraSemaphore;
    }

    public Semaphore getCompleteCameraSemaphore(){
        return completeCameraSemaphore;
    }

    public static ArrayBlockingQueue<ThreeTuple<int[]>> getliDARQ(){
        return Scheduler.liDARQ;
    }

    public static ArrayBlockingQueue<ThreeTuple> getMapQ(){
        return Scheduler.mapQ;
    }

    public static PriorityBlockingQueue<ThreeTuple<Detector.Recognition>> getCameraQ(){
        return Scheduler.cameraQ;
    }

    public void destroy(){
        handlerThread.quitSafely();
    }
}