package lidar.LidarModule;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.*;

public class DataHandler implements Runnable {
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
    protected static Handler handler;

    public DataHandler() {
        if (frame == null) {
            frame = new byte[14407];
        }
        if (frameQ == null){
            frameQ = new ArrayBlockingQueue<>(10);
        }
        dataQ = lidar.LidarModule.LidarHelper.getDataQ();
//        executor = Executors.newSingleThreadExecutor();
//        handlerThread = new HandlerThread("DHandler_print");
//        handlerThread.start();
//        looper = handlerThread.getLooper();
//        handler  = new Handler(looper);
        //this.dataLength = data.length;
    }

    @Override
    public void run() {
        while (true) {
            try {
                for (byte b : dataQ.take()){
                    streamingHeader(b);
                }
//                byte[] arr = dataQ.take();
//                for (int i = 0; i < arr.length-2; i++) {
//                    byte b = arr[i];
//                    if (b == _65_0x41) {
//                        if (arr[i + 1] == _56_0x38) {
//                            if (arr[i + 2] == _8_0x8) {
//                                System.out.println(dataIndex);
//                                if (dataIndex == 14407) frameQ.add(frame.clone());
//                                dataIndex = 0;
//                            }
//                        }
//                    }
//                    if (dataIndex >= 14407) dataIndex = 0;
//                    frame[dataIndex] = arr[i];
//                    dataIndex++;
//                }


            } catch (ArrayIndexOutOfBoundsException arrEx) {
                dataIndex = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void streamingHeader(byte bite) {
        if (!check1 && bite == _65_0x41){
            check1 = true;
            check2 = false;
        }else if(check1 && bite == _56_0x38){
            check2 = true;
        }else if(check2 && bite == _8_0x8){
            System.out.println(""+ frame[0] +":"+ dataIndex);
            if (dataIndex == 14407) frameQ.add(frame.clone());
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