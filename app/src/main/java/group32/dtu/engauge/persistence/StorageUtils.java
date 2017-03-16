package group32.dtu.engauge.persistence;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import group32.dtu.engauge.model.TrainingSessionModel;

/**
 * Created by oskar on 16.03.17.
 */

public class StorageUtils {

    private static final String TAG = "STORAGE";
    private static final String FILENAME = "hello_file";
    private static final String FILENAME2 = "hello_obj";
    private StorageUtils(){};

    public static void store(Context context){
        String string = "hello world!";
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
            Log.d(TAG, "WROTE STUFF");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void retrieve(Context context){
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(FILENAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            Log.d(TAG, "READ STUFF");
            Log.d(TAG, br.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void storeObjects(Context context){
        TrainingSessionModel model = new TrainingSessionModel("SOME SESSION OBJ");

    }

    public static void retrieveObjects(Context context){

    }
}
