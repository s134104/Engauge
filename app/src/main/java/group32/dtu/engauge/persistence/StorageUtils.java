package group32.dtu.engauge.persistence;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
        OutputStreamWriter osw = getOutPutStreamWriter(context, fileName);
        Gson gson = new Gson();
        String sessionString = gson.toJson(session);
        try{
            osw.write(sessionString);
            Log.d(TAG, "Persisted to file");
            osw.close();

        }catch (IOException e){
            Log.e(TAG, "Exception while writing to file", e);
        }
    }

    private static OutputStreamWriter getOutPutStreamWriter(Context context, String fileName){
        FileOutputStream fos;
        OutputStreamWriter osw = null;
        try{
            fos = context.openFileOutput(fileName, Context.MODE_APPEND);
            osw = new OutputStreamWriter(fos);
        } catch (IOException e){
            Log.d(TAG, "Exception creating stream", e);
        }
        return osw;
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
        Gson gson = new Gson();
        for (File sessionFile : files) {
            String fileName = sessionFile.getName();
            Log.d(TAG, "Trying to read file " + fileName);
            FileInputStream fis;
            TrainingSession session;
            String readStr = "";
            String line = null;
            try{
                fis = context.openFileInput(fileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                while ((line = br.readLine()) != null) {
                    readStr += line;
                }
            }
            catch (FileNotFoundException e) {
                Log.e(TAG, "FILE NOT FOUND EXCEPTION", e);
            } catch (IOException e) {
                Log.e(TAG, "IO EXCEPTION", e);
            }
            Log.d(TAG, "TRYING TO CONVERT");
            session  = gson.fromJson(readStr.toString(), TrainingSession.class);
            trainingSessions.add(session);
        }
        return trainingSessions;
    }
}
