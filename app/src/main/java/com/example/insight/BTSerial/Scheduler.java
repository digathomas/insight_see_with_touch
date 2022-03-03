package com.example.insight.BTSerial;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;

public class Scheduler implements Runnable{

    private static ArrayBlockingQueue<ThreeTuple<int[]>> scheduleQ;
    @Override
    public void run() {
        while (true){
            try{
                ThreeTuple<int[]> tuple = scheduleQ.take();
                //if (tuple.getDeadline().isAfter(Instant.now())){
                //TODO: send to BT serial
                    //int delay = tuple.getPriority();
                    //wait(delay);
            }catch (Exception ignored){

            }
        }
    }

    public void enqueue(int[] data, Instant deadline, int delay){
        scheduleQ.offer(new ThreeTuple<>(data, deadline, delay));
    }

    public void enqueue(ThreeTuple<int[]> tuple){
        scheduleQ.offer(new ThreeTuple<>(tuple.getData(), tuple.getDeadline(), 0));
    }
}
