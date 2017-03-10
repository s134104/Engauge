package group32.dtu.engauge;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import group32.dtu.engauge.group32.dtu.engauge.bluetooth.BluetoothUtils;

import static group32.dtu.engauge.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, ConnectionCallbacks, OnConnectionFailedListener{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private LocationRequest mLocationRequest;

    private List<Location> locations;

    private final int[] cols = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};

    private Location location;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 108);

        /*
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "***REQUESTING PERMISSIONS");



        } else {
            Log.i(TAG, "***PERMISSIONS ALREADY GRANTED");
        }
        */

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000) // 1 second, in milliseconds
                .setSmallestDisplacement(0);


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "LOCATION SERVICES SUSPENDED");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "LOCATION SERVICES CONNECTION FAILED, PLEASE RECONNECT");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "LOCATION SERVICES CONNECTED");
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17));

        }
        catch (SecurityException e){
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        Log.i(TAG, "LOCATION SERVICES TRYING RECONNECT");
    }


    @Override
    protected void onPause() {
        super.onPause();
        //if (mGoogleApiClient.isConnected()) {
         //   LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
          //  mGoogleApiClient.disconnect();
        //}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 108:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //continueYourTask
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //mMap.moveCamera();


        if (locations == null){
            locations = new ArrayList<>();
            Log.i(TAG, "LOCATIONS NULL");

        }
        Log.i(TAG, "LOCATION CHANGED");
        locations.add(location);
        handleNewLocation(location);
    }


    private void handleNewLocation(Location location) {
        Log.d(TAG, "HANDLING NEW LOCATION");

        drawPrimaryLinePath(location);

    }



    private void drawPrimaryLinePath(Location location)
    {
        int idx = new Random().nextInt(cols.length);
        PolylineOptions options = new PolylineOptions()
                .width(5)
                .color(cols[idx])
                .add(new LatLng(this.location.getLatitude(), this.location.getLongitude()))
                .add(new LatLng(location.getLatitude(), location.getLongitude()));

        mMap.addPolyline(options);
        this.location = location;

    }

    private void initBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }
        BluetoothUtils.doBlue(mBluetoothAdapter);
    }

}
