package group32.dtu.engauge;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static group32.dtu.engauge.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, ConnectionCallbacks, OnConnectionFailedListener, ActivityCompat.OnRequestPermissionsResultCallback{

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private LocationRequest mLocationRequest;
    private boolean isBtConnected = false;

    public BluetoothDevice dev;

    BluetoothAdapter myBluetooth;
    BluetoothSocket btSocket;
    private List<Location> locations;

    private final int[] cols = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK};
    String address = null;
    private Location location;

    //private UUID uuid;
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

            //initBluetooth();
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

        //uuid = UUID.randomUUID();

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
       // Log.i(TAG, "LOCATION CHANGED");
        locations.add(location);
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        //Log.d(TAG, "HANDLING NEW LOCATION");

        drawPrimaryLinePath(location);

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

    private void initBluetooth(){

        Log.d(TAG, "TRYING BLUETOOTH");
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        Log.d(TAG, pairedDevices.toString());
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            BluetoothSocket tmp = null;
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                Log.d(TAG, "DEVICE NAME - " + deviceName);
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(TAG, "TRUE - " + deviceName.equals("HC-05\\r\\n"));
                if (deviceName.equals("HC-05\\r\\n")){
                    Log.d(TAG, "TRYING BLUETOOTH CONNECT TO HC");
                    address = deviceHardwareAddress;
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    dev = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available

                        //btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(uuid);//create a RFCOMM (SPP) connection
                        //btSocket.connect();
                        //new MyBluetoothService().someStuff(btSocket);

                        //new MyBluetoothService().someStuff(btSocket);




                    new ConnectBT().execute();
                }
            }
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {

        private boolean ConnectSuccess = true; //if it's here, it's almost connected
        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    Log.d(TAG, "TRYING TO CONNECT DEVICE");
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    Log.d(TAG, "GOT DEVICE " + dispositivo.getName());
                    //btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(uuid);//create a RFCOMM (SPP) connection
                    try {
                        btSocket = (BluetoothSocket) dev.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(dev,1);
                        btSocket.connect();//start connection
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    //btSocket = dispositivo.createRfcommSocketToServiceRecord(uuid);
                    Log.d(TAG, "IS CONNECTED - " + btSocket.isConnected());
                    Log.d(TAG, "GOT SOCKET " + btSocket.getConnectionType());
                    //BluetoothAdapter.getDefaultAdapter().cancelDiscovery();


                    Log.d(TAG, "DID CONNECT");
                }
            }
            catch (IOException e)
            {
                Log.d(TAG, "CAUGH IO EX");
                Log.d(TAG, e.getMessage());
                Log.d(TAG, "IO MSG END");
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            Log.d(TAG, "BLUETOOTH CONNECTION DONE");
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                // Is it a SPP Bluetooth? Try again.
                Log.d(TAG, "Connection Failed.");
                finish();
            }
            else {
                Log.d(TAG, "Connected.");
                isBtConnected = true;

                try {
                    InputStream mmInStream = btSocket.getInputStream();

                    Log.d(TAG, "TRYING TO READ");

                    InputStreamReader isr = new InputStreamReader(mmInStream);

                    char[] buffer = new char[28];

                    //byte[] buffer = new byte[256];
                    int bytes;

                    while (true) {
                        try {
                            //bytes = mmInStream.read(buffer);            //read bytes from input buffer
                            //String readMessage = new String(buffer, 0, bytes);

                            int charsRead = isr.read(buffer);

                            // substring(0, charsRead);
                            String sensorMessage = new String(buffer);

                            // Send the obtained bytes to the UI Activity via handler
                            Log.d(TAG, "GOT MESSAGE - " + sensorMessage);
                            //bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                        } catch (Exception e) {
                            Log.d(TAG, "CAUGHT EX IN LOOP");
                            Log.d(TAG, e.getMessage());
                        }
                    }
                } catch (Exception e){
                    Log.d(TAG, e.getMessage());
                }
            }
        }
    }
}
