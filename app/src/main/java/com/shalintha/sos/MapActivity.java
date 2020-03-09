package com.shalintha.sos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



/**
 *
 *
 *
 *
 * Intent intent = getIntent();
 *         String location_long = intent.getStringExtra("long");
 *         String location_lat = intent.getStringExtra("lat");
 * An activity that displays a Google map with a marker (pin) to indicate a particular location.
 */
public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    double loc_long;
    double loc_lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        String location_long = intent.getStringExtra("long");
        String location_lat = intent.getStringExtra("lat");
        loc_long = Double.parseDouble(location_long);
        loc_lat = Double.parseDouble(location_lat);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=1500&type=restaurant&keyword=cruise&key=AIzaSyBV1O_pdaq984Lqjf8DAe-fdpn8egV8Dtw


    }

    /**
     * Manipulates the map when it's available.
     * The API invokes this callback when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user receives a prompt to install
     * Play services inside the SupportMapFragment. The API invokes this method after the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        LatLng sydney = new LatLng(loc_lat, loc_long);

        googleMap.addMarker(new MarkerOptions().position(sydney)
                .title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc_lat, loc_long), 10.0f));
        Toast.makeText(getApplicationContext(),loc_lat+" , " +loc_long,Toast.LENGTH_LONG).show();


    }
}
