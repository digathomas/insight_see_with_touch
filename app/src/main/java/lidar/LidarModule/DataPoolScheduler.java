package lidar.LidarModule;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import com.example.insight.BTSerial.BLE;
import com.example.insight.MainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class DataPoolScheduler{
    private Handler handler;
    private BLE ble;
    private int horizontalSectors = 5;
    private int verticalSectors = 2;
    private final static int FRAME_WIDTH = 160;
    private final static int FRAME_HEIGHT = 60;
    private BitmapGenerator bitmapGenerator;

    public DataPoolScheduler() {
        bitmapGenerator = new BitmapGenerator();
        this.ble = MainActivity.getBle();
        initializeHandlers();
    }

    private void initializeHandlers() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void sectorGenerator(int[] hapticInt, int[] frameInt){
        try{
            List<SectorMax> calls = new ArrayList<>();
            List<Integer> hapticOut = new ArrayList<>();
            int sectorWidth = FRAME_WIDTH/horizontalSectors;
            int sectorHeight = FRAME_HEIGHT/verticalSectors;
            for (int i = 0; i < horizontalSectors; i++) {
                for (int j = 0; j < verticalSectors; j++) {
                    int left = i * sectorWidth;
                    int right = left + sectorWidth;
                    int top = j * sectorHeight;
                    int bottom = top + sectorHeight;
                    hapticOut.add(new SectorMax(left, right, top, bottom, hapticInt).call());
                }
            }
            System.out.println(hapticOut);
            int[] serialOut = new int[20];
            Arrays.fill(serialOut, 0);
            int priority = -1;
            for (int i = 0; i < 2*horizontalSectors; i++) {
                int value = hapticOut.get(i) / 128;
                if (value > priority) priority = value;
                serialOut[i] = value;
            }
            //Goes to bitmapGenerator and to ble
            lidarToBle(serialOut);
            if (MainActivity.lidarUiState) bitmapGenerator.generateBitmap(serialOut,frameInt);
        }catch (Exception e) {
            Log.e("DataPool", e.getMessage());
        }
    }

    private void lidarToBle(int[] serialOut){
        handler.post(() ->{
            ble.writeToGatt(BLE.LEFT_GATT,serialOut);
        });
    }


}
