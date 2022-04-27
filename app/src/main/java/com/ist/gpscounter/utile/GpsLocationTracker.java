package com.ist.gpscounter.utile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;
import com.ist.onlinemovieapplication.BuildConfig;


public class GpsLocationTracker extends Service implements LocationListener {

    static GoogleApiClient googleApiClient;

    /**
     * context of calling class
     */
    private Context mContext;

    /**
     * flag for gps status
     */
    private boolean isGpsEnabled = false;

    /**
     * flag for network status
     */
    private boolean isNetworkEnabled = false;

    /**
     * flag for gps
     */
    private boolean canGetLocation = false;

    /**
     * location
     */
    private Location mLocation;

    /**
     * latitude
     */
    private double mLatitude;

    /**
     * longitude
     */
    private double mLongitude;

    /**
     * min distance change to get location update
     */
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 10;

    /**
     * min time for location update
     * 60000 = 1min
     */
    private static final long MIN_TIME_FOR_UPDATE = 60000;

    /**
     * location manager
     */
    private LocationManager mLocationManager;


    /**
     * @param mContext constructor of the class
     */
    public GpsLocationTracker(Context mContext) {

        this.mContext = mContext;
        getLocation();
    }


    /**
     * @return location
     */
    @SuppressLint("MissingPermission")
    public Location getLocation() {

        try {

            mLocationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            /*getting status of the gps*/
            isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            /*getting status of network provider*/
            isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled && !isNetworkEnabled) {
                openSettings();
                /*no location provider enabled*/
            } else {

                this.canGetLocation = true;

                /*getting location from network provider*/
                if (isNetworkEnabled) {

                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_FOR_UPDATE,
                            MIN_DISTANCE_CHANGE_FOR_UPDATE,
                            this);

                    if (mLocationManager != null) {

                        mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (mLocation != null) {

                            mLatitude = mLocation.getLatitude();

                            mLongitude = mLocation.getLongitude();
                            AppUtiles.curtLatlng=new LatLng(mLatitude,mLongitude);
                        }
                    }
                    /*if gps is enabled then get location using gps*/
                    if (isGpsEnabled) {

                        if (mLocation == null) {

                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                    MIN_TIME_FOR_UPDATE,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATE, this);

                            if (mLocationManager != null) {

                                mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if (mLocation != null) {

                                    mLatitude = mLocation.getLatitude();

                                    mLongitude = mLocation.getLongitude();
                                    AppUtiles.curtLatlng=new LatLng(mLatitude,mLongitude);
                                }

                            }
                        }

                    }
                }
            }

            if(mLocation==null) {
                LocationRequest request = new LocationRequest();
                //Specify how often your app should request the device’s location//
                request.setInterval(10000);
                //Get the most accurate location data available//
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(mContext);
                int permission = ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION);

                int permission1 = ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION);
                //If the app currently has access to the location permission...//
                if (permission == PackageManager.PERMISSION_GRANTED && permission1==PackageManager.PERMISSION_GRANTED) {
                    //...then request location updates//

                    client.requestLocationUpdates(request, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            //Get a reference to the database, so your app can perform read and write operations//
                            Location location = locationResult.getLastLocation();
                            if (location != null) {
                                mLatitude = location.getLatitude();

                                mLongitude = location.getLongitude();
                                AppUtiles.curtLatlng=new LatLng(mLatitude,mLongitude);
                            }
                        }
                    }, null);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
            Log.v("Loc Exception",e.getMessage());
        }

        return mLocation;
    }

    /**
     * call this function to stop using gps in your application
     */
    public void stopUsingGps() {

        if (mLocationManager != null) {

            mLocationManager.removeUpdates(GpsLocationTracker.this);

        }
    }

    /**
     * @return latitude
     *         <p/>
     *         function to get latitude
     */
    public double getLatitude() {

        if (mLocation != null) {

            mLatitude = mLocation.getLatitude();
        }
        return mLatitude;
    }

    /**
     * @return longitude
     *         function to get longitude
     */
    public double getLongitude() {

        if (mLocation != null) {

            mLongitude = mLocation.getLongitude();

        }

        return mLongitude;
    }

    /**
     * @return to check gps or wifi is enabled or not
     */
    public boolean canGetLocation() {

        return this.canGetLocation;
    }

    /**
     * function to prompt user to open
     * settings to enable gps
     */

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub

    }

    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }
    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



    public static void enableLoc(final Activity context, GoogleApiClient googleApi, final int REQUEST_LOCATION) {

        googleApiClient = googleApi;
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error","Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(context, REQUEST_LOCATION);

                                //finish();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
    }

    public static boolean isGPSOn(Context context){
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE );
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }


}