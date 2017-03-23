package group32.dtu.engauge.persistence;

import android.content.Context;
import android.util.Log;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import group32.dtu.engauge.model.TrainingSession;

/**
 * Created by oskar on 16.03.17.
 */

public class StorageUtils {

    private static final String TAG = "STORAGE";
    private StorageUtils(){};

    public static void persistSessionToFile(Context context, TrainingSession session){

        String fileName = "EG-" + Long.toString(session.getStartTimestamp());
        ObjectOutputStream oos = getOutPutStream(context, fileName);

        try{
            oos.writeObject(session);
            Log.d(TAG, "Persisted to file");

        }catch (IOException e){
            Log.e(TAG, "Exception while writing to file", e);
        }
    }

    private static ObjectOutputStream getOutPutStream(Context context, String fileName){
        FileOutputStream fos;
        ObjectOutputStream aoos = null;
        try{
            fos = context.openFileOutput(fileName, Context.MODE_APPEND);
            aoos = new ObjectOutputStream(fos);

        } catch (IOException e){
            Log.d(TAG, "Exception creating stream", e);
        }
        return aoos;
    }

    public static ArrayList<TrainingSession> getTrainingSessions(Context context){

        File dir = new File(context.getFilesDir().getPath());

        Log.d(TAG, "Reading file stored at " + context.getFilesDir().getPath());

        File [] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("EG-");
            }
        });
        ArrayList<TrainingSession> trainingSessions = new ArrayList<>();
        Log.d(TAG, Arrays.deepToString(files));

        for (File sessionFile : files) {
            String fileName = sessionFile.getName();
            Log.d(TAG, "Trying to read file " + fileName);
            FileInputStream fis;
            TrainingSession session;
            try{
                fis = context.openFileInput(fileName);
                ObjectInputStream ois = new ObjectInputStream(fis);
                session = (TrainingSession) ois.readObject();
                trainingSessions.add(session);
            }
            catch (EOFException e){
                Log.d(TAG, "END OF FILE");
            }
            catch (IOException | ClassNotFoundException e ){
                Log.e(TAG, "Exception while retrieving from file", e);
            }
        }
        return trainingSessions;
    }
}
