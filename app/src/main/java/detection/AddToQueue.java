package detection;

import com.example.insight.BTSerial.PriorityModule;
import com.example.insight.BTSerial.ThreeTuple;

import java.util.concurrent.ArrayBlockingQueue;

public class AddToQueue {
    private double size;
    private String label;
    private static final double MIN_SIZE = 0.5;
    private static final ArrayBlockingQueue<ThreeTuple<String>> cameraQ = PriorityModule.getCameraQ();

    public static void Add(float size, String label){
        if (size < MIN_SIZE){
            return;
        }
        int priority = (int)((size-0.5) / 0.5) * 16;
        //return new ThreeTuple()
    }
}
