package group32.dtu.engauge.model;

import android.location.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by oskar on 17.03.17.
 */

public class TrainingSession implements Serializable{

    String sessionName;
    long startTimestamp;
    long endTimeStamp;
    List<Location> locations;
    List<BrakingDataPoint> brakingPoints;
    long duration;

    boolean isActiveInView = false;


    public TrainingSession(String sessionName){
        this.sessionName = sessionName;
    };

    public TrainingSession(String sessionName, long startTimestamp){
        this.sessionName = sessionName;
        this.startTimestamp = startTimestamp;
        this.locations = new ArrayList<>();
        this.brakingPoints = new ArrayList<>();
    }

    public void addLocation(Location location){
        locations.add(location);
    }

    public void addBrakingDataPoint(BrakingDataPoint point){
        brakingPoints.add(point);
    }

    public void stopSession(){
        endTimeStamp = System.currentTimeMillis();
        duration = endTimeStamp - startTimestamp;
    }

    public String toString(){
        return "ToString - Session " + sessionName;
    }

    public String getSessionName(){
        return this.sessionName;
    }

    public void activate(){
        isActiveInView = true;
    }

    public void disable(){
        isActiveInView = false;
    }

    public boolean isActiveInView(){
        return isActiveInView;
    }

}
