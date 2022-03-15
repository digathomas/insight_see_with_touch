package lidar.LidarModule;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import com.example.insight.MainActivity;

import java.util.concurrent.*;

public class DataHandler {
    private static ArrayBlockingQueue<byte[]> dataQ;
    private static ArrayBlockingQueue<byte[]> frameQ;
    //private static LinkedBlockingQueue<Byte> byteQ;
    //3D data header
    private static final byte _8_0x8 = (byte) 8;
    private static final byte _56_0x38 = (byte) 56;
    private static final byte _65_0x41 = (byte) 65;
    //Info header
    private static final byte _7_0x7 = (byte) 7;
    private static final byte _0_0x0 = (byte) 0;
    private static final byte _16_0x10 = (byte) 16;
    //private static int length;
    private static byte[] frame;
    private static int dataIndex;
    private int dataLength;
    private int output;
    private byte[] frameOut;
    private static int total;
    private static boolean check1 = false;
    private static boolean check2 = false;
    protected HandlerThread handlerThread;
    protected Looper looper;
    protected static Handler handler = null;
    protected LidarRenderer lidarRenderer;
    private ThreadPoolExecutor executor;

    public DataHandler() {
        if (frame == null) {
            frame = new byte[14407];
        }
        if (frameQ == null){
            frameQ = new ArrayBlockingQueue<>(10);
        }
        dataQ = lidar.LidarModule.LidarHelper.getDataQ();

        initializeHandlers();
        lidarRenderer = new LidarRenderer(handler,executor);

    }

    private void initializeHandlers() {
        handlerThread = new HandlerThread("DataHandler",10);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool
                (1,new ThreadFactory() {
                    int threadNo = 1;
                    @Override
                    public Thread newThread(Runnable runnable) {
                        return new Thread(runnable,"ExecutorPool:"+ threadNo++);
                    }
                });
    }

//    @Override
//    public void run() {
//        while (true) {
//            try {
//                for (byte b : dataQ.take()){
//                    streamingHeader(b);
//                }
//            } catch (ArrayIndexOutOfBoundsException arrEx) {
//                dataIndex = 0;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//    }

    private void streamingHeader(byte bite) {
        if (!check1 && bite == _65_0x41){
            check1 = true;
            check2 = false;
        }else if(check1 && bite == _56_0x38){
            check2 = true;
        }else if(check2 && bite == _8_0x8){
            System.out.println(""+ frame[0] +":"+ dataIndex);
            if (dataIndex == 14407) lidarRenderer.frameProcessing(frame.clone());
            dataIndex = 0;
            //frameQ.add(frame.clone());
            check1 = false;
            check2 = false;
        }else{
            check1 = false;
            check2 = false;
        }
        if (dataIndex >= 14407) dataIndex = 0;
        frame[dataIndex] = bite;
        dataIndex++;

    }

    public void postToDataHandler(byte[] data){
        handler.post(() -> {
            for (byte b : data) {
                streamingHeader(b);
            }
        });
    }

    public static ArrayBlockingQueue<byte[]> getFrameQ() {
        return DataHandler.frameQ;
    }

    private static class tPrint implements Runnable {
        private final String out;

        public tPrint(String out) {
            this.out = out;
        }

        @Override
        public void run() {
            System.out.println(this.out);
        }
    }
}