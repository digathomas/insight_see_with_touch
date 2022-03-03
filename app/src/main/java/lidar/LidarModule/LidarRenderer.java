package lidar.LidarModule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.widget.ImageView;

import java.util.concurrent.ArrayBlockingQueue;

public class LidarRenderer implements Runnable {
    private static ArrayBlockingQueue<byte[]> frameQ;
    private static ArrayBlockingQueue<Bitmap> bitmapQ;
    private static ArrayBlockingQueue<int[]> colorQ;
    private static ArrayBlockingQueue<int[]> hapticQ;
    private static int[] frameInt;
    private static int[] hapticInt;
    private final Handler handler;
    private final ImageView bitmapImageView;
    private static Bitmap newBitmap;

    public LidarRenderer(Context context, ImageView bitmapImageView) {
        if (frameQ == null) LidarRenderer.frameQ = DataHandler.getFrameQ();
        if (frameInt == null) LidarRenderer.frameInt = new int[9600];
        if (hapticInt == null) LidarRenderer.hapticInt = new int[9600];
        handler = new Handler(context.getMainLooper());
        LidarRenderer.bitmapQ = new ArrayBlockingQueue<>(100);
        LidarRenderer.colorQ = new ArrayBlockingQueue<>(100);
        LidarRenderer.hapticQ = new ArrayBlockingQueue<>(100);
        this.bitmapImageView = bitmapImageView;
    }

    @Override
    public void run() {
        while (true) {
            try {
                //Since the data is in bytes (0x__) and each pixel is three hex values (0x___)
                //It is necessary to split a byte such that (0x11)(0x22)(0x33) --> (0x112)(0x233)
                byte[] frame = frameQ.take();
                int frameIndex = 0;
                for (int i = 1; i < 14399; i+=3) {
                    int a = frame[i]&0x0ff;
                    int b = frame[i + 1]&0x0ff;
                    int c = frame[i + 2]&0x0ff;
//                    System.out.println(a+": "+Integer.toBinaryString(a));
//                    System.out.println(b+": "+Integer.toBinaryString(b));
//                    System.out.println(c+": "+Integer.toBinaryString(c));

                    int first = a;
                    int second = b;
                    first <<= 4;
                    first = first | b >>> 4;
                    second = second & 0x0f;
                    second <<= 8;
                    second = second| c;

//                    if(first > 4081 || second > 4081){
//                        System.out.println(""+a+"| "+b+"| "+c+ " |" + first+ " |"+ second +"| "+ Integer.toBinaryString(first)+"| "+Integer.toBinaryString(second));
//                    }
                    hapticInt[frameIndex] = first;
                    frameInt[frameIndex++] = makeColor(first);
                    hapticInt[frameIndex] = second;
                    frameInt[frameIndex++] = makeColor(second);
                }
                hapticQ.put(hapticInt.clone());
                colorQ.put(frameInt.clone());
                Bitmap oldBitmap = newBitmap;
                newBitmap = bitmapQ.take();
                //newBitmap = Bitmap.createBitmap(frameInt, 160, 60, Bitmap.Config.ARGB_8888);
                runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                bitmapImageView.setImageBitmap(newBitmap);
                            }
                        }
                );
                oldBitmap.recycle();
                System.gc();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static int makeColor(int value) {
        int rValue = (value+1)/4;
        int gValue = (value+1)/4;
        int bValue = (value+1)/4;
        //mvalue = 128;
        //return Color.argb(255,0, 0 ,0);
        return Color.argb(255, rValue, gValue, bValue);
    }

    private void runOnUiThread(Runnable r) {
        handler.post(r);
    }

    public static ArrayBlockingQueue<Bitmap> getBitmapQ() {
        return bitmapQ;
    }

    public static ArrayBlockingQueue<int[]> getColorQ() {
        return colorQ;
    }

    public static ArrayBlockingQueue<int[]> getHapticQ() {
        return hapticQ;
    }

}
