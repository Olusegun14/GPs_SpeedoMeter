package com.ist.gpscounter.activity;

import android.Manifest;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.ist.onlinemovieapplication.R;
import com.ist.gpscounter.utile.AppUtiles;
import com.ist.gpscounter.utile.AutoCompleteAdapter;
import com.ist.gpscounter.utile.GpsLocationTracker;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    //    class views
    ImageView ivBack;
    MapView mapView;
    TextView tvGO,tvTitle;
    AutoCompleteTextView etSearchLocation;
    GpsLocationTracker gpsLocation;

    //calss varibles
    GoogleMap mMap;
    int screenUseFor;
    locationResponce litener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ivBack = findViewById(R.id.ivBack);
        tvGO = findViewById(R.id.tvGO);
        mapView=findViewById(R.id.mapview);
        etSearchLocation=findViewById(R.id.etSearchLocation);
        gpsLocation=new GpsLocationTracker(this);
        Bundle bundle=getIntent().getExtras();

        if(bundle!=null){
            screenUseFor=bundle.getInt("screenUseFor");
        }

        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        setupMapView(savedInstanceState);

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }


    private void setupMapView(Bundle savedInstanceState) {
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            mapView.getMapAsync(this);
        }


        setUpButtons();
    }

    private void setUpButtons() {
        ivBack.setOnClickListener(this);
        tvGO.setOnClickListener(this);

        placeSuggestion();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivBack:{
                finish();
                break;
            }
            case R.id.tvGO:{


                finish();
                break;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap=googleMap;

        try{
            CameraUpdate camPosition = CameraUpdateFactory.newLatLngZoom(AppUtiles.curtLatlng,16);

            googleMap.animateCamera(camPosition);

        }catch (Exception e){
            e.printStackTrace();
        }
        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                // Cleaning all the markers.
                if (googleMap != null) {
                    googleMap.clear();
                }

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                double lat=googleMap.getCameraPosition().target.latitude;
                double lng=googleMap.getCameraPosition().target.longitude;

                AppUtiles.propertyLatlng=new LatLng(lat,lng);

                try {
                    addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    if(addresses.size()>0) {
                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//                        AddPropertiesActivity.tvAddress.setText(address);
                        etSearchLocation.setText(address);

                    }
                } catch (IOException e) {
                    Log.v("IOException Address",e.getMessage());
                }
            }
        });



    }


    public LatLng getLocationFromAddress(String strAddress){

        Geocoder coder;
        List<Address> address;
        coder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            address = coder.getFromLocationName(strAddress,5);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);
            location.getLatitude();
            location.getLongitude();

            LatLng latLng = new LatLng((double) (location.getLatitude() ),
                    (double) (location.getLongitude() ));

            return latLng;
        }catch (Exception e){
            Log.v("",e.getMessage());
        }
        return null;
    }



    public interface locationResponce {
        public void searchRider(LatLng propertyLatlng);
    }

    public void searchRiderResponce(locationResponce listener) {
        this.litener = listener;

    }



    //    address Suggestion
    public void placeSuggestion() {
        etSearchLocation.setText("");
        Places.initialize(getApplicationContext(), getResources().getString(R.string.maps_key));
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);
        AutoCompleteAdapter autoCompleteAdapter = new AutoCompleteAdapter(MapActivity.this, placesClient);
        etSearchLocation.setAdapter(autoCompleteAdapter);

        etSearchLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String address = etSearchLocation.getText().toString();

                AppUtiles.propertyLatlng=getLocationFromAddress(address);
//                AddPropertiesActivity.tvAddress.setText(address);

                CameraUpdate camPosition = CameraUpdateFactory.newLatLngZoom(AppUtiles.propertyLatlng,16);
                mMap.animateCamera(camPosition);
            }
        });
    }

}
/*

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.ist.onlinemovieapplication.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    MapView mapView;
    AutoCompleteTextView etSearchLocation;
    LatLng curtLatlng;
    GoogleMap mMap;
    Location locationGPS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView=findViewById(R.id.mapview);
        etSearchLocation=findViewById(R.id.etSearchLocation);


        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        setupMapView(savedInstanceState);

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationManager nManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationGPS = nManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);



    }


    private void setupMapView(Bundle savedInstanceState) {
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            mapView.getMapAsync(this);
        }


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap=googleMap;

        try{
            curtLatlng= new LatLng(locationGPS.getLatitude(),locationGPS.getLongitude());
            CameraUpdate camPosition = CameraUpdateFactory.newLatLngZoom(curtLatlng,16);

            googleMap.animateCamera(camPosition);

            etSearchLocation.setText(getCompleteAddressString(locationGPS.getLatitude(),locationGPS.getLongitude()));
        }catch (Exception e){
            e.printStackTrace();
        }
    */
/*//*
/googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                // Cleaning all the markers.
                if (googleMap != null) {
                    googleMap.clear();
                }

                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                double lat=googleMap.getCameraPosition().target.latitude;
                double lng=googleMap.getCameraPosition().target.longitude;

                propertyLatlng=new LatLng(lat,lng);

                try {
                    addresses = geocoder.getFromLocation(lat, lng, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    if(addresses.size()>0) {
                        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        etSearchLocation.setText(address);

                    }
                } catch (IOException e) {
                    Log.v("IOException Address",e.getMessage());
                }
            }
        });*//*




    }


    @SuppressLint("LongLogTag")
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.d("My Current loction address", strReturnedAddress.toString());
            } else {
                Log.d("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }

}*/
