package lidar.LidarModule;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import com.example.insight.BTSerial.BLE;
import com.example.insight.BTSerial.Scheduler;
import com.example.insight.BTSerial.ThreeTuple;
import com.example.insight.MainActivity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class DataPoolScheduler{
    private static ArrayBlockingQueue<int[]> hapticQ;
    private static ArrayBlockingQueue<ThreeTuple<int[]>> LiDARQ;
    private HandlerThread handlerThread;
    private Handler handler;
    private ThreadPoolExecutor executor;
    private int horizontalSectors = 5;
    private int verticalSectors = 2;
    private final static int FRAME_WIDTH = 160;
    private final static int FRAME_HEIGHT = 60;
    private BitmapGenerator bitmapGenerator;

    public DataPoolScheduler() {
        DataPoolScheduler.hapticQ = LidarRenderer.getHapticQ();
        DataPoolScheduler.LiDARQ = Scheduler.getliDARQ();
        bitmapGenerator = new BitmapGenerator();
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlerThread = new HandlerThread("DataPoolHandler");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool
                (4,  new ThreadFactory() {
                    int threadNo = 1;
                    @Override
                    public Thread newThread(Runnable runnable) {
                        return new Thread(runnable,"DataPoolExecutor:"+ threadNo++);
                    }
                });
    }

//    @Override
//    public void run() {
//        while (true) {
//            try {
//                int[] frame = hapticQ.take();
//                sectorGenerator(frame);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private void sectorGenerator(int[] hapticInt, int[] frameInt){
        try{
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
                    calls.add(new SectorMax(left, right, top, bottom, hapticInt));
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
            //LiDARQ.put(new ThreeTuple<>(serialOut, Instant.now(), priority));
            //Goes to bitmapGenerator and to ble

            if (MainActivity.lidarUiState) bitmapGenerator.postToBitmapHandler(serialOut,frameInt);
        }catch (Exception e) {
            Log.e("DataPool", e.getMessage());
        }
    }

    public void postToDataPoolHandler(int[] hapticInt, int[] frameInt){
        handler.post(() ->{
            sectorGenerator(hapticInt, frameInt);
        });
    }
}
