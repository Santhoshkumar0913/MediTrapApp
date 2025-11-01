package com.example.meditrackapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NearByLocationActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "NearByLocation";

    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvCurrentLocation;
    private Button btnFindMedicalShops;
    private ImageView backArrowLocation;
    private Location lastKnownLocation;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_by_location);

        initializeViews();
        setupClickListeners();
        initializeLocationCallback();
    }

    private void initializeViews() {
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation);
        btnFindMedicalShops = findViewById(R.id.btnFindMedicalShops);
        backArrowLocation = findViewById(R.id.backArrowLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void setupClickListeners() {
        backArrowLocation.setOnClickListener(v -> finish());
        btnFindMedicalShops.setOnClickListener(v -> checkLocationPermissions());
    }

    private void initializeLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e(TAG, "Location result is null");
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    lastKnownLocation = location;
                    Log.d(TAG, "Location updated: Lat=" + location.getLatitude() + ", Lon=" + location.getLongitude());

                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    getAddressFromLocation(location);
                    findNearbyMedicalShops(location);
                }
            }
        };
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getDeviceLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            } else {
                Toast.makeText(this, "Location permission is required to find nearby shops", Toast.LENGTH_LONG).show();
                tvCurrentLocation.setText("Location permission denied");
            }
        }
    }

    private void getDeviceLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            tvCurrentLocation.setText("Getting your current location...");

            // First try to get last known location for immediate result
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        lastKnownLocation = location;
                        Log.d(TAG, "Last known location: Lat=" + location.getLatitude() + ", Lon=" + location.getLongitude());
                        getAddressFromLocation(location);
                        findNearbyMedicalShops(location);
                    } else {
                        // If no last location, request fresh location updates
                        requestLocationUpdates();
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error getting location: " + e.getMessage());
            tvCurrentLocation.setText("Unable to get location. Please try again.");
            Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationUpdates() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setMaxWaitTime(10000);
            locationRequest.setNumUpdates(1);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

        } catch (Exception e) {
            Log.e(TAG, "Error requesting location updates: " + e.getMessage());
        }
    }

    private void getAddressFromLocation(Location location) {
        if (location == null) return;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder addressStr = new StringBuilder();

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressStr.append(address.getAddressLine(i)).append("\n");
                }

                String displayAddress = "Current Location:\n" + addressStr.toString().trim();
                tvCurrentLocation.setText(displayAddress);
                Log.d(TAG, "Address: " + displayAddress);
            } else {
                String coords = "Current Location:\nLat: " + String.format("%.6f", location.getLatitude()) +
                        "\nLon: " + String.format("%.6f", location.getLongitude());
                tvCurrentLocation.setText(coords);
                Log.d(TAG, coords);
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error: " + e.getMessage());
            String coords = "Current Location:\nLat: " + String.format("%.6f", location.getLatitude()) +
                    "\nLon: " + String.format("%.6f", location.getLongitude());
            tvCurrentLocation.setText(coords);
        }
    }

    private void findNearbyMedicalShops(Location location) {
        if (location == null) {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String uri = "geo:" + location.getLatitude() + "," + location.getLongitude() + "?q=medical+shop+pharmacy";
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
                Log.d(TAG, "Opened Google Maps with URI: " + uri);
            } else {
                openInBrowser(location);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening maps: " + e.getMessage());
            openInBrowser(location);
        }
    }

    private void openInBrowser(Location location) {
        Toast.makeText(this, "Google Maps not installed. Opening in browser...", Toast.LENGTH_SHORT).show();
        String webUri = "https://www.google.com/maps/search/medical+shop+pharmacy/@" +
                location.getLatitude() + "," + location.getLongitude() + ",15z";
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
        startActivity(webIntent);
        Log.d(TAG, "Opened browser with URI: " + webUri);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}