package com.example.insight.BTSerial;

import android.content.Context;

import java.util.concurrent.ArrayBlockingQueue;

public class PriorityModule implements Runnable{
    private Context context;
    private static ArrayBlockingQueue<byte[]> LiDARQ;
    private static ArrayBlockingQueue<byte[]> MapQ;
    private static ArrayBlockingQueue<byte[]> CameraQ;

    public PriorityModule(Context context){
        this.context = context;
    }

    @Override
    public void run(){
        while(true){
            try{

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static ArrayBlockingQueue<byte[]> getliDARQ(){
        return PriorityModule.LiDARQ;
    }

    public static ArrayBlockingQueue<byte[]> getMapQ(){
        return PriorityModule.MapQ;
    }

    public static ArrayBlockingQueue<byte[]> getCameraQ(){
        return PriorityModule.CameraQ;
    }
}