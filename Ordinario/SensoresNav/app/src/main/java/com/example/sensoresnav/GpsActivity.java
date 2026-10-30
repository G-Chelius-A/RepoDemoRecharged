package com.example.sensoresnav;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class GpsActivity extends BaseActivity implements LocationListener {

    private static final int PERMISSION_REQUEST_LOCATION = 1003;

    private LocationManager locationManager;

    private TextView tvLatitude, tvLongitude, tvAltitude, tvSpeed, tvAccuracy, tvProvider, tvGpsStatus;
    private MaterialButton btnToggleGps;

    private boolean isTracking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_gps);
        }

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAltitude = findViewById(R.id.tvAltitude);
        tvSpeed = findViewById(R.id.tvSpeed);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvProvider = findViewById(R.id.tvProvider);
        tvGpsStatus = findViewById(R.id.tvGpsStatus);

        btnToggleGps = findViewById(R.id.btnToggleGps);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        btnToggleGps.setOnClickListener(v -> {
            if (isTracking) {
                stopGpsTracking();
            } else {
                checkPermissionAndStartGps();
            }
        });
    }

    private void checkPermissionAndStartGps() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        } else {
            startGpsTracking();
        }
    }

    private void startGpsTracking() {
        if (locationManager == null) return;

        try {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGpsEnabled && !isNetworkEnabled) {
                Toast.makeText(this, "El servicio de Ubicación / GPS está desactivado en el dispositivo", Toast.LENGTH_LONG).show();
                tvGpsStatus.setText("GPS desactivado en ajustes");
                return;
            }

            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
            }
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 1, this);
            }

            isTracking = true;
            btnToggleGps.setText(R.string.btn_stop_gps);
            tvGpsStatus.setText(R.string.gps_searching);

            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnown == null) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnown != null) {
                onLocationChanged(lastKnown);
            }

        } catch (SecurityException e) {
            Toast.makeText(this, "Error de permisos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopGpsTracking() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        isTracking = false;
        btnToggleGps.setText(R.string.btn_start_gps);
        tvGpsStatus.setText(R.string.gps_stopped);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTracking) {
            stopGpsTracking();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        tvLatitude.setText(String.format(Locale.getDefault(), "Latitud: %.6f°", location.getLatitude()));
        tvLongitude.setText(String.format(Locale.getDefault(), "Longitud: %.6f°", location.getLongitude()));
        tvAltitude.setText(String.format(Locale.getDefault(), "Altitud: %.2f m", location.getAltitude()));

        float speedKmH = location.getSpeed() * 3.6f;
        tvSpeed.setText(String.format(Locale.getDefault(), "Velocidad: %.1f km/h", speedKmH));
        tvAccuracy.setText(String.format(Locale.getDefault(), "Precisión: ±%.1f m", location.getAccuracy()));
        tvProvider.setText(String.format(Locale.getDefault(), "Proveedor: %s", location.getProvider()));

        tvGpsStatus.setText("Ubicación actualizada en tiempo real");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGpsTracking();
            } else {
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }
}