package lidar.LidarModule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class DataPoolScheduler implements Runnable {
    private static ArrayBlockingQueue<int[]> hapticQ;
    private ThreadPoolExecutor executor;
    private int horizontalSectors = 8;
    private int verticalSectors = 2;
    private final static int FRAME_WIDTH = 160;
    private final static int FRAME_HEIGHT = 60;

    public DataPoolScheduler() {
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
        DataPoolScheduler.hapticQ = LidarRenderer.getHapticQ();
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
                for (int i = 0; i < 10; i++) {
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
                String serialOut = "";
                for (Integer integer : hapticOut) {
                    Integer value = integer / 314;
                    switch (value) {
                        case 0:
                            serialOut += 0;
                            break;
                        case 1:
                            serialOut += 1;
                            break;
                        case 2:
                            serialOut += 2;
                            break;
                        case 3:
                            serialOut += 3;
                            break;
                        case 4:
                            serialOut += 4;
                            break;
                        case 5:
                            serialOut += 5;
                            break;
                        case 6:
                            serialOut += 6;
                            break;
                        case 7:
                            serialOut += 7;
                            break;
                        case 8:
                            serialOut += 8;
                            break;
                        case 9:
                            serialOut += 9;
                            break;
                        case 10:
                            serialOut += "A";
                            break;
                        case 11:
                            serialOut += "B";
                            break;
                        case 12:
                            serialOut += "C";
                            break;
                        case 13:
                            serialOut += "D";
                            break;
                        case 14:
                            serialOut += "E";
                            break;
                        case 15:
                            serialOut += "F";
                            break;
                    }
                }
                System.out.println(hapticOut);
                System.out.println(serialOut);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
