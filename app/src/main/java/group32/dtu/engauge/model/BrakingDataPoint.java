package group32.dtu.engauge.model;

/**
 * Created by oskar on 17.03.17.
 */

public class BrakingDataPoint {
    long timeStamp;
    int braking;

    public BrakingDataPoint(long timeStamp, int braking){
        this.timeStamp = timeStamp;
        this.braking = braking;
    }

}
