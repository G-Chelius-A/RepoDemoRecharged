package com.example.sensoresnav;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        MaterialCardView cardAccel = findViewById(R.id.cardAccelerometer);
        MaterialCardView cardPedometer = findViewById(R.id.cardPedometer);
        MaterialCardView cardGyro = findViewById(R.id.cardGyroscope);
        MaterialCardView cardCamera = findViewById(R.id.cardCamera);
        MaterialCardView cardGps = findViewById(R.id.cardGps);
        MaterialCardView cardMag = findViewById(R.id.cardMagnetometer);

        cardAccel.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AccelerometerActivity.class)));
        cardPedometer.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PedometerActivity.class)));
        cardGyro.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GyroscopeActivity.class)));
        cardCamera.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CameraActivity.class)));
        cardGps.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, GpsActivity.class)));
        cardMag.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MagnetometerActivity.class)));
    }
}