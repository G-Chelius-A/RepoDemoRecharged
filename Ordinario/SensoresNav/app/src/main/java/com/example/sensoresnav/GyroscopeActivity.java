package com.example.sensoresnav;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class GyroscopeActivity extends BaseActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor gyroscope;

    private TextView tvGyroX, tvGyroY, tvGyroZ;
    private ProgressBar progressGyroX, progressGyroY, progressGyroZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_gyroscope);
        }

        tvGyroX = findViewById(R.id.tvGyroX);
        tvGyroY = findViewById(R.id.tvGyroY);
        tvGyroZ = findViewById(R.id.tvGyroZ);

        progressGyroX = findViewById(R.id.progressGyroX);
        progressGyroY = findViewById(R.id.progressGyroY);
        progressGyroZ = findViewById(R.id.progressGyroZ);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        if (gyroscope == null) {
            Toast.makeText(this, R.string.sensor_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float xRad = event.values[0]; // Pitch
            float yRad = event.values[1]; // Roll
            float zRad = event.values[2]; // Yaw

            float xDeg = (float) Math.toDegrees(xRad);
            float yDeg = (float) Math.toDegrees(yRad);
            float zDeg = (float) Math.toDegrees(zRad);

            tvGyroX.setText(String.format(Locale.getDefault(), "Pitch (Eje X): %.2f rad/s (%.1f°/s)", xRad, xDeg));
            tvGyroY.setText(String.format(Locale.getDefault(), "Roll (Eje Y): %.2f rad/s (%.1f°/s)", yRad, yDeg));
            tvGyroZ.setText(String.format(Locale.getDefault(), "Yaw (Eje Z): %.2f rad/s (%.1f°/s)", zRad, zDeg));

            progressGyroX.setProgress(clampProgress(xRad));
            progressGyroY.setProgress(clampProgress(yRad));
            progressGyroZ.setProgress(clampProgress(zRad));
        }
    }

    private int clampProgress(float radVal) {
        // Range -10 to +10 rad/s mapped to 0 to 20
        int progress = (int) (radVal * 2 + 10);
        if (progress < 0) return 0;
        if (progress > 20) return 20;
        return progress;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}