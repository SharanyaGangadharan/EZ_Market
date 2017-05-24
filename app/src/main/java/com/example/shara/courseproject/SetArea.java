package com.example.shara.courseproject;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.shara.courseproject.R.id.add;
import static com.example.shara.courseproject.R.id.container;

public class SetArea extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener{

    private GoogleMap mMap;
    public Double lat, lng;
    Circle circle, point;
    SeekBar mSeekBar;
    TextView mMiles;
    double radius;
    int miles;
    EditText locationSearch;
    String loc=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_area);

        radius = 40;
        miles = 40;
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        locationSearch = (EditText) findViewById(R.id.search_text);
        mMiles = (TextView) findViewById(R.id.miles);
        mMiles.setText( radius + " miles");

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                miles = progresValue;
               // Toast.makeText(getApplicationContext(), "Changing seekbar's progress", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mMiles.setText(miles + " miles");
                radius = convertMilesToMeters(miles);
                send();
                //Toast.makeText(getApplicationContext(), "Stopped tracking seekbar", Toast.LENGTH_SHORT).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public double convertMilesToMeters(double radius)
    {
        double meters =  (radius * 1609.34);
        return meters;
    }

    public void save(View v)
    {
        Intent intent = getIntent();
        intent.putExtra("lat",lat);
        intent.putExtra("lng",lng);
        intent.putExtra("loc",loc);
        intent.putExtra("miles",miles);
        intent.putExtras(intent);
        setResult(RESULT_OK,intent);
        finish();
    }

    public void cancel(View v)
    {
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
    }

    public void onMapClick(LatLng location) {
        lat = location.latitude;
        lng = location.longitude;
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            String cityName = addresses.get(0).getAddressLine(0);
            String stateName = addresses.get(0).getAddressLine(1);
            String countryName = addresses.get(0).getAddressLine(2);
            String[] split = stateName.split(", ");
            loc = split[0];
        }catch (Exception e)
        {
            e.getMessage();
        }
        Log.d("rew", "new Location " + lat + " longitude " + lng );
        radius = (int)convertMilesToMeters(miles);
        send();
    }

    public void onMapSearch(View view) {
        loc = locationSearch.getText().toString();
        List<Address> addressList = null;

        if (loc != null) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(loc, 1);
            } catch (Exception e) {
                locationSearch.setError("Enter location");
                e.printStackTrace();
            }
            try{
                Address address = addressList.get(0);
                lat = address.getLatitude();
                lng = address.getLongitude();
                LatLng latLng = new LatLng(lat, lng);
                //mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                radius = (int)convertMilesToMeters(miles);
                send();
            }
            catch (Exception e1)
            {
                locationSearch.setError("Enter location");
            }
        }
    }

    public void send() {
        if (circle != null) {
            circle.remove();
            point.remove();
        }
        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        //GoogleMap map;
        // ... get a map.
        // Add a circle in Sydney
        //mMap.getUiSettings().setMyLocationButtonEnabled(true);
        circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(lat, lng))
                .radius((int)radius)
                .strokeColor(0x6042A5F5)
                .fillColor(0x6042A5F5)
                .strokeWidth(2));
        point = mMap.addCircle(new CircleOptions()
                .center(new LatLng(lat, lng))
                .radius(1000)
                .strokeColor(0xff1976D2)
                .fillColor(0xff1976D2)
                .strokeWidth(2)
                );
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
    }
}
