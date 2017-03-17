package group32.dtu.engauge.persistence;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import group32.dtu.engauge.model.TrainingSession;

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

    public static void persistSessionToFile(Context context, TrainingSession session){
        FileOutputStream fos = null;
        try{
            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(session);

        }catch (IOException e){
            Log.e(TAG, "Exception while writing to file", e);
        }
    }

    public static void readSessionsFromFile(Context context){
        FileInputStream fis = null;
        try{
            fis = context.openFileInput(FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);

            TrainingSession model = (TrainingSession) ois.readObject();
            Log.d(TAG, "READ OBJECT");


        }
        catch (IOException | ClassNotFoundException e ){
            Log.e(TAG, "Exception while retrieving from file", e);
        }

    }
}
