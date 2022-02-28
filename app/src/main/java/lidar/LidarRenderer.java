package lidar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.widget.ImageView;

import java.util.concurrent.ArrayBlockingQueue;

public class LidarRenderer implements Runnable {
    private static ArrayBlockingQueue<byte[]> frameQ;
    private static int[] frameInt;
    private static int frameIndex;
    private final Handler handler;
    private static ImageView bitmapImageView;

    public LidarRenderer(Context context, ImageView bitmapImageView) {
        if (frameQ == null) LidarRenderer.frameQ = DataHandler.getFrameQ();
        if (frameInt == null) LidarRenderer.frameInt = new int[9600];
        if (LidarRenderer.bitmapImageView == null) LidarRenderer.bitmapImageView = bitmapImageView;
        handler = new Handler(context.getMainLooper());
    }

    @Override
    public void run() {
        while (true) {
            try {
                //Since the data is in bytes (0x__) and each pixel is three hex values (0x___)
                //It is necessary to split a byte such that (0x11)(0x22)(0x33) --> (0x112)(0x233)
                byte[] frame = frameQ.take();
                frameIndex = 0;
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

                    frameInt[frameIndex++] = makeColor(first);
                    frameInt[frameIndex++] = makeColor(second);
                }
                Bitmap bitmap = Bitmap.createBitmap(frameInt, 160, 60, Bitmap.Config.ARGB_8888);
                runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                bitmapImageView.setImageBitmap(bitmap);
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static int makeColor(int value) {
        int mvalue = (value+1)/16;
        //mvalue = 128;
        //return Color.argb(255,100, 0 ,0);
        return Color.argb(255, mvalue, mvalue, mvalue);
    }

    private void runOnUiThread(Runnable r) {
        handler.post(r);
    }
}
