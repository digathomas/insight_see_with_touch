package lidar.LidarModule;

import android.graphics.Bitmap;

import java.util.concurrent.ArrayBlockingQueue;

public class BitmapGenerator implements Runnable{
    private static ArrayBlockingQueue<Bitmap> bitmapQ;
    private static ArrayBlockingQueue<int[]> colorQ;

    public BitmapGenerator(){
        BitmapGenerator.bitmapQ = LidarRenderer.getBitmapQ();
        BitmapGenerator.colorQ = LidarRenderer.getColorQ();
    }

    @Override
    public void run() {
        while(true){
            try {
                bitmapQ.put(Bitmap.createBitmap(colorQ.take(), 160, 60, Bitmap.Config.ARGB_8888));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
