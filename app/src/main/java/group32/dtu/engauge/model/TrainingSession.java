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
    ArrayList<Location> locations;
    ArrayList<BrakingDataPoint> brakingPoints;
    long duration;
    boolean isActiveInView = false;


    public TrainingSession(String sessionName){
        this.sessionName = sessionName;
    };

    public TrainingSession(String sessionName, long startTimestamp){
        this.sessionName = sessionName;
        this.startTimestamp = startTimestamp;
        this.locations = new ArrayList<Location>();
        this.brakingPoints = new ArrayList<BrakingDataPoint>();
    }

    public void addLocation(Location location){
        locations.add(new Location(location));
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

    public long getStartTimestamp(){
        return  startTimestamp;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(long endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;
    }

    public List<BrakingDataPoint> getBrakingPoints() {
        return brakingPoints;
    }

    public void setBrakingPoints(ArrayList<BrakingDataPoint> brakingPoints) {
        this.brakingPoints = brakingPoints;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setActiveInView(boolean activeInView) {
        isActiveInView = activeInView;
    }

}
