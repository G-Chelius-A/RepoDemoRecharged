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

public class AccelerometerActivity extends BaseActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView tvAccelX, tvAccelY, tvAccelZ, tvShakeStatus;
    private ProgressBar progressX, progressY, progressZ;

    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_accelerometer);
        }

        tvAccelX = findViewById(R.id.tvAccelX);
        tvAccelY = findViewById(R.id.tvAccelY);
        tvAccelZ = findViewById(R.id.tvAccelZ);
        tvShakeStatus = findViewById(R.id.tvShakeStatus);

        progressX = findViewById(R.id.progressX);
        progressY = findViewById(R.id.progressY);
        progressZ = findViewById(R.id.progressZ);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            Toast.makeText(this, R.string.sensor_not_available, Toast.LENGTH_SHORT).show();
            tvShakeStatus.setText(R.string.sensor_not_available);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            tvAccelX.setText(String.format(Locale.getDefault(), "Eje X: %.2f m/s²", x));
            tvAccelY.setText(String.format(Locale.getDefault(), "Eje Y: %.2f m/s²", y));
            tvAccelZ.setText(String.format(Locale.getDefault(), "Eje Z: %.2f m/s²", z));

            progressX.setProgress(clampProgress(x));
            progressY.setProgress(clampProgress(y));
            progressZ.setProgress(clampProgress(z));

            // Shake detection threshold
            double acceleration = Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
            long currentTime = System.currentTimeMillis();

            if (acceleration > 5.0) { // Threshold for shake
                lastShakeTime = currentTime;
                tvShakeStatus.setText(R.string.shake_detected);
                tvShakeStatus.setTextColor(getResources().getColor(R.color.accelerometer_color, getTheme()));
            } else if (currentTime - lastShakeTime > 1500) {
                tvShakeStatus.setText(R.string.shake_none);
                tvShakeStatus.setTextColor(0xFF333333);
            }
        }
    }

    private int clampProgress(float val) {
        // Range -20 to +20 mapped to 0 to 40
        int progress = (int) (val + 20);
        if (progress < 0) return 0;
        if (progress > 40) return 40;
        return progress;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}