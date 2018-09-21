package com.example.dcl.loctask;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


/**
 * Created by HP-HP on 26-11-2015.
 */
public class BackgroundLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "BackgroundLocationService";

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    private Intent mIntentService;
    private PendingIntent mPendingIntent;

    IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BackgroundLocationService getServerInstance() {
            return BackgroundLocationService.this;
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate()");

        mIntentService = new Intent(this, LocationUpdates.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);

        buildGoogleApiClient();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @SuppressLint("LongLogTag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG + " onStartCommand", "GoogleApiClient Connected");
            return START_STICKY;
        }

        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            Log.i(TAG + " onStartCommand", "GoogleApiClient not Connected");
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }

    @SuppressLint("LongLogTag")
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    @SuppressLint({"LongLogTag", "RestrictedApi"})
    protected void createLocationRequest() {
        Log.i(TAG, "createLocationRequest()");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressLint("LongLogTag")
    protected void startLocationUpdates() {
        Log.i(TAG, "Started Location Updates");

        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, mPendingIntent);
    }

    @SuppressLint("LongLogTag")
    protected void stopLocationUpdates() {
        Log.i(TAG,"Stopped Location Updates");

        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mPendingIntent);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

            startLocationUpdates();
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
}
