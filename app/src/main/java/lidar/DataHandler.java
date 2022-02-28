package lidar;

import java.util.concurrent.ArrayBlockingQueue;

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

    public DataHandler() {
        if (frame == null) {
            frame = new byte[14407];
        }
        if (frameQ == null){
            frameQ = new ArrayBlockingQueue<>(10);
        }
        dataQ = LidarHelper.getDataQ();
        //this.dataLength = data.length;
    }

    @Override
    public void run() {
        while (true) {
            try {
                //byte[] dataChunk = dataQ.take();
                for (byte b : dataQ.take()) {
                    streamingHeader_(b);
                }
            } catch (ArrayIndexOutOfBoundsException arrEx) {
                dataIndex = 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static void streamingHeader_(byte bite) {
        if (bite == _65_0x41){
            check1 = true;
            check2 = false;
        }else if(check1 && bite == _56_0x38){
            check2 = true;
        }else if(check2 && bite == _8_0x8){
            Thread t = new Thread(new tPrint(""+ frame[0] +":"+ dataIndex));
            t.start();
            dataIndex = 0;
            frameQ.add(frame.clone());
            check1 = false;
            check2 = false;
        }else{
            check1 = false;
            check2 = false;
        }
        frame[dataIndex] = bite;
        dataIndex++;

    }

    private static boolean streamingHeader(byte bite) {
        boolean out = false;
        if (check1) {
            if (check2) {
                if (bite == _8_0x8) {
                    out = true;
                } else {
                    check1 = false;
                    check2 = false;
                    out = false;
                }
            } else if (bite== _56_0x38) {
                check2 = true;
                out = false;
            } else {
                check1 = false;
            }
        } else if (bite==_65_0x41) {
            check1 = true;
            out = false;
        }
        frame[dataIndex] = bite;
        dataIndex++;
        if (out) {
            //System.out.println(dataIndex/* + Arrays.toString(frame)*/);

            Thread t = new Thread(new tPrint(""+ frame[0] +":"+ dataIndex));
            t.start();
            dataIndex = 0;

        }
        return out;
    }

    private static int findHeader(byte[] data, int dataLength) {
        for (int i = 2; i < dataLength; i++) {
            if (data[i] == _8_0x8 && data[i - 1] == _56_0x38 && data[i - 2] == _65_0x41) {
                dataIndex = 0;
                Thread t = new Thread(new tPrint(total + ""));
                //System.out.println(total + Arrays.toString(frame));
                total = 0;
                return i + 1;
            }
            //if (i > 7) break;
        }
        return 0;
    }

    private static void fillFrame(byte[] data, int dataLength, int index, int oldDataIndex) {
        int limit = dataLength - index;
        if (limit >= 0) System.arraycopy(data, index, frame, oldDataIndex, limit);
        dataIndex += limit;
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