package com.pidemo.servicedemo;

import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by KRUNAL on 5/13/2016.
 */
public class MyService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private final int REQUEST_CODE_CLICK_IMAGE = 02;
    double latitude;
    double longitude;

    Button btn;
    SQLiteDatabase database;

    private static final String TAG = "LOC";
    private static final long INTERVAL = 1000 * 5;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service Created...");



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service Started...");
        database = this.openOrCreateDatabase("update_location1", MODE_PRIVATE, null);

        database.execSQL("create table if not exists update_master1 (location_id INTEGER PRIMARY KEY AUTOINCREMENT,latitude VARCHAR, longtitude VARCHAR)");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if(mGoogleApiClient!=null){
            mGoogleApiClient.connect();
        }
        createLocationRequest();
        updateUI();
        return START_STICKY;


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void loadDataIntoDataBase() {

        //noinspection ResultOfMethodCallIgnored
        String strLatitude = String.valueOf(latitude);
        String strLongtitude = String.valueOf(longitude);
        database.execSQL("insert into update_master1 (latitude,longtitude) values('" + strLatitude + "','" + strLongtitude + "')");
        Toast.makeText(getBaseContext(), "Data Saved Succesfully", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
            Log.d("LOC", "Lat : " + latitude + "\n" + "Lon : " + longitude);
            loadDataIntoDataBase();
        } else {
            Log.d(TAG, "location is null ...............");
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service Destroy...");
        super.onDestroy();
    }
}