package lidar.LidarModule;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import android.os.Process;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class LidarHelper implements SerialInputOutputManager.Listener {
    private static final int BAUD_RATE_3M = 3_000_000;
    private static final int BAUD_RATE_250K = 250_000;
    private static final int TIMEOUT = 1000;
    private static final byte[] GET_DEVICE_INFO = {(byte)0x5A, (byte)0x77, (byte)-1, (byte) 0x02, (byte) 0x0, (byte)0x10, (byte)0x0, (byte)0x12};
    private static final byte[] THREE_D_MODE = {(byte)0x5A, (byte)0x77, (byte)-1, (byte)0x02, (byte) 0x0, (byte) 0x08, (byte) 0x0, (byte) 0x0A};
    private static final byte[] STOP = {(byte)0x5A, (byte)0x77, (byte)-1, (byte)0x02, (byte) 0x0, (byte) 0x02, (byte)0x0, (byte)0x0};
    private static final byte[] SET_BAUD_3M = {(byte)0x5A, (byte)0x77, (byte)-1, (byte)0x02, (byte) 0x0, (byte)0x12, (byte)0x55, (byte)0x97};
    private static final byte[] SET_BAUD_250K = {(byte)0x5A, (byte)0x77, (byte)-1, (byte)0x02, (byte) 0x0, (byte)0x12, (byte)0x77, (byte)0xB5};
    private static ArrayBlockingQueue<byte[]> dataQ;

    private static UsbManager usbManager;
    private static UsbSerialPort port;
    private static UsbDeviceConnection connection;
    private SerialInputOutputManager ioManager;

    public LidarHelper(UsbManager manager) {
        if (LidarHelper.dataQ == null){
            LidarHelper.dataQ = new ArrayBlockingQueue<>(1000);
        }
        if (LidarHelper.usbManager == null) {
            LidarHelper.usbManager = manager;
        }
        connectUsb();
    }

    public LidarHelper LidarHelper() throws Exception {
        return new LidarHelper(null);
    }

    public void connectUsb() {
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (availableDrivers.isEmpty()) {
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());
        if (LidarHelper.port == null) {
            LidarHelper.port = driver.getPorts().get(0);
        }

        if (connection != null) {
            try {
                port.open(connection);
                port.setParameters(BAUD_RATE_3M, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                LidarHelper.connection = connection;
                ioManager = new SerialInputOutputManager(port, this);
                ioManager.setThreadPriority(-19);
                ioManager.setReadBufferSize(500);
                //ioManager.setThreadPriority(19);
                ioManager.start();
            } catch (IOException e) {
                e.printStackTrace();
            };
        }
    }

    public boolean sendInfoRequest() throws IOException {
        if (connection != null) {
            port.write(GET_DEVICE_INFO, TIMEOUT);
            return true;
        }
        else{
            return false;
        }
    }

    public boolean sendSetBaud() throws IOException{
        if (connection != null) {
            port.write(SET_BAUD_3M, TIMEOUT);
            return true;
        }
        else{
            return false;
        }
    }

    public boolean sendStart3D() throws IOException{
        if (connection != null) {
            port.write(THREE_D_MODE, TIMEOUT);
            return true;
        }
        else{
            return false;
        }
    }

    public boolean sendStop() throws IOException{
        if (connection != null) {
            port.write(STOP, TIMEOUT);
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void onNewData(byte[] data) {
        //if (data.length > 0) dataQ.add(data);
        dataQ.add(data);
    }

    @Override
    public void onRunError(Exception e) {

    }

    public static void closePort() throws IOException{
        port.close();
    }

    public static ArrayBlockingQueue<byte[]> getDataQ() {
        return dataQ;
    }
}
