/**
 * Utilities function and attributes
 */

package com.example.hamburger_w.eventreporter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static String username = null;

    public static String md5Encryption(final String input) {
        String result = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(input.getBytes(Charset.forName("UTF8")));
            byte[] resultByte = messageDigest.digest();
            result = new String(Hex.encodeHex(resultByte)); // encodeHex converts byte[] to char[]
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * Transform timestamp milliseconds to human readable format string
     * @param millis
     * @return
     */
    public static String timeTransformer(long millis) {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - millis;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (seconds < 60) {
            return seconds + " seconds ago";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else {
            return days + " days ago";
        }
    }

    /**
     * Download an Image from the given URL, then decodes and returns a Bitmap object.
     */
    public static Bitmap getBitmapFromURL(String imageUrl) {
        Bitmap bitmap = null;

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // 1: send a request to Firebase Storage
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream(); // 2: get some input data
            bitmap = BitmapFactory.decodeStream(input); // 3: decode inputstream data to bitmap
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error: ", e.getMessage().toString());
        }

        return bitmap;
    }

    /**
     * Calculate the distance between two locations
     */
    public static int distanceBetweenTwoLocations(double currentLatitude, double currentLongitude, double destLatitude, double destLongitude) {
        Location currentLocation = new Location("CurrentLocation");
        currentLocation.setLatitude(currentLatitude);
        currentLocation.setLongitude(currentLongitude);
        Location destLocation = new Location("DestLocation");
        destLocation.setLatitude(destLatitude);
        destLocation.setLongitude(destLongitude);
        double distance = currentLocation.distanceTo(destLocation); // return meters

        double inches = (39.370078 * distance);
        int miles = (int) (inches / 63360);
        return miles;
    }
}

