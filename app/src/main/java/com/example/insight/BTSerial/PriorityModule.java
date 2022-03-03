package com.example.insight.BTSerial;

import android.content.Context;

import java.util.concurrent.ArrayBlockingQueue;

public class PriorityModule implements Runnable{
    private Context context;
    private static ArrayBlockingQueue<ThreeTuple<int[]>> liDARQ;
    private static ArrayBlockingQueue<ThreeTuple> mapQ;
    private static ArrayBlockingQueue<ThreeTuple<String>> cameraQ;

    public PriorityModule(Context context){
        this.context = context;
        if (PriorityModule.liDARQ == null){
            PriorityModule.liDARQ = new ArrayBlockingQueue<>(100);
        }
        if (PriorityModule.mapQ == null){
            PriorityModule.mapQ = new ArrayBlockingQueue<>(100);
        }
        if (PriorityModule.cameraQ == null){
            PriorityModule.cameraQ = new ArrayBlockingQueue<>(100);
        }
    }

    @Override
    public void run(){
        while(true){
            try{
                //TODO: Flow control for incoming CameraTTs
                ThreeTuple<int[]> lidarTT = liDARQ.poll();
                int lidarPriority = 0;
                if (lidarTT != null){
                    lidarPriority = lidarTT.getPriority();
                }
                ThreeTuple mapTT = mapQ.poll();
                int mapPriority = 0;
                if (mapTT != null){
                    mapPriority = mapTT.getPriority();
                }
                ThreeTuple<String> cameraTT = cameraQ.poll();
                int cameraPriority = 0;
                if (cameraTT != null){
                    cameraPriority = cameraTT.getPriority();
                }
                int maxPriority = ThreeTuple.max(new ThreeTuple[]{lidarTT, mapTT, cameraTT});

                if (maxPriority == lidarPriority){
                    //TODO: Send to scheduler/BT
                }
                else if (maxPriority == cameraPriority){
                    //TODO: parse and send to BT
                    String data = cameraTT.getData();
                    for (int i = 0; i < data.length(); i++){
                        int[] braille = BrailleParser.parse(data.charAt(i));
                    }
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

    public static ArrayBlockingQueue<ThreeTuple<int[]>> getliDARQ(){
        return PriorityModule.liDARQ;
    }

    public static ArrayBlockingQueue<ThreeTuple> getMapQ(){
        return PriorityModule.mapQ;
    }

    public static ArrayBlockingQueue<ThreeTuple<String>> getCameraQ(){
        return PriorityModule.cameraQ;
    }
}