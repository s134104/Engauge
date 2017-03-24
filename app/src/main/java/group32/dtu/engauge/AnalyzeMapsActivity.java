package group32.dtu.engauge;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        String sessionsString = getIntent().getStringExtra("sessionsString");
        sessions = new Gson().fromJson(sessionsString, new TypeToken<ArrayList<TrainingSession>>(){}.getType());

        for (TrainingSession session : sessions){
            Log.d(TAG, "FOUND SESSION " + session.getSessionName() + " with start " + Long.toString(session.getStartTimestamp()));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        TrainingSession firstSession = sessions.get(0);
        Location zoomLoc = firstSession.getLocations().get(0);
        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(zoomLoc.getLatitude(),zoomLoc.getLongitude()) , 14.0f) );

        TextView analyzeStats1 = (TextView) findViewById(R.id.analyzeStats1);
        displayStats(firstSession, analyzeStats1);
        drawRoute(firstSession, Color.BLUE);

        if (sessions.size() >= 2){
            TrainingSession secondSession = sessions.get(1);
            TextView analyzeStats2 = (TextView) findViewById(R.id.analyzeStats2);
            displayStats(secondSession, analyzeStats2);
            drawRoute(secondSession, Color.RED);
        }
    }

    private void displayStats(TrainingSession session, TextView statsView){
        statsView.setText(getDisplayableStatsString(session));
    }

    private void drawRoute(TrainingSession session, int color){
        PolylineOptions opt1 = getPolylineOptions(session);
        opt1.width(10);
        opt1.color(color);
        mMap.addPolyline(opt1);
    }

    private PolylineOptions getPolylineOptions(TrainingSession session){
        PolylineOptions options = new PolylineOptions();
        for (Location loc : session.getLocations()){
            options.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
            Log.d(TAG, "ADDING LOCATION " + loc);
        }
        return  options;
    }

    private String getDisplayableStatsString(TrainingSession session){
        StringBuilder statsBuilder = new StringBuilder();
        statsBuilder.append("Name: " + session.getSessionName() + "\n");
        statsBuilder.append("Duration " + Long.toString(session.getDuration()) + "\n");
        List<BrakingDataPoint> brakings = session.getBrakingPoints();
        int brakingSum = 0;
        for (BrakingDataPoint brakingPoint : brakings){
            brakingSum += brakingPoint.getBraking();
        }
        statsBuilder.append("Braking total: " + Integer.toString(brakingSum) + "\n");
        return statsBuilder.toString();
    }
}
