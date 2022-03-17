package lidar.LidarModule;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.widget.ImageView;
import com.example.insight.MainActivity;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

public class BitmapGenerator{
    private Handler uiHandler;
    private ImageView bitmapImageView;

    public BitmapGenerator(){
        this.bitmapImageView = MainActivity.getBitmapImageView();
        initializeHandlers();

    }

    private void initializeHandlers() {
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public void generateBitmap(int[] serialOut, int[] frameInt){
        try {

            Bitmap bitmap_ = Bitmap.createBitmap(frameInt, 160, 60, Bitmap.Config.ARGB_8888);
            Bitmap bitmap = bitmap_.copy(Bitmap.Config.ARGB_8888, true);
            bitmap_.recycle();
            System.gc();
            //TODO: change to peek
            try {
                int[] data = new int[20];
                data = serialOut;
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
            setBitmapOnUIThread(bitmap);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setBitmapOnUIThread(Bitmap newBitmap) {
        uiHandler.post(() -> bitmapImageView.setImageBitmap(newBitmap));
    }

}
