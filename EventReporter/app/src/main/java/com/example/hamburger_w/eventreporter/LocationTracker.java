/**
 * Track current location
 */

package com.example.hamburger_w.eventreporter;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;

public class LocationTracker implements LocationListener {
    private final Activity mContext;
    private static final int PERMISSIONS_REQUEST_LOCATION = 99;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // distance update interval
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60; // time update interval: 1 min

    // two methods to get current location: GPS, internet
    private boolean mIsGPSEnabled = false;
    private boolean mIsNetworkEnabled;

    private boolean mCangetLocation;
    private Location location; // location
    private double latitude; // latitude
    private double longitude; // longitude
    // Declaring a Location Manager
    private LocationManager locationManager;

    public LocationTracker(Activity context) {
        this.mContext = context;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /**
     * Method to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    /**
     * Method to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    // runtime permission check
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(mContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }
        return true;
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE); // provide current latitude and longitude

            // getting GPS status
            mIsGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            mIsNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!mIsGPSEnabled && !mIsNetworkEnabled) {
                return null;
            } else {
                mCangetLocation = true;
                // First get location from Network Provider
                checkLocationPermission();
                if (mIsNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (mIsGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    // (latitude, longitude) -> specific address
    public static JSONObject getLocationInfo(double lat, double lng) {
        // virtual devices doesn't provide GPS, so we need to use internet
        // send a http request to google map api
        HttpGet httpGet = new HttpGet("http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=true");
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet); // get response
            HttpEntity entity = response.getEntity(); // get string response from inputstream
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject(); // convert string to JSON
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    // parse data from JSON
    public List<String> getCurrentLocationViaJSON(double lat, double lng) {
        List<String> address = new ArrayList<String>();
        JSONObject jsonObj = getLocationInfo(lat, lng);
        try {
            String status = jsonObj.getString("status").toString();
            if (status.equalsIgnoreCase("OK")) {
                JSONArray results = jsonObj.getJSONArray("results");

                int i = 0;
                do { // results may contain more than one result.
                    JSONObject r = results.getJSONObject(i);
                    if (!r.getString("formatted_address").equals("")) {
                        String formatted_addresses[] = r.getString("formatted_address").split(",");
                        address.add(formatted_addresses[0]);
                        address.add(formatted_addresses[1]);
                        address.add(formatted_addresses[2]);
                        address.add(formatted_addresses[3]);
                        break;
                    }
                    i++;
                } while (i < results.length());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return address;
    }
}
