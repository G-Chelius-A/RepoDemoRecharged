package com.example.sensoresnav;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MagnetometerActivity extends BaseActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;

    private TextView tvMagX, tvMagY, tvMagZ, tvMagTotal, tvCompassHeading;
    private ImageView imgCompass;

    private final float[] lastAccelerometer = new float[3];
    private final float[] lastMagnetometer = new float[3];
    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;

    private final float[] rotationMatrix = new float[9];
    private final float[] orientation = new float[3];

    private float currentAzimuthDegrees = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magnetometer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_magnetometer);
        }

        tvMagX = findViewById(R.id.tvMagX);
        tvMagY = findViewById(R.id.tvMagY);
        tvMagZ = findViewById(R.id.tvMagZ);
        tvMagTotal = findViewById(R.id.tvMagTotal);
        tvCompassHeading = findViewById(R.id.tvCompassHeading);

        imgCompass = findViewById(R.id.imgCompass);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if (magnetometer == null) {
            Toast.makeText(this, R.string.sensor_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            if (magnetometer != null) {
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
            }
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            }
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
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.length);
            lastAccelerometerSet = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.length);
            lastMagnetometerSet = true;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            double totalField = Math.sqrt(x * x + y * y + z * z);

            tvMagX.setText(String.format(Locale.getDefault(), "Campo X: %.2f µT", x));
            tvMagY.setText(String.format(Locale.getDefault(), "Campo Y: %.2f µT", y));
            tvMagZ.setText(String.format(Locale.getDefault(), "Campo Z: %.2f µT", z));
            tvMagTotal.setText(String.format(Locale.getDefault(), "Intensidad Total: %.2f µT", totalField));
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer);
            if (success) {
                SensorManager.getOrientation(rotationMatrix, orientation);
                float azimuthInRadians = orientation[0];
                float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);

                if (azimuthInDegrees < 0) {
                    azimuthInDegrees += 360f;
                }

                currentAzimuthDegrees = azimuthInDegrees;
                imgCompass.setRotation(-currentAzimuthDegrees);

                String directionName = getDirectionName(currentAzimuthDegrees);
                tvCompassHeading.setText(String.format(Locale.getDefault(), "%.0f° %s", currentAzimuthDegrees, directionName));
            }
        }
    }

    private String getDirectionName(float degrees) {
        if (degrees >= 337.5 || degrees < 22.5) return "Norte (N)";
        if (degrees >= 22.5 && degrees < 67.5) return "Noreste (NE)";
        if (degrees >= 67.5 && degrees < 112.5) return "Este (E)";
        if (degrees >= 112.5 && degrees < 157.5) return "Sureste (SE)";
        if (degrees >= 157.5 && degrees < 202.5) return "Sur (S)";
        if (degrees >= 202.5 && degrees < 247.5) return "Suroeste (SO)";
        if (degrees >= 247.5 && degrees < 292.5) return "Oeste (O)";
        if (degrees >= 292.5 && degrees < 337.5) return "Noroeste (NO)";
        return "";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}