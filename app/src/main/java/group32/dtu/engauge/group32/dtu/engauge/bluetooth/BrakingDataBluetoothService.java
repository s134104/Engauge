package group32.dtu.engauge.group32.dtu.engauge.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class BrakingDataBluetoothService {
    private static final String TAG = "BRAKING SERVICE";
    private Handler mHandler;

    public void startDataService(BluetoothSocket sock, Handler handler){
        mHandler = handler;
        new ConnectedThread(sock).start();
    }

    public void startMockDataService(Handler handler){
        mHandler = handler;
        TestingThread test = new TestingThread();
        test.start();
    }

    private interface MessageConstants {
        int MESSAGE_READ = 0;
    }

    private class TestingThread extends Thread {
        public void run() {
            int i = 0;
            String messageString;
            Random r = new Random();
            while (true){
                try {
                    Integer braking = (Integer) r.nextInt(11);
                    messageString = braking.toString();

                    Message message = mHandler.obtainMessage(MessageConstants.MESSAGE_READ, messageString);
                    message.sendToTarget();
                    Thread.sleep(200);
                } catch (Exception e) {
                    Log.e(TAG, "Error with testing", e);
                }
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final BufferedReader br;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;

            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (Exception e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            mmInStream = tmpIn;
            br =  new BufferedReader(new InputStreamReader(mmInStream));
        }

        public void run() {
            Log.d(TAG, "MSG FROM BLUETOOTH");
            String messageString;
            /*
            byte[] buf = new byte[256];
            try {
                mmInStream.read(buf);
                Log.d(TAG, new String(buf));
            } catch (IOException e) {
                e.printStackTrace();
            }
            */

            try{
                while ((messageString = br.readLine()) != null) {

                    if (messageString.length() == 1){
                        Message message = mHandler.obtainMessage(MessageConstants.MESSAGE_READ, messageString);
                        message.sendToTarget();
                    }

                    //Log.d(TAG, "MESSAGE FROM DEV " + messageString);
                }
            }catch (IOException e){
                Log.e(TAG, "Error while reading message", e);
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}
