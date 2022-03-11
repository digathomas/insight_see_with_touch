package com.example.insight.BTSerial;

import android.content.Context;

import com.example.insight.MainActivity;

import org.tensorflow.lite.examples.detection.tflite.Detector;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Scheduler implements Runnable{
    private Context context;
    private static ArrayBlockingQueue<ThreeTuple<int[]>> liDARQ;
    private static ArrayBlockingQueue<ThreeTuple> mapQ;
    private static PriorityBlockingQueue<ThreeTuple<Detector.Recognition>> cameraQ;
    private int[] lastLidarData = new int[20];
    private String cameraMessage;
    private int cameraMessageIndex = 0;
    private Instant cameraTimer;
    private static final int CHAR_DURATION = 500;
    private int[] lastBraille = new int[20];
    private final Comparator<ThreeTuple> comparator = Comparator.comparingInt(ThreeTuple::getPriority);
    private BLE ble;

    public Scheduler(Context context){
        this.context = context;
        if (Scheduler.liDARQ == null){
            Scheduler.liDARQ = new ArrayBlockingQueue<>(100);
        }
        if (Scheduler.mapQ == null){
            Scheduler.mapQ = new ArrayBlockingQueue<>(100);
        }
        if (Scheduler.cameraQ == null){
            Scheduler.cameraQ = new PriorityBlockingQueue<>(100,comparator);
        }
        ble = MainActivity.getBle();
    }

    @Override
    public void run(){
        while(true){
            try{
                //TODO: Flow control for incoming CameraTTs
                ThreeTuple<int[]> lidarTT = liDARQ.take();
                ThreeTuple mapTT = mapQ.poll();
                ThreeTuple<Detector.Recognition> cameraTT = cameraQ.poll();
                ble.writeToGatt(BLE.LEFT_GATT, Arrays.copyOfRange(lidarTT.getData(),0,10));
//                if (lidarTT == null && mapTT == null && cameraTT == null){
//                    return;
//                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
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

    public static ArrayBlockingQueue<ThreeTuple<int[]>> getliDARQ(){
        return Scheduler.liDARQ;
    }

    public static ArrayBlockingQueue<ThreeTuple> getMapQ(){
        return Scheduler.mapQ;
    }

    public static PriorityBlockingQueue<ThreeTuple<Detector.Recognition>> getCameraQ(){
        return Scheduler.cameraQ;
    }

}