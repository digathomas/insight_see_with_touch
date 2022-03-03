package lidar.LidarModule;

import java.util.concurrent.Callable;

public class SectorMax implements Callable<Integer> {
    private int left, right, top, bottom;
    private int[] frame;
    private static final int FRAME_WIDTH = 160;

    public SectorMax(int left, int right, int top, int bottom, int[]frame){
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.frame = frame;
        //System.out.println("L: " + left + " R: " + right + " T: " + top + " B: " + bottom);
    }

    @Override
    public Integer call() throws Exception {
        int max = 5000;
        for (int i = top; i < bottom; i+=2){
            for(int j = left + i*FRAME_WIDTH; j < right+i*FRAME_WIDTH; j+=2){
                max = Math.min(max, frame[j]);
            }
        }
        return max;
    }
}
