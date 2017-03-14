package group32.dtu.engauge;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import group32.dtu.engauge.group32.dtu.engauge.bluetooth.BrakingDataBluetoothService;

import static group32.dtu.engauge.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, ConnectionCallbacks, OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = "MAPS ACTIVITY";
    private LocationRequest mLocationRequest;
    private List<Location> locations;
    private final int[] cols = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};
    private Context context;
    private Location location;
    private Random random;

    private Location curLoc;

    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        // Manifest.permission.READ_PHONE_STATE
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 108);

        random = new Random();
        context = this.getApplicationContext();

        /*
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

        } else {
            //TODO
        }
        */
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
                .setInterval(200)
                .setFastestInterval(200)
                .setSmallestDisplacement((float) 0.0)
                .setMaxWaitTime(200);
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

            initBluetooth();

            //initMockBluetooth();
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //mMap.moveCamera();

        curLoc = location;
        Log.i(TAG, location.toString());


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

    private void drawPrimaryLinePath(Location location)
    {
        int idx = new Random().nextInt(cols.length);
        PolylineOptions options = new PolylineOptions()
                .width(10)
                .color(Color.BLUE)
                .add(new LatLng(this.location.getLatitude(), this.location.getLongitude()))
                .add(new LatLng(location.getLatitude(), location.getLongitude()));

        mMap.addPolyline(options);
        this.location = location;

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

    private void putBrakingPin(Integer brakingPower){

    }


    private Handler messageHandler = new Handler() {
        public void handleMessage(Message msg) {
            String message = (String) msg.obj;


            //Log.d(TAG, msg.toString());
            drawBrakingDot(Integer.parseInt(message));

            //Log.d(TAG, msg.toString());
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
                if (deviceName.equals("HC-05\\r\\n")){
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
}
