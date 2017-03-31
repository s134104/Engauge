package group32.dtu.engauge;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import group32.dtu.engauge.model.BrakingDataPoint;
import group32.dtu.engauge.model.TrainingSession;

import static group32.dtu.engauge.R.id.map;

public class AnalyzeMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final String TAG = "ANALYZE MAPS";

    private ArrayList<TrainingSession> sessions;

    private Context context;
    private Polyline sectionLine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        context = this.getApplicationContext();

        String sessionsString = getIntent().getStringExtra("sessionsString");
        sessions = new Gson().fromJson(sessionsString, new TypeToken<ArrayList<TrainingSession>>(){}.getType());

        for (TrainingSession session : sessions){
            Log.d(TAG, "FOUND SESSION " + session.getSessionName() + " with start " + Long.toString(session.getStartTimestamp()));
        }
    }

    private class ChangeColorListener implements GoogleMap.OnPolylineClickListener {
        @Override
        public void onPolylineClick(Polyline polyline) {

            /*
            if (polyline.getColor() == ContextCompat.getColor(context, R.color.red_inactive)){
                polyline.setColor(ContextCompat.getColor(context, R.color.red_active));
            } else if (polyline.getColor() == ContextCompat.getColor(context, R.color.red_active)){
                polyline.setColor(ContextCompat.getColor(context, R.color.red_inactive));
            } else if (polyline.getColor() == ContextCompat.getColor(context, R.color.blue_inactive)){
                polyline.setColor(ContextCompat.getColor(context, R.color.blue_active));
            } else if (polyline.getColor() == ContextCompat.getColor(context, R.color.blue_active)){
                polyline.setColor(ContextCompat.getColor(context, R.color.blue_inactive));
            }
            */

            if (sectionLine != null){
                sectionLine.remove();
            }

            Location zoomLoc = sessions.get(0).getLocations().get(0);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(zoomLoc.getLatitude(),zoomLoc.getLongitude()) , 17.0f) );

            TextView analyzeStats1 = (TextView) findViewById(R.id.analyzeStats1);
            setStatsTextView(sessions.get(0), analyzeStats1);

            TextView analyzeStats2 = (TextView) findViewById(R.id.analyzeStats1);
            setStatsTextView(sessions.get(1), analyzeStats2);

            //displayTwoSessionsConsolidated(sessions.get(0), sessions.get(1));

        }
    }

    private class CompareBrakingListener implements GoogleMap.OnMapLongClickListener{
        @Override
        public void onMapLongClick(LatLng latLng) {
            if (sectionLine != null){
                sectionLine.remove();
            }

            Log.d(TAG, "LONG CLICK DETECTED");
            Log.d(TAG, "sess 1 loc size: " + sessions.get(0).getLocations().size());
            Log.d(TAG, "sess 2 loc size: " + sessions.get(1).getLocations().size());

            float best = Float.MAX_VALUE;
            Location bestLoc = new Location("Collected");

            Location clickLoc = new Location("Mapclick");
            clickLoc.setLatitude(latLng.latitude);
            clickLoc.setLongitude(latLng.longitude);
            for (int i = 0; i < sessions.get(0).getLocations().size(); i++){
                Location curLoc = sessions.get(0).getLocations().get(i);
                best = curLoc.distanceTo(clickLoc) <= best ?  curLoc.distanceTo(clickLoc) : best;
                bestLoc = curLoc.distanceTo(clickLoc) <= best ? curLoc : bestLoc;
            }

            Log.d(TAG, "Click location  " + clickLoc.getLatitude() + ", " + clickLoc.getLongitude());
            Log.d(TAG, "Reference point " + bestLoc.getLatitude() + ", " + bestLoc.getLongitude());

            redisplayWithRespectToPoint(bestLoc);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(bestLoc.getLatitude(), bestLoc.getLongitude()), 20));

            // redraw route with respect to bestLoc
            // put braking info

        }
    }

    private void redisplayWithRespectToPoint(Location referencePoint){
        PolylineOptions options = new PolylineOptions();

        long start1 = Long.MAX_VALUE;
        long stop1 = Long.MIN_VALUE;

        long start2 = Long.MAX_VALUE;
        long stop2 = Long.MIN_VALUE;

        for (Location loc : sessions.get(0).getLocations()){
            // get earliest and latest timestamps
            if (loc.distanceTo(referencePoint) <= 10.0){
                options.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
                if (loc.getTime() <= start1){
                    start1 = loc.getTime();
                }
                if (loc.getTime() >= stop1){
                    stop1 = loc.getTime();
                }
            }
        }

        options.width(15);
        options.color(ContextCompat.getColor(context, R.color.red_active));
        sectionLine = mMap.addPolyline(options);


        for (Location loc : sessions.get(1).getLocations()){
            if (loc.distanceTo(referencePoint) <= 10.0){
                if (loc.getTime() <= start2){
                    start2 = loc.getTime();
                }
                if (loc.getTime() >= stop2){
                    stop2 = loc.getTime();
                }
            }
        }

        List<BrakingDataPoint> brakings1 = sessions.get(0).getBrakingPoints();
        int brakingSum1 = 0;
        for (BrakingDataPoint brakingPoint : brakings1){
            if (brakingPoint.getTimeStamp() <= stop1 && brakingPoint.getTimeStamp() >= start1){
                brakingSum1 += brakingPoint.getBraking();
            }
        }

        List<BrakingDataPoint> brakings2 = sessions.get(1).getBrakingPoints();
        int brakingSum2 = 0;
        for (BrakingDataPoint brakingPoint : brakings2){
            if (brakingPoint.getTimeStamp() <= stop2 && brakingPoint.getTimeStamp() >= start2){
                brakingSum2 += brakingPoint.getBraking();
            }
        }

        long s1 = (stop1 - start1) / 1000;
        long s2 = (stop2 - start2) / 1000;

        TextView analyzeStats1 = (TextView) findViewById(R.id.analyzeStats1);
        analyzeStats1.setText("1 time: " + s1 + "s \n" + "1 braking : " + brakingSum1);

        TextView analyzeStats2 = (TextView) findViewById(R.id.analyzeStats2);
        analyzeStats2.setText("2 time: " + s2 + "s \n" + "2 braking : " + brakingSum1);

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnPolylineClickListener(new ChangeColorListener());

        mMap.setOnMapLongClickListener(new CompareBrakingListener());
        if (sessions.size() >= 2){
            TrainingSession firstSession = sessions.get(0);
            TrainingSession secondSession = sessions.get(1);
            Location zoomLoc = firstSession.getLocations().get(0);
            mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(zoomLoc.getLatitude(),zoomLoc.getLongitude()) , 17.0f) );

            //displayTwoSessionsSeparate(firstSession, secondSession);

            // if sessions have similar starting location
            displayTwoSessionsConsolidated(firstSession, secondSession);
        }
    }

    private void displayTwoSessionsConsolidated(TrainingSession firstSession, TrainingSession secondSession){
        // TODO - draw some consolidated view of the route instead of just choosing one
        drawRouteLine(firstSession, ContextCompat.getColor(context, R.color.blue_inactive));


        TextView analyzeStats1 = (TextView) findViewById(R.id.analyzeStats1);
        setStatsTextView(firstSession, analyzeStats1);
        TextView analyzeStats2 = (TextView) findViewById(R.id.analyzeStats2);
        setStatsTextView(secondSession, analyzeStats2);

    }

    private void displayTwoSessionsSeparate(TrainingSession firstSession, TrainingSession secondSession){
        // TODO clear map

        TextView analyzeStats1 = (TextView) findViewById(R.id.analyzeStats1);
        setStatsTextView(firstSession, analyzeStats1);
        drawRouteLine(firstSession, ContextCompat.getColor(context, R.color.blue_inactive));
        drawRouteDots(firstSession, ContextCompat.getColor(context, R.color.blue_inactive));

        TextView analyzeStats2 = (TextView) findViewById(R.id.analyzeStats2);
        setStatsTextView(secondSession, analyzeStats2);
        drawRouteLine(secondSession, ContextCompat.getColor(context, R.color.red_inactive));
        drawRouteDots(secondSession, ContextCompat.getColor(context, R.color.red_inactive));
    }

    private void setStatsTextView(TrainingSession session, TextView statsView){
        statsView.setText(getDisplayableStatsString(session));
    }

    private void drawRouteDots(TrainingSession session, int color){

        for (Location loc : session.getLocations()){
            CircleOptions circleOpts = new CircleOptions()
                    .center(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .radius(0.1)
                    .strokeColor(color)
                    .fillColor(color);
            mMap.addCircle(circleOpts);
        }
    }

    private void drawRouteLine(TrainingSession session, int color){
        PolylineOptions opt = getPolylineOptions(session);
        opt.width(20);
        opt.color(color);


        Polyline polyline = mMap.addPolyline(opt);
        polyline.setClickable(true);


    }

    private PolylineOptions getPolylineOptions(TrainingSession session){
        PolylineOptions options = new PolylineOptions();

        for (Location loc : session.getLocations()){
            options.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
            //Log.d(TAG, "ADDING LOCATION " + loc);
        }
        return  options;
    }

    private String getDisplayableStatsString(TrainingSession session){
        StringBuilder statsBuilder = new StringBuilder();
        statsBuilder.append("Name: " + session.getSessionName() + "\n");
        statsBuilder.append("Duration " + Long.toString(session.getDuration() / 1000) + "s \n");
        List<BrakingDataPoint> brakings = session.getBrakingPoints();
        int brakingSum = 0;
        for (BrakingDataPoint brakingPoint : brakings){
            brakingSum += brakingPoint.getBraking();
        }
        statsBuilder.append("Braking total: " + Integer.toString(brakingSum) + "\n");
        return statsBuilder.toString();
    }
}
