package lidar.LidarModule;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.example.insight.BTSerial.PriorityModule;
import com.example.insight.BTSerial.ThreeTuple;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

public class BitmapGenerator implements Runnable{
    private static ArrayBlockingQueue<Bitmap> bitmapQ;
    private static ArrayBlockingQueue<ThreeTuple> liDARQ;
    private static ArrayBlockingQueue<int[]> colorQ;

    public BitmapGenerator(){
        BitmapGenerator.bitmapQ = LidarRenderer.getBitmapQ();
        BitmapGenerator.colorQ = LidarRenderer.getColorQ();
        BitmapGenerator.liDARQ = PriorityModule.getliDARQ();
    }

    @Override
    public void run() {
        while(true){
            try {
                Bitmap bitmap = Bitmap.createBitmap(colorQ.take(), 160, 60, Bitmap.Config.ARGB_8888);
                //TODO: change to peek
                try {
                    int[] data = liDARQ.take().getData();
                    Canvas canvas = new Canvas(bitmap);
                    // new antialised Paint
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    // text color - #3D3D3D
                    paint.setColor(Color.rgb(110,110, 110));
                    // text size in pixels
                    paint.setTextSize((int) 12);
                    Rect bounds = new Rect();
                    paint.getTextBounds(Arrays.toString(data), 0, Arrays.toString(data).length(), bounds);
                    int x = (bitmap.getWidth() - bounds.width())/6;
                    int y = (bitmap.getHeight() + bounds.height())/5;

                    canvas.drawText(Arrays.toString(data), x , y , paint);
                }catch (Exception ignored){}

                bitmapQ.put(bitmap);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
