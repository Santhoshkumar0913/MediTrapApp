package com.example.meditrackapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.Address;
import android.location.Geocoder; // NEW
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest; // NEW
import com.google.android.gms.location.LocationCallback; // NEW
import com.google.android.gms.location.LocationResult; // NEW

import java.io.IOException; // NEW
import java.util.List; // NEW
import java.util.Locale; // NEW

public class NearByLocationActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvCurrentLocation;
    private Button btnFindMedicalShops;
    private Location lastKnownLocation;

    // Field to handle location updates
    private LocationCallback locationCallback; // NEW

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_by_location);

        tvCurrentLocation = findViewById(R.id.tvCurrentLocation);
        btnFindMedicalShops = findViewById(R.id.btnFindMedicalShops);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize the LocationCallback
        initializeLocationCallback(); // NEW

        // 1. Check Location Permissions when button is clicked
        btnFindMedicalShops.setOnClickListener(v -> checkLocationPermissions());
    }

    // --- NEW: Initialize Location Callback for accurate updates ---
    private void initializeLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                // Get the most recent location
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        lastKnownLocation = location;
                        // Stop updates to save battery and proceed
                        fusedLocationClient.removeLocationUpdates(locationCallback);

                        // Convert coordinates to address and update UI
                        getAddressFromLocation(lastKnownLocation);

                        // Proceed to find nearby shops
                        findNearbyMedicalShops(lastKnownLocation);
                        return;
                    }
                }
            }
        };
    }

    // --- Location Permission Handling ---

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Request permissions if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        // If permissions are granted, get the location
        getDeviceLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now get location
                getDeviceLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Cannot find nearby shops.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // --- Get Accurate Location ---

    private void getDeviceLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                // Configure a high-accuracy location request
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(10000); // 10 seconds
                locationRequest.setFastestInterval(5000); // 5 seconds

                tvCurrentLocation.setText("Awaiting Accurate Location and Address..."); // Update UI to show loading state

                // Start requesting location updates using the callback
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */);

            }
        } catch (Exception e) {
            Log.e("Location", "Exception: " + e.getMessage());
            tvCurrentLocation.setText("Current Location: Unable to get location.");
        }
    }

    // --- NEW: Geocoding to get Address from Location ---

    private void getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            // Get at most 1 address from the coordinates
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);

            if (addresses != null && !addresses.isEmpty()) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();

                // Concatenate the address lines
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }

                // Update the TextView with the human-readable address
                tvCurrentLocation.setText("Current Location (Approx):\n" + strReturnedAddress.toString().trim());
            } else {
                // Fallback to displaying raw coordinates if no address found
                tvCurrentLocation.setText("Current Location:\nLat: " + location.getLatitude() +"\nLon: " + location.getLongitude());
            }
        } catch (IOException e) {
            // Geocoding service error (e.g., network issue)
            Log.e("Geocoding", "Error: " + e.getMessage());
            tvCurrentLocation.setText("Current Location: Geocoding service error. Lat/Lon displayed.\nLat: " + location.getLatitude() + "\nLon: " + location.getLongitude());
        }
    }

    // --- Find Shops via Implicit Intent ---

    private void findNearbyMedicalShops(Location location) {
        // Use an Implicit Intent to open Google Maps and search for "medical shop" near coordinates
        String uri = "geo:" + location.getLatitude() + "," + location.getLongitude() + "?q=medical+shop";
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps"); // Forces the intent to open in Google Maps

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps app not installed. Opening in browser...", Toast.LENGTH_LONG).show();

            // CORRECTED Fallback: search via Google Maps website
            String webUri = "https://maps.google.com/?q=medical+shop&ll=" + location.getLatitude() + "," + location.getLongitude();
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
            startActivity(webIntent);
        }
    }

    // --- Clean up location updates when activity is destroyed ---
    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}