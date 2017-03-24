package group32.dtu.engauge.model;

import java.io.Serializable;

/**
 * Created by oskar on 17.03.17.
 */

public class BrakingDataPoint implements Serializable{
    long timeStamp;
    int braking;

    public BrakingDataPoint(long timeStamp, int braking){
        this.timeStamp = timeStamp;
        this.braking = braking;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getBraking() {
        return braking;
    }

    public void setBraking(int braking) {
        this.braking = braking;
    }
}
