package com.example.smarttravelguide;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BusinessHelper {
    MainActivity activity;
    boolean isLocation_Granted = false;
    BusinessHelper(MainActivity activity){
        this.activity = activity;
    }
    private static final int INTERNET_PERMISSION_CODE = 1;
    private static final int LOCATION_PERMISSION_CODE = 2;
    boolean flag = true;

    public boolean permissionCheck() {

        //Check for internet permission
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setMessage("This App Require Internet Permit")
                    .setPositiveButton("Allow", (dialogInterface, i) ->
                    {
                        ActivityCompat.requestPermissions(activity,
                                new String[]{INTERNET}, INTERNET_PERMISSION_CODE);
                        System.exit(0);
                    })
                    .setNegativeButton("Cancle", (dialogInterface, i) ->
                    {
                        System.exit(0);
                    })
                    .show();
            flag = false;
        }

        // Check for location permission
        if (ContextCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setMessage("This App Require Location Permit")
                    .setPositiveButton("Allow", (dialogInterface, i) ->
            {
                ActivityCompat.requestPermissions(activity,
                    new String[]{ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                System.exit(0);
            })
                    .setNegativeButton("Cancle", (dialogInterface, i) ->
            {
                System.exit(0);
            })
                    .show();
            flag = false;
        }

        if (!isNetworkAvailable()) {
            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setMessage("Turn Internet on First !")
                    .setPositiveButton("Allow", (dialogInterface, i) ->
                    {
                        System.exit(0);
                    })
                    .show();
            flag = false;
        }

        if (!isGPSEnabled()) {
            new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setMessage("Turn Location on First !")
                    .setPositiveButton("Allow", (dialogInterface, i) ->
                    {
                        System.exit(0);
                    })
                    .show();
            flag = false;
        }

        return flag;
   }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}






