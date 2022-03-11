package lidar.LidarModule;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.example.insight.BTSerial.Scheduler;
import com.example.insight.BTSerial.ThreeTuple;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

public class BitmapGenerator implements Runnable{
    private static ArrayBlockingQueue<Bitmap> bitmapQ;
    private static ArrayBlockingQueue<ThreeTuple<int[]>> liDARQ;
    private static ArrayBlockingQueue<int[]> colorQ;

    public BitmapGenerator(){
        BitmapGenerator.bitmapQ = LidarRenderer.getBitmapQ();
        BitmapGenerator.colorQ = LidarRenderer.getColorQ();
        BitmapGenerator.liDARQ = Scheduler.getliDARQ();
    }

    @Override
    public void run() {
        while(true){
            try {
                Bitmap bitmap_ = Bitmap.createBitmap(colorQ.take(), 160, 60, Bitmap.Config.ARGB_8888);
                Bitmap bitmap = bitmap_.copy(Bitmap.Config.ARGB_8888, true);
                bitmap_.recycle();
                System.gc();
                //TODO: change to peek
                try {
                    int[] data = new int[20];
                    ThreeTuple<int[]> item = liDARQ.peek();
                    if (item != null) {
                        data = item.getData();
                    }
                    Canvas canvas = new Canvas(bitmap);
                    // new antialised Paint
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    // text color - #3D3D3D
                    paint.setColor(Color.rgb(255,0, 0));
                    // text size in pixels
                    paint.setTextSize((int) 8);
                    Rect bounds = new Rect();
                    paint.getTextBounds(Arrays.toString(data), 0, Arrays.toString(data).length(), bounds);
                    for (int i = 1; i < 5; i++){
                        canvas.drawLine(i*32-1,0,i*32-1,59, paint);
                    }
                    canvas.drawLine(0,30,159,30, paint);
                    for (int i = 0; i < 10; i++){
                        if (i%2 == 0){
                            canvas.drawText(String.valueOf(data[i]), ((int)(i/2))*160/5+15, 15, paint);
                        }else{
                            canvas.drawText(String.valueOf(data[i]), ((int)(i/2))*160/5+15, 45, paint);
                        }
                    }
                }catch (Exception ignored){
                    ignored.printStackTrace();
                }

                bitmapQ.put(bitmap);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
