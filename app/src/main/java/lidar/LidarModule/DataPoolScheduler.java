package lidar.LidarModule;

import com.example.insight.BTSerial.Scheduler;
import com.example.insight.BTSerial.ThreeTuple;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class DataPoolScheduler implements Runnable {
    private static ArrayBlockingQueue<int[]> hapticQ;
    private static ArrayBlockingQueue<ThreeTuple<int[]>> LiDARQ;
    private ThreadPoolExecutor executor;
    private int horizontalSectors = 5;
    private int verticalSectors = 2;
    private final static int FRAME_WIDTH = 160;
    private final static int FRAME_HEIGHT = 60;

    public DataPoolScheduler() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        DataPoolScheduler.hapticQ = LidarRenderer.getHapticQ();
        DataPoolScheduler.LiDARQ = Scheduler.getliDARQ();
    }

    @Override
    public void run() {
        while (true) {
            try {
                int[] frame = hapticQ.take();
                List<SectorMax> calls = new ArrayList<>();
                List<Future<Integer>> callsOut = new ArrayList<>();
                //TODO: talk to mathew about motor addressing
                int sectorWidth = FRAME_WIDTH/horizontalSectors;
                int sectorHeight = FRAME_HEIGHT/verticalSectors;
                for (int i = 0; i < horizontalSectors; i++) {
                    for (int j = 0; j < verticalSectors; j++) {
                        int left = i * sectorWidth;
                        int right = left + sectorWidth;
                        int top = j * sectorHeight;
                        int bottom = top + sectorHeight;
                        calls.add(new SectorMax(left, right, top, bottom, frame));
                    }
                }
                callsOut = executor.invokeAll(calls);
                List<Integer> hapticOut = new ArrayList<>();
                for (Future f : callsOut) hapticOut.add((Integer) f.get());
                System.out.println(hapticOut);
                //TODO: make generic
                int[] serialOut = new int[20];
                Arrays.fill(serialOut, 0);
                int priority = -1;
                for (int i = 0; i < 2*horizontalSectors; i++) {
                    int value = hapticOut.get(i) / 128;
                    if (value > priority) priority = value;
                    serialOut[i] = value;
                }
                //System.out.println(hapticOut);
                //System.out.println(Arrays.toString(serialOut));
                LiDARQ.put(new ThreeTuple<>(serialOut, Instant.now(), priority));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
