package com.example.insight.BTSerial;

import android.content.Context;
import org.tensorflow.lite.examples.detection.tflite.Detector;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class PriorityModule implements Runnable{
    private Context context;
    private static ArrayBlockingQueue<ThreeTuple<int[]>> liDARQ;
    private static ArrayBlockingQueue<ThreeTuple> mapQ;
    private static PriorityBlockingQueue<ThreeTuple<Detector.Recognition>> cameraQ;
    private Scheduler scheduler = new Scheduler();
    private int[] lastLidarData = new int[20];
    private String cameraMessage;
    private int cameraMessageIndex = 0;
    private Instant cameraTimer;
    private static final int CHAR_DURATION = 500;
    private int[] lastBraille = new int[20];
    private final Comparator<ThreeTuple> comparator = Comparator.comparingInt(ThreeTuple::getPriority);

    public PriorityModule(Context context){
        this.context = context;
        if (PriorityModule.liDARQ == null){
            PriorityModule.liDARQ = new ArrayBlockingQueue<>(100);
        }
        if (PriorityModule.mapQ == null){
            PriorityModule.mapQ = new ArrayBlockingQueue<>(100);
        }
        if (PriorityModule.cameraQ == null){
            PriorityModule.cameraQ = new PriorityBlockingQueue<>(100,comparator);
        }
    }

    @Override
    public void run(){
        while(true){
            try{
                //TODO: Flow control for incoming CameraTTs
                ThreeTuple<int[]> lidarTT = liDARQ.poll();
                ThreeTuple mapTT = mapQ.peek();
                ThreeTuple<Detector.Recognition> cameraTT = cameraQ.peek();
                if (lidarTT == null && mapTT == null && cameraTT == null){
                    return;
                }
                int lidarPriority = -1;
                if (lidarTT != null){
                    lastLidarData = lidarTT.getData();
                    lidarPriority = lidarTT.getPriority();
                }
                int mapPriority = -1;
                if (mapTT != null){
                    if (mapTT.getDeadline().isAfter(Instant.now())) {
                        mapPriority = mapTT.getPriority();
                    }
                    else{
                        mapQ.take();
                    }
                }
                int cameraPriority = -1;
                if (cameraTT != null){
                    if (cameraTT.getDeadline().isAfter(Instant.now())) {
                        cameraPriority = cameraTT.getPriority();
                    }
                    else{
                        cameraQ.take();
                    }
                }
                int maxPriority = ThreeTuple.max(new ThreeTuple[]{lidarTT, mapTT, cameraTT});

                if (maxPriority == lidarPriority){
                    //TODO: Send to scheduler/BT
                    scheduler.enqueue(lidarTT);
                }
                else if (maxPriority == cameraPriority){
                    //TODO: parse and send to BT
                    int[] mergedOutput = new int[20];
                    if (cameraTimer == null){
                        //cameraMessage = cameraTT.getData();
                        cameraTimer = Instant.now().plusMillis(CHAR_DURATION);
                        lastBraille = BrailleParser.parse(cameraMessage.charAt(cameraMessageIndex));
                        mergedOutput = mergeOutput(lastLidarData, lastBraille, false);
                    }else if (cameraTimer.isAfter(Instant.now())){
                        mergedOutput = mergeOutput(lastLidarData, lastBraille, false);
                    }else{
                        cameraMessageIndex++;
                        if (cameraMessageIndex >= cameraMessage.length()){
                            cameraTimer = null;
                            cameraMessageIndex = 0;
                        }
                        else{
                            cameraTimer = Instant.now().plusMillis(CHAR_DURATION);
                            lastBraille = BrailleParser.parse(cameraMessage.charAt(cameraMessageIndex));
                            mergedOutput = mergeOutput(lastLidarData, lastBraille, false);
                        }
                    }
                    scheduler.enqueue(mergedOutput, Instant.now(), 0);
                }
                else if (maxPriority == mapPriority){
                    //TODO: Merge with 2nd prio
                }
                else{
                    continue;
                }

                cameraQ.take();
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
        return PriorityModule.liDARQ;
    }

    public static ArrayBlockingQueue<ThreeTuple> getMapQ(){
        return PriorityModule.mapQ;
    }

    public static PriorityBlockingQueue<ThreeTuple<Detector.Recognition>> getCameraQ(){
        return PriorityModule.cameraQ;
    }

}