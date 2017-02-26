package group11.dtu.engauge;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        boolean flag = false;

        Random rand = new Random();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        double startXPos = 55.787245;
        double startYPos = 12.522783;

        for(int i = 0; i <= 10; i++) {
            double randPosition = rand.nextDouble() / 10000;
            Log.i("MapsActivity", "Random: " + randPosition);
            double oldX = startXPos;
            double oldY = startYPos;
            if(i >= 5 && randPosition < 5.5E-5) {
                flag = !flag;
            }

            if(flag == false) {
                startXPos += randPosition;
                startYPos -= randPosition;
            } else {
                startXPos -= randPosition;
                startYPos -= randPosition;
            }

            LatLng pos = new LatLng(startXPos, startYPos);

            if(randPosition < 6.0E-5) {
                //IconGenerator iconFactory = new IconGenerator(this);
                //Marker mMarker = mMap.addMarker(new MarkerOptions().position(pos));
                //mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("Braking: " + i)));
                mMap.addMarker(new MarkerOptions().position(pos).title("Braking score:" + i)).showInfoWindow();
                PolylineOptions polylineOptions = new PolylineOptions().add(new LatLng(oldX, oldY)).add(new LatLng(startXPos, startYPos)).color(Color.RED);
                Polyline polyline = mMap.addPolyline(polylineOptions);
            } else {
                PolylineOptions polylineOptions = new PolylineOptions().add(new LatLng(oldX, oldY)).add(new LatLng(startXPos, startYPos)).color(Color.GREEN);
                Polyline polyline = mMap.addPolyline(polylineOptions);
            }

            builder.include(pos);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 30));
        }

        final LatLngBounds bounds = builder.build();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 30));
            }
        });

    }

}
