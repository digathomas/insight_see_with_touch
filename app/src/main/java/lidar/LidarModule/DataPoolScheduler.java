package lidar.LidarModule;

import com.example.insight.BTSerial.PriorityModule;
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
    private int horizontalSectors = 8;
    private int verticalSectors = 2;
    private final static int FRAME_WIDTH = 160;
    private final static int FRAME_HEIGHT = 60;

    public DataPoolScheduler() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        DataPoolScheduler.hapticQ = LidarRenderer.getHapticQ();
        DataPoolScheduler.LiDARQ = PriorityModule.getliDARQ();
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
                //String serialOut = "";
                //TODO: make generic
                int[] serialOut = new int[20];
                serialOut[0] = -1;
                serialOut[1] = -1;
                serialOut[18] = -1;
                serialOut[19] = -1;
                int priority = -1;
                for (int i = 2; i < 18; i++) {
                    int value = hapticOut.get(i - 2) / 128;
                    if (value > priority) priority = value;
                    serialOut[i] = value;
//                    switch (value) {
//                        case 0:
//                            serialOut[i] = 0;
//                            break;
//                        case 1:
//                            serialOut[i] = 1;
//                            break;
//                        case 2:
//                            serialOut[i] = 2;
//                            break;
//                        case 3:
//                            serialOut[i] = 3;
//                            break;
//                        case 4:
//                            serialOut[i] = 4;
//                            break;
//                        case 5:
//                            serialOut[i] = 5;
//                            break;
//                        case 6:
//                            serialOut[i] = 6;
//                            break;
//                        case 7:
//                            serialOut[i] = 7;
//                            break;
//                        case 8:
//                            serialOut[i] = 8;
//                            break;
//                        case 9:
//                            serialOut[i] = 9;
//                            break;
//                        case 10:
//                            serialOut[i] = 10;
//                            break;
//                        case 11:
//                            serialOut[i] = 11;
//                            break;
//                        case 12:
//                            serialOut[i] = 12;
//                            break;
//                        case 13:
//                            serialOut[i] = 13;
//                            break;
//                        case 14:
//                            serialOut[i] = 14;
//                            break;
//                        case 15:
//                            serialOut[i] = 15;
//                            break;
//                    }
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
