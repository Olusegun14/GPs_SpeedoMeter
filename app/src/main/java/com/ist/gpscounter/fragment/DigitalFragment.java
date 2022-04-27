package com.ist.gpscounter.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.gc.materialdesign.widgets.Dialog;
import com.google.gson.Gson;
import com.ist.onlinemovieapplication.R;
import com.ist.gpscounter.gps.Data;
import com.ist.gpscounter.gps.GpsServices;
import com.melnykov.fab.FloatingActionButton;

import java.util.Locale;


public class DigitalFragment extends Fragment implements LocationListener, GpsStatus.Listener {


    private SharedPreferences sharedPreferences;
    private LocationManager mLocationManager;
    private static Data data;
    private FloatingActionButton fab;
    private FloatingActionButton refresh;
    private ProgressBarCircularIndeterminate progressBarCircularIndeterminate;
    private TextView satellite;
    private TextView status;
    private TextView accuracy;
    private TextView currentSpeed;
    private TextView maxSpeed;
    private TextView averageSpeed;
    private TextView distance, stepsCount;
    private Chronometer time;
    private Data.OnGpsServiceUpdate onGpsServiceUpdate;

    private boolean firstfix;


    public DigitalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_digital, container, false);


        satellite = view.findViewById(R.id.satellite);
        status = view.findViewById(R.id.status);
        accuracy = view.findViewById(R.id.accuracy);
        maxSpeed = view.findViewById(R.id.maxSpeed);
        averageSpeed = view.findViewById(R.id.averageSpeed);
        distance = view.findViewById(R.id.distance);
        stepsCount = view.findViewById(R.id.stepsCounter);
        time = view.findViewById(R.id.time);
        currentSpeed = view.findViewById(R.id.currentSpeed);
        progressBarCircularIndeterminate = (ProgressBarCircularIndeterminate) view.findViewById(R.id.progressBarCircularIndeterminate);


        data = new Data(onGpsServiceUpdate);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //setTitle("");
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        refresh = (FloatingActionButton) view.findViewById(R.id.refresh);
        refresh.setVisibility(View.INVISIBLE);


        try {
            onGpsServiceUpdate = new Data.OnGpsServiceUpdate() {
                @Override
                public void update() {
                    double maxSpeedTemp = data.getMaxSpeed();
                    double distanceTemp = data.getDistance();
                    double averageTemp;
                    if (sharedPreferences.getBoolean("auto_average", false)) {
                        averageTemp = data.getAverageSpeedMotion();
                    } else {
                        averageTemp = data.getAverageSpeed();
                    }

                    String speedUnits;
                    String distanceUnits;
                    if (sharedPreferences.getBoolean("miles_per_hour", false)) {
                        maxSpeedTemp *= 0.62137119;
                        distanceTemp = distanceTemp / 1000.0 * 0.62137119;
                        averageTemp *= 0.62137119;
                        speedUnits = "mi/h";
                        distanceUnits = "mi";
                    } else {
                        speedUnits = "km/h";
                        if (distanceTemp <= 1000.0) {
                            distanceUnits = "m";
                        } else {
                            distanceTemp /= 1000.0;
                            distanceUnits = "km";
                        }
                    }

                    SpannableString s = new SpannableString(String.format("%.0f %s", maxSpeedTemp, speedUnits));
                    s.setSpan(new RelativeSizeSpan(0.5f), s.length() - speedUnits.length() - 1, s.length(), 0);
                    maxSpeed.setText(s);

                    s = new SpannableString(String.format("%.0f %s", averageTemp, speedUnits));
                    s.setSpan(new RelativeSizeSpan(0.5f), s.length() - speedUnits.length() - 1, s.length(), 0);
                    averageSpeed.setText(s);

                    s = new SpannableString(String.format("%.3f %s", distanceTemp, distanceUnits));
                    s.setSpan(new RelativeSizeSpan(0.5f), s.length() - distanceUnits.length() - 1, s.length(), 0);
                    distance.setText(s);

                    try {
                        if (!s.toString().contains("km")) {
                            String steps = s.toString().replace("m", "");
                            double stepCount = Double.parseDouble(steps) * 2.5;
                            stepsCount.setText(stepCount + "");
                            Log.d("Steps", stepCount + "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    Toast.makeText(getContext(), "update", Toast.LENGTH_SHORT).show();

                }
            };

        } catch (Exception e) {
            e.printStackTrace();
        }

        mLocationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        time.setText("00:00:00");
        time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            boolean isPair = true;

            @Override
            public void onChronometerTick(Chronometer chrono) {
                long time;
                if (data.isRunning()) {
                    time = SystemClock.elapsedRealtime() - chrono.getBase();
                    data.setTime(time);
                } else {
                    time = data.getTime();
                }

                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                chrono.setText(hh + ":" + mm + ":" + ss);

                if (data.isRunning()) {
                    chrono.setText(hh + ":" + mm + ":" + ss);
                } else {
                    if (isPair) {
                        isPair = false;
                        chrono.setText(hh + ":" + mm + ":" + ss);
                    } else {
                        isPair = true;
                        chrono.setText("");
                    }
                }

            }
        });

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!data.isRunning()) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
                    data.setRunning(true);
                    time.setBase(SystemClock.elapsedRealtime() - data.getTime());
                    time.start();
                    data.setFirstTime(true);
                    getActivity().getApplicationContext().startService(new Intent(getActivity(), GpsServices.class));
                    refresh.setVisibility(View.INVISIBLE);
                } else {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                    data.setRunning(false);
                    status.setText("");
                    getActivity().stopService(new Intent(getActivity(), GpsServices.class));
                    refresh.setVisibility(View.VISIBLE);
                }
            }
        });

        refresh.setOnClickListener(view1 -> {
            resetData();
            getActivity().stopService(new Intent(getActivity(), GpsServices.class));
        });

    }


    @Override
    public void onResume() {
        super.onResume();

        try {
            firstfix = true;
            if (!data.isRunning()) {
                Gson gson = new Gson();
                String json = sharedPreferences.getString("data", "");
                data = gson.fromJson(json, Data.class);
            }
            if (data == null) {
                data = new Data(onGpsServiceUpdate);
            } else {
                data.setOnGpsServiceUpdate(onGpsServiceUpdate);
            }

            if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
            } else {
                Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
            }

            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGpsDisabledDialog();
            }


            mLocationManager.addGpsStatusListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mLocationManager.removeUpdates(this);
            mLocationManager.removeGpsStatusListener(this);
            SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(data);
            prefsEditor.putString("data", json);
            prefsEditor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().stopService(new Intent(getActivity(), GpsServices.class));
    }


    @Override
    public void onLocationChanged(Location location) {
        if (location.hasAccuracy()) {
            double acc = location.getAccuracy();
            String units;
            if (sharedPreferences.getBoolean("miles_per_hour", false)) {
                units = "ft";
                acc *= 3.28084;
            } else {
                units = "m";
            }
            SpannableString s = new SpannableString(String.format("%.0f %s", acc, units));
            s.setSpan(new RelativeSizeSpan(0.75f), s.length() - units.length() - 1, s.length(), 0);
            accuracy.setText(s);

            if (firstfix) {
                status.setText("");
                fab.setVisibility(View.VISIBLE);
                if (!data.isRunning() && !TextUtils.isEmpty(maxSpeed.getText())) {
                    refresh.setVisibility(View.VISIBLE);
                }
                firstfix = false;
            }
        } else {
            firstfix = true;
        }

        if (location.hasSpeed()) {
            progressBarCircularIndeterminate.setVisibility(View.GONE);
            double speed = location.getSpeed() * 3.6;
            String units;
            if (sharedPreferences.getBoolean("miles_per_hour", false)) { // Convert to MPH
                speed *= 0.62137119;
                units = "mi/h";
            } else {
                units = "km/h";
            }
            SpannableString s = new SpannableString(String.format(Locale.ENGLISH, "%.0f %s", speed, units));
            s.setSpan(new RelativeSizeSpan(0.25f), s.length() - units.length() - 1, s.length(), 0);
            currentSpeed.setText(s);
        }

    }

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                @SuppressLint("MissingPermission") GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                int satsInView = 0;
                int satsUsed = 0;
                Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
                for (GpsSatellite sat : sats) {
                    satsInView++;
                    if (sat.usedInFix()) {
                        satsUsed++;
                    }
                }
                satellite.setText(String.valueOf(satsUsed) + "/" + String.valueOf(satsInView));
                if (satsUsed == 0) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                    data.setRunning(false);
                    status.setText("");
                    getActivity().stopService(new Intent(getActivity(), GpsServices.class));
                    fab.setVisibility(View.INVISIBLE);
                    refresh.setVisibility(View.INVISIBLE);
                    accuracy.setText("");
                    status.setText(getResources().getString(R.string.waiting_for_fix));
                    firstfix = true;
                }
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showGpsDisabledDialog();
                }
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
        }
    }

    public void showGpsDisabledDialog() {
        Dialog dialog = new Dialog(getActivity(), getResources().getString(R.string.gps_disabled), getResources().getString(R.string.please_enable_gps));

        dialog.setOnAcceptButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
            }
        });
        dialog.show();
    }

    public void resetData() {
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
        refresh.setVisibility(View.INVISIBLE);
        time.stop();
        maxSpeed.setText("");
        averageSpeed.setText("");
        distance.setText("");
        stepsCount.setText("");
        time.setText("00:00:00");
        data = new Data(onGpsServiceUpdate);
    }

    public static Data getData() {
        return data;
    }


    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

}