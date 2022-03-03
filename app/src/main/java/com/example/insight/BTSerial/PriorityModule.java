package com.example.insight.BTSerial;

import android.content.Context;

import java.util.concurrent.ArrayBlockingQueue;

public class PriorityModule implements Runnable{
    private Context context;
    private static ArrayBlockingQueue<ThreeTuple> liDARQ;
    private static ArrayBlockingQueue<ThreeTuple> mapQ;
    private static ArrayBlockingQueue<ThreeTuple> cameraQ;

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
                cameraQ.take();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static ArrayBlockingQueue<ThreeTuple> getliDARQ(){
        return PriorityModule.liDARQ;
    }

    public static ArrayBlockingQueue<ThreeTuple> getMapQ(){
        return PriorityModule.mapQ;
    }

    public static ArrayBlockingQueue<ThreeTuple> getCameraQ(){
        return PriorityModule.cameraQ;
    }
}