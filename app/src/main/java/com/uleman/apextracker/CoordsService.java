package com.uleman.apextracker;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CoordsService extends Service {

    LocationManager locationManager;

    DB db;

    Handler h;

    Handler.Callback hc = new Handler.Callback() {

        public boolean handleMessage(Message msg) {

            updateCoords();

            return false;
        }

    };


    public CoordsService() {
    }

    @Override
    public void onCreate() {

        super.onCreate();

        db = new DB(this);

        db.open();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        h = new Handler(hc);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        updateCoords();

        return super.onStartCommand(intent, flags, startId);

    }

    private void updateCoords() {

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                DB db = new DB(getBaseContext());
                db.open();
                String appId = db.getConstant("appId");
                String lat = db.getConstant("lat");
                String lon = db.getConstant("lon");
                db.close();

                if (lat == null || lon == null || (!lat.equals(String.valueOf(location.getLatitude()))
                    && !lon.equals(String.valueOf(location.getLongitude()))) ) {

                    Date currentDate = new Date();

                    //LocalDate localDate = new LocalDate(DateTimeZone.forID("Europe/Moscow")

                    // Форматирование времени как "день.месяц.год"
                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03"));
                    String dateText = dateFormat.format(currentDate);

                    HttpClient httpClient = new HttpClient(getBaseContext());
                    httpClient.request_get("td?ac=" + Connections.trackersDataAccessKey + "&id=" + appId
                            + "&date=" + dateText
                            + "&lat=" + location.getLatitude()
                            + "&lon=" + location.getLongitude(), new HttpRequestInterface() {
                        @Override
                        public void setProgressVisibility(int visibility) {

                        }

                        @Override
                        public void processResponse(String response) {

                            DB db = new DB(getBaseContext());
                            db.open();

                            db.updateConstant("lat", String.valueOf(location.getLatitude()));
                            db.updateConstant("lon", String.valueOf(location.getLongitude()));

                            db.close();

                        }
                    });
                }
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {

            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        });

        h.sendEmptyMessageDelayed(1, 10 * 1000);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        db.close();


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
