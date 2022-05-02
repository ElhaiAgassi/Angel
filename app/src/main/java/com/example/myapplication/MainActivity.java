package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.app.LoaderManager;

public class MainActivity extends AppCompatActivity implements LocationListener {
    LocationManager locationManager;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    final static  int PERMISSIONS_All = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(Build.VERSION.SDK_INT>=23)
        {
            requestPermissions(PERMISSIONS,PERMISSIONS_All);
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestLocation();
                handler.postDelayed(this,1000 * 60 * 5);

            }
        },1000);
    }

    @Override
    public void onLocationChanged(@NonNull Location location)
    {
    Log.d("mylog", "Got Location" + location.getLatitude() + "," + location.getLongitude());
        Toast.makeText(this, "Got Location: " + location.getLatitude() + "," + location.getLongitude(),Toast.LENGTH_SHORT).show();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //HERE WE REQUEST LOC
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestLocation();
                    handler.postDelayed(this,1000 * 60 * 5);

                }
            },1000);

        }
    }

    public void requestLocation()
    {
        if(locationManager == null)
        {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000,1000,this);
            }
        }
    }
}
