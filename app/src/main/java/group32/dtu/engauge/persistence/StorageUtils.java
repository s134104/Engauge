package group32.dtu.engauge.persistence;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import group32.dtu.engauge.model.TrainingSession;

/**
 * Created by oskar on 16.03.17.
 */

public class StorageUtils {

    private static final String TAG = "STORAGE";
    private static final String FILENAME = "SESSION_OBJECTS";
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
        ObjectOutputStream oos = getOutPutStream(context);
        try{
            oos.writeObject(session);
            Log.d(TAG, "Persisted to file");

        }catch (IOException e){
            Log.e(TAG, "Exception while writing to file", e);
        }
    }

    private static boolean fileExists(Context context) {
        File file = context.getFileStreamPath(FILENAME);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }

    private static ObjectOutputStream getOutPutStream(Context context){
        FileOutputStream fos;
        ObjectOutputStream oos = null;
        try{
            fos = context.openFileOutput(FILENAME, Context.MODE_APPEND);
            if (!fileExists(context)){
                Log.d(TAG, "FILE DOESN'T EXIST");
                return new ObjectOutputStream(fos);
            } else{
                Log.d(TAG, "FILE EXISTS");
                return new AppendingObjectOutputStream(fos);
            }
        } catch (IOException e){
            Log.d(TAG, "Exception creating stream", e);
        }
        return null;
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

    public static ArrayList<TrainingSession> getTrainingSessions(Context context){
        FileInputStream fis;
        ArrayList<TrainingSession> trainingSessions = new ArrayList<>();
        TrainingSession session;
        try{
            fis = context.openFileInput(FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            while(true){
                session = (TrainingSession) ois.readObject();
                trainingSessions.add(session);
            }
        }
        catch (EOFException e){
            Log.d(TAG, "END OF FILE");
        }
        catch (IOException | ClassNotFoundException e ){
            Log.e(TAG, "Exception while retrieving from file", e);
        }
        return trainingSessions;
    }
}
