package com.example.mobileoffloadingmatrix;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class SlaveHomePage extends AppCompatActivity {
    TextView device,battery,lat,longi;
    FusedLocationProviderClient fusedLocationClient;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slave_home_page);

        intialize();
        getBatteryInfo();
        getLocationInfo();
    }

    public void intialize()
    {
        intent = getIntent();
        device = findViewById(R.id.devicenameslave);
        battery = findViewById(R.id.batteryslave);
        lat = findViewById(R.id.latslave);
        longi = findViewById(R.id.longislave);

        device.setText("Slave Name : "+intent.getStringExtra("ID"));
    }

    public void getBatteryInfo(){
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, iFilter);
        int batteryPercentage = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        battery.setText(batteryPercentage + "%");
        //insertBatteryData(batteryPercentage, deviceName, "");
    }
    @SuppressLint("MissingPermission")
    public void getLocationInfo(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.i("testloc","Inside getloc");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Log.i("testloc","Inside getloc 2");
                    double latitude_1 = location.getLatitude();
                    double longitude_1 = location.getLongitude();
                    Toast.makeText(getApplicationContext(), "lat "+latitude_1, Toast.LENGTH_SHORT).show();
                    lat.setText(latitude_1+"");
                    longi.setText(longitude_1+"");
                    //implement code for location of connected slave/Master.
                    double distance = locationProximity(latitude_1, longitude_1, 33.1, -111.1);
                }
            }
        });


    }

    public double locationProximity(double latitude_1, double longitude_1, double latitude_2, double longitude_2){
        Location location_1 = new Location("Location_1");
        location_1.setLatitude(latitude_1);
        location_1.setLongitude(longitude_1);
        Location location_2 = new Location("Location_2");
        location_1.setLatitude(latitude_2);
        location_1.setLongitude(longitude_2);
        double distance = location_1.distanceTo(location_2);
        return distance;
    }

}