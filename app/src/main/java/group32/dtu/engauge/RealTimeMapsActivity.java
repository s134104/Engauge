package group32.dtu.engauge;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import group32.dtu.engauge.bluetooth.BrakingDataBluetoothService;
import group32.dtu.engauge.model.BrakingDataPoint;
import group32.dtu.engauge.model.TrainingSession;
import group32.dtu.engauge.persistence.StorageUtils;

import static group32.dtu.engauge.R.id.map;

public class RealTimeMapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, ConnectionCallbacks, OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = "MAPS ACTIVITY";
    private LocationRequest mLocationRequest;
    private List<Location> locations;
    private final int[] cols = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};
    private Context context;
    private Random random;

    private Location curLoc;
    private Button sessionButton;
    private Button previousButton;
    private boolean sessionActive;
    private TrainingSession currentSession;
    private MarkerOptions curLocMarkerOptions;
    private Marker curLocMarker;

    private TextView realtimeStatsArea;
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d(TAG, "MAPSACTIVITY CREATED");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        random = new Random();
        context = this.getApplicationContext();
        sessionActive = false;
        sessionButton = (Button)findViewById(R.id.sessionButton);
        previousButton = (Button)findViewById(R.id.previousButton);
        previousButton.setOnClickListener(new previousButtonListener());
        realtimeStatsArea = (TextView) findViewById(R.id.realtimeStatsArea);


        mapFragment.getMapAsync(this);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(200)
                .setFastestInterval(200)
                .setSmallestDisplacement((float) 1.0)
                .setMaxWaitTime(200);

        /*
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

        } else {
            //TODO
        }
        */


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

        Log.d(TAG, "GOOGLE MAP READY");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "***REQUESTING PERMISSIONS");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 108);
        } else {
            Log.i(TAG, "***PERMISSIONS ALREADY GRANTED");
            connectToGoogleApi();
        }
    }

    private void connectToGoogleApi(){
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "LOCATION SERVICES CONNECTED");
        try{

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            curLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            curLocMarkerOptions = new MarkerOptions().position(new LatLng(curLoc.getLatitude(), curLoc.getLongitude())).title("You are here");
            curLocMarker = mMap.addMarker(curLocMarkerOptions);

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(curLoc.getLatitude(), curLoc.getLongitude()), 17));

            //initBluetooth();
            initMockBluetooth();

            sessionButton.setOnClickListener(new sessionButtonListener());
        }
        catch (SecurityException e){
            Log.e(TAG, "GOT SECURITY EXCEPTION", e);
        }
    }

    private class previousButtonListener implements  Button.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RealTimeMapsActivity.this, SessionsActivity.class);
            startActivity(intent);
        }
    }

    private class sessionButtonListener implements Button.OnClickListener{
        public void onClick(View v) {
            if (sessionButton.getText().equals(getString(R.string.start_session))){
                sessionButton.setText(R.string.stop_session);
                Log.d(TAG, "SESSION STARTED");

                sessionActive = true;
                currentSession = new TrainingSession("some_session", System.currentTimeMillis());
                currentSession.addLocation(curLoc);

                Toast.makeText(context, "Session started", Toast.LENGTH_SHORT).show();

            } else {
                sessionActive = false;
                stopAndStoreSession();
                sessionButton.setText(R.string.start_session);
                Log.d(TAG, "SESSION STOPPED");
                Toast.makeText(context, "Session stopped", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Fragment activitiy started or resumed");
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 108){
            Log.d(TAG, "FINE LOCATION PERMISSIONS GRANTED");
            connectToGoogleApi();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if (sessionActive){
            drawLineToNewLocation(location);
        }


        curLocMarker.remove();
        curLocMarkerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
        curLocMarker = mMap.addMarker(curLocMarkerOptions);

        curLoc = location;
        //Log.i(TAG, location.toString());

        if (sessionActive){
            currentSession.addLocation(curLoc);
        }
        //Toast.makeText(context, location.toString(), Toast.LENGTH_SHORT).show();

        /*
        if (locations == null){
            locations = new ArrayList<>();
            Log.i(TAG, "LOCATIONS NULL");

        }
        locations.add(location);
        drawPrimaryLinePath(location);
        */
    }

    private void drawLineToNewLocation(Location newLocation)
    {
        int idx = new Random().nextInt(cols.length);
        PolylineOptions options = new PolylineOptions()
                .width(10)
                .color(Color.BLUE)
                .add(new LatLng(curLoc.getLatitude(), curLoc.getLongitude()))
                .add(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));

        mMap.addPolyline(options);
    }

    private void drawBrakingDot(Integer brakingPower){

        int intCol = Color.argb(255, 255, 0, 0);

        Toast.makeText(context, brakingPower.toString(), Toast.LENGTH_SHORT).show();

        Log.d(TAG, "BRAKING POWER " + brakingPower);
        if (brakingPower >= 1){
            intCol = Color.argb(255, 0, 0, 0);
        }
        Double alpha = new Double(((double)(brakingPower + 2)/12)  * 255);
        //Log.d(TAG, "COLOR ALPHA " + (double)(breakingPower + 2)/12);
        int a = alpha.intValue();

        if (curLoc != null){
            CircleOptions circleOpts = new CircleOptions()
                    .center(new LatLng(curLoc.getLatitude(), curLoc.getLongitude()))
                    .radius(0.1)
                    .strokeColor(intCol)
                    .fillColor(intCol);
            mMap.addCircle(circleOpts);
        }
    }

    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            String message = (String) msg.obj;
            if (sessionActive){
                currentSession.addBrakingDataPoint(new BrakingDataPoint(System.currentTimeMillis(), Integer.parseInt(message)));
                realtimeStatsArea.setText("Braking: " + message);
            }

            //drawBrakingDot(Integer.parseInt(message));

        }
    };

    private void initMockBluetooth(){
        BrakingDataBluetoothService brakingDataService = new BrakingDataBluetoothService();
        brakingDataService.startMockDataService(messageHandler);
    }

    private void initBluetooth() {
        Log.d(TAG, "Initiating bluetooth");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Log.d(TAG, pairedDevices.toString());
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                Log.d(TAG, "Found device - " + deviceName);
                String deviceHardwareAddress = device.getAddress();
                if (deviceName.equals("H-C-2010-06-01" +
                        "")){
                    connectToDeviceAndStartReceivingData(deviceHardwareAddress);
                    break;
                }
            }
        }
    }

    private void connectToDeviceAndStartReceivingData(String deviceAddress){
        Log.d(TAG, "Trying to connect to HC-05 using bluetooth");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice brakingMeter = bluetoothAdapter.getRemoteDevice(deviceAddress);
        try{
            BluetoothSocket btSocket = (BluetoothSocket) brakingMeter.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(brakingMeter,1);


            btSocket.connect();
            BrakingDataBluetoothService brakingDataService = new BrakingDataBluetoothService();
            brakingDataService.startDataService(btSocket, messageHandler);
            Log.d(TAG, "CONNECTION TYPE: " + btSocket.getConnectionType());

            Log.d(TAG, "DONE WITH BLUETOOTH IN MAIN");
        } catch (Exception e){
            Log.e(TAG, "Exception establishing bluetooth connection", e);
        }
    }

    public void stopAndStoreSession(){
        currentSession.stopSession();
        StorageUtils.persistSessionToFile(context, currentSession);

        new PostSessionTask().execute(currentSession);

    }


    private class PostSessionTask extends AsyncTask<TrainingSession, Void,  HttpResponse> {

        @Override
        protected HttpResponse doInBackground(TrainingSession... sessions){
            Gson gson = new Gson();
            String jsonString = gson.toJson(sessions[0]);

            HttpClient client = HttpClientBuilder.create().build();
            StringEntity requestEntity = new StringEntity(
                    jsonString,
                    ContentType.APPLICATION_JSON);

            HttpPost post = new HttpPost("http://engauge-server.herokuapp.com/api/sessions");
            post.setEntity(requestEntity);

            try{
                return client.execute(post);
            } catch (IOException e){
                Log.e(TAG, "Expcetion while POSTing", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(HttpResponse o) {
            Log.d(TAG, "POST session - " + o);
        }

    }

}
